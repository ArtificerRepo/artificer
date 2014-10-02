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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;

import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.ontology.OntologyUpdateException;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.common.ontology.SrampOntology.SrampOntologyClass;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.repository.jcr.i18n.Messages;

/**
 * Class that knows how to write an ontology to the given JCR node.
 *
 * @author eric.wittmann@redhat.com
 */
public final class OntologyToJCRNode {

    /**
     * Constructor.
     */
    public OntologyToJCRNode() {
    }

	/**
	 * Write the given ontology to the given JCR node.
	 * @param ontology
	 * @param jcrNode
	 * @throws RepositoryException
	 */
	public void write(SrampOntology ontology, Node jcrNode) throws RepositoryException {
		jcrNode.setProperty(JCRConstants.SRAMP_UUID, ontology.getUuid());
		jcrNode.setProperty(JCRConstants.SRAMP_LABEL, ontology.getLabel());
		jcrNode.setProperty(JCRConstants.SRAMP_COMMENT, ontology.getComment());
		jcrNode.setProperty(JCRConstants.SRAMP_BASE, ontology.getBase());
		jcrNode.setProperty(JCRConstants.SRAMP_ID, ontology.getId());

		for (SrampOntology.SrampOntologyClass sclass : ontology.getRootClasses()) {
			addClass(jcrNode, sclass);
		}
	}

	/**
	 * Adds a class node.
	 * @param parentNode
	 * @param sclass
	 * @throws RepositoryException
	 */
	private void addClass(Node parentNode, SrampOntology.SrampOntologyClass sclass) throws RepositoryException {
		Node classNode = parentNode.addNode(sclass.getId(), JCRConstants.SRAMP_CLASS);
		classNode.setProperty(JCRConstants.SRAMP_URI, sclass.getUri().toString());
		classNode.setProperty(JCRConstants.SRAMP_ID, sclass.getId());
		classNode.setProperty(JCRConstants.SRAMP_LABEL, sclass.getLabel());
		classNode.setProperty(JCRConstants.SRAMP_COMMENT, sclass.getComment());

		for (SrampOntology.SrampOntologyClass childClass : sclass.getChildren()) {
			addClass(classNode, childClass);
		}
	}

    /**
     * Updates the given existing ontology in JCR.  This should create any missing nodes
     * as well as delete nodes that are not in the ontology.
     * @param ontology
     * @param ontologyJcrNode
     * @throws RepositoryException
     * @throws SrampException
     */
    public void update(SrampOntology ontology, Node ontologyJcrNode) throws RepositoryException, SrampException {
        String base = ontologyJcrNode.getProperty(JCRConstants.SRAMP_BASE).getString();
        if (!base.equals(ontology.getBase())) {
            throw new OntologyUpdateException(Messages.i18n.format("CANNOT_CHANGE_ONTOLOGY_BASE"));
        }

        ontologyJcrNode.setProperty(JCRConstants.SRAMP_LABEL, ontology.getLabel());
        ontologyJcrNode.setProperty(JCRConstants.SRAMP_COMMENT, ontology.getComment());
        ontologyJcrNode.setProperty(JCRConstants.SRAMP_ID, ontology.getId());

        // Check for deleted root classes first
        NodeIterator childNodes = ontologyJcrNode.getNodes();
        while (childNodes.hasNext()) {
            Node childNode = childNodes.nextNode();
            String childNodeId = childNode.getProperty(JCRConstants.SRAMP_ID).getString();
            if (!hasClass(ontology.getRootClasses(), childNodeId)) {
                childNode.remove();
            }
        }

        // Now add/update any root classes
        for (SrampOntology.SrampOntologyClass sclass : ontology.getRootClasses()) {
            addOrUpdateClass(ontologyJcrNode, sclass);
        }

    }

    /**
     * Either adds a new node for the given class or else updates an existing one.  This is
     * determined by checking the parent node for a child node with the ID of the class.
     * @param parentNode
     * @param sclass
     * @throws RepositoryException
     */
    private void addOrUpdateClass(Node parentNode, SrampOntologyClass sclass) throws RepositoryException {
        if (parentNode.hasNode(sclass.getId())) {
            Node classNode = parentNode.getNode(sclass.getId());
            classNode.setProperty(JCRConstants.SRAMP_LABEL, sclass.getLabel());
            classNode.setProperty(JCRConstants.SRAMP_COMMENT, sclass.getComment());
            // Check for deleted classes first
            NodeIterator childNodes = classNode.getNodes();
            while (childNodes.hasNext()) {
                Node childNode = childNodes.nextNode();
                String childNodeId = childNode.getProperty(JCRConstants.SRAMP_ID).getString();
                if (!hasClass(sclass.getChildren(), childNodeId)) {
                    childNode.remove();
                }
            }
            // Now add/update any classes that are in common
            for (SrampOntology.SrampOntologyClass childClass : sclass.getChildren()) {
                addOrUpdateClass(classNode, childClass);
            }
        } else {
            addClass(parentNode, sclass);
        }
    }

    /**
     * Returns true if given list of classes contains a class with the given ID.
     * @param sclass
     * @param classId
     */
    private boolean hasClass(List<SrampOntologyClass> classes, String classId) {
        for (SrampOntologyClass childClass : classes) {
            if (childClass.getId().equals(classId)) {
                return true;
            }
        }
        return false;
    }

}
