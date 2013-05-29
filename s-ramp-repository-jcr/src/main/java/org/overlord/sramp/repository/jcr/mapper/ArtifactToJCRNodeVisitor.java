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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFaultEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.FaultEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.MessageEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTypeEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtensionEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdTypeEnum;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter;
import org.overlord.sramp.repository.jcr.ClassificationHelper;
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * An artifact visitor used to update a JCR node.  This class is responsible
 * for modifying a JCR node using information found in the supplied s-ramp
 * artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactToJCRNodeVisitor extends HierarchicalArtifactVisitorAdapter {

    private ArtifactType artifactType;
	private Node jcrNode;
	private Exception error;
	private JCRReferenceFactory referenceFactory;
	private ClassificationHelper classificationHelper;
	private boolean processRelationships = true;

	/**
	 * Constructor.
     * @param artifactType the type of the artifact being persisted
	 * @param jcrNode the JCR node this visitor will be updating
	 * @param referenceFactory a resolver to find JCR nodes by UUID
	 * @param classificationHelper helps resolve, verify, and normalize classifications
	 */
    public ArtifactToJCRNodeVisitor(ArtifactType artifactType, Node jcrNode,
            JCRReferenceFactory referenceFactory, ClassificationHelper classificationHelper) {
	    this.artifactType = artifactType;
		this.jcrNode = jcrNode;
		this.referenceFactory = referenceFactory;
		this.classificationHelper = classificationHelper;
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		try {
			updateArtifactMetaData(artifact);
			updateClassifications(artifact);
			updateArtifactProperties(artifact);
			updateGenericRelationships(artifact);
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
		try {
			setRelationship("relatedDocument", 1, 1, artifact.getRelatedDocument().getArtifactType().toString(),
					false, artifact.getRelatedDocument());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
	 */
	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
		try {
			this.jcrNode.setProperty("sramp:namespace", artifact.getNamespace());

			setRelationships("extension", -1, 1, WsdlExtensionEnum.WSDL_EXTENSION.toString(), false, artifact.getExtension());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitNamedWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType)
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
			setProperty("sramp:name", artifact.getName());
		else
		    setProperty("sramp:name", artifact.getClass().getSimpleName());
		if (artifact.getDescription() != null)
			setProperty("sramp:description", artifact.getDescription());
		if (artifact.getVersion() != null)
			setProperty("version", artifact.getVersion());
		setProperty("sramp:derived", this.artifactType.isDerived());
	}

	/**
	 * Updates the classifications.
	 *
	 * @param artifact
	 * @throws Exception
	 */
	private void updateClassifications(BaseArtifactType artifact) throws Exception {
		Collection<URI> classifications = this.classificationHelper.resolveAll(artifact.getClassifiedBy());
		Collection<URI> normalizedClassifications = this.classificationHelper.normalizeAll(classifications);

		// Store the classifications
		String [] values = new String[classifications.size()];
		int idx = 0;
		for (URI classification : classifications) {
			values[idx++] = classification.toString();
		}
		setProperty("sramp:classifiedBy", values);

		// Store the normalized classifications
		values = new String[normalizedClassifications.size()];
		idx = 0;
		for (URI classification : normalizedClassifications) {
			values[idx++] = classification.toString();
		}
		setProperty("sramp:normalizedClassifiedBy", values);
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
			setProperty(qname, val);
		}
	}

	/**
	 * Updates the generic artifact relationships.
	 * @param artifact
	 * @throws RepositoryException
	 */
	private void updateGenericRelationships(BaseArtifactType artifact) throws Exception {
		// Create/Update all the relationships included in the artifact
		Set<String> updatedRelationshipTypes = new HashSet<String>();
		for (Relationship relationship : artifact.getRelationship()) {
			setRelationships(relationship.getRelationshipType(), -1, 0, null, true, relationship.getRelationshipTarget());
			updatedRelationshipTypes.add(relationship.getRelationshipType());
		}

		// Now remove any relationships that weren't just updated or created (the ones
		// not included on the artifact but that have existing JCR nodes).
		NodeIterator existingNodes = this.jcrNode.getNodes();
		while (existingNodes.hasNext()) {
			Node node = existingNodes.nextNode();
			// Only roemove generic relationships
			if (node.isNodeType("sramp:relationship") && node.hasProperty("sramp:generic")
					&& node.getProperty("sramp:generic").getBoolean()) {
				String type = node.getProperty("sramp:relationshipType").getString();
				// If this relationship type was *not* updated above, then remove it because
				// it's not included in the latest artifact meta-data
				if (!updatedRelationshipTypes.contains(type)) {
					node.remove();
				}
			}
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
	    super.visit(artifact);
        try {
            setProperty("sramp:targetNamespace", artifact.getTargetNamespace());
        } catch (Exception e) {
            error = e;
        }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty("sramp:namespace", artifact.getNamespace());
            setProperty("sramp:ncName", artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty("sramp:namespace", artifact.getNamespace());
            setProperty("sramp:ncName", artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty("sramp:namespace", artifact.getNamespace());
            setProperty("sramp:ncName", artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty("sramp:namespace", artifact.getNamespace());
            setProperty("sramp:ncName", artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		super.visit(artifact);
		try {
			setProperty("sramp:targetNamespace", artifact.getTargetNamespace());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * Message has references to all its {@link Part}s.
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
	 */
	@Override
	public void visit(Message artifact) {
		super.visit(artifact);
		try {
			setRelationships("part", -1, 1, PartEnum.PART.toString(), false, artifact.getPart());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
	 */
	@Override
	public void visit(Part artifact) {
		super.visit(artifact);
		try {
			if (artifact.getElement() != null) {
				if (this.jcrNode.hasNode("sramp-relationships:type")) {
					this.jcrNode.getNode("sramp-relationships:type").remove();
				}
				setRelationship("element", 1, 1, ElementEnum.ELEMENT.toString(), false, artifact.getElement());
			} else if (artifact.getType() != null) {
				if (this.jcrNode.hasNode("sramp-relationships:element")) {
					this.jcrNode.getNode("sramp-relationships:element").remove();
				}
				setRelationship("type", 1, 1, XsdTypeEnum.XSD_TYPE.toString(), false, artifact.getType());
			}
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		super.visit(artifact);
		try {
			setRelationships("operation", -1, 1, OperationEnum.OPERATION.toString(), false, artifact.getOperation());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		super.visit(artifact);
		try {
			setRelationship("input", 1, 1, OperationInputEnum.OPERATION_INPUT.toString(), false, artifact.getInput());
			setRelationship("output", 1, 1, OperationOutputEnum.OPERATION_OUTPUT.toString(), false, artifact.getOutput());
			setRelationships("fault", -1, 1, FaultEnum.FAULT.toString(), false, artifact.getFault());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		super.visit(artifact);
		try {
			setRelationship("message", 1, 1, MessageEnum.MESSAGE.toString(), false, artifact.getMessage());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		super.visit(artifact);
		try {
			setRelationship("message", 1, 1, MessageEnum.MESSAGE.toString(), false, artifact.getMessage());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		super.visit(artifact);
		try {
			setRelationship("message", 1, 1, MessageEnum.MESSAGE.toString(), false, artifact.getMessage());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		super.visit(artifact);
		try {
			setRelationships("bindingOperation", -1, 1, BindingOperationEnum.BINDING_OPERATION.toString(), false, artifact.getBindingOperation());
			setRelationship("portType", 1, 1, PortTypeEnum.PORT_TYPE.toString(), false, artifact.getPortType());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding)
	 */
	@Override
	public void visit(SoapBinding artifact) {
		super.visit(artifact);
		try {
			setProperty("sramp:style", artifact.getStyle());
			setProperty("sramp:transport", artifact.getTransport());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		super.visit(artifact);
		try {
			setRelationship("input", 1, 1, BindingOperationInputEnum.BINDING_OPERATION_INPUT.toString(), false, artifact.getInput());
			setRelationship("output", 1, 1, BindingOperationOutputEnum.BINDING_OPERATION_OUTPUT.toString(), false, artifact.getOutput());
			setRelationships("fault", -1, 1, BindingOperationFaultEnum.BINDING_OPERATION_FAULT.toString(), false, artifact.getFault());
			setRelationship("operation", 1, 1, OperationEnum.OPERATION.toString(), false, artifact.getOperation());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {
		super.visit(artifact);
		try {
			setRelationships("port", -1, 1, PortEnum.PORT.toString(), false, artifact.getPort());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
	 */
	@Override
	public void visit(Port artifact) {
		super.visit(artifact);
		try {
			setRelationship("binding", 1, 1, BindingEnum.BINDING.toString(), false, artifact.getBinding());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress)
	 */
	@Override
	public void visit(SoapAddress artifact) {
		super.visit(artifact);
		try {
			setProperty("sramp:soapLocation", artifact.getSoapLocation());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets all of the custom properties from the artifact and returns them as a map.
	 * @param artifact
	 */
	private static Map<String, String> getArtifactProperties(BaseArtifactType artifact) {
		Map<String, String> props = new HashMap<String, String>();
		for (org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property property : artifact.getProperty())
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
	 * @param minCardinality
	 * @param targetType
	 * @param isGeneric
	 * @param target
	 * @throws Exception
	 */
	private void setRelationship(String relationshipType, int maxCardinality, int minCardinality,
			String targetType, boolean isGeneric, Target target) throws Exception {
	    if (!isProcessRelationships())
	        return;
		if (target != null || minCardinality == 0) {
			Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, relationshipType, maxCardinality,
					targetType, isGeneric);
			Value [] values = new Value[1];
			values[0] = this.referenceFactory.createReference(target.getValue());
			relationshipNode.setProperty("sramp:relationshipTarget", values);
		} else {
			// If the minimum cardinality is > 0 but no targets have been provided, then
			// remove the relationship node.
			removeRelationship(relationshipType);
		}
	}

	/**
	 * Sets a relationship on the given artifact parent node.
	 * @param relationshipType
	 * @param maxCardinality
	 * @param minCardinality
	 * @param targetType
	 * @param isGeneric
	 * @param targets
	 * @throws Exception
	 */
	private void setRelationships(String relationshipType, int maxCardinality, int minCardinality,
			String targetType, boolean isGeneric, List<? extends Target> targets) throws Exception {
        if (!isProcessRelationships())
            return;
		if ((targets != null && targets.size() > 0) || minCardinality == 0) {
			Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, relationshipType, maxCardinality,
					targetType, isGeneric);
			Value[] values = new Value[targets.size()];
			for (int idx = 0; idx < targets.size(); idx++) {
				values[idx] = this.referenceFactory.createReference(targets.get(idx).getValue());
			}
			relationshipNode.setProperty("sramp:relationshipTarget", values);
		} else {
			// If the minimum cardinality is > 0 but no targets have been provided, then
			// remove the relationship node.
			removeRelationship(relationshipType);
		}
	}

	/**
	 * Removes the relationship of the given type from the JCR node.
	 * @param relationshipType
	 * @throws Exception
	 */
	private void removeRelationship(String relationshipType) throws RepositoryException, VersionException,
	LockException, ConstraintViolationException, AccessDeniedException, PathNotFoundException {
		String nodeName = "sramp-relationships:" + relationshipType;
		if (this.jcrNode.hasNode(nodeName)) {
			this.jcrNode.getNode(nodeName).remove();
		}
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
	 * Sets the named property.  Only sets the value if it has changed.  Call this method
	 * rather than directly setting the property on the node so that auditing can work
	 * properly.
	 * @param propertyName
	 * @param propertyValue
	 * @throws RepositoryException
	 * @throws PathNotFoundException
	 */
	protected void setProperty(String propertyName, String propertyValue) throws PathNotFoundException, RepositoryException {
	    if (!this.jcrNode.hasProperty(propertyName)) {
	        this.jcrNode.setProperty(propertyName, propertyValue);
	    } else {
	        Property prop = this.jcrNode.getProperty(propertyName);
	        String currentValue = prop.getValue().getString();
	        if (!currentValue.equals(propertyValue)) {
	            prop.setValue(propertyValue);
	        }
	    }
	}

    /**
     * Sets the named property.  Only sets the value if it has changed.  Call this method
     * rather than directly setting the property on the node so that auditing can work
     * properly.
     * @param propertyName
     * @param propertyValue
     * @throws RepositoryException
     * @throws PathNotFoundException
     */
    protected void setProperty(String propertyName, boolean propertyValue) throws PathNotFoundException, RepositoryException {
        if (!this.jcrNode.hasProperty(propertyName)) {
            this.jcrNode.setProperty(propertyName, propertyValue);
        } else {
            Property prop = this.jcrNode.getProperty(propertyName);
            boolean currentValue = prop.getValue().getBoolean();
            if (currentValue != propertyValue) {
                prop.setValue(propertyValue);
            }
        }
    }

    /**
     * Sets the named property.  Only sets the value if it has changed.  Call this method
     * rather than directly setting the property on the node so that auditing can work
     * properly.
     * @param propertyName
     * @param propertyValue
     * @throws RepositoryException
     * @throws PathNotFoundException
     */
    protected void setProperty(String propertyName, String [] propertyValue) throws PathNotFoundException, RepositoryException {
        if (!this.jcrNode.hasProperty(propertyName)) {
            this.jcrNode.setProperty(propertyName, propertyValue);
        } else {
            Set<String> newValues = new HashSet<String>();
            for (String v : propertyValue) {
                newValues.add(v);
            }
            Property prop = this.jcrNode.getProperty(propertyName);
            Value[] currentValue = prop.getValues();
            boolean identical = newValues.size() == currentValue.length;
            for (Value value : currentValue) {
                if (!newValues.contains(value)) {
                    identical = false;
                    break;
                }
            }
            if (!identical) {
                this.jcrNode.setProperty(propertyName, propertyValue);
            }
        }
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
     * @return the processRelationships
     */
    public boolean isProcessRelationships() {
        return processRelationships;
    }

    /**
     * @param processRelationships the processRelationships to set
     */
    public void setProcessRelationships(boolean processRelationships) {
        this.processRelationships = processRelationships;
    }

    /**
	 * Interface used by this visitor to resolve JCR node references by s-ramp
	 * UUID.  In other words, given an s-ramp UUID, this interface will create a
	 * reference to the appropriate JCR node.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	public static interface JCRReferenceFactory {
		/**
		 * Creates a reference value to another JCR node.
         * @param uuid UUID of target artifact
		 * @return a reference Value
		 * @throws SrampException
		 */
		public Value createReference(String uuid) throws SrampException;
	}

}
