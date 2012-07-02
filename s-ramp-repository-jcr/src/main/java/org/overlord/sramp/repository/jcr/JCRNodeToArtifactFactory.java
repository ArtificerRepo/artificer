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
package org.overlord.sramp.repository.jcr;

import javax.jcr.Node;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.repository.DerivedArtifactsCreationException;
import org.overlord.sramp.repository.jcr.mapper.XmlModel;
import org.overlord.sramp.repository.jcr.mapper.XsdModel;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * A simple visitor that will create an S-RAMP artifact from a
 * 
 * @author eric.wittmann@redhat.com
 */
public final class JCRNodeToArtifactFactory {

	/**
	 * Private constructor.
	 */
	private JCRNodeToArtifactFactory() {
	}
	
	/**
	 * Creates a S-RAMP artifact from the given JCR node.
	 * @param jcrNode a node in the JCR repo
	 * @param artifactType the type of artifact represented by the {@link Node}
	 * @return S-RAMP artifact
	 */
	public static BaseArtifactType createArtifact(Node jcrNode, ArtifactType artifactType) {
		// TODO I don't think XYZModel.getXYZDocument() should throw a DerivedArtifactsCreationException (something else maybe)
        try {
			if (artifactType == ArtifactType.XsdDocument) {
			    return XsdModel.getXsdDocument(jcrNode);
			} else if (artifactType == ArtifactType.XmlDocument) {
			    return XmlModel.getXmlDocument(jcrNode);
			} else {
				return null;
			}
		} catch (DerivedArtifactsCreationException e) {
			throw new RuntimeException(e);
		}
	}
	
}
