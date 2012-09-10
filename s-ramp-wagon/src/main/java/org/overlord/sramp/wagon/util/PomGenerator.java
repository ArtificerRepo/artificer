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
package org.overlord.sramp.wagon.util;

import java.io.InputStream;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.overlord.sramp.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class used to generate a POM for an S-RAMP artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class PomGenerator {
	private static final String POM_NS = "http://maven.apache.org/POM/4.0.0";

	/**
	 * Default constructor.
	 */
	public PomGenerator() {
	}

	/**
	 * Generates a Maven pom for the given S-RAMP artifact.
	 * @param artifact an S-RAMP artifact
	 * @param type the artifact type
	 * @return an XML document (the generated Maven pom)
	 * @throws Exception
	 */
	public Document generatePom(BaseArtifactType artifact, ArtifactType type) throws Exception {
		Document document = loadTemplate();

		String groupId = type.getArtifactType().getModel() + "." + type.getArtifactType().getType();
		String artifactId = artifact.getUuid();
		String version = artifact.getVersion();
		String name = artifact.getName();
		String description = artifact.getDescription();
		// TODO this might not be good enough - might need to specify the artifact file extension in the ArtifactType enum
		String pomType = type.getArtifactType().getModel();

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
	 * Loads the pom.xml template.
	 * @throws ClassNotFoundException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws URISyntaxException
	 */
	private Document loadTemplate() throws Exception {
		InputStream templateIS = getClass().getResourceAsStream("pom.template");
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		dbFactory.setNamespaceAware(true);
		dbFactory.setValidating(false);
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(templateIS);
		return doc;
	}

	/**
	 * Sets the text of an element.
	 * @param document the source {@link Document}
	 * @param elementName the name of the {@link Element} to change
	 * @param value the new value for the {@link Element}
	 */
	private void setElementText(Document document, String elementName, String value) {
		if (value == null)
			return;
		Element elem = (Element) document.getElementsByTagNameNS(POM_NS, elementName).item(0);
		elem.appendChild(document.createTextNode(value));
	}

}
