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
import org.s_ramp.xmlns._2010.s_ramp.ElementEnum;
import org.s_ramp.xmlns._2010.s_ramp.Fault;
import org.s_ramp.xmlns._2010.s_ramp.FaultEnum;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.MessageEnum;
import org.s_ramp.xmlns._2010.s_ramp.NamedWsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Operation;
import org.s_ramp.xmlns._2010.s_ramp.OperationEnum;
import org.s_ramp.xmlns._2010.s_ramp.OperationInput;
import org.s_ramp.xmlns._2010.s_ramp.OperationInputEnum;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutputEnum;
import org.s_ramp.xmlns._2010.s_ramp.Part;
import org.s_ramp.xmlns._2010.s_ramp.PartEnum;
import org.s_ramp.xmlns._2010.s_ramp.PartTarget;
import org.s_ramp.xmlns._2010.s_ramp.PortType;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.Target;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XsdTypeEnum;

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
		try {
			setRelationship("relatedDocument", 1, artifact.getRelatedDocument().getArtifactType().toString(),
					false, artifact.getRelatedDocument());
		} catch (Exception e) {
			error = e;
		}
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
			// Only roemove generic relationships
			if (rNode.isNodeType("sramp:relationship") && rNode.hasProperty("sramp:generic") && rNode.getProperty("sramp:generic").getBoolean()) {
				rNode.remove();
			}
		}
		// Create all the relationships
		for (Relationship relationship : artifact.getRelationship()) {
			Node rNode = this.jcrNode.addNode("sramp-relationships:" + relationship.getRelationshipType(), "sramp:relationship");
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
	 * Message has references to all its {@link Part}s.
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Message)
	 */
	@Override
	public void visit(Message artifact) {
		super.visit(artifact);

		try {
			setRelationships("part", -1, PartEnum.PART.toString(), false, artifact.getPart());
			Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, "part", -1,
					PartEnum.PART.toString(), false);

			List<PartTarget> partTargets = artifact.getPart();
			Value [] values = new Value[partTargets.size()];
			int idx = 0;
			for (PartTarget target : partTargets) {
				values[idx++] = this.referenceFactory.createReference(target.getValue());
			}
			relationshipNode.setProperty("sramp:relationshipTarget", values);
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Part)
	 */
	@Override
	public void visit(Part artifact) {
		super.visit(artifact);

		try {
			if (artifact.getElement() != null) {
				if (this.jcrNode.hasNode("sramp-relationships:type")) {
					this.jcrNode.getNode("sramp-relationships:type").remove();
				}
				setRelationship("element", 1, ElementEnum.ELEMENT.toString(), false, artifact.getElement());
			} else if (artifact.getType() != null) {
				if (this.jcrNode.hasNode("sramp-relationships:element")) {
					this.jcrNode.getNode("sramp-relationships:element").remove();
				}
				setRelationship("type", 1, XsdTypeEnum.XSD_TYPE.toString(), false, artifact.getType());
			}
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		super.visit(artifact);

		try {
			setRelationships("operation", -1, OperationEnum.OPERATION.toString(), false, artifact.getOperation());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		super.visit(artifact);

		try {
			setRelationship("input", 1, OperationInputEnum.OPERATION_INPUT.toString(), false, artifact.getInput());
			setRelationship("output", 1, OperationOutputEnum.OPERATION_OUTPUT.toString(), false, artifact.getOutput());
			setRelationships("fault", -1, FaultEnum.FAULT.toString(), false, artifact.getFault());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		super.visit(artifact);

		try {
			setRelationship("message", 1, MessageEnum.MESSAGE.toString(), false, artifact.getMessage());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		super.visit(artifact);

		try {
			setRelationship("message", 1, MessageEnum.MESSAGE.toString(), false, artifact.getMessage());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		super.visit(artifact);

		try {
			setRelationship("message", 1, MessageEnum.MESSAGE.toString(), false, artifact.getMessage());
		} catch (Exception e) {
			error = e;
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
	 * Sets a relationship on the given artifact parent node.
	 * @param relationshipType
	 * @param maxCardinality
	 * @param targetType
	 * @param isGeneric
	 * @param target
	 * @throws Exception
	 */
	private void setRelationship(String relationshipType, int maxCardinality, String targetType,
			boolean isGeneric, Target target) throws Exception {
		Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, relationshipType, maxCardinality,
				targetType, isGeneric);
		Value [] values = new Value[1];
		values[0] = this.referenceFactory.createReference(target.getValue());
		relationshipNode.setProperty("sramp:relationshipTarget", values);
	}

	/**
	 * Sets a relationship on the given artifact parent node.
	 * @param relationshipType
	 * @param maxCardinality
	 * @param targetType
	 * @param isGeneric
	 * @param targets
	 * @throws Exception
	 */
	private void setRelationships(String relationshipType, int maxCardinality,
			String targetType, boolean isGeneric, List<? extends Target> targets) throws Exception {
		Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, relationshipType, maxCardinality,
				targetType, isGeneric);
		Value[] values = new Value[targets.size()];
		for (int idx = 0; idx < targets.size(); idx++) {
			values[idx] = this.referenceFactory.createReference(targets.get(idx).getValue());
		}
		relationshipNode.setProperty("sramp:relationshipTarget", values);
	}

	/**
	 * Will either find and return a relationship child node, or else will create a new
	 * one and return that.  The provided information is added to the created node when
	 * appropriate.
	 * @param parentNode
	 * @param relationshipType
	 * @param maxCardinality
	 * @param targetType
	 * @param isGeneric
	 * @throws Exception
	 */
	private static Node getOrCreateRelationshipNode(Node parentNode, String relationshipType,
			int maxCardinality, String targetType, boolean isGeneric) throws Exception {
		Node relationshipNode = null;
		String nodeName = "sramp-relationships:" + relationshipType;
		if (parentNode.hasNode(nodeName)) {
			relationshipNode = parentNode.getNode(nodeName);
		} else {
			relationshipNode = parentNode.addNode(nodeName, "sramp:relationship");
			relationshipNode.setProperty("sramp:relationshipType", relationshipType);
			if (maxCardinality != -1)
				relationshipNode.setProperty("sramp:maxCardinality", maxCardinality);
			if (targetType != null)
				relationshipNode.setProperty("sramp:targetType", targetType);
			relationshipNode.setProperty("sramp:generic", isGeneric);
		}
		return relationshipNode;
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
