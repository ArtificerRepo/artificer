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

import org.apache.commons.lang.StringUtils;
import org.artificer.repository.jcr.JCRConstants;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerException;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.repository.jcr.ClassificationHelper;

import javax.jcr.*;
import javax.jcr.Property;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

/**
 * An artifact visitor used to update a JCR node.  This class is responsible
 * for modifying a JCR node using information found in the supplied s-ramp
 * artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactToJCRNodeVisitor extends HierarchicalArtifactVisitor {

    private ArtifactType artifactType;
	private Node jcrNode;
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
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
        super.visitBase(artifact);
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
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
        super.visitDerived(artifact);
		try {
		    if (artifact.getRelatedDocument() != null) {
    			setRelationship("relatedDocument", 1, 1, false, false, artifact.getRelatedDocument(),
    			        artifact.getRelatedDocument().getArtifactType().toString());
		    }
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
	 */
	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        super.visitWsdlDerived(artifact);
		try {
			this.jcrNode.setProperty(JCRConstants.SRAMP_NAMESPACE, artifact.getNamespace());

			setRelationships("extension", -1, 1, false, false, artifact.getExtension(),
			        WsdlExtensionEnum.WSDL_EXTENSION.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitNamedWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType)
	 */
	@Override
	protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
        super.visitNamedWsdlDerived(artifact);
		try {
			this.jcrNode.setProperty(JCRConstants.SRAMP_NC_NAME, artifact.getNCName());
		} catch (Exception e) {
			error = e;
		}
	}
    
    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        super.visitServiceImplementation(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (DocumentArtifactTarget documentation : artifact.getDocumentation()) {
                targetTypes.add(documentation.getArtifactType().toString());
            }
            setRelationships("documentation", -1, -1, false, false, artifact.getDocumentation(), targetTypes);
        } catch (Exception e) {
            error = e;
        }
    }
    
    @Override
    protected void visitSoa(SoaModelType artifact) {
        super.visitSoa(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (DocumentArtifactTarget documentation : artifact.getDocumentation()) {
                targetTypes.add(documentation.getArtifactType().toString());
            }
            setRelationships("documentation", -1, -1, false, false, artifact.getDocumentation(), targetTypes);
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitElement(Element artifact) {
        super.visitElement(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ElementTarget target : artifact.getRepresents()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("represents", -1, -1, false, false, artifact.getRepresents(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (ElementTarget target : artifact.getUses()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("uses", -1, -1, false, false, artifact.getUses(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (ServiceTarget target : artifact.getPerforms()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("performs", -1, -1, false, false, artifact.getPerforms(), targetTypes);
            
            if (artifact.getDirectsOrchestration() != null) {
                setRelationship("directsOrchestration", -1, 1, false, false, artifact.getDirectsOrchestration(),
                        artifact.getDirectsOrchestration().getArtifactType().toString());
            }
            
            if (artifact.getDirectsOrchestrationProcess() != null) {
                setRelationship("directsOrchestrationProcess", -1, 1, false, false, artifact.getDirectsOrchestrationProcess(),
                        artifact.getDirectsOrchestrationProcess().getArtifactType().toString());
            }
            
            targetTypes = new ArrayList<String>();
            for (EventTarget target : artifact.getGenerates()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("generates", -1, -1, false, false, artifact.getGenerates(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (EventTarget target : artifact.getRespondsTo()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("respondsTo", -1, -1, false, false, artifact.getRespondsTo(), targetTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    protected void visitActor(Actor artifact) {
        super.visitActor(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (TaskTarget target : artifact.getDoes()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("does", -1, -1, false, false, artifact.getDoes(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (PolicyTarget target : artifact.getSetsPolicy()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("setsPolicy", -1, -1, false, false, artifact.getSetsPolicy(), targetTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * Updates the basic s-ramp meta data.
	 * @param artifact
	 * @throws Exception
	 */
	private void updateArtifactMetaData(BaseArtifactType artifact) throws Exception {
		if (artifact.getName() != null)
			setProperty(JCRConstants.SRAMP_NAME, artifact.getName());
		else
		    setProperty(JCRConstants.SRAMP_NAME, artifact.getClass().getSimpleName());
		if (artifact.getDescription() != null)
			setProperty(JCRConstants.SRAMP_DESCRIPTION, artifact.getDescription());
		if (artifact.getVersion() != null)
			setProperty("version", artifact.getVersion());
		setProperty(JCRConstants.SRAMP_DERIVED, this.artifactType.isDerived());
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
		setProperty(JCRConstants.SRAMP_CLASSIFIED_BY, values);

		// Store the normalized classifications
		values = new String[normalizedClassifications.size()];
		idx = 0;
		for (URI classification : normalizedClassifications) {
			values[idx++] = classification.toString();
		}
		setProperty(JCRConstants.SRAMP_NORMALIZED_CLASSIFIED_BY, values);
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
			if (StringUtils.isEmpty(val)) {
				// Need to support no-value properties, but JCR will remove it if it's null.  Further, if it's an
				// empty string, the property existence query fails.  Therefore, use a placeholder that will eventually
				// be removed by JCRNodeToArtifactVisitor.
				val = JCRConstants.NO_VALUE;
			}
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
			setRelationships(relationship.getRelationshipType(), -1, 0, true, false,
					relationship.getRelationshipTarget(), Collections.EMPTY_LIST, relationship.getOtherAttributes());
			updatedRelationshipTypes.add(relationship.getRelationshipType());
		}

		// Now remove any relationships that weren't just updated or created (the ones
		// not included on the artifact but that have existing JCR nodes).
		NodeIterator existingNodes = this.jcrNode.getNodes();
		while (existingNodes.hasNext()) {
			Node node = existingNodes.nextNode();
			// Only remove generic relationships
			if (node.isNodeType(JCRConstants.SRAMP_RELATIONSHIP) && node.hasProperty(JCRConstants.SRAMP_GENERIC)
					&& node.getProperty(JCRConstants.SRAMP_GENERIC).getBoolean()) {
				String type = node.getProperty(JCRConstants.SRAMP_RELATIONSHIP_TYPE).getString();
				// If this relationship type was *not* updated above, then remove it because
				// it's not included in the latest artifact meta-data
				if (!updatedRelationshipTypes.contains(type)) {
					node.remove();
				}
			}
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
	 */
	@Override
	public void visit(ExtendedArtifactType artifact) {
	    super.visit(artifact);
        try {
            if (artifact.getExtendedType() != null)
                setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, artifact.getExtendedType());
        } catch (Exception e) {
            error = e;
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
	@Override
	public void visit(ExtendedDocument artifact) {
	    super.visit(artifact);
        try {
            if (artifact.getExtendedType() != null)
                setProperty(JCRConstants.SRAMP_EXTENDED_TYPE, artifact.getExtendedType());
        } catch (Exception e) {
            error = e;
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
	    super.visit(artifact);
        try {
            setProperty(JCRConstants.SRAMP_TARGET_NAMESPACE, artifact.getTargetNamespace());
            setRelationships("importedXsds", -1, 1, false, false, artifact.getImportedXsds(),
                    XsdDocumentEnum.XSD_DOCUMENT.toString());
            setRelationships("includedXsds", -1, 1, false, false, artifact.getIncludedXsds(),
                    XsdDocumentEnum.XSD_DOCUMENT.toString());
            setRelationships("redefinedXsds", -1, 1, false, false, artifact.getRedefinedXsds(),
                    XsdDocumentEnum.XSD_DOCUMENT.toString());
        } catch (Exception e) {
            error = e;
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty(JCRConstants.SRAMP_NAMESPACE, artifact.getNamespace());
            setProperty(JCRConstants.SRAMP_NC_NAME, artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty(JCRConstants.SRAMP_NAMESPACE, artifact.getNamespace());
            setProperty(JCRConstants.SRAMP_NC_NAME, artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty(JCRConstants.SRAMP_NAMESPACE, artifact.getNamespace());
            setProperty(JCRConstants.SRAMP_NC_NAME, artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
        super.visit(artifact);
        try {
            setProperty(JCRConstants.SRAMP_NAMESPACE, artifact.getNamespace());
            setProperty(JCRConstants.SRAMP_NC_NAME, artifact.getNCName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		super.visit(artifact);
		try {
			setProperty(JCRConstants.SRAMP_TARGET_NAMESPACE, artifact.getTargetNamespace());
			setRelationships("importedXsds", -1, 1, false, false, artifact.getImportedXsds(),
                    XsdDocumentEnum.XSD_DOCUMENT.toString());
            setRelationships("includedXsds", -1, 1, false, false, artifact.getIncludedXsds(),
                    XsdDocumentEnum.XSD_DOCUMENT.toString());
            setRelationships("redefinedXsds", -1, 1, false, false, artifact.getRedefinedXsds(),
                    XsdDocumentEnum.XSD_DOCUMENT.toString());
			setRelationships("importedWsdls", -1, 1, false, false, artifact.getImportedWsdls(),
					WsdlDocumentEnum.WSDL_DOCUMENT.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * Message has references to all its {@link Part}s.
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
	 */
	@Override
	public void visit(Message artifact) {
		super.visit(artifact);
		try {
			setRelationships("part", -1, 1, false, true, artifact.getPart(), PartEnum.PART.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
	 */
	@Override
	public void visit(Part artifact) {
		super.visit(artifact);
		try {
			if (artifact.getElement() != null) {
				if (this.jcrNode.hasNode(JCRConstants.SRAMP_RELATIONSHIPS + ":type")) {
					this.jcrNode.getNode(JCRConstants.SRAMP_RELATIONSHIPS + ":type").remove();
				}
				setRelationship("element", 1, 1, false, true, artifact.getElement(), ElementDeclarationEnum.ELEMENT_DECLARATION.toString());
			} else if (artifact.getType() != null) {
				if (this.jcrNode.hasNode(JCRConstants.SRAMP_RELATIONSHIPS + ":element")) {
					this.jcrNode.getNode(JCRConstants.SRAMP_RELATIONSHIPS + ":element").remove();
				}
				setRelationship("type", 1, 1, false, true, artifact.getType(), XsdTypeEnum.XSD_TYPE.toString());
			}
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		super.visit(artifact);
		try {
			setRelationships("operation", -1, 1, false, true, artifact.getOperation(), OperationEnum.OPERATION.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		super.visit(artifact);
		try {
			setRelationship("input", 1, 1, false, true, artifact.getInput(), OperationInputEnum.OPERATION_INPUT.toString());
			setRelationship("output", 1, 1, false, true, artifact.getOutput(),
                    OperationOutputEnum.OPERATION_OUTPUT.toString());
			setRelationships("fault", -1, 1, false, true, artifact.getFault(), FaultEnum.FAULT.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		super.visit(artifact);
		try {
			setRelationship("message", 1, 1, false, true, artifact.getMessage(), MessageEnum.MESSAGE.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		super.visit(artifact);
		try {
			setRelationship("message", 1, 1, false, true, artifact.getMessage(), MessageEnum.MESSAGE.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		super.visit(artifact);
		try {
			setRelationship("message", 1, 1, false, true, artifact.getMessage(), MessageEnum.MESSAGE.toString());
		} catch (Exception e) {
			error = e;
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		super.visit(artifact);
		try {
			setRelationships("bindingOperation", -1, 1, false, true, artifact.getBindingOperation(),
                    BindingOperationEnum.BINDING_OPERATION.toString());
			setRelationship("portType", 1, 1, false, true, artifact.getPortType(), PortTypeEnum.PORT_TYPE.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding)
	 */
	@Override
	public void visit(SoapBinding artifact) {
		super.visit(artifact);
		try {
			setProperty(JCRConstants.SRAMP_STYLE, artifact.getStyle());
			setProperty(JCRConstants.SRAMP_TRANSPORT, artifact.getTransport());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		super.visit(artifact);
		try {
			setRelationship("input", 1, 1, false, true, artifact.getInput(),
			        BindingOperationInputEnum.BINDING_OPERATION_INPUT.toString());
			setRelationship("output", 1, 1, false, true, artifact.getOutput(),
                    BindingOperationOutputEnum.BINDING_OPERATION_OUTPUT.toString());
			setRelationships("fault", -1, 1, false, true, artifact.getFault(),
			        BindingOperationFaultEnum.BINDING_OPERATION_FAULT.toString());
			setRelationship("operation", 1, 1, false, true, artifact.getOperation(), OperationEnum.OPERATION.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {
		super.visit(artifact);
		try {
			setRelationships("port", -1, 1, false, true, artifact.getPort(), PortEnum.PORT.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
	 */
	@Override
	public void visit(Port artifact) {
		super.visit(artifact);
		try {
			setRelationship("binding", 1, 1, false, true, artifact.getBinding(), BindingEnum.BINDING.toString());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress)
	 */
	@Override
	public void visit(SoapAddress artifact) {
		super.visit(artifact);
		try {
			setProperty(JCRConstants.SRAMP_SOAP_LOCATION, artifact.getSoapLocation());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
    
    @Override
    public void visit(ServiceEndpoint artifact) {
        super.visit(artifact);
        try {
            if (artifact.getEndpointDefinedBy() != null) {
                setRelationship("endpointDefinedBy", -1, 1, false, false, artifact.getEndpointDefinedBy(),
                        artifact.getEndpointDefinedBy().getArtifactType().toString());
            }
            this.jcrNode.setProperty(JCRConstants.SRAMP_URL, artifact.getUrl());
            // TODO: These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            this.jcrNode.setProperty(JCRConstants.SRAMP_END, artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceInstance artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (BaseArtifactTarget target : artifact.getUses()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("uses", -1, -1, false, false, artifact.getUses(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (BaseArtifactTarget target : artifact.getDescribedBy()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("describedBy", -1, -1, false, false, artifact.getDescribedBy(), targetTypes);
            
            // TODO: These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            this.jcrNode.setProperty(JCRConstants.SRAMP_END, artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceOperation artifact) {
        super.visit(artifact);
        try {
            if (artifact.getOperationDefinedBy() != null) {
                setRelationship("operationDefinedBy", -1, 1, false, false, artifact.getOperationDefinedBy(),
                        artifact.getOperationDefinedBy().getArtifactType().toString());
            }
            // TODO: These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            this.jcrNode.setProperty(JCRConstants.SRAMP_END, artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(Policy artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (PolicySubjectTarget target : artifact.getAppliesTo()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("appliesTo", -1, -1, false, false, artifact.getAppliesTo(), targetTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceInterface artifact) {
        super.visit(artifact);
        try {
            if (artifact.getInterfaceDefinedBy() != null) {
                setRelationship("interfaceDefinedBy", -1, 1, false, false, artifact.getInterfaceDefinedBy(),
                        artifact.getInterfaceDefinedBy().getArtifactType().toString());
            }
            
            if (artifact.getHasOperation() != null) {
                setRelationship("hasOperation", -1, 1, false, false, artifact.getHasOperation(),
                        artifact.getHasOperation().getArtifactType().toString());
            }
            
            List<String> targetTypes = new ArrayList<String>();
            for (InformationTypeTarget target : artifact.getHasOutput()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasOutput", -1, -1, false, false, artifact.getHasOutput(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (InformationTypeTarget target : artifact.getHasInput()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasInput", -1, -1, false, false, artifact.getHasInput(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (ServiceTarget target : artifact.getIsInterfaceOf()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("isInterfaceOf", -1, -1, false, false, artifact.getIsInterfaceOf(), targetTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(ServiceContract artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ActorTarget target : artifact.getInvolvesParty()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("involvesParty", -1, -1, false, false, artifact.getInvolvesParty(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (EffectTarget target : artifact.getSpecifies()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("specifies", -1, -1, false, false, artifact.getSpecifies(), targetTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(Organization artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ServiceImplementationModelTarget target : artifact.getProvides()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("provides", -1, -1, false, false, artifact.getProvides(), targetTypes);

            this.jcrNode.setProperty(JCRConstants.SRAMP_END, artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public void visit(Service artifact) {
        super.visit(artifact);
        try {
            List<String> targetTypes = new ArrayList<String>();
            for (ServiceContractTarget target : artifact.getHasContract()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasContract", -1, -1, false, false, artifact.getHasContract(), targetTypes);
            
            targetTypes = new ArrayList<String>();
            for (ServiceInterfaceTarget target : artifact.getHasInterface()) {
                targetTypes.add(target.getArtifactType().toString());
            }
            setRelationships("hasInterface", -1, -1, false, false, artifact.getHasInterface(), targetTypes);
            
            if (artifact.getHasInstance() != null) {
                setRelationship("hasInstance", -1, 1, false, false, artifact.getHasInstance(),
                        artifact.getHasInstance().getArtifactType().toString());
            }
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
	 * @param isGeneric
	 * @param isDerived
	 * @param target
     * @param targetType
	 * @throws Exception
	 */
	private void setRelationship(String relationshipType, int maxCardinality, int minCardinality,
			boolean isGeneric, boolean isDerived, Target target, String targetType) throws Exception {
	    if (!isProcessRelationships())
	        return;
		if (target != null || minCardinality == 0) {
			Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, relationshipType,
			        maxCardinality, isGeneric, isDerived);
            if (StringUtils.isNotBlank(target.getValue())) {
                Value targetValue = this.referenceFactory.createReference(target.getValue());
                Node targetNode = getOrCreateTargetNode(relationshipNode, targetType, target.getValue(), targetValue);
                setOtherAttributes(targetNode, target.getOtherAttributes());
            }
		} else {
			// If the minimum cardinality is > 0 but no targets have been provided, then
			// remove the relationship node.
			removeRelationship(relationshipType);
		}
	}

    private void setRelationships(String relationshipType, int maxCardinality, int minCardinality,
            boolean isGeneric, boolean isDerived, List<? extends Target> targets, List<String> targetTypes,
            Map<QName, String> relationshipOtherAttributes) throws Exception {
        if (!isProcessRelationships())
            return;
        if ((targets != null && targets.size() > 0) || minCardinality == 0) {
            Node relationshipNode = getOrCreateRelationshipNode(this.jcrNode, relationshipType,
                    maxCardinality, isGeneric, isDerived);
            for (int i = 0; i < targets.size(); i++) {
                Target target = targets.get(i);
                if (StringUtils.isNotBlank(target.getValue())) {
                    Value targetValue = this.referenceFactory.createReference(target.getValue());
                    String targetType = targetTypes.size() > i ? targetTypes.get(i) : null;
                    Node targetNode = getOrCreateTargetNode(relationshipNode, targetType, target.getValue(), targetValue);
                    setOtherAttributes(targetNode, target.getOtherAttributes());
                }
            }

            setOtherAttributes(relationshipNode, relationshipOtherAttributes);
        } else {
            // If the minimum cardinality is > 0 but no targets have been provided, then
            // remove the relationship node.
            removeRelationship(relationshipType);
        }
    }

    private void setRelationships(String relationshipType, int maxCardinality, int minCardinality,
            boolean isGeneric, boolean isDerived, List<? extends Target> targets, List<String> targetTypes) throws Exception {
        setRelationships(relationshipType, maxCardinality, minCardinality, isGeneric, isDerived, targets,
                targetTypes, Collections.EMPTY_MAP);
    }

	private void setRelationships(String relationshipType, int maxCardinality, int minCardinality,
            boolean isGeneric, boolean isDerived, List<? extends Target> targets, String targetType) throws Exception {
        List<String> targetTypes = new ArrayList<String>();
        for (int i = 0; i < targets.size(); i++) {
            targetTypes.add(targetType);
        }
        setRelationships(relationshipType, maxCardinality, minCardinality, isGeneric, isDerived, targets, targetTypes);
    }

    private void setOtherAttributes(Node node, Map<QName, String> otherAttributes) throws Exception {
        // store any 'other' attributes
        String attributeKeyPrefix = JCRConstants.SRAMP_OTHER_ATTRIBUTES + ":";
        for (QName qname : otherAttributes.keySet()) {
            String attributeKey = attributeKeyPrefix + qname.toString();
            String attributeValue = otherAttributes.get(qname);
            if (StringUtils.isEmpty(attributeValue)) {
                // Need to support no-value properties, but JCR will remove it if it's null.  Further, if it's an
                // empty string, the property existence query fails.  Therefore, use a placeholder that will eventually
                // be removed by JCRNodeToArtifactVisitor.
                attributeValue = JCRConstants.NO_VALUE;
            }
            node.setProperty(attributeKey, attributeValue);
        }
    }

	/**
	 * Removes the relationship of the given type from the JCR node.
	 * @param relationshipType
	 * @throws Exception
	 */
	private void removeRelationship(String relationshipType) throws RepositoryException, VersionException,
	LockException, ConstraintViolationException, AccessDeniedException, PathNotFoundException {
		String nodeName = JCRConstants.SRAMP_RELATIONSHIPS + ":" + relationshipType;
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
	 * @param isGeneric
	 * @param isDerived
	 * @throws Exception
	 */
	private static Node getOrCreateRelationshipNode(Node parentNode, String relationshipType,
			int maxCardinality, boolean isGeneric, boolean isDerived) throws Exception {
		Node relationshipNode = null;
		String nodeName = JCRConstants.SRAMP_RELATIONSHIPS + ":" + relationshipType;
		if (parentNode.hasNode(nodeName)) {
			relationshipNode = parentNode.getNode(nodeName);
		} else {
			relationshipNode = parentNode.addNode(nodeName, JCRConstants.SRAMP_RELATIONSHIP);
			relationshipNode.setProperty(JCRConstants.SRAMP_RELATIONSHIP_TYPE, relationshipType);
			if (maxCardinality != -1)
				relationshipNode.setProperty(JCRConstants.SRAMP_MAX_CARDINALITY, maxCardinality);
			relationshipNode.setProperty(JCRConstants.SRAMP_GENERIC, isGeneric);
			relationshipNode.setProperty(JCRConstants.SRAMP_DERIVED, isDerived);
		}
		return relationshipNode;
	}

    private static Node getOrCreateTargetNode(Node parentNode, String targetType, String targetUuid, Value targetValue) throws Exception {
        Node targetNode = null;
        String nodeName = "sramp-targets:" + targetUuid;
        if (parentNode.hasNode(nodeName)) {
            targetNode = parentNode.getNode(nodeName);
        } else {
            targetNode = parentNode.addNode(nodeName, JCRConstants.SRAMP_TARGET);
            targetNode.setProperty(JCRConstants.SRAMP_TARGET_TYPE, targetType);
            targetNode.setProperty(JCRConstants.SRAMP_TARGET_ARTIFACT, targetValue);
        }
        return targetNode;
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
		 * @throws org.artificer.common.ArtificerException
		 */
		public Value createReference(String uuid) throws ArtificerException;

        public void trackNode(String uuid, Node node);
	}

}
