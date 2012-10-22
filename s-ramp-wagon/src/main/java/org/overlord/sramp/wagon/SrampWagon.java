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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.events.TransferEvent;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.SrampModelUtils;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.archive.SrampArchiveException;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.jar.DefaultMetaDataFactory;
import org.overlord.sramp.client.jar.DiscoveredArtifact;
import org.overlord.sramp.client.jar.JarToSrampArchive;
import org.overlord.sramp.wagon.models.MavenGavInfo;
import org.overlord.sramp.wagon.util.DevNullOutputStream;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Implements a wagon provider that uses the S-RAMP Atom API.
 *
 * @author eric.wittmann@redhat.com
 */
@SuppressWarnings("unchecked")
@Component(role = Wagon.class, hint = "sramp", instantiationStrategy = "per-lookup")
public class SrampWagon extends StreamWagon {

	@Requirement
	private Logger logger;

	private transient SrampArchive archive;

	/**
	 * Constructor.
	 */
	public SrampWagon() {
	}

	/**
	 * @return the endpoint to use for the s-ramp repo
	 */
	private String getSrampEndpoint() {
		return getRepository().getUrl().replace("sramp:", "http:").replace("sramps:", "https:");
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
		try {
			this.archive = new SrampArchive();
		} catch (SrampArchiveException e) {
			throw new ConnectionException("Failed to create the s-ramp archive (temporary storage)", e);
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
		// Skip maven-metadata.xml files - they are not (yet?) supported
		if (resource.getName().contains("maven-metadata.xml"))
			throw new ResourceDoesNotExistException("Could not find file: '" + resource + "'");

		logger.debug("Looking up resource from s-ramp repository: " + resource);

		MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
		if (gavInfo.isHash()) {
			doGetHash(gavInfo, inputData);
		} else {
			doGetArtifact(gavInfo, inputData);
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
		if (gavInfo.getType().endsWith(".md5")) {
			hashPropName = "maven.hash.md5";
			artyPath = artyPath.substring(0, artyPath.length() - 4);
		} else {
			hashPropName = "maven.hash.sha1";
			artyPath = artyPath.substring(0, artyPath.length() - 5);
		}
		SrampArchiveEntry entry = this.archive.getEntry(artyPath);
		if (entry == null) {
			throw new ResourceDoesNotExistException("Failed to find resource hash: " + gavInfo.getName());
		}
		BaseArtifactType metaData = entry.getMetaData();

		String hashValue = SrampModelUtils.getCustomProperty(metaData, hashPropName);
		if (hashValue == null) {
			throw new ResourceDoesNotExistException("Failed to find resource hash: " + gavInfo.getName());
		}
		inputData.setInputStream(IOUtils.toInputStream(hashValue));
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
			String endpoint = getSrampEndpoint();
			SrampAtomApiClient client = new SrampAtomApiClient(endpoint);

			// Query the artifact meta data using GAV info
			BaseArtifactType artifact = findExistingArtifact(client, gavInfo);
			if (artifact == null)
				throw new ResourceDoesNotExistException("Artifact not found in s-ramp repository: '" + gavInfo.getName() + "'");
			this.archive.addEntry(gavInfo.getFullName(), artifact, null);
			ArtifactType type = ArtifactType.valueOf(artifact);

			// Get the artifact content as an input stream
			InputStream artifactContent = client.getArtifactContent(type, artifact.getUuid());
			inputData.setInputStream(artifactContent);
		} catch (ResourceDoesNotExistException e) {
			throw e;
		} catch (SrampClientException e) {
			if (e.getCause() instanceof HttpHostConnectException) {
				this.logger.debug("Could not connect to s-ramp repository: " + e.getMessage());
			} else {
				this.logger.error(e.getMessage(), e);
			}
			throw new ResourceDoesNotExistException("Failed to get resource from s-ramp: " + gavInfo.getName());
		} catch (Throwable t) {
			this.logger.error(t.getMessage(), t);
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
		logger.info("Uploading s-ramp artifact: " + resource.getName());
		firePutInitiated(resource, source);

		firePutStarted(resource, source);
		if (resource.getName().contains("maven-metadata.xml")) {
			logger.info("Skipping unsupported artifact: " + resource.getName());
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
	 */
	private ArtifactType getArtifactType(MavenGavInfo gavInfo) {
		String fileName = gavInfo.getName();
		int extensionIdx = fileName.lastIndexOf('.');
		String extension = gavInfo.getName().substring(extensionIdx + 1);
		return ArtifactType.fromFileExtension(extension);
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
		logger.info("Storing hash value as s-ramp property: " + gavInfo.getName());
		try {
			String artyPath = gavInfo.getFullName();
			String hashPropName;
			if (gavInfo.getType().endsWith(".md5")) {
				hashPropName = "maven.hash.md5";
				artyPath = artyPath.substring(0, artyPath.length() - 4);
			} else {
				hashPropName = "maven.hash.sha1";
				artyPath = artyPath.substring(0, artyPath.length() - 5);
			}
			String hashValue = IOUtils.toString(resourceInputStream);

			SrampArchiveEntry entry = this.archive.getEntry(artyPath);
			BaseArtifactType metaData = entry.getMetaData();
			SrampModelUtils.setCustomProperty(metaData, hashPropName, hashValue);
			this.archive.updateEntry(entry, null);

			// The meta-data has been updated in the local/temp archive - now send it to the remote repo
			String endpoint = getSrampEndpoint();
			SrampAtomApiClient client = new SrampAtomApiClient(endpoint);
			// See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
			// context classloader magic.
			ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
			try {
				client.updateArtifactMetaData(metaData);
			} catch (Throwable t) {
				throw new TransferFailedException(t.getMessage(), t);
			} finally {
				Thread.currentThread().setContextClassLoader(oldCtxCL);
			}
		} catch (Exception e) {
			throw new TransferFailedException("Failed to store a hash: " + gavInfo.getName(), e);
		}
	}

	/**
	 * Puts the artifact into the s-ramp repository.
	 * @param gavInfo
	 * @param resourceInputStream
	 * @throws TransferFailedException
	 */
	private void doPutArtifact(final MavenGavInfo gavInfo, InputStream resourceInputStream) throws TransferFailedException {
		ArtifactType artifactType = getArtifactType(gavInfo);
		String endpoint = getSrampEndpoint();
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);
		// See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
		// context classloader magic.
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
		File tempResourceFile = null;
		SrampArchive archive = null;
		JarToSrampArchive j2sramp = null;
		try {
			// First, stash the content in a temp file - we may need it multiple times.
			tempResourceFile = stashResourceContent(resourceInputStream);
			resourceInputStream = FileUtils.openInputStream(tempResourceFile);

			// Only search for existing artifacts by GAV info here
			BaseArtifactType artifact = findExistingArtifactByGAV(client, gavInfo);
			// If we found an artifact, we should update its content.  If not, we should upload
			// the artifact to the repository.
			if (artifact != null) {
				this.archive.addEntry(gavInfo.getFullName(), artifact, null);
				client.updateArtifact(artifact, resourceInputStream);
				if (shouldExpand(gavInfo)) {
					final String parentUUID = artifact.getUuid();
					cleanExpandedArtifacts(client, parentUUID);
				}
			} else {
				// Upload the content, then add the maven properties to the artifact
				// as meta-data
				Entry entry = client.uploadArtifact(artifactType, resourceInputStream, gavInfo.getName());
				artifact = SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
				SrampModelUtils.setCustomProperty(artifact, "maven.groupId", gavInfo.getGroupId());
				SrampModelUtils.setCustomProperty(artifact, "maven.artifactId", gavInfo.getArtifactId());
				SrampModelUtils.setCustomProperty(artifact, "maven.version", gavInfo.getVersion());
				if (gavInfo.getClassifier() != null)
					SrampModelUtils.setCustomProperty(artifact, "maven.classifier", gavInfo.getClassifier());
				SrampModelUtils.setCustomProperty(artifact, "maven.type", gavInfo.getType());
				client.updateArtifactMetaData(artifact);
				this.archive.addEntry(gavInfo.getFullName(), artifact, null);
			}

			// Now also add "expanded" content to the s-ramp repository
			if (shouldExpand(gavInfo)) {
				final String parentUUID = artifact.getUuid();
				j2sramp = new JarToSrampArchive(tempResourceFile);
				j2sramp.setMetaDataFactory(new DefaultMetaDataFactory() {
					@Override
					public BaseArtifactType createMetaData(DiscoveredArtifact artifact) {
						BaseArtifactType metaData = super.createMetaData(artifact);
						SrampModelUtils.setCustomProperty(metaData, "maven.parent-groupId", gavInfo.getGroupId());
						SrampModelUtils.setCustomProperty(metaData, "maven.parent-artifactId", gavInfo.getArtifactId());
						SrampModelUtils.setCustomProperty(metaData, "maven.parent-version", gavInfo.getVersion());
						SrampModelUtils.setCustomProperty(metaData, "maven.parent-type", gavInfo.getType());
						SrampModelUtils.addGenericRelationship(metaData, "expandedFromDocument", parentUUID);
						return metaData;
					}
				});
				archive = j2sramp.createSrampArchive();
				client.uploadBatch(archive);
			}
		} catch (Throwable t) {
			throw new TransferFailedException(t.getMessage(), t);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
			SrampArchive.closeQuietly(archive);
			JarToSrampArchive.closeQuietly(j2sramp);
			FileUtils.deleteQuietly(tempResourceFile);
		}
	}

	/**
	 * Deletes the 'expanded' artifacts from the s-ramp repository.
	 * @param client
	 * @param parentUUID
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	private void cleanExpandedArtifacts(SrampAtomApiClient client, String parentUUID) throws SrampAtomException, SrampClientException {
		String query = String.format("/s-ramp[mavenParent[@uuid = '%1$s']]", parentUUID);
		boolean done = false;
		while (!done) {
			Feed feed = client.query(query, 0, 20, "name", true);
			if (feed.getEntries().size() == 0) {
				done = true;
			} else {
				for (Entry entry : feed.getEntries()) {
					ArtifactType artifactType = SrampAtomUtils.getArtifactType(entry);
					String uuid = entry.getId().toString();
					client.deleteArtifact(uuid, artifactType);
				}
			}
		}
	}

	/**
	 * @param gavInfo resource GAV information
	 * @return true if this maven artifact should be expanded in s-ramp (its contents exploded)
	 */
	private boolean shouldExpand(MavenGavInfo gavInfo) {
		// TODO this should be configurable in the pom.xml
		Set<String> expandedTypes = new HashSet<String>();
		expandedTypes.add("jar");
		expandedTypes.add("war");
		expandedTypes.add("ear");
		return expandedTypes.contains(gavInfo.getType()) && gavInfo.getClassifier() == null;
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
			resourceTempFile = File.createTempFile("s-ramp-wagon-resource", ".tmp");
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
		String query = null;
		// Search by classifier if we have one...
		if (gavInfo.getClassifier() == null) {
			query = String.format("/s-ramp[@maven.groupId = '%1$s' and @maven.artifactId = '%2$s' and @maven.version = '%3$s' and @maven.type = '%4$s']",
					gavInfo.getGroupId(), gavInfo.getArtifactId(), gavInfo.getVersion(), gavInfo.getType());
		} else {
			query = String.format("/s-ramp[@maven.groupId = '%1$s' and @maven.artifactId = '%2$s' and @maven.version = '%3$s' and @maven.classifier = '%4$s' and @maven.type = '%5$s']",
					gavInfo.getGroupId(), gavInfo.getArtifactId(), gavInfo.getVersion(), gavInfo.getClassifier(), gavInfo.getType());
		}
		Feed feed = client.query(query);
		if (feed.getEntries().size() > 0) {
			for (Entry entry : feed.getEntries()) {
				String uuid = entry.getId().toString();
				ArtifactType artifactType = SrampAtomUtils.getArtifactType(entry);
				entry = client.getFullArtifactEntry(artifactType, uuid);
				BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
				// If no classifier in the GAV info, only return the artifact that also has no classifier
				if (gavInfo.getClassifier() == null) {
					String artyClassifier = SrampModelUtils.getCustomProperty(arty, "maven.classifier");
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
		Entry entry = null;
		try {
			entry = client.getFullArtifactEntry(ArtifactType.valueOf(artifactType), uuid);
		} catch (Throwable t) {
			logger.debug(t.getMessage());
		}
		if (entry != null)
			return SrampAtomUtils.unwrapSrampArtifact(ArtifactType.valueOf(artifactType), entry);
		return null;
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#fillOutputData(org.apache.maven.wagon.OutputData)
	 */
	@Override
	public void fillOutputData(OutputData outputData) throws TransferFailedException {
		// Since the wagon is implementing the put method directly, the StreamWagon's
		// implementation is never called.
		throw new RuntimeException("Should never get here!");
	}

}
