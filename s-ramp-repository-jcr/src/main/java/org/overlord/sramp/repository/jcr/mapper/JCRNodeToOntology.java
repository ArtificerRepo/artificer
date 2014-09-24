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
package org.overlord.sramp.repository.jcr.mapper;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.overlord.sramp.common.ontology.SrampOntology;

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
	public void read(SrampOntology ontology, Node jcrNode) throws RepositoryException {
		ontology.setUuid(JCRMapperUtil.getPropertyString(jcrNode, "sramp:uuid")); //$NON-NLS-1$
		ontology.setLabel(JCRMapperUtil.getPropertyString(jcrNode, "sramp:label")); //$NON-NLS-1$
		ontology.setComment(JCRMapperUtil.getPropertyString(jcrNode, "sramp:comment")); //$NON-NLS-1$
		ontology.setBase(JCRMapperUtil.getPropertyString(jcrNode, "sramp:base")); //$NON-NLS-1$
		ontology.setId(JCRMapperUtil.getPropertyString(jcrNode, "sramp:id")); //$NON-NLS-1$
		ontology.setCreatedBy(JCRMapperUtil.getPropertyString(jcrNode, "jcr:createdBy")); //$NON-NLS-1$
		ontology.setLastModifiedBy(JCRMapperUtil.getPropertyString(jcrNode, "jcr:lastModifiedBy")); //$NON-NLS-1$
		if (jcrNode.hasProperty("jcr:created")) { //$NON-NLS-1$
			Date d = jcrNode.getProperty("jcr:created").getDate().getTime(); //$NON-NLS-1$
			ontology.setCreatedOn(d);
		}
		if (jcrNode.hasProperty("jcr:lastModified")) { //$NON-NLS-1$
			Date d = jcrNode.getProperty("jcr:lastModified").getDate().getTime(); //$NON-NLS-1$
			ontology.setLastModifiedOn(d);
		}

		NodeIterator nodes = jcrNode.getNodes();
		while (nodes.hasNext()) {
			Node childNode = nodes.nextNode();
			SrampOntology.SrampOntologyClass sclass = readClass(childNode);
			sclass.setParent(null);
			ontology.getRootClasses().add(sclass);
		}

	}

	/**
	 * Reads an ontology class from the given JCR node.
	 * @param jcrNode
	 * @throws RepositoryException
	 */
	private SrampOntology.SrampOntologyClass readClass(Node jcrNode) throws RepositoryException {
		SrampOntology.SrampOntologyClass rval = new SrampOntology.SrampOntologyClass();

		try {
			rval.setUri(new URI(JCRMapperUtil.getPropertyString(jcrNode, "sramp:uri"))); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			throw new RepositoryException(e);
		}
		rval.setId(JCRMapperUtil.getPropertyString(jcrNode, "sramp:id")); //$NON-NLS-1$
		rval.setLabel(JCRMapperUtil.getPropertyString(jcrNode, "sramp:label")); //$NON-NLS-1$
		rval.setComment(JCRMapperUtil.getPropertyString(jcrNode, "sramp:comment")); //$NON-NLS-1$

		NodeIterator nodes = jcrNode.getNodes();
		while (nodes.hasNext()) {
			Node childNode = nodes.nextNode();
			SrampOntology.SrampOntologyClass sclass = readClass(childNode);
			sclass.setParent(rval);
			rval.getChildren().add(sclass);
		}

		return rval;
	}

}
