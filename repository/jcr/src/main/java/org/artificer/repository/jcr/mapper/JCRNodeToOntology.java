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
package org.artificer.repository.jcr.mapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.repository.jcr.JCRConstants;

/**
 * Reads an ontology from the given JCR node.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRNodeToOntology {

	/**
	 * Reads an ontology from the given JCR node.
	 * @param ontology
	 * @param jcrNode
	 * @throws RepositoryException
	 */
	public void read(ArtificerOntology ontology, Node jcrNode) throws RepositoryException {
		ontology.setUuid(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_UUID));
		ontology.setLabel(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_LABEL));
		ontology.setComment(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_COMMENT));
		ontology.setBase(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_BASE));
		ontology.setId(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_ID));
		ontology.setCreatedBy(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.JCR_CREATED_BY));
		ontology.setLastModifiedBy(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.JCR_LAST_MODIFIED_BY));
		if (jcrNode.hasProperty(JCRConstants.JCR_CREATED)) {
			Date d = jcrNode.getProperty(JCRConstants.JCR_CREATED).getDate().getTime();
			ontology.setCreatedOn(d);
		}
		if (jcrNode.hasProperty(JCRConstants.JCR_LAST_MODIFIED)) {
			Date d = jcrNode.getProperty(JCRConstants.JCR_LAST_MODIFIED).getDate().getTime();
			ontology.setLastModifiedOn(d);
		}

		NodeIterator nodes = jcrNode.getNodes();
		while (nodes.hasNext()) {
			Node childNode = nodes.nextNode();
			ArtificerOntology.ArtificerOntologyClass sclass = readClass(childNode);
			sclass.setParent(null);
			ontology.getRootClasses().add(sclass);
		}

	}

	/**
	 * Reads an ontology class from the given JCR node.
	 * @param jcrNode
	 * @throws RepositoryException
	 */
	private ArtificerOntology.ArtificerOntologyClass readClass(Node jcrNode) throws RepositoryException {
		ArtificerOntology.ArtificerOntologyClass rval = new ArtificerOntology.ArtificerOntologyClass();

		try {
			rval.setUri(new URI(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_URI)));
		} catch (URISyntaxException e) {
			throw new RepositoryException(e);
		}
		rval.setId(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_ID));
		rval.setLabel(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_LABEL));
		rval.setComment(JCRMapperUtil.getPropertyString(jcrNode, JCRConstants.SRAMP_COMMENT));

		NodeIterator nodes = jcrNode.getNodes();
		while (nodes.hasNext()) {
			Node childNode = nodes.nextNode();
			ArtificerOntology.ArtificerOntologyClass sclass = readClass(childNode);
			sclass.setParent(rval);
			rval.getChildren().add(sclass);
		}

		return rval;
	}

}
