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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.FaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.MessageTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTypeTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtensionTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdTypeTarget;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter;
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * A visitor for going from a JCR node to an S-RAMP artifact instance.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRNodeToArtifactVisitor extends HierarchicalArtifactVisitorAdapter {

	private Node jcrNode;
	private JCRReferenceResolver referenceResolver;

	/**
	 * Constructor.
	 * @param jcrNode
	 * @param referenceResolver
	 */
	public JCRNodeToArtifactVisitor(Node jcrNode, JCRReferenceResolver referenceResolver) {
		this.jcrNode = jcrNode;
		this.referenceResolver = referenceResolver;
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		try {
			DatatypeFactory dtFactory = DatatypeFactory.newInstance();
			ArtifactType artifactType = ArtifactType.valueOf(artifact);
			BaseArtifactEnum apiType = artifactType.getArtifactType().getApiType();
			artifact.setArtifactType(apiType);

			// First map in the standard s-ramp meta-data
			artifact.setCreatedBy(getProperty(jcrNode, "jcr:createdBy"));
			XMLGregorianCalendar createdTS = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, "jcr:created"));
			artifact.setCreatedTimestamp(createdTS);
			artifact.setDescription(getProperty(jcrNode, "sramp:description"));
			artifact.setLastModifiedBy(getProperty(jcrNode, "jcr:lastModifiedBy"));
			XMLGregorianCalendar modifiedTS = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, "jcr:lastModified"));
			artifact.setLastModifiedTimestamp(modifiedTS);
			artifact.setName(getProperty(jcrNode, "sramp:name"));
			artifact.setUuid(getProperty(jcrNode, "sramp:uuid"));
			artifact.setVersion(getProperty(jcrNode, "version"));

			// Map in the classifications
			if (jcrNode.hasProperty("sramp:classifiedBy")) {
				Property classifiedByProp = jcrNode.getProperty("sramp:classifiedBy");
				Value [] values = classifiedByProp.getValues();
				for (Value value : values) {
					String classification = value.getString();
					artifact.getClassifiedBy().add(classification);
				}
			}

			// Map in all the s-ramp extended properties.
			String srampPropsPrefix = JCRConstants.SRAMP_PROPERTIES + ":";
			int srampPropsPrefixLen = srampPropsPrefix.length();
			PropertyIterator properties = jcrNode.getProperties();
			while (properties.hasNext()) {
				Property property = properties.nextProperty();
				String propQName = property.getName();
				if (propQName.startsWith(srampPropsPrefix)) {
					String propName = propQName.substring(srampPropsPrefixLen);
					String propValue = property.getValue().getString();
					org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property srampProp = new org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property();
					srampProp.setPropertyName(propName);
					srampProp.setPropertyValue(propValue);
					artifact.getProperty().add(srampProp);
				}
			}

			// Map in the generic relationships
			NodeIterator rnodes = jcrNode.getNodes();
			while (rnodes.hasNext()) {
				Node rNode = rnodes.nextNode();
				if (rNode.isNodeType("sramp:relationship")) {
					String rtype = getProperty(rNode, "sramp:relationshipType");
					boolean generic = false;
					if (rNode.hasProperty("sramp:generic")) {
						generic = rNode.getProperty("sramp:generic").getBoolean();
					}
					if (!generic)
						continue;
					Relationship relationship = new Relationship();
					relationship.setRelationshipType(rtype);
					if (rNode.hasProperty("sramp:relationshipTarget")) {
						Property property = rNode.getProperty("sramp:relationshipTarget");
						Value[] values = property.getValues();
						for (Value value : values) {
							if (value.getType() == PropertyType.REFERENCE) {
								String targetUUID = referenceResolver.resolveReference(value);
								Target target = new Target();
								target.setValue(targetUUID);
								relationship.getRelationshipTarget().add(target);
							}
						}
					}

					artifact.getRelationship().add(relationship);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
		try {
			// Pull out the 'relatedDocument' relationship, if one exists (it should!)
			if (this.jcrNode.hasNode("sramp-relationships:relatedDocument")) {
				Node relatedDocNode = this.jcrNode.getNode("sramp-relationships:relatedDocument");
				String targetType = getProperty(relatedDocNode, "sramp:targetType");
				Property property = relatedDocNode.getProperty("sramp:relationshipTarget");
				Value[] values = property.getValues();
				if (values.length > 1) {
					throw new Exception("Maximum cardinality check failed for 'relatedDocument'.  Expected a max cardinality of 1, found " + values.length);
				}
				Value value = values[0];
				String targetUUID = referenceResolver.resolveReference(value);

				DocumentArtifactTarget target = new DocumentArtifactTarget();
				target.setValue(targetUUID);
				if (targetType != null) {
					target.setArtifactType(DocumentArtifactEnum.valueOf(targetType));
				}
				artifact.setRelatedDocument(target);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
	 */
	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
		artifact.setNamespace(getProperty(jcrNode, "sramp:namespace"));
		try {
			artifact.getExtension().addAll(getRelationships("extension", WsdlExtensionTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitNamedWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType)
	 */
	@Override
	protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
		artifact.setNCName(getProperty(jcrNode, "sramp:ncName"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitXsdDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdType)
	 */
	@Override
	protected void visitXsdDerived(XsdType artifact) {
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
	 */
	@Override
	protected void visitDocument(DocumentArtifactType artifact) {
		artifact.setContentSize(getPropertyLong(jcrNode,"sramp:contentSize"));
		artifact.setContentType(getProperty(jcrNode, "sramp:contentType"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
		artifact.setContentEncoding(getProperty(jcrNode, "sramp:contentEncoding"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitExtended(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
	 */
	@Override
	protected void visitExtended(ExtendedArtifactType artifact) {
        String extendedType = getProperty(jcrNode, "sramp:extendedType");
        String extendedDerived = getProperty(jcrNode, "sramp:derived", "false");

        artifact.setExtendedType(extendedType);
        artifact.getOtherAttributes().put(SrampConstants.SRAMP_DERIVED_QNAME, extendedDerived);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitExtendedDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
	@Override
	protected void visitExtendedDocument(ExtendedDocument artifact) {
        String extendedType = getProperty(jcrNode, "sramp:extendedType");
        String contentType = getProperty(jcrNode,"jcr:content/jcr:mimeType");
        String contentLength = String.valueOf(getPropertyLength(jcrNode,"jcr:content/jcr:data"));

        artifact.setExtendedType(extendedType);
        if (contentType != null && contentLength != null) {
            artifact.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_SIZE_QNAME, contentLength);
            artifact.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_TYPE_QNAME, contentType);
        }
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
	    super.visit(artifact);

        artifact.setTargetNamespace(getProperty(jcrNode, "sramp:targetNamespace"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, "sramp:namespace"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, "sramp:namespace"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, "sramp:namespace"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, "sramp:namespace"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		super.visit(artifact);

		artifact.setTargetNamespace(getProperty(jcrNode, "sramp:targetNamespace"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
	 */
	@Override
	public void visit(Message artifact) {
		super.visit(artifact);
		try {
			artifact.getPart().addAll(getRelationships("part", PartTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
	 */
	@Override
	public void visit(Part artifact) {
		super.visit(artifact);
		try {
			artifact.setElement(getRelationship("element", ElementTarget.class));
			artifact.setType(getRelationship("type", XsdTypeTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		super.visit(artifact);
		try {
			artifact.getOperation().addAll(getRelationships("operation", OperationTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		super.visit(artifact);
		try {
			artifact.setInput(getRelationship("input", OperationInputTarget.class));
			artifact.setOutput(getRelationship("output", OperationOutputTarget.class));
			artifact.getFault().addAll(getRelationships("fault", FaultTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		super.visit(artifact);
		try {
			artifact.setMessage(getRelationship("message", MessageTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		super.visit(artifact);
		try {
			artifact.setMessage(getRelationship("message", MessageTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		super.visit(artifact);
		try {
			artifact.setMessage(getRelationship("message", MessageTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		super.visit(artifact);
		try {
			artifact.getBindingOperation().addAll(getRelationships("bindingOperation", BindingOperationTarget.class));
			artifact.setPortType(getRelationship("portType", PortTypeTarget.class));
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
		artifact.setStyle(getProperty(jcrNode, "sramp:style"));
		artifact.setTransport(getProperty(jcrNode, "sramp:transport"));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		super.visit(artifact);
		try {
			artifact.setInput(getRelationship("input", BindingOperationInputTarget.class));
			artifact.setOutput(getRelationship("output", BindingOperationOutputTarget.class));
			artifact.getFault().addAll(getRelationships("fault", BindingOperationFaultTarget.class));
			artifact.setOperation(getRelationship("operation", OperationTarget.class));
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
			artifact.getPort().addAll(getRelationships("port", PortTarget.class));
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
			artifact.setBinding(getRelationship("binding", BindingTarget.class));
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
		artifact.setSoapLocation(getProperty(jcrNode, "sramp:soapLocation"));
	}

	/**
	 * Gets the singular relationship of the given type.  This is called for relationships
	 * that have a max cardinality of 1.
	 * @param relationshipType
	 * @param targetClass
	 * @return list of relationship targets
	 */
	private <T> T getRelationship(String relationshipType, Class<T> targetClass) throws Exception {
		String relNodeName = "sramp-relationships:" + relationshipType;
		if (this.jcrNode.hasNode(relNodeName)) {
			Node relationshipNode = this.jcrNode.getNode(relNodeName);
			String targetType = relationshipNode.getProperty("sramp:targetType").getString();
			if (relationshipNode.hasProperty("sramp:relationshipTarget")) {
				Property targetProperty = relationshipNode.getProperty("sramp:relationshipTarget");
				Value[] values = targetProperty.getValues();
				Value value = values[0];
				T t = targetClass.newInstance();
				Target target = (Target) t;
				target.setValue(referenceResolver.resolveReference(value));
				// Use reflection to set the 'artifact type' attribute found on
				// most (all?) targets.  Unfortunately, the method and field are
				// redefined in each subclass of Target.
				try {
					Method m = targetClass.getMethod("getArtifactType");
					Class<?> mc = m.getReturnType();
					m = mc.getMethod("valueOf", String.class);
					Object o = m.invoke(null, targetType);
					m = targetClass.getMethod("setArtifactType", o.getClass());
					m.invoke(target, o);
				} catch (Exception e) {
					// eat it
				}
				return t;
			}
		}
		return null;
	}

	/**
	 * Gets the relationships of the given type.
	 * @param relationshipType
	 * @param targetClass
	 * @return list of relationship targets
	 */
	private <T> List<T> getRelationships(String relationshipType, Class<T> targetClass) throws Exception {
		List<T> rval = new ArrayList<T>();
		String relNodeName = "sramp-relationships:" + relationshipType;
		if (this.jcrNode.hasNode(relNodeName)) {
			Node relationshipNode = this.jcrNode.getNode(relNodeName);
			String targetType = relationshipNode.getProperty("sramp:targetType").getString();
			if (relationshipNode.hasProperty("sramp:relationshipTarget")) {
				Property targetsProperty = relationshipNode.getProperty("sramp:relationshipTarget");
				Value[] values = targetsProperty.getValues();
				for (Value value : values) {
					T t = targetClass.newInstance();
					Target target = (Target) t;
					target.setValue(referenceResolver.resolveReference(value));
					// Use reflection to set the 'artifact type' attribute found on
					// most (all?) targets.  Unfortunately, the method and field are
					// redefined in each subclass of Target.
					try {
						Method m = targetClass.getMethod("getArtifactType");
						Class<?> mc = m.getReturnType();
						m = mc.getMethod("valueOf", String.class);
						Object o = m.invoke(null, targetType);
						m = targetClass.getMethod("setArtifactType", o.getClass());
						m.invoke(target, o);
					} catch (Exception e) {
						// eat it
					}
					rval.add(t);
				}
			}
		}
		return rval;
	}

	/**
	 * Gets a single property from the given JCR node.  This returns null
	 * if the property does not exist.
	 * @param node the JCR node
	 * @param propertyName the name of the property
	 * @return the String value of the property
	 */
	protected static final String getProperty(Node node, String propertyName) {
		return getProperty(node, propertyName, null);
	}

	/**
	 * Gets a single property from the given JCR node.  This returns a default value if
	 * the property does not exist.
	 * @param node the JCR node
	 * @param propertyName the name of the property
	 * @param defaultValue a default value if the property does not exist on the node
	 * @return the String value of the property
	 */
	protected static final String getProperty(Node node, String propertyName, String defaultValue) {
		try {
			return node.getProperty(propertyName).getString();
		} catch (ValueFormatException e) {
		} catch (PathNotFoundException e) {
		} catch (javax.jcr.RepositoryException e) {
		}
		return defaultValue;
	}

	/**
	 * Gets a single property from the given JCR node.  This returns null
	 * if the property does not exist.
	 * @param node the JCR node
	 * @param propertyName the name of the property
	 * @return the String value of the property
	 */
	protected static final long getPropertyLong(Node node, String propertyName) {
		return getPropertyLong(node, propertyName, -1);
	}

	/**
	 * Gets a single property from the given JCR node.  This returns a default value if
	 * the property does not exist.
	 * @param node the JCR node
	 * @param propertyName the name of the property
	 * @param defaultValue a default value if the property does not exist on the node
	 * @return the String value of the property
	 */
	protected static final long getPropertyLong(Node node, String propertyName, long defaultValue) {
		try {
			return node.getProperty(propertyName).getLong();
		} catch (ValueFormatException e) {
		} catch (PathNotFoundException e) {
		} catch (javax.jcr.RepositoryException e) {
		}
		return defaultValue;
	}

	/**
	 * Gets a single property from the given JCR node.  This returns null
	 * if the property does not exist.
	 * @param node the JCR node
	 * @param propertyName the name of the property
	 * @return the String value of the property
	 */
	protected static final Long getPropertyLength(Node node, String propertyName) {
		return getPropertyLength(node, propertyName, null);
	}

	/**
	 * Gets a single property from the given JCR node.  This returns a default value if
	 * the property does not exist.
	 * @param node the JCR node
	 * @param propertyName the name of the property
	 * @param defaultValue a default value if the property does not exist on the node
	 * @return the String value of the property
	 */
	protected static final Long getPropertyLength(Node node, String propertyName, Long defaultValue) {
		try {
			return node.getProperty(propertyName).getLength();
		} catch (ValueFormatException e) {
		} catch (PathNotFoundException e) {
		} catch (javax.jcr.RepositoryException e) {
		}
		return defaultValue;
	}

	/**
	 * A simple interface used by this class to resolve JCR references into s-ramp artifact UUIDs.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	public static interface JCRReferenceResolver {

		/**
		 * Resolves a JCR reference into an s-ramp artifact UUID.
		 * @param reference a JCR reference
		 * @return the UUID of an s-ramp artifact (or null if it fails to resolve)
		 */
		public String resolveReference(Value reference);

	}

}
