/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.overlord.sramp.wagon;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.archive.SrampArchiveException;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.MetaDataProvider;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.registry.ArchiveInfo;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveRegistry;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.wagon.i18n.Messages;
import org.overlord.sramp.wagon.models.MavenGavInfo;
import org.overlord.sramp.wagon.util.DevNullOutputStream;

/**
 * Implements a wagon provider that uses the S-RAMP Atom API.
 *
 * @author eric.wittmann@redhat.com
 */
@Component(role = Wagon.class, hint = "sramp", instantiationStrategy = "per-lookup")
public class SrampWagon extends StreamWagon {

	@Requirement
	private Logger logger;

	private transient SrampArchive archive;
	private transient SrampAtomApiClient client;

	/**
	 * Constructor.
	 */
	public SrampWagon() {
	}

	/**
	 * @return the endpoint to use for the s-ramp repo
	 */
	private String getSrampEndpoint() {
		String pomUrl = getRepository().getUrl();
		if (pomUrl.indexOf('?') > 0) {
		    pomUrl = pomUrl.substring(0, pomUrl.indexOf('?'));
		}
        String replace = pomUrl.replace("sramp:", "http:").replace("sramps:", "https:"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return replace;
	}

	/**
	 * @see org.apache.maven.wagon.AbstractWagon#openConnectionInternal()
	 */
	@Override
	protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
		// Even though the S-RAMP Atom API is session-less, use this open method
		// to start building up an S-RAMP archive containing the artifacts we are
		// storing in the repository (along with the meta-data for those artifacts).
		// The archive will serve as a temporary place to stash information we may
		// need later.
        ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
		try {
		    // Create the archive
			this.archive = new SrampArchive();

			// Now create and configure the client.
            String endpoint = getSrampEndpoint();
            // Use sensible defaults
            String username = null;
            String password = null;
            AuthenticationInfo authInfo = this.getAuthenticationInfo();
            if (authInfo != null) {
                if (authInfo.getUserName() != null) {
                    username = authInfo.getUserName();
                }
                if (authInfo.getPassword() != null) {
                    password = authInfo.getPassword();
                }
            }
            if (username == null) {
                username = promptForUsername();
            }
            if (password == null) {
                password = promptForPassword();
            }

            this.client = new SrampAtomApiClient(endpoint, username, password, true);
		} catch (SrampArchiveException e) {
			throw new ConnectionException(Messages.i18n.format("FAILED_TO_CREATE_ARCHIVE"), e); //$NON-NLS-1$
		} catch (SrampClientException e) {
            throw new ConnectionException(Messages.i18n.format("FAILED_TO_CONNECT_TO_SRAMP"), e); //$NON-NLS-1$
        } catch (SrampAtomException e) {
            throw new ConnectionException(Messages.i18n.format("FAILED_TO_CONNECT_TO_SRAMP"), e); //$NON-NLS-1$
        } finally {
            Thread.currentThread().setContextClassLoader(oldCtxCL);
        }
	}

    /**
     * Prompts the user to enter a username for authentication credentials.
     */
    private String promptForUsername() {
        Console console = System.console();
        if (console != null) {
            return console.readLine(Messages.i18n.format("USERNAME_PROMPT")); //$NON-NLS-1$
        } else {
            System.err.println(Messages.i18n.format("NO_CONSOLE_ERROR_1")); //$NON-NLS-1$
            return null;
        }
    }

    /**
     * Prompts the user to enter a password for authentication credentials.
     */
    private String promptForPassword() {
        Console console = System.console();
        if (console != null) {
            return new String(console.readPassword(Messages.i18n.format("PASSWORD_PROMPT"))); //$NON-NLS-1$
        } else {
            System.err.println(Messages.i18n.format("NO_CONSOLE_ERROR_2")); //$NON-NLS-1$
            return null;
        }
    }

    /**
	 * @see org.apache.maven.wagon.StreamWagon#closeConnection()
	 */
	@Override
	public void closeConnection() throws ConnectionException {
		SrampArchive.closeQuietly(archive);
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#fillInputData(org.apache.maven.wagon.InputData)
	 */
	@Override
	public void fillInputData(InputData inputData) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		Resource resource = inputData.getResource();
        MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
        if (gavInfo.isMavenMetaData() && gavInfo.getVersion() == null) {
            doGenerateArtifactDirMavenMetaData(gavInfo, inputData);
            return;
        }

        if (gavInfo.isMavenMetaData() && gavInfo.getVersion() != null) {
            doGenerateSnapshotMavenMetaData(gavInfo, inputData);
            return;
        }

		debug(Messages.i18n.format("LOOKING_UP_RESOURCE_IN_SRAMP", resource)); //$NON-NLS-1$

		if (gavInfo.isHash()) {
			doGetHash(gavInfo, inputData);
		} else {
			doGetArtifact(gavInfo, inputData);
		}

	}

	/**
	 * Generates the maven-metadata.xml file dynamically for a given groupId/artifactId pair.  This will
	 * list all of the versions available for that groupId+artifactId, along with the latest release and
	 * snapshot versions.
     * @param gavInfo
     * @param inputData
	 * @throws ResourceDoesNotExistException
     */
    private void doGenerateArtifactDirMavenMetaData(MavenGavInfo gavInfo, InputData inputData) throws ResourceDoesNotExistException {
        // See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
        // context classloader magic.
        ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
        try {
            String artyPath = gavInfo.getFullName();
            if (gavInfo.isHash()) {
                artyPath = artyPath.substring(0, artyPath.lastIndexOf('.'));
            }
            SrampArchiveEntry entry = this.archive.getEntry(artyPath);
            if (entry == null) {
                QueryResultSet resultSet = client.buildQuery("/s-ramp[@maven.groupId = ? and @maven.artifactId = ?]") //$NON-NLS-1$
                        .parameter(gavInfo.getGroupId())
                        .parameter(gavInfo.getArtifactId())
                        .propertyName("maven.version") //$NON-NLS-1$
                        .count(500).orderBy("createdTimestamp").ascending().query(); //$NON-NLS-1$
                if (resultSet.size() == 0) {
                    throw new Exception(Messages.i18n.format("NO_ARTIFACTS_FOUND")); //$NON-NLS-1$
                }

                String groupId = gavInfo.getGroupId();
                String artifactId = gavInfo.getArtifactId();
                String latest = null;
                String release = null;
                String lastUpdated = null;

                LinkedHashSet<String> versions = new LinkedHashSet<String>();
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$
                for (ArtifactSummary artifactSummary : resultSet) {
                    String version = artifactSummary.getCustomPropertyValue("maven.version"); //$NON-NLS-1$
                    if (versions.add(version)) {
                        latest = version;
                        if (!version.endsWith("-SNAPSHOT")) { //$NON-NLS-1$
                            release = version;
                        }
                    }
                    lastUpdated = format.format(artifactSummary.getCreatedTimestamp());
                }

                StringBuilder mavenMetadata = new StringBuilder();
                mavenMetadata.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
                mavenMetadata.append("<metadata>\n"); //$NON-NLS-1$
                mavenMetadata.append("  <groupId>").append(groupId).append("</groupId>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("  <artifactId>").append(artifactId).append("</artifactId>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("  <versioning>\n"); //$NON-NLS-1$
                mavenMetadata.append("    <latest>").append(latest).append("</latest>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("    <release>").append(release).append("</release>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("    <versions>\n"); //$NON-NLS-1$
                for (String version : versions) {
                    mavenMetadata.append("      <version>").append(version).append("</version>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                mavenMetadata.append("    </versions>\n"); //$NON-NLS-1$
                mavenMetadata.append("    <lastUpdated>").append(lastUpdated).append("</lastUpdated>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("  </versioning>\n"); //$NON-NLS-1$
                mavenMetadata.append("</metadata>\n"); //$NON-NLS-1$

                BaseArtifactType artifact = ArtifactType.ExtendedDocument("MavenMetaData").newArtifactInstance(); //$NON-NLS-1$
                this.archive.addEntry(artyPath, artifact, IOUtils.toInputStream(mavenMetadata.toString()));

                entry = this.archive.getEntry(artyPath);
            }

            if (!gavInfo.isHash()) {
                inputData.setInputStream(this.archive.getInputStream(entry));
            } else {
                String hash = generateHash(this.archive.getInputStream(entry), gavInfo.getHashAlgorithm());
                inputData.setInputStream(IOUtils.toInputStream(hash));
            }
        } catch (Exception e) {
            throw new ResourceDoesNotExistException(Messages.i18n.format("FAILED_TO_GENERATE_METADATA"), e); //$NON-NLS-1$
        } finally {
            Thread.currentThread().setContextClassLoader(oldCtxCL);
        }
    }

    /**
     * Generates the maven-metadata.xml file dynamically for a given groupId/artifactId/snapshot-version.
     * This will list all of the snapshot versions available.
     * @param gavInfo
     * @param inputData
     * @throws ResourceDoesNotExistException
     */
    private void doGenerateSnapshotMavenMetaData(MavenGavInfo gavInfo, InputData inputData) throws ResourceDoesNotExistException {
        // See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
        // context classloader magic.
        ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
        try {
            String artyPath = gavInfo.getFullName();
            if (gavInfo.isHash()) {
                artyPath = artyPath.substring(0, artyPath.lastIndexOf('.'));
            }
            SrampArchiveEntry entry = this.archive.getEntry(artyPath);
            if (entry == null) {
                QueryResultSet resultSet = client.buildQuery("/s-ramp[@maven.groupId = ? and @maven.artifactId = ? and @maven.version = ?]") //$NON-NLS-1$
                        .parameter(gavInfo.getGroupId())
                        .parameter(gavInfo.getArtifactId())
                        .parameter(gavInfo.getVersion())
                        .propertyName("maven.classifier").propertyName("maven.type") //$NON-NLS-1$ //$NON-NLS-2$
                        .count(500).orderBy("createdTimestamp").ascending().query(); //$NON-NLS-1$
                if (resultSet.size() == 0) {
                    throw new Exception(Messages.i18n.format("NO_ARTIFACTS_FOUND")); //$NON-NLS-1$
                }

                SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyyMMdd.HHmmss"); //$NON-NLS-1$
                SimpleDateFormat updatedFormat = new SimpleDateFormat("yyyyMMddHHmmss"); //$NON-NLS-1$

                StringBuilder snapshotVersions = new StringBuilder();
                snapshotVersions.append("    <snapshotVersions>\n"); //$NON-NLS-1$
                Set<String> processed = new HashSet<String>();
                Date latestDate = null;
                for (ArtifactSummary artifactSummary : resultSet) {
                    String extension = artifactSummary.getCustomPropertyValue("maven.type"); //$NON-NLS-1$
                    String classifier = artifactSummary.getCustomPropertyValue("maven.classifier"); //$NON-NLS-1$
                    String value = gavInfo.getVersion();
                    Date updatedDate = artifactSummary.getLastModifiedTimestamp();
                    String updated = updatedFormat.format(updatedDate);
                    String pkey = classifier+"::"+extension; //$NON-NLS-1$
                    if (processed.add(pkey)) {
                        snapshotVersions.append("      <snapshotVersion>\n"); //$NON-NLS-1$
                        if (classifier != null)
                            snapshotVersions.append("        <classifier>").append(classifier).append("</classifier>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        snapshotVersions.append("        <extension>").append(extension).append("</extension>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        snapshotVersions.append("        <value>").append(value).append("</value>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        snapshotVersions.append("        <updated>").append(updated).append("</updated>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                        snapshotVersions.append("      </snapshotVersion>\n"); //$NON-NLS-1$
                        if (latestDate == null || latestDate.before(updatedDate)) {
                            latestDate = updatedDate;
                        }
                    }
                }
                snapshotVersions.append("    </snapshotVersions>\n"); //$NON-NLS-1$

                String groupId = gavInfo.getGroupId();
                String artifactId = gavInfo.getArtifactId();
                String version = gavInfo.getVersion();
                String lastUpdated = updatedFormat.format(latestDate);

                StringBuilder mavenMetadata = new StringBuilder();
                mavenMetadata.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"); //$NON-NLS-1$
                mavenMetadata.append("<metadata>\n"); //$NON-NLS-1$
                mavenMetadata.append("  <groupId>").append(groupId).append("</groupId>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("  <artifactId>").append(artifactId).append("</artifactId>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("  <version>").append(version).append("</version>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("  <versioning>\n"); //$NON-NLS-1$
                mavenMetadata.append("    <snapshot>\n"); //$NON-NLS-1$
                mavenMetadata.append("      <timestamp>").append(timestampFormat.format(latestDate)).append("</timestamp>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append("      <buildNumber>1</buildNumber>\n"); //$NON-NLS-1$
                mavenMetadata.append("    </snapshot>\n"); //$NON-NLS-1$
                mavenMetadata.append("    <lastUpdated>").append(lastUpdated).append("</lastUpdated>\n"); //$NON-NLS-1$ //$NON-NLS-2$
                mavenMetadata.append(snapshotVersions.toString());
                mavenMetadata.append("  </versioning>\n"); //$NON-NLS-1$
                mavenMetadata.append("</metadata>\n"); //$NON-NLS-1$

                BaseArtifactType artifact = ArtifactType.ExtendedDocument("MavenMetaData").newArtifactInstance(); //$NON-NLS-1$
                this.archive.addEntry(artyPath, artifact, IOUtils.toInputStream(mavenMetadata.toString()));

                entry = this.archive.getEntry(artyPath);
            }

            if (!gavInfo.isHash()) {
                inputData.setInputStream(this.archive.getInputStream(entry));
            } else {
                String hash = generateHash(this.archive.getInputStream(entry), gavInfo.getHashAlgorithm());
                inputData.setInputStream(IOUtils.toInputStream(hash));
            }
        } catch (Exception e) {
            throw new ResourceDoesNotExistException(Messages.i18n.format("FAILED_TO_GENERATE_METADATA"), e); //$NON-NLS-1$
        } finally {
            Thread.currentThread().setContextClassLoader(oldCtxCL);
        }
    }

    /**
	 * Gets the hash data from the s-ramp repository and stores it in the {@link InputData} for
	 * use by Maven.
	 * @param gavInfo
	 * @param inputData
	 * @throws TransferFailedException
	 * @throws ResourceDoesNotExistException
	 * @throws AuthorizationException
	 */
	private void doGetHash(MavenGavInfo gavInfo, InputData inputData) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		String artyPath = gavInfo.getFullName();
		String hashPropName;
		if (gavInfo.getType().endsWith(".md5")) { //$NON-NLS-1$
			hashPropName = "maven.hash.md5"; //$NON-NLS-1$
			artyPath = artyPath.substring(0, artyPath.length() - 4);
		} else {
			hashPropName = "maven.hash.sha1"; //$NON-NLS-1$
			artyPath = artyPath.substring(0, artyPath.length() - 5);
		}
        // See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
        // context classloader magic.
        ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
        try {
    		SrampArchiveEntry entry = this.archive.getEntry(artyPath);
    		if (entry == null) {
    			throw new ResourceDoesNotExistException(Messages.i18n.format("MISSING_RESOURCE_HASH", gavInfo.getName())); //$NON-NLS-1$
    		}
    		BaseArtifactType metaData = entry.getMetaData();

    		String hashValue = SrampModelUtils.getCustomProperty(metaData, hashPropName);
    		if (hashValue == null) {
    			throw new ResourceDoesNotExistException(Messages.i18n.format("MISSING_RESOURCE_HASH", gavInfo.getName())); //$NON-NLS-1$
    		}
    		inputData.setInputStream(IOUtils.toInputStream(hashValue));
        } finally {
            Thread.currentThread().setContextClassLoader(oldCtxCL);
        }
	}

	/***
	 * Gets the artifact content from the s-ramp repository and stores it in the {@link InputData}
	 * object for use by Maven.
	 * @param gavInfo
	 * @param inputData
	 * @throws TransferFailedException
	 * @throws ResourceDoesNotExistException
	 * @throws AuthorizationException
	 */
	private void doGetArtifact(MavenGavInfo gavInfo, InputData inputData) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		// RESTEasy uses the current thread's context classloader to load its logger class.  This
		// fails in Maven because the context classloader is the wagon plugin's classloader, which
		// doesn't know about any of the RESTEasy JARs.  So here we're temporarily setting the
		// context classloader to the s-ramp wagon extension's classloader, which should have access
		// to all the right stuff.
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
		try {
			// Query the artifact meta data using GAV info
			BaseArtifactType artifact = findExistingArtifact(client, gavInfo);
			if (artifact == null)
				throw new ResourceDoesNotExistException(Messages.i18n.format("ARTIFACT_NOT_FOUND", gavInfo.getName())); //$NON-NLS-1$
			this.archive.addEntry(gavInfo.getFullName(), artifact, null);
			ArtifactType type = ArtifactType.valueOf(artifact);

			// Get the artifact content as an input stream
			InputStream artifactContent = client.getArtifactContent(type, artifact.getUuid());
			inputData.setInputStream(artifactContent);
		} catch (ResourceDoesNotExistException e) {
			throw e;
		} catch (SrampClientException e) {
			if (e.getCause() instanceof HttpHostConnectException) {
				this.debug(Messages.i18n.format("SRAMP_CONNECTION_FAILED", e.getMessage())); //$NON-NLS-1$
			} else {
				this.error(e.getMessage(), e);
			}
			throw new ResourceDoesNotExistException(Messages.i18n.format("FAILED_TO_GET_RESOURCE_FROM_SRAMP", gavInfo.getName())); //$NON-NLS-1$
		} catch (Throwable t) {
			this.error(t.getMessage(), t);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
	}

    /**
	 * @see org.apache.maven.wagon.StreamWagon#putFromStream(java.io.InputStream, java.lang.String)
	 */
	@Override
	public void putFromStream(InputStream stream, String destination) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		Resource resource = new Resource(destination);
		putCommon(resource, null, stream);
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#putFromStream(java.io.InputStream, java.lang.String, long, long)
	 */
	@Override
	public void putFromStream(InputStream stream, String destination, long contentLength, long lastModified)
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		Resource resource = new Resource(destination);
		resource.setContentLength(contentLength);
		resource.setLastModified(lastModified);
		putCommon(resource, null, stream);
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#put(java.io.File, java.lang.String)
	 */
	@Override
	public void put(File source, String resourceName) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		InputStream resourceInputStream = null;
		try {
			resourceInputStream = new FileInputStream(source);
		} catch (FileNotFoundException e) {
			throw new TransferFailedException(e.getMessage());
		}

		Resource resource = new Resource(resourceName);
		resource.setContentLength(source.length());
		resource.setLastModified(source.lastModified());
		putCommon(resource, source, resourceInputStream);
	}

	/**
	 * Common put implementation.  Handles firing events and ultimately sending the data via the
	 * s-ramp client.
	 * @param resource
	 * @param source
	 * @param content
	 * @throws TransferFailedException
	 * @throws ResourceDoesNotExistException
	 * @throws AuthorizationException
	 */
	private void putCommon(Resource resource, File source, InputStream content)
			throws TransferFailedException, ResourceDoesNotExistException, AuthorizationException {
		logger.info(Messages.i18n.format("UPLOADING_TO_SRAMP", resource.getName())); //$NON-NLS-1$
		firePutInitiated(resource, source);

		firePutStarted(resource, source);
		if (resource.getName().contains("maven-metadata.xml")) { //$NON-NLS-1$
			logger.info(Messages.i18n.format("SKIPPING_ARTY", resource.getName())); //$NON-NLS-1$
			try {
				transfer(resource, content, new DevNullOutputStream(), TransferEvent.REQUEST_PUT);
			} catch (IOException e) {
				throw new TransferFailedException(e.getMessage(), e);
			}
		} else {
			doPut(resource, content);
		}
		firePutCompleted(resource, source);
	}

	/**
	 * Gets the artifact type from the resource.
	 * @param gavInfo
	 * @param archiveType
	 */
	private ArtifactType getArtifactType(MavenGavInfo gavInfo, String archiveType) {
	    String customAT = getParamFromRepositoryUrl("artifactType"); //$NON-NLS-1$
	    if (gavInfo.getType().equals("pom")) { //$NON-NLS-1$
	        return ArtifactType.valueOf("MavenPom"); //$NON-NLS-1$
	    } else if (isPrimaryArtifact(gavInfo) && customAT != null) {
	        return ArtifactType.valueOf(customAT);
	    } else if (isPrimaryArtifact(gavInfo) && archiveType != null) {
	        return ArtifactType.valueOf("ext", archiveType, true); //$NON-NLS-1$
	    } else if ("jar".equals(gavInfo.getType())) { //$NON-NLS-1$
	        return ArtifactType.valueOf(JavaModel.TYPE_ARCHIVE);
	    } else if ("xml".equals(gavInfo.getType())) { //$NON-NLS-1$
	        return ArtifactType.XmlDocument();
	    } else {
	        return ArtifactType.valueOf(ArtifactTypeEnum.Document.name());
	    }
	}

	/**
	 * Puts the maven resource into the s-ramp repository.
	 * @param resource
	 * @param resourceInputStream
	 * @throws TransferFailedException
	 */
	private void doPut(Resource resource, InputStream resourceInputStream) throws TransferFailedException {
		MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
		if (gavInfo.isHash()) {
			doPutHash(gavInfo, resourceInputStream);
		} else {
			doPutArtifact(gavInfo, resourceInputStream);
		}
	}

	/**
	 * Updates an artifact by storing its hash value as an S-RAMP property.
	 * @param gavInfo
	 * @param resourceInputStream
	 * @throws TransferFailedException
	 */
	private void doPutHash(MavenGavInfo gavInfo, InputStream resourceInputStream) throws TransferFailedException {
		logger.info(Messages.i18n.format("STORING_HASH_AS_PROP", gavInfo.getName())); //$NON-NLS-1$
		try {
			String artyPath = gavInfo.getFullName();
			String hashPropName;
			if (gavInfo.getType().endsWith(".md5")) { //$NON-NLS-1$
				hashPropName = "maven.hash.md5"; //$NON-NLS-1$
				artyPath = artyPath.substring(0, artyPath.length() - 4);
			} else {
				hashPropName = "maven.hash.sha1"; //$NON-NLS-1$
				artyPath = artyPath.substring(0, artyPath.length() - 5);
			}
			String hashValue = IOUtils.toString(resourceInputStream);

            // See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
            // context classloader magic.
            ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
            try {
    			SrampArchiveEntry entry = this.archive.getEntry(artyPath);
    			// Re-fetch the artifact meta-data in case it changed on the server since we uploaded it.
    			BaseArtifactType metaData = client.getArtifactMetaData(entry.getMetaData().getUuid());
    			SrampModelUtils.setCustomProperty(metaData, hashPropName, hashValue);
    			this.archive.updateEntry(entry, null);

    			// The meta-data has been updated in the local/temp archive - now send it to the remote repo
				client.updateArtifactMetaData(metaData);
			} catch (Throwable t) {
				throw new TransferFailedException(t.getMessage(), t);
			} finally {
				Thread.currentThread().setContextClassLoader(oldCtxCL);
			}
		} catch (Exception e) {
			throw new TransferFailedException(Messages.i18n.format("FAILED_TO_STORE_HASH", gavInfo.getName()), e); //$NON-NLS-1$
		}
	}

	/**
	 * Puts the artifact into the s-ramp repository.
	 * @param gavInfo
	 * @param resourceInputStream
	 * @throws TransferFailedException
	 */
	private void doPutArtifact(final MavenGavInfo gavInfo, InputStream resourceInputStream) throws TransferFailedException {

		// See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
		// context classloader magic.
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
		File tempResourceFile = null;
        ZipToSrampArchive expander = null;
		SrampArchive archive = null;
		BaseArtifactType artifactGrouping = null;
		try {
			// First, stash the content in a temp file - we may need it multiple times.
			tempResourceFile = stashResourceContent(resourceInputStream);
			resourceInputStream = FileUtils.openInputStream(tempResourceFile);

			ArchiveInfo archiveInfo = ZipToSrampArchiveRegistry.inspectArchive(resourceInputStream);
			ArtifactType artifactType = getArtifactType(gavInfo, archiveInfo.type);

			resourceInputStream = FileUtils.openInputStream(tempResourceFile);

			// Is the artifact grouping option enabled?
			if (isPrimaryArtifact(gavInfo) && getParamFromRepositoryUrl("artifactGrouping") != null) { //$NON-NLS-1$
			    artifactGrouping = ensureArtifactGrouping();
			}

			// Only search for existing artifacts by GAV info here
			BaseArtifactType artifact = findExistingArtifactByGAV(client, gavInfo);
			// If we found an artifact, we should update its content.  If not, we should upload
			// the artifact to the repository.
			if (artifact != null) {
                throw new TransferFailedException(Messages.i18n.format("ARTIFACT_UPDATE_NOT_ALLOWED", gavInfo.getFullName())); //$NON-NLS-1$

			} else {
				// Upload the content, then add the maven properties to the artifact
				// as meta-data
				artifact = client.uploadArtifact(artifactType, resourceInputStream, gavInfo.getName());
				SrampModelUtils.setCustomProperty(artifact, "maven.groupId", gavInfo.getGroupId()); //$NON-NLS-1$
				SrampModelUtils.setCustomProperty(artifact, "maven.artifactId", gavInfo.getArtifactId()); //$NON-NLS-1$
				SrampModelUtils.setCustomProperty(artifact, "maven.version", gavInfo.getVersion()); //$NON-NLS-1$
				artifact.setVersion(gavInfo.getVersion());
                if (gavInfo.getClassifier() != null) {
					SrampModelUtils.setCustomProperty(artifact, "maven.classifier", gavInfo.getClassifier()); //$NON-NLS-1$
                }
                if (gavInfo.getSnapshotId() != null && !gavInfo.getSnapshotId().equals("")) { //$NON-NLS-1$
                    SrampModelUtils.setCustomProperty(artifact, "maven.snapshot.id", gavInfo.getSnapshotId()); //$NON-NLS-1$
                }
				SrampModelUtils.setCustomProperty(artifact, "maven.type", gavInfo.getType()); //$NON-NLS-1$
				// Also create a relationship to the artifact grouping, if necessary
				if (artifactGrouping != null) {
				    SrampModelUtils.addGenericRelationship(artifact, "groupedBy", artifactGrouping.getUuid()); //$NON-NLS-1$
				    SrampModelUtils.addGenericRelationship(artifactGrouping, "groups", artifact.getUuid()); //$NON-NLS-1$
				    client.updateArtifactMetaData(artifactGrouping);
				}

				client.updateArtifactMetaData(artifact);
				this.archive.addEntry(gavInfo.getFullName(), artifact, null);
			}

			// Now also add "expanded" content to the s-ramp repository
            expander = ZipToSrampArchiveRegistry.createExpander(artifactType, tempResourceFile);
            if (expander != null) {
                expander.setContextParam(DefaultMetaDataFactory.PARENT_UUID, artifact.getUuid());
                expander.addMetaDataProvider(new MetaDataProvider() {
                    @Override
                    public void provideMetaData(BaseArtifactType artifact) {
                        SrampModelUtils.setCustomProperty(artifact, "maven.parent-groupId", gavInfo.getGroupId()); //$NON-NLS-1$
                        SrampModelUtils.setCustomProperty(artifact, "maven.parent-artifactId", gavInfo.getArtifactId()); //$NON-NLS-1$
                        SrampModelUtils.setCustomProperty(artifact, "maven.parent-version", gavInfo.getVersion()); //$NON-NLS-1$
                        SrampModelUtils.setCustomProperty(artifact, "maven.parent-type", gavInfo.getType()); //$NON-NLS-1$
                    }
                });
                archive = expander.createSrampArchive();
                client.uploadBatch(archive);
            }
		} catch (Throwable t) {
			throw new TransferFailedException(t.getMessage(), t);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
			SrampArchive.closeQuietly(archive);
            ZipToSrampArchive.closeQuietly(expander);
			FileUtils.deleteQuietly(tempResourceFile);
		}
	}

    /**
     * Ensures that the required ArtifactGrouping is present in the repository.
	 * @throws SrampAtomException
	 * @throws SrampClientException
     */
    private BaseArtifactType ensureArtifactGrouping() throws SrampClientException, SrampAtomException {
        String groupingName = getParamFromRepositoryUrl("artifactGrouping"); //$NON-NLS-1$
        if (groupingName == null || groupingName.trim().length() == 0) {
            logger.warn(Messages.i18n.format("NO_ARTIFACT_GROUPING_NAME")); //$NON-NLS-1$
            return null;
        }
        QueryResultSet query = client.buildQuery("/s-ramp/ext/ArtifactGrouping[@name = ?]").parameter(groupingName).count(2).query(); //$NON-NLS-1$
        if (query.size() > 1) {
            logger.warn(Messages.i18n.format("MULTIPLE_ARTIFACT_GROUPSING_FOUND", groupingName)); //$NON-NLS-1$
            return null;
        } else if (query.size() == 1) {
            ArtifactSummary summary = query.get(0);
            return client.getArtifactMetaData(summary.getType(), summary.getUuid());
        } else {
            ExtendedArtifactType groupingArtifact = new ExtendedArtifactType();
            groupingArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            groupingArtifact.setExtendedType("ArtifactGrouping"); //$NON-NLS-1$
            groupingArtifact.setName(groupingName);
            groupingArtifact.setDescription(Messages.i18n.format("ARTIFACT_GROUPING_DESCRIPTION")); //$NON-NLS-1$
            return client.createArtifact(groupingArtifact);
        }
    }

	/**
	 * Make a temporary copy of the resource by saving the content to a temp file.
	 * @param resourceInputStream
	 * @throws IOException
	 */
	private File stashResourceContent(InputStream resourceInputStream) throws IOException {
		File resourceTempFile = null;
		OutputStream oStream = null;
		try {
			resourceTempFile = File.createTempFile("s-ramp-wagon-resource", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
			oStream = FileUtils.openOutputStream(resourceTempFile);
		} finally {
			IOUtils.copy(resourceInputStream, oStream);
			IOUtils.closeQuietly(resourceInputStream);
			IOUtils.closeQuietly(oStream);
		}
		return resourceTempFile;
	}

	/**
	 * Finds an existing artifact in the s-ramp repository that matches the type and GAV information.
	 * @param client
	 * @param artifactType
	 * @param gavInfo
	 * @return an s-ramp artifact (if found) or null (if not found)
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 * @throws JAXBException
	 */
	private BaseArtifactType findExistingArtifact(SrampAtomApiClient client, MavenGavInfo gavInfo)
			throws SrampAtomException, SrampClientException, JAXBException {
		BaseArtifactType artifact = findExistingArtifactByGAV(client, gavInfo);
		if (artifact == null)
			artifact = findExistingArtifactByUniversal(client, gavInfo);
		return artifact;
	}

	/**
	 * Finds an existing artifact in the s-ramp repository that matches the GAV information.
	 * @param client
	 * @param gavInfo
	 * @return an s-ramp artifact (if found) or null (if not found)
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 * @throws JAXBException
	 */
	private BaseArtifactType findExistingArtifactByGAV(SrampAtomApiClient client, MavenGavInfo gavInfo)
			throws SrampAtomException, SrampClientException, JAXBException {
		SrampClientQuery clientQuery = null;

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("/s-ramp"); //$NON-NLS-1$
        List<String> criteria = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();

        criteria.add("@maven.groupId = ?"); //$NON-NLS-1$
        params.add(gavInfo.getGroupId());
        criteria.add("@maven.artifactId = ?"); //$NON-NLS-1$
        params.add(gavInfo.getArtifactId());
        criteria.add("@maven.version = ?"); //$NON-NLS-1$
        params.add(gavInfo.getVersion());

        if (StringUtils.isNotBlank(gavInfo.getType())) {
            criteria.add("@maven.type = ?"); //$NON-NLS-1$
            params.add(gavInfo.getType());
        }
        if (StringUtils.isNotBlank(gavInfo.getClassifier())) {
            criteria.add("@maven.classifier = ?"); //$NON-NLS-1$
            params.add(gavInfo.getClassifier());
        }
        if (StringUtils.isNotBlank(gavInfo.getSnapshotId())) {
            return null;
        } else {
            criteria.add("xp2:not(@maven.snapshot.id)"); //$NON-NLS-1$
        }

        if (criteria.size() > 0) {
            queryBuilder.append("["); //$NON-NLS-1$
            queryBuilder.append(StringUtils.join(criteria, " and ")); //$NON-NLS-1$
            queryBuilder.append("]"); //$NON-NLS-1$
        }
        clientQuery = client.buildQuery(queryBuilder.toString());
        for (Object param : params) {
            if (param instanceof String) {
                clientQuery.parameter((String) param);
            }
            if (param instanceof Calendar) {
                clientQuery.parameter((Calendar) param);
            }
        }

		QueryResultSet rset = clientQuery.count(100).query();
		if (rset.size() > 0) {
			for (ArtifactSummary summary : rset) {
				String uuid = summary.getUuid();
				ArtifactType artifactType = summary.getType();
				BaseArtifactType arty = client.getArtifactMetaData(artifactType, uuid);
				// If no classifier in the GAV info, only return the artifact that also has no classifier
				// TODO replace this with "not(@maven.classifer)" in the query, then force the result set to return 2 items (expecting only 1)
				if (gavInfo.getClassifier() == null) {
					String artyClassifier = SrampModelUtils.getCustomProperty(arty, "maven.classifier"); //$NON-NLS-1$
					if (artyClassifier == null) {
						return arty;
					}
				} else {
					// If classifier was supplied in the GAV info, we'll get the first artifact <shrug>
					return arty;
				}
			}
		}
		return null;
	}

	/**
	 * Finds an existing artifact in the s-ramp repository using 'universal' form.  This allows
	 * any artifact in the s-ramp repository to be referenced as a Maven dependency using the
	 * model.type and UUID of the artifact.
	 * @param client
	 * @param artifactType
	 * @param gavInfo
	 * @return an existing s-ramp artifact (if found) or null (if not found)
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 * @throws JAXBException
	 */
	private BaseArtifactType findExistingArtifactByUniversal(SrampAtomApiClient client, MavenGavInfo gavInfo)
			throws SrampAtomException, SrampClientException, JAXBException {
		String artifactType = gavInfo.getGroupId().substring(gavInfo.getGroupId().indexOf('.') + 1);
		String uuid = gavInfo.getArtifactId();
		try {
			return client.getArtifactMetaData(ArtifactType.valueOf(artifactType), uuid);
		} catch (Throwable t) {
			debug(t.getMessage());
		}
		return null;
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#fillOutputData(org.apache.maven.wagon.OutputData)
	 */
	@Override
	public void fillOutputData(OutputData outputData) throws TransferFailedException {
		// Since the wagon is implementing the put method directly, the StreamWagon's
		// implementation is never called.
		throw new RuntimeException("Should never get here!"); //$NON-NLS-1$
	}

	/**
	 * Gets a URL parameter by name from the repository URL.
	 * @param paramName
	 */
	protected String getParamFromRepositoryUrl(String paramName) {
	    String url = getRepository().getUrl();
	    int idx = url.indexOf('?');
	    if (idx == -1)
	        return null;
        String query = url.substring(idx + 1);
	    String [] params = query.split("&"); //$NON-NLS-1$
	    for (String paramPair : params) {
	        String [] pp = paramPair.split("="); //$NON-NLS-1$
	        if (pp.length == 2) {
    	        String key = pp[0];
    	        String val = pp[1];
    	        if (key.equals(paramName)) {
    	            return val;
    	        }
	        } else {
	            throw new RuntimeException(Messages.i18n.format("INVALID_QUERY_PARAM")); //$NON-NLS-1$
	        }
	    }
	    return null;
	}

    /**
     * Returns true if this represents the primary artifact in the Maven module.
     * @param gavInfo
     */
	protected boolean isPrimaryArtifact(MavenGavInfo gavInfo) {
        return gavInfo.getClassifier() == null;
    }

    /**
     * Generates a hash for the given content using the given hash algorithm.
     * @param inputStream
     * @param hashAlgorithm
     */
    private String generateHash(InputStream inputStream, String hashAlgorithm) throws Exception {
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
            byte[] dataBytes = new byte[1024];

            int nread = 0;

            while ((nread = inputStream.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            byte[] mdbytes = md.digest();

            // convert the byte to hex format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

            return sb.toString();
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    /**
     * @param message
     */
    private void debug(String message) {
        if (logger != null)
            logger.debug(message);
    }

    /**
     * @param message
     * @param t
     */
    private void error(String message, Throwable t) {
        if (logger != null)
            logger.error(message, t);
    }

}
