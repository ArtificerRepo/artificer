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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.security.MessageDigest;

import javax.xml.bind.JAXBException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

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
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampServerException;
import org.overlord.sramp.wagon.models.MavenGavInfo;
import org.overlord.sramp.wagon.util.DevNullOutputStream;
import org.overlord.sramp.wagon.util.PomGenerator;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.w3c.dom.Document;

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

	/**
	 * Constructor.
	 */
	public SrampWagon() {
	}

	/**
	 * @see org.apache.maven.wagon.AbstractWagon#openConnectionInternal()
	 */
	@Override
	protected void openConnectionInternal() throws ConnectionException, AuthenticationException {
		// Even though the S-RAMP Atom API is session-less, use this open method
		// to start building up an S-RAMP archive containing the artifacts we are
		// storing in the repository (along with the meta-data for those artifacts).
		// The S-RAMP Atom API is session-less, so no connections to open

	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#closeConnection()
	 */
	@Override
	public void closeConnection() throws ConnectionException {
		// The S-RAMP Atom API is session-less, so no connections to close
		System.out.println("****** CLOSE CONNECTION *******");
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#fillInputData(org.apache.maven.wagon.InputData)
	 */
	@Override
	public void fillInputData(InputData inputData) throws TransferFailedException,
			ResourceDoesNotExistException, AuthorizationException {
		Resource resource = inputData.getResource();

		if (resource.getName().endsWith("maven-metadata.xml"))
			throw new ResourceDoesNotExistException("Could not find file: '" + resource + "'");

		logger.debug("Looking up resource from s-ramp repository: " + resource);
		// RESTEasy uses the current thread's context classloader to load its logger class.  This
		// fails in Maven because the context classloader is the wagon plugin's classloader, which
		// doesn't know about any of the RESTEasy JARs.  So here we're temporarily setting the
		// context classloader to the s-ramp wagon extension's classloader, which should have access
		// to all the right stuff.
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
		try {
			MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
			String endpoint = getRepository().getUrl().replace("sramp:", "http:").replace("sramps:", "https:");
			SrampAtomApiClient client = new SrampAtomApiClient(endpoint);

			// Query the artifact meta data using GAV info
			BaseArtifactType artifact = findExistingArtifact(client, gavInfo);
			if (artifact == null)
				throw new ResourceDoesNotExistException("Artifact not found in s-ramp repository: '" + resource + "'");
			ArtifactType type = ArtifactType.valueOf(artifact);

			if ("pom".equals(gavInfo.getType())) {
				String serializedPom = generatePom(artifact);
			    inputData.setInputStream(new ByteArrayInputStream(serializedPom.getBytes("UTF-8")));
			    return;
			} else if ("pom.sha1".equals(gavInfo.getType())) {
				// Generate a SHA1 hash on the fly for the POM
				String serializedPom = generatePom(artifact);
				MessageDigest md = MessageDigest.getInstance("SHA1");
				md.update(serializedPom.getBytes("UTF-8"));
				byte[] mdbytes = md.digest();
				StringBuilder sb = new StringBuilder();
			    for (int i = 0; i < mdbytes.length; i++) {
			    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			    }
			    inputData.setInputStream(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
			    return;
			} else if (gavInfo.getType().endsWith(".sha1")) {
				InputStream artifactContent = client.getArtifactContent(type, artifact.getUuid());
				String sha1Hash = generateSHA1Hash(artifactContent);
			    inputData.setInputStream(new ByteArrayInputStream(sha1Hash.getBytes("UTF-8")));
			    return;
			} else  {
				// Get the artifact content as an input stream
				InputStream artifactContent = client.getArtifactContent(type, artifact.getUuid());
				inputData.setInputStream(artifactContent);
				return;
			}
		} catch (ResourceDoesNotExistException e) {
			throw e;
		} catch (SrampClientException e) {
			if (e.getCause() instanceof HttpHostConnectException)
				this.logger.debug("Could not connect to s-ramp repository: " + e.getMessage());
			else
				this.logger.error(e.getMessage(), e);
		} catch (Throwable t) {
			this.logger.error(t.getMessage(), t);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
		throw new ResourceDoesNotExistException("Could not find file: '" + resource + "'");
	}

	/**
	 * Generates a SHA1 hash for the given binary content.
	 * @param artifactContent an s-ramp artifact input stream
	 * @return a SHA1 hash
	 */
	private String generateSHA1Hash(InputStream artifactContent) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA1");
			byte[] buff = new byte[2048];
			int count = artifactContent.read(buff);
			while (count != -1) {
				md.update(buff, 0, count);
				count = artifactContent.read(buff);
			}
			byte[] mdbytes = md.digest();
			StringBuilder sb = new StringBuilder();
		    for (int i = 0; i < mdbytes.length; i++) {
		    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		    }
		    return sb.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			IOUtils.closeQuietly(artifactContent);
		}
	}

	/**
	 * Generates a POM for the artifact.
	 * @param artifact
	 * @throws Exception
	 */
	private String generatePom(BaseArtifactType artifact) throws Exception {
		ArtifactType type = ArtifactType.valueOf(artifact);
		PomGenerator pomGenerator = new PomGenerator();
		Document pomDoc = pomGenerator.generatePom(artifact, type);
		String serializedPom = serializeDocument(pomDoc);
		return serializedPom;
	}

	/**
	 * Serialize a document to a string.
	 * @param document
	 */
	private String serializeDocument(Document document) {
		try {
			StringWriter writer = new StringWriter();
			Transformer transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
			transformer.transform(new DOMSource(document), new StreamResult(writer));
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
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
		firePutInitiated(resource, source);

		firePutStarted(resource, source);
		ArtifactType artifactType = getArtifactType(resource);
		if (artifactType == null || resource.getName().endsWith("maven-metadata.xml")) {
			// Unsupported type, stream to /dev/null
			logger.info("Skipping unsupported artifact: " + resource.getName());
			try {
				transfer(resource, content, new DevNullOutputStream(), TransferEvent.REQUEST_PUT);
			} catch (IOException e) {
				throw new TransferFailedException(e.getMessage(), e);
			}
		} else {
			doPut(resource, artifactType, content);
		}
		firePutCompleted(resource, source);
	}

	/**
	 * Gets the artifact type from the resource.
	 * @param resource
	 */
	private ArtifactType getArtifactType(Resource resource) {
		String fileName = resource.getName();
		int extensionIdx = fileName.lastIndexOf('.');
		String extension = resource.getName().substring(extensionIdx + 1);
		return ArtifactType.fromFileExtension(extension);
	}

	/**
	 * Puts the maven resource into the s-ramp repository.
	 * @param resource
	 * @param artifactType
	 * @param resourceInputStream
	 * @throws TransferFailedException
	 */
	private void doPut(Resource resource, ArtifactType artifactType, InputStream resourceInputStream) throws TransferFailedException {
		MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
		String endpoint = getRepository().getUrl().replace("sramp:", "http:").replace("sramps:", "https:");
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);
		// See the comment in {@link SrampWagon#fillInputData(InputData)} about why we're doing this
		// context classloader magic.
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(SrampWagon.class.getClassLoader());
		try {
			BaseArtifactType artifact = findExistingArtifact(client, gavInfo);
			if (artifact != null) {
				client.updateArtifact(artifact, resourceInputStream);
			} else {
				Entry entry = client.uploadArtifact(artifactType, resourceInputStream, resource.getName());
				artifact = SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
				SrampModelUtils.setCustomProperty(artifact, "maven.groupId", gavInfo.getGroupId());
				SrampModelUtils.setCustomProperty(artifact, "maven.artifactId", gavInfo.getArtifactId());
				SrampModelUtils.setCustomProperty(artifact, "maven.version", gavInfo.getVersion());
				client.updateArtifactMetaData(artifact);
			}
		} catch (Throwable t) {
			throw new TransferFailedException(t.getMessage(), t);
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
	}

	/**
	 * Finds an existing artifact in the s-ramp repository that matches the type and GAV information.
	 * @param client
	 * @param artifactType
	 * @param gavInfo
	 * @return an s-ramp artifact (if found) or null (if not found)
	 * @throws SrampClientException
	 * @throws SrampServerException
	 * @throws JAXBException
	 */
	private BaseArtifactType findExistingArtifact(SrampAtomApiClient client, MavenGavInfo gavInfo) throws SrampServerException, SrampClientException, JAXBException {
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
	 * @throws SrampServerException
	 * @throws JAXBException
	 */
	private BaseArtifactType findExistingArtifactByGAV(SrampAtomApiClient client, MavenGavInfo gavInfo) throws SrampServerException, SrampClientException, JAXBException {
		String query = String.format("/s-ramp[@maven.groupId = '%1$s' and @maven.artifactId = '%2$s' and @maven.version = '%3$s']",
				gavInfo.getGroupId(), gavInfo.getArtifactId(), gavInfo.getVersion());
		Feed feed = client.query(query);
		if (feed.getEntries().size() == 1) {
			Entry entry = feed.getEntries().get(0);
			String uuid = entry.getId().toString();
			ArtifactType artifactType = SrampAtomUtils.getArtifactType(entry);
			entry = client.getFullArtifactEntry(artifactType, uuid);
			return SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} else if (feed.getEntries().size() > 1) {
			// If we got multiple results, then we don't really know what to do.
			logger.info("Found multiple s-ramp artifact entries for GAV information:");
			logger.info(gavInfo.toString());
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
	 * @throws SrampServerException
	 * @throws JAXBException
	 */
	private BaseArtifactType findExistingArtifactByUniversal(SrampAtomApiClient client, MavenGavInfo gavInfo) throws SrampServerException, SrampClientException, JAXBException {
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
