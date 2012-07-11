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
package org.overlord.sramp.maven.repo.handlers;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.maven.repo.MavenRepositoryPath;
import org.overlord.sramp.maven.repo.atom.SRAMPAtomApiClient;
import org.overlord.sramp.maven.repo.servlets.MavenRepositoryRequestHandler;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;

/**
 * A handler that downloads/streams the content of an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactPomHandler implements MavenRepositoryRequestHandler {

	private static final String POM_NS = "http://maven.apache.org/POM/4.0.0";
	
	private MavenRepositoryPath repositoryPath;

	/**
	 * Constructor.
	 * @param repositoryPath
	 */
	public ArtifactPomHandler(MavenRepositoryPath repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	/**
	 * @see org.overlord.sramp.maven.repo.servlets.MavenRepositoryRequestHandler#handle(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			// Query the artifact meta data
			Entry fullEntry = SRAMPAtomApiClient.getInstance().getFullArtifactEntry(repositoryPath.getArtifactModel(), 
					repositoryPath.getArtifactType(), repositoryPath.getArtifactUuid());
			if (fullEntry == null)
				throw new Exception("Failed to find artifact with UUID: " + repositoryPath.getArtifactUuid());
			
			// Unwrap the meta data
			ArtifactType type = ArtifactType.valueOf(repositoryPath.getArtifactType());
			Artifact srampArty = fullEntry.getAnyOtherJAXBObject(Artifact.class);
			BaseArtifactType artifact = type.unwrap(srampArty);

			Document pomDoc = generatePom(artifact, type);
			writePomToResponse(pomDoc, response);
		} catch (Exception e) {
			// TODO add a logger
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}

	}

	/**
	 * Generates a Maven pom for the given S-RAMP artifact.
	 * @param artifact an S-RAMP artifact
	 * @param type the artifact type
	 * @return an XML document (the generated Maven pom)
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 * @throws ClassCastException 
	 * @throws URISyntaxException 
	 */
	private Document generatePom(BaseArtifactType artifact, ArtifactType type) throws ClassCastException, ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException {
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
		DOMImplementationLS impl = (DOMImplementationLS) registry.getDOMImplementation("LS");
		LSParser builder = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
		URI templateUri = getClass().getResource("pom.template").toURI();
		Document document = builder.parseURI(templateUri.toString());

		String groupId = type.getModel() + "." + type.name();
		String artifactId = artifact.getUuid();
		String version = artifact.getVersion();
		String name = artifact.getName();
		String description = artifact.getDescription();
		// TODO this might not be good enough - might need to specify the artifact file extension in the ArtifactType enum
		String pomType = type.getModel();

		if (version == null)
			version = "1.0";
		if (name == null || name.trim().length() == 0)
			name = artifactId;

		setElementText(document, "groupId", groupId);
		setElementText(document, "artifactId", artifactId);
		setElementText(document, "version", version);
		setElementText(document, "name", name);
		setElementText(document, "description", description);
		setElementText(document, "type", pomType);
		
		return document;
	}

	/**
	 * Sets the text of an element.
	 * @param document the source {@link Document}
	 * @param elementName the name of the {@link Element} to change
	 * @param value the new value for the {@link Element}
	 */
	private void setElementText(Document document, String elementName, String value) {
		Element elem = (Element) document.getElementsByTagNameNS(POM_NS, elementName).item(0);
		elem.appendChild(document.createTextNode(value));
	}

	/**
	 * Writes the generated pom out to the response.
	 * @param pomDoc an XML document (the generated Maven pom)
	 * @param response the http response
	 * @throws IOException 
	 */
	private void writePomToResponse(Document pomDoc, HttpServletResponse response) throws IOException {
		DOMImplementationLS domImplementation = (DOMImplementationLS) pomDoc.getImplementation();
	    LSSerializer lsSerializer = domImplementation.createLSSerializer();
	    String serializedPom = lsSerializer.writeToString(pomDoc);
	    byte [] pomBytes = serializedPom.getBytes("UTF-8");
	    response.setContentType("text/xml");
	    response.setContentLength(serializedPom.length());
	    response.getOutputStream().write(pomBytes);
	}

}
