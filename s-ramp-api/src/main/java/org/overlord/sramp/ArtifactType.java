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
package org.overlord.sramp;

import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * An enum representing all of the Artifact Types defined by S-RAMP.
 * 
 * @author eric.wittmann@redhat.com
 */
public enum ArtifactType {

	Document("core", "Document", DocumentArtifactType.class), 
	XmlDocument("core", "XML Document", XmlDocument.class),
	XsdDocument("xsd", "XML Schema", XsdDocument.class),
	AttributeDeclaration("xsd", "XML Schema Attribute Declaration", AttributeDeclaration.class),
	ElementDeclaration("xsd", "XML Schema Element Declaration", ElementDeclaration.class),
	SimpleTypeDeclaration("xsd", "XML Schema Simple Type Declaration", SimpleTypeDeclaration.class),
	ComplexTypeDeclaration("xsd", "XML Schema Complex Type Declaration", ComplexTypeDeclaration.class),
	;

	private final String model;
	private final String label;
	private final Class<? extends BaseArtifactType> typeClass;

	/**
	 * Constructor.
	 * @param model the S-RAMP Artifact Model that this Artifact Type is a part of
	 * @param label a human friendly label for the artifact type
	 * @param typeClass the class that implements this Artifact Type
	 */
	private ArtifactType(String model, String label, Class<? extends BaseArtifactType> typeClass) {
		this.model = model;
		this.label = label;
		this.typeClass = typeClass;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the typeClass
	 */
	public Class<? extends BaseArtifactType> getTypeClass() {
		return typeClass;
	}
	
	/**
	 * Figures out the type from the artifact instance.
	 * @param artifact
	 */
	public static ArtifactType valueOf(BaseArtifactType artifact) {
		ArtifactType[] values = values();
		for (ArtifactType artifactType : values) {
			if (artifactType.getTypeClass().equals(artifact.getClass())) {
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from artifact class: " + artifact.getClass());
	}
	

}
