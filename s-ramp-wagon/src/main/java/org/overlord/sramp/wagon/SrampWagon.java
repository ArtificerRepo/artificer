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
import java.io.InputStream;
import java.io.StringWriter;
import java.security.MessageDigest;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.apache.maven.wagon.ConnectionException;
import org.apache.maven.wagon.InputData;
import org.apache.maven.wagon.OutputData;
import org.apache.maven.wagon.ResourceDoesNotExistException;
import org.apache.maven.wagon.StreamWagon;
import org.apache.maven.wagon.TransferFailedException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.authentication.AuthenticationException;
import org.apache.maven.wagon.authorization.AuthorizationException;
import org.apache.maven.wagon.resource.Resource;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.logging.Logger;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.wagon.models.MavenGavInfo;
import org.overlord.sramp.wagon.util.PomGenerator;
import org.overlord.sramp.wagon.util.SrampAtomApiClient;
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
		logger.debug("Looking up resource: " + resource);
		try {
			MavenGavInfo gavInfo = MavenGavInfo.fromResource(resource);
			logger.debug("GAV: " + gavInfo);
			// TODO get the endpoint from the pom
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
		} catch (Throwable t) {
			this.logger.error(t.getMessage(), t);
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
	 * @see org.apache.maven.wagon.StreamWagon#fillOutputData(org.apache.maven.wagon.OutputData)
	 */
	@Override
	public void fillOutputData(OutputData outputData) throws TransferFailedException {
		// TODO Auto-generated method stub
		System.out.println("fillOutputData");
	}

}
