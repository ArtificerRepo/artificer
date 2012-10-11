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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.NamedWsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.Target;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDerivedArtifactType;

/**
 * An artifact visitor used to update a JCR node.  This class is responsible
 * for modifying a JCR node using information found in the supplied s-ramp
 * artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactToJCRNodeVisitor extends HierarchicalArtifactVisitorAdapter {

	private Node jcrNode;
	private Exception error;
	private JCRReferenceFactory referenceFactory;

	/**
	 * Constructor.
	 * @param jcrNode the JCR node this visitor will be updating
	 * @param referenceFactory a resolver to find JCR nodes by UUID
	 */
	public ArtifactToJCRNodeVisitor(Node jcrNode, JCRReferenceFactory referenceFactory) {
		this.jcrNode = jcrNode;
		this.referenceFactory = referenceFactory;
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		try {
			updateArtifactMetaData(artifact);
			updateArtifactProperties(artifact);
			updateGenericRelationships(artifact);
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitWsdlDerived(org.s_ramp.xmlns._2010.s_ramp.WsdlDerivedArtifactType)
	 */
	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
		try {
			this.jcrNode.setProperty("sramp:namespace", artifact.getNamespace());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitNamedWsdlDerived(org.s_ramp.xmlns._2010.s_ramp.NamedWsdlDerivedArtifactType)
	 */
	@Override
	protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
		try {
			this.jcrNode.setProperty("sramp:ncName", artifact.getNCName());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * Updates the basic s-ramp meta data.
	 * @param artifact
	 * @throws Exception
	 */
	private void updateArtifactMetaData(BaseArtifactType artifact) throws Exception {
		if (artifact.getName() != null)
			this.jcrNode.setProperty("sramp:name", artifact.getName());
		if (artifact.getDescription() != null)
			this.jcrNode.setProperty("sramp:description", artifact.getDescription());
		if (artifact.getVersion() != null)
			this.jcrNode.setProperty("version", artifact.getVersion());
	}

	/**
	 * Updates the custom s-ramp properties.
	 * @param artifact
	 * @throws Exception
	 */
	private void updateArtifactProperties(BaseArtifactType artifact) throws Exception {
		Map<String, String> artifactProps = getArtifactProperties(artifact);
		Set<String> nodeProps = getNodePropertyNames(jcrNode);

		Set<String> propsToRemove = nodeProps;
		propsToRemove.removeAll(artifactProps.keySet());

    	String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";

		// Remove all properties that have been earmarked for removal.
		for (String propToRemove : propsToRemove) {
			String qname = srampPropsPrefix + propToRemove;
			this.jcrNode.setProperty(qname, (Value) null);
		}

		// Set all new property values
		for (Entry<String, String> prop : artifactProps.entrySet()) {
			String name = prop.getKey();
			String qname = srampPropsPrefix + name;
			String val = prop.getValue();
			this.jcrNode.setProperty(qname, val);
		}
	}

	/**
	 * Updates the generic artifact relationships.
	 * @param artifact
	 * @throws RepositoryException
	 */
	private void updateGenericRelationships(BaseArtifactType artifact) throws RepositoryException {
		// First remove all existing relationship nodes.
		// TODO update this to diff the existing node graph with the artifact and apply, rather than
		// delete all and recreate all (and make sure to update the comments appropriately)
		NodeIterator existingNodes = this.jcrNode.getNodes();
		while (existingNodes.hasNext()) {
			Node rNode = existingNodes.nextNode();
			if (rNode.isNodeType("sramp:relationship")) {
				rNode.remove();
			}
		}
		// Create all the relationships
		for (Relationship relationship : artifact.getRelationship()) {
			Node rNode = this.jcrNode.addNode(relationship.getRelationshipType(), "sramp:relationship");
			rNode.setProperty("sramp:relationshipType", relationship.getRelationshipType());
			List<Target> targets = relationship.getRelationshipTarget();
			Value [] values = new Value[targets.size()];
			for (int idx = 0; idx < values.length; idx++) {
				Target target = targets.get(idx);
				Value reference = this.referenceFactory.createReference(target.getValue());
				if (reference == null) {
					throw new RepositoryException("Relationship creation error - failed to find an s-ramp artifact with target UUID: " + target.getValue());
				}
				values[idx] = reference;
			}
			rNode.setProperty("sramp:relationshipTarget", values);
			rNode.setProperty("sramp:generic", true);
		}
	}

	/**
	 * Gets all of the custom properties from the artifact and returns them as a map.
	 * @param artifact
	 */
	private static Map<String, String> getArtifactProperties(BaseArtifactType artifact) {
		Map<String, String> props = new HashMap<String, String>();
		for (org.s_ramp.xmlns._2010.s_ramp.Property property : artifact.getProperty())
			props.put(property.getPropertyName(), property.getPropertyValue());
		return props;
	}

	/**
	 * Gets all of the custom s-ramp property names currently stored on the given
	 * JCR node.
	 * @param jcrNode
	 * @throws RepositoryException
	 */
	private static Set<String> getNodePropertyNames(Node jcrNode) throws RepositoryException {
    	String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";
    	int srampPropsPrefixLen = srampPropsPrefix.length();

    	Set<String> rval = new HashSet<String>();
		PropertyIterator properties = jcrNode.getProperties();
		while (properties.hasNext()) {
			Property prop = properties.nextProperty();
			String propName = prop.getName();
			if (propName.startsWith(srampPropsPrefix)) {
				propName = propName.substring(srampPropsPrefixLen);
				rval.add(propName);
			}
		}
		return rval;
	}

	/**
	 * Returns true if this visitor encountered an error during visitation.
	 */
	public boolean hasError() {
		return error != null;
	}

	/**
	 * Returns the error encountered during visitation.
	 */
	public Exception getError() {
		return error;
	}

	/**
	 * Interface used by this visitor to resolve JCR node references by s-ramp
	 * UUID.  In other words, given an s-ramp UUID, this interface will create a
	 * reference to the appropriate JCR node.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	protected static interface JCRReferenceFactory {

		/**
		 * Creates a reference value to another JCR node.
		 * @param otherNode the node being referenced
		 * @return a reference Value
		 */
		public Value createReference(String uuid);

	}

}
