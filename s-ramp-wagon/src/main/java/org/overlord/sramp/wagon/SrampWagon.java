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
import java.util.HashMap;
import java.util.Map;

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
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientUtils;
import org.overlord.sramp.client.SrampServerException;
import org.overlord.sramp.wagon.models.MavenGavInfo;
import org.overlord.sramp.wagon.util.DevNullOutputStream;
import org.overlord.sramp.wagon.util.PomGenerator;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
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
	private static final Map<String, ArtifactType> supportedTypes = new HashMap<String, ArtifactType>();
	static {
		supportedTypes.put("xml", ArtifactType.XmlDocument);
		supportedTypes.put("xsd", ArtifactType.XsdDocument);
		supportedTypes.put("wsdl", ArtifactType.WsdlDocument);
	}
	
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
		// The S-RAMP Atom API is session-less, so no connections to open
	}

	/**
	 * @see org.apache.maven.wagon.StreamWagon#closeConnection()
	 */
	@Override
	public void closeConnection() throws ConnectionException {
		// The S-RAMP Atom API is session-less, so no connections to close
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

			// Query the artifact meta data using universal/uuid form
			String artifactModel = gavInfo.getGroupId().substring(0, gavInfo.getGroupId().indexOf('.'));
			String artifactType = gavInfo.getGroupId().substring(gavInfo.getGroupId().indexOf('.') + 1);
			String uuid = gavInfo.getArtifactId();
			SrampAtomApiClient client = new SrampAtomApiClient(endpoint);
			Entry fullEntry = client.getFullArtifactEntry(artifactModel, artifactType, uuid);
			if (fullEntry == null)
				throw new ResourceDoesNotExistException("Could not find file: '" + resource + "'");

			if ("pom".equals(gavInfo.getType())) {
				String serializedPom = generatePom(artifactType, fullEntry);
			    inputData.setInputStream(new ByteArrayInputStream(serializedPom.getBytes("UTF-8")));
			    return;
			} else if (artifactModel.equals(gavInfo.getType())) {
				// Get the artifact content as an input stream
				InputStream artifactContent = client.getArtifactContent(artifactModel, artifactType, uuid);
				inputData.setInputStream(artifactContent);
				return;
			} else if (gavInfo.getType().equals(artifactModel + ".sha1")) {
				InputStream artifactContent = client.getArtifactContent(artifactModel, artifactType, uuid);
				String sha1Hash = generateSHA1Hash(artifactContent);
			    inputData.setInputStream(new ByteArrayInputStream(sha1Hash.getBytes("UTF-8")));
			    return;
			} else if ("pom.sha1".equals(gavInfo.getType())) {
				// Generate a SHA1 hash on the fly for the POM
				String serializedPom = generatePom(artifactType, fullEntry);
				MessageDigest md = MessageDigest.getInstance("SHA1");
				md.update(serializedPom.getBytes("UTF-8"));
				byte[] mdbytes = md.digest();
				StringBuilder sb = new StringBuilder();
			    for (int i = 0; i < mdbytes.length; i++) {
			    	sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
			    }
			    inputData.setInputStream(new ByteArrayInputStream(sb.toString().getBytes("UTF-8")));
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
	 * @param artifactType
	 * @param fullEntry
	 * @throws Exception
	 */
	private String generatePom(String artifactType, Entry fullEntry) throws Exception {
		ArtifactType type = ArtifactType.valueOf(artifactType);
		Artifact srampArty = fullEntry.getAnyOtherJAXBObject(Artifact.class);
		BaseArtifactType artifact = type.unwrap(srampArty);
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
	 * Gets the artifact type from the resource.  Returns null if the resource type does
	 * not map to an artifact type.
	 * @param resource
	 */
	private ArtifactType getArtifactType(Resource resource) {
		MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
		if (supportedTypes.containsKey(gavInfo.getType())) {
			return supportedTypes.get(gavInfo.getType());
		} else {
			return null;
		}
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
			BaseArtifactType artifact = findExistingArtifact(client, artifactType, gavInfo);
			if (artifact != null) {
				updateArtifactContent(client, artifact, resourceInputStream);
			} else {
				Entry entry = client.uploadArtifact(artifactType, resourceInputStream, resource.getName());
				artifact = SrampClientUtils.unwrapSrampArtifact(artifactType, entry);
				SrampClientUtils.setCustomProperty(artifact, "maven.groupId", gavInfo.getGroupId());
				SrampClientUtils.setCustomProperty(artifact, "maven.artifactId", gavInfo.getArtifactId());
				SrampClientUtils.setCustomProperty(artifact, "maven.version", gavInfo.getVersion());
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
	 * @throws SrampClientException 
	 * @throws SrampServerException 
	 * @throws JAXBException 
	 */
	private BaseArtifactType findExistingArtifact(SrampAtomApiClient client, ArtifactType artifactType, MavenGavInfo gavInfo) throws SrampServerException, SrampClientException, JAXBException {
		String query = String.format("/s-ramp/%1$s/%2$s[@maven.groupId = '%3$s' and @maven.artifactId = '%4$s' and @maven.version = '%5$s']", 
				artifactType.getModel(), artifactType.name(), gavInfo.getGroupId(), gavInfo.getArtifactId(), gavInfo.getVersion());
		Feed feed = client.query(query);
		if (feed.getEntries().size() == 1) {
			Entry entry = feed.getEntries().get(0);
			String uuid = entry.getId().toString();
			entry = client.getFullArtifactEntry(artifactType, uuid);
			return SrampClientUtils.unwrapSrampArtifact(artifactType, entry);
		}
		return null;
	}

	/**
	 * Updates the content of the given artifact.  This is called when the Maven deploy is run
	 * but the s-ramp repository already has an artifact deployed with the same type and GAV 
	 * information given (resulting in a re-deploy of the artifact content).
	 * @param client
	 * @param artifact
	 * @param resourceInputStream
	 * @throws SrampClientException
	 */
	private void updateArtifactContent(SrampAtomApiClient client, BaseArtifactType artifact,
			InputStream resourceInputStream) throws SrampClientException {
		client.updateArtifact(artifact, resourceInputStream);
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
