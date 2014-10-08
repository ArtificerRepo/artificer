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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ActorTarget;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EffectTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EventTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.FaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationTypeTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.MessageTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubjectTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTypeTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContractTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstanceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterfaceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.TaskTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtensionTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
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
        super.visitBase(artifact);
		try {
			DatatypeFactory dtFactory = DatatypeFactory.newInstance();
			ArtifactType artifactType = ArtifactType.valueOf(artifact);
			BaseArtifactEnum apiType = artifactType.getArtifactType().getApiType();
			artifact.setArtifactType(apiType);

			// First map in the standard s-ramp meta-data
			artifact.setCreatedBy(getProperty(jcrNode, JCRConstants.JCR_CREATED_BY));
			XMLGregorianCalendar createdTS = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, JCRConstants.JCR_CREATED));
			artifact.setCreatedTimestamp(createdTS);
			artifact.setDescription(getProperty(jcrNode, JCRConstants.SRAMP_DESCRIPTION));
			artifact.setLastModifiedBy(getProperty(jcrNode, JCRConstants.JCR_LAST_MODIFIED_BY));
			XMLGregorianCalendar modifiedTS = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, JCRConstants.JCR_LAST_MODIFIED));
			artifact.setLastModifiedTimestamp(modifiedTS);
			artifact.setName(getProperty(jcrNode, JCRConstants.SRAMP_NAME));
			artifact.setUuid(getProperty(jcrNode, JCRConstants.SRAMP_UUID));
			artifact.setVersion(getProperty(jcrNode, "version"));

			// Map in the classifications
			if (jcrNode.hasProperty(JCRConstants.SRAMP_CLASSIFIED_BY)) {
				Property classifiedByProp = jcrNode.getProperty(JCRConstants.SRAMP_CLASSIFIED_BY);
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
				if (rNode.isNodeType(JCRConstants.SRAMP_RELATIONSHIP)) {
					String rtype = getProperty(rNode, JCRConstants.SRAMP_RELATIONSHIP_TYPE);
					boolean generic = false;
					if (rNode.hasProperty(JCRConstants.SRAMP_GENERIC)) {
						generic = rNode.getProperty(JCRConstants.SRAMP_GENERIC).getBoolean();
					}
					if (!generic)
						continue;
					Relationship relationship = new Relationship();
					relationship.setRelationshipType(rtype);
					if (rNode.hasProperty(JCRConstants.SRAMP_RELATIONSHIP_TARGET)) {
						Property property = rNode.getProperty(JCRConstants.SRAMP_RELATIONSHIP_TARGET);
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
        super.visitDerived(artifact);
		try {
		    artifact.setRelatedDocument(getRelationship("relatedDocument", DocumentArtifactTarget.class));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
	 */
	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        super.visitWsdlDerived(artifact);
		artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
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
        super.visitNamedWsdlDerived(artifact);
		artifact.setNCName(getProperty(jcrNode, JCRConstants.SRAMP_NC_NAME));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
	 */
	@Override
	protected void visitDocument(DocumentArtifactType artifact) {
        super.visitDocument(artifact);
		artifact.setContentSize(getPropertyLong(jcrNode, JCRConstants.SRAMP_CONTENT_SIZE));
		artifact.setContentType(getProperty(jcrNode, JCRConstants.SRAMP_CONTENT_TYPE));
		artifact.setContentHash(getProperty(jcrNode, JCRConstants.SRAMP_CONTENT_HASH));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	protected void visitXmlDocument(XmlDocument artifact) {
        super.visitXmlDocument(artifact);
		artifact.setContentEncoding(getProperty(jcrNode, JCRConstants.SRAMP_CONTENT_ENCODING));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitExtended(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
	 */
	@Override
	protected void visitExtended(ExtendedArtifactType artifact) {
        super.visitExtended(artifact);
        String extendedType = getProperty(jcrNode, JCRConstants.SRAMP_EXTENDED_TYPE);
        String extendedDerived = getProperty(jcrNode, JCRConstants.SRAMP_DERIVED, "false");

        artifact.setExtendedType(extendedType);
        artifact.getOtherAttributes().put(SrampConstants.SRAMP_DERIVED_QNAME, extendedDerived);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitExtendedDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
	@Override
	protected void visitExtendedDocument(ExtendedDocument artifact) {
        super.visitExtendedDocument(artifact);
        String extendedType = getProperty(jcrNode, JCRConstants.SRAMP_EXTENDED_TYPE);
        String contentType = getProperty(jcrNode, JCRConstants.JCR_CONTENT_MIME_TYPE);
        String contentLength = String.valueOf(getPropertyLength(jcrNode, JCRConstants.JCR_CONTENT_DATA));

        artifact.setExtendedType(extendedType);
        if (contentType != null && contentLength != null) {
            artifact.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_SIZE_QNAME, contentLength);
            artifact.getOtherAttributes().put(SrampConstants.SRAMP_CONTENT_TYPE_QNAME, contentType);
        }
	}
    
    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        super.visitServiceImplementation(artifact);
        try {
            artifact.getDocumentation().addAll(getRelationships("documentation", DocumentArtifactTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void visitSoa(SoaModelType artifact) {
        super.visitSoa(artifact);
        try {
            artifact.getDocumentation().addAll(getRelationships("documentation", DocumentArtifactTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void visitElement(Element artifact) {
        super.visitElement(artifact);
        try {
            artifact.getRepresents().addAll(getRelationships("represents", ElementTarget.class));
            artifact.getUses().addAll(getRelationships("uses", ElementTarget.class));
            artifact.getPerforms().addAll(getRelationships("performs", ServiceTarget.class));
            artifact.setDirectsOrchestration(getRelationship("directsOrchestration", OrchestrationTarget.class));
            artifact.setDirectsOrchestrationProcess(
                    getRelationship("directsOrchestrationProcess", OrchestrationTarget.class));
            artifact.getGenerates().addAll(getRelationships("generates", EventTarget.class));
            artifact.getRespondsTo().addAll(getRelationships("respondsTo", EventTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void visitActor(Actor artifact) {
        super.visitActor(artifact);
        try {
            artifact.getDoes().addAll(getRelationships("does", TaskTarget.class));
            artifact.getSetsPolicy().addAll(getRelationships("setsPolicy", PolicyTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
	    super.visit(artifact);

        artifact.setTargetNamespace(getProperty(jcrNode, JCRConstants.SRAMP_TARGET_NAMESPACE));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
	    super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
	}

	/**
	 * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		super.visit(artifact);

		artifact.setTargetNamespace(getProperty(jcrNode, JCRConstants.SRAMP_TARGET_NAMESPACE));
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
		artifact.setStyle(getProperty(jcrNode, JCRConstants.SRAMP_STYLE));
		artifact.setTransport(getProperty(jcrNode, JCRConstants.SRAMP_TRANSPORT));
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
		artifact.setSoapLocation(getProperty(jcrNode, JCRConstants.SRAMP_SOAP_LOCATION));
	}
    
    @Override
    public void visit(ServiceEndpoint artifact) {
        super.visit(artifact);
        try {
            artifact.setEndpointDefinedBy(getRelationship("endpointDefinedBy", DerivedArtifactTarget.class));
            artifact.setUrl(getProperty(jcrNode, JCRConstants.SRAMP_URL));
            // TODO: These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            artifact.setEnd(getProperty(jcrNode, JCRConstants.SRAMP_END));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceInstance artifact) {
        super.visit(artifact);
        try {
            artifact.getDescribedBy().addAll(getRelationships("uses", ServiceInstanceTarget.class));
            artifact.getUses().addAll(getRelationships("describedBy", ServiceInstanceTarget.class));
            // TODO: These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            artifact.setEnd(getProperty(jcrNode, JCRConstants.SRAMP_END));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceOperation artifact) {
        super.visit(artifact);
        try {
            artifact.setOperationDefinedBy(getRelationship("operationDefinedBy", DerivedArtifactTarget.class));
            // TODO: These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            artifact.setEnd(getProperty(jcrNode, JCRConstants.SRAMP_END));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(Policy artifact) {
        super.visit(artifact);
        try {
            artifact.getAppliesTo().addAll(getRelationships("appliesTo", PolicySubjectTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceInterface artifact) {
        super.visit(artifact);
        try {
            // TODO: #interfaceDefinedBy needs to be a Target!!!
//            artifact.setInterfaceDefinedBy(getRelationship("interfaceDefinedBy", DerivedArtifactTarget.class));
            artifact.setHasOperation(getRelationship("hasOperation", ServiceOperationTarget.class));
            artifact.getHasOutput().addAll(getRelationships("hasOutput", InformationTypeTarget.class));
            artifact.getHasInput().addAll(getRelationships("hasInput", InformationTypeTarget.class));
            artifact.getIsInterfaceOf().addAll(getRelationships("isInterfaceOf", ServiceTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceContract artifact) {
        super.visit(artifact);
        try {
            artifact.getInvolvesParty().addAll(getRelationships("involvesParty", ActorTarget.class));
            artifact.getSpecifies().addAll(getRelationships("specifies", EffectTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(Organization artifact) {
        super.visit(artifact);
        try {
            artifact.getProvides().addAll(getRelationships("provides", ServiceImplementationModelTarget.class));
            artifact.setEnd(getProperty(jcrNode, JCRConstants.SRAMP_END));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(Service artifact) {
        super.visit(artifact);
        try {
            artifact.getHasContract().addAll(getRelationships("hasContract", ServiceContractTarget.class));
            artifact.getHasInterface().addAll(getRelationships("hasInterface", ServiceInterfaceTarget.class));
            artifact.setHasInstance(getRelationship("hasInstance", ServiceInstanceTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * Gets the singular relationship of the given type.  This is called for relationships
	 * that have a max cardinality of 1.
	 * @param relationshipType
	 * @param targetClass
	 * @return list of relationship targets
	 */
	private <T> T getRelationship(String relationshipType, Class<T> targetClass) throws Exception {
	    List<T> relationships = getRelationships(relationshipType, targetClass);
	    return relationships.size() > 0 ? relationships.get(0) : null;
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
			if (relationshipNode.hasProperty(JCRConstants.SRAMP_RELATIONSHIP_TARGET)) {
			    Value[] relationshipTargets = relationshipNode.getProperty(JCRConstants.SRAMP_RELATIONSHIP_TARGET)
                        .getValues();
                Value[] targetTypes = relationshipNode.getProperty(JCRConstants.SRAMP_TARGET_TYPE).getValues();
                for (int i = 0; i < relationshipTargets.length; i++) {
                    Value relationshipTarget = relationshipTargets[i];
                    Value targetType = targetTypes[i];
                    
					T t = targetClass.newInstance();
					Target target = (Target) t;
					target.setValue(referenceResolver.resolveReference(relationshipTarget));
					// Use reflection to set the 'artifact type' attribute found on
					// most (all?) targets.  Unfortunately, the method and field are
					// redefined in each subclass of Target.
					// TODO: Get ^^^ changed in the spec!
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
