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
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.visitors.ArtifactVisitor;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToArtifactVisitor;
import org.overlord.sramp.repository.jcr.mapper.JCRNodeToArtifactVisitor.JCRReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple visitor that will create an S-RAMP artifact from a
 *
 * @author eric.wittmann@redhat.com
 */
public final class JCRNodeToArtifactFactory {

    private static Logger log = LoggerFactory.getLogger(JCRRepositoryFactory.class);

	/**
	 * Private constructor.
	 */
	private JCRNodeToArtifactFactory() {
	}

	/**
	 * Creates a S-RAMP artifact from the given JCR node.
	 * @param jcrNode a JCR node
	 */
	public static BaseArtifactType createArtifact(final Session session, Node jcrNode) {
        try {
			String artifactType = jcrNode.getProperty(JCRConstants.SRAMP_ARTIFACT_TYPE).getValue().getString();
			return createArtifact(session, jcrNode, ArtifactType.valueOf(artifactType));
		} catch (PathNotFoundException e) {
			throw new RuntimeException(Messages.i18n.format("INVALID_JCR_NODE"), e);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates a S-RAMP artifact from the given JCR node.
	 * @param jcrNode a node in the JCR repo
	 * @param artifactType the type of artifact represented by the {@link Node}
	 * @return S-RAMP artifact
	 * @throws SrampException
	 */
    public static BaseArtifactType createArtifact(final Session session, Node jcrNode,
            ArtifactType artifactType) throws SrampException {        
		try {
	        // Early exit.  Atom service needs to 404 if the wrong model was given.
	        String jcrArtifactType = jcrNode.getProperty(JCRConstants.SRAMP_ARTIFACT_TYPE).getValue().getString();
	        if (! jcrArtifactType.equalsIgnoreCase(artifactType.getArtifactType().getApiType().value())) {
	            return null;
	        }
	        
			BaseArtifactType artifact = artifactType.newArtifactInstance();
			ArtifactVisitor visitor = new JCRNodeToArtifactVisitor(jcrNode, new JCRReferenceResolver() {
				@Override
				public String resolveReference(Value reference) {
					try {
						String ident = reference.getString();
						Node node = session.getNodeByIdentifier(ident);
						return node.getProperty(JCRConstants.SRAMP_UUID).getString();
					} catch (Exception e) {
						log.debug(Messages.i18n.format("ERROR_RESOLVING_JCR_REF"), e);
					}
					return null;
				}
			});
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			return artifact;
		} catch (Exception e) {
			if (e.getCause() != null) {
				throw new SrampServerException(e.getCause());
			} else {
				throw new SrampServerException(e);
			}
		}
	}

}
