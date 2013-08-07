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

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.overlord.sramp.common.ontology.SrampOntology;

/**
 * Class that knows how to write an ontology to the given JCR node.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyToJCRNode {

	/**
	 * Write the given ontology to the given JCR node.
	 * @param ontology
	 * @param jcrNode
	 * @throws RepositoryException
	 */
	public void write(SrampOntology ontology, Node jcrNode) throws RepositoryException {
		jcrNode.setProperty("sramp:uuid", ontology.getUuid()); //$NON-NLS-1$
		jcrNode.setProperty("sramp:label", ontology.getLabel()); //$NON-NLS-1$
		jcrNode.setProperty("sramp:comment", ontology.getComment()); //$NON-NLS-1$
		jcrNode.setProperty("sramp:base", ontology.getBase()); //$NON-NLS-1$
		jcrNode.setProperty("sramp:id", ontology.getId()); //$NON-NLS-1$

		for (SrampOntology.Class sclass : ontology.getRootClasses()) {
			addClass(jcrNode, sclass);
		}
	}

	/**
	 * Adds a class node.
	 * @param parentNode
	 * @param sclass
	 * @throws RepositoryException
	 */
	private void addClass(Node parentNode, SrampOntology.Class sclass) throws RepositoryException {
		Node classNode = parentNode.addNode(sclass.getId(), "sramp:class"); //$NON-NLS-1$
		classNode.setProperty("sramp:uri", sclass.getUri().toString()); //$NON-NLS-1$
		classNode.setProperty("sramp:id", sclass.getId()); //$NON-NLS-1$
		classNode.setProperty("sramp:label", sclass.getLabel()); //$NON-NLS-1$
		classNode.setProperty("sramp:comment", sclass.getComment()); //$NON-NLS-1$

		for (SrampOntology.Class childClass : sclass.getChildren()) {
			addClass(classNode, childClass);
		}
	}

}
