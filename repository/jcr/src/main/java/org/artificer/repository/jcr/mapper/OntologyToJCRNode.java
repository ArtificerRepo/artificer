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

import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.common.ontology.ArtificerOntology;
import org.artificer.common.ontology.ArtificerOntologyClass;
import org.artificer.repository.jcr.JCRConstants;
import org.artificer.repository.jcr.i18n.Messages;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import java.util.List;

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
	public void write(ArtificerOntology ontology, Node jcrNode) throws RepositoryException {
		jcrNode.setProperty(JCRConstants.SRAMP_UUID, ontology.getUuid());
		jcrNode.setProperty(JCRConstants.SRAMP_LABEL, ontology.getLabel());
		jcrNode.setProperty(JCRConstants.SRAMP_COMMENT, ontology.getComment());
		jcrNode.setProperty(JCRConstants.SRAMP_BASE, ontology.getBase());
		jcrNode.setProperty(JCRConstants.SRAMP_ID, ontology.getId());

		for (ArtificerOntologyClass sclass : ontology.getRootClasses()) {
			addClass(jcrNode, sclass);
		}
	}

	/**
	 * Adds a class node.
	 * @param parentNode
	 * @param sclass
	 * @throws RepositoryException
	 */
	private void addClass(Node parentNode, ArtificerOntologyClass sclass) throws RepositoryException {
		Node classNode = parentNode.addNode(sclass.getId(), JCRConstants.SRAMP_CLASS);
		classNode.setProperty(JCRConstants.SRAMP_URI, sclass.getUri().toString());
		classNode.setProperty(JCRConstants.SRAMP_ID, sclass.getId());
		classNode.setProperty(JCRConstants.SRAMP_LABEL, sclass.getLabel());
		classNode.setProperty(JCRConstants.SRAMP_COMMENT, sclass.getComment());

		for (ArtificerOntologyClass childClass : sclass.getChildren()) {
			addClass(classNode, childClass);
		}
	}

    /**
     * Updates the given existing ontology in JCR.  This should create any missing nodes
     * as well as delete nodes that are not in the ontology.
     * @param ontology
     * @param ontologyJcrNode
     * @throws RepositoryException
     * @throws org.artificer.common.ArtificerException
     */
    public void update(ArtificerOntology ontology, Node ontologyJcrNode) throws RepositoryException, ArtificerException {
        String base = ontologyJcrNode.getProperty(JCRConstants.SRAMP_BASE).getString();
        if (!base.equals(ontology.getBase())) {
            throw new ArtificerUserException(Messages.i18n.format("CANNOT_CHANGE_ONTOLOGY_BASE"));
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
        for (ArtificerOntologyClass sclass : ontology.getRootClasses()) {
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
    private void addOrUpdateClass(Node parentNode, ArtificerOntologyClass sclass) throws RepositoryException {
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
            for (ArtificerOntologyClass childClass : sclass.getChildren()) {
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
    private boolean hasClass(List<ArtificerOntologyClass> classes, String classId) {
        for (ArtificerOntologyClass childClass : classes) {
            if (childClass.getId().equals(classId)) {
                return true;
            }
        }
        return false;
    }

}
