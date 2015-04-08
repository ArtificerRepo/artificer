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

import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.repository.jcr.JCRConstants;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ActorTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Comment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.EffectTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclarationTarget;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcessTarget;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocumentTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtensionTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocumentTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdTypeTarget;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A visitor for going from a JCR node to an S-RAMP artifact instance.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRNodeToArtifactVisitor extends HierarchicalArtifactVisitor {

    private final Node jcrNode;
    private final JCRReferenceResolver referenceResolver;
    private final DatatypeFactory dtFactory;

    /**
     * Constructor.
     * @param jcrNode
     * @param referenceResolver
     */
    public JCRNodeToArtifactVisitor(Node jcrNode, JCRReferenceResolver referenceResolver) {
        this.jcrNode = jcrNode;
        this.referenceResolver = referenceResolver;

        try {
            dtFactory = DatatypeFactory.newInstance();
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
     */
    @Override
    protected void visitBase(BaseArtifactType artifact) {
        super.visitBase(artifact);
        try {
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
                    // Need to support no-value properties, but JCR will remove it if it's null.  Further, if it's an
                    // empty string, the property existence query fails.  Therefore, ArtifactToJCRNodeVisitor uses
                    // this placeholder.
                    if (propValue.equals(JCRConstants.NO_VALUE)) {
                        propValue = null;
                    }

                    org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property srampProp = new org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property();
                    srampProp.setPropertyName(propName);
                    srampProp.setPropertyValue(propValue);
                    artifact.getProperty().add(srampProp);
                }
            }

            // Map in the generic relationships
            visitGenericRelationships(artifact);

            // Map all comments
            visitComments(artifact);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void visitGenericRelationships(BaseArtifactType artifact) throws Exception {
        NodeIterator relationshipNodes = jcrNode.getNodes();
        // TODO: After ARTIF-658, switch to using the following.  This doesn't work in 4.0 due to MODE-2338.
//        NodeIterator relationshipNodes = jcrNode.getNodes(JCRConstants.SRAMP_RELATIONSHIPS + "*");
        while (relationshipNodes.hasNext()) {
            Node relationshipNode = relationshipNodes.nextNode();
            // TODO: See above.  This check can also be removed.
            if (relationshipNode.isNodeType(JCRConstants.SRAMP_RELATIONSHIP)) {
                String rtype = getProperty(relationshipNode, JCRConstants.SRAMP_RELATIONSHIP_TYPE);
                boolean generic = false;
                if (relationshipNode.hasProperty(JCRConstants.SRAMP_GENERIC)) {
                    generic = relationshipNode.getProperty(JCRConstants.SRAMP_GENERIC).getBoolean();
                }
                if (!generic)
                    continue;

                Relationship relationship = new Relationship();
                relationship.setRelationshipType(rtype);
                NodeIterator targetNodes = relationshipNode.getNodes();
                while (targetNodes.hasNext()) {
                    Node targetNode = targetNodes.nextNode();
                    Value value = targetNode.getProperty(JCRConstants.SRAMP_TARGET_ARTIFACT).getValue();
                    if (value.getType() == PropertyType.WEAKREFERENCE) {
                        Target target = createTarget(Target.class, value);
                        relationship.getRelationshipTarget().add(target);
                    }
                }

                setOtherAttributes(relationshipNode, relationship.getOtherAttributes());

                artifact.getRelationship().add(relationship);
            }
        }
    }

    private void visitComments(BaseArtifactType artifact) throws Exception {
        NodeIterator commentNodes = jcrNode.getNodes();
        // TODO: After ARTIF-658, switch to using the following.  This doesn't work in 4.0 due to MODE-2338.
//        NodeIterator commentNodes = jcrNode.getNodes(JCRConstants.ARTIFICER_COMMENTS);
        while (commentNodes.hasNext()) {
            Node commentNode = commentNodes.nextNode();

            // TODO: See above.  This check can also be removed.
            if (commentNode.isNodeType(JCRConstants.ARTIFICER_COMMENT)) {
                String createdBy = getProperty(commentNode, JCRConstants.JCR_CREATED_BY);
                XMLGregorianCalendar createdTimestamp = dtFactory.newXMLGregorianCalendar(getProperty(jcrNode, JCRConstants.JCR_CREATED));
                String text = getProperty(commentNode, JCRConstants.ARTIFICER_TEXT);

                Comment comment = new Comment();
                comment.setCreatedBy(createdBy);
                comment.setCreatedTimestamp(createdTimestamp);
                comment.setText(text);

                artifact.getComment().add(comment);
            }
        }
    }

    private void setOtherAttributes(Node node, Map<QName, String> otherAttributes) throws Exception {
        String attributeKeyPrefix = JCRConstants.SRAMP_OTHER_ATTRIBUTES + ":";
        PropertyIterator properties = node.getProperties();
        while (properties.hasNext()) {
            Property property = properties.nextProperty();
            String propName = property.getName();
            if (propName.startsWith(attributeKeyPrefix)) {
                String qname = propName.substring(attributeKeyPrefix.length());
                String propValue = property.getValue().getString();
                // Need to support no-value properties, but JCR will remove it if it's null.  Further, if it's an
                // empty string, the property existence query fails.  Therefore, ArtifactToJCRNodeVisitor uses
                // this placeholder.
                if (propValue.equals(JCRConstants.NO_VALUE)) {
                    propValue = null;
                }

                otherAttributes.put(QName.valueOf(qname), propValue);
            }
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitNamedWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType)
     */
    @Override
    protected void visitNamedWsdlDerived(NamedWsdlDerivedArtifactType artifact) {
        super.visitNamedWsdlDerived(artifact);
        artifact.setNCName(getProperty(jcrNode, JCRConstants.SRAMP_NC_NAME));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
     */
    @Override
    protected void visitDocument(DocumentArtifactType artifact) {
        super.visitDocument(artifact);
        artifact.setContentSize(getPropertyLong(jcrNode, JCRConstants.SRAMP_CONTENT_SIZE));
        artifact.setContentType(getProperty(jcrNode, JCRConstants.SRAMP_CONTENT_TYPE));
        artifact.setContentHash(getProperty(jcrNode, JCRConstants.SRAMP_CONTENT_HASH));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
     */
    @Override
    protected void visitXmlDocument(XmlDocument artifact) {
        super.visitXmlDocument(artifact);
        artifact.setContentEncoding(getProperty(jcrNode, JCRConstants.SRAMP_CONTENT_ENCODING));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitExtended(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    protected void visitExtended(ExtendedArtifactType artifact) {
        super.visitExtended(artifact);
        String extendedType = getProperty(jcrNode, JCRConstants.SRAMP_EXTENDED_TYPE);
        String extendedDerived = getProperty(jcrNode, JCRConstants.SRAMP_DERIVED, "false");

        artifact.setExtendedType(extendedType);
        artifact.getOtherAttributes().put(ArtificerConstants.SRAMP_DERIVED_QNAME, extendedDerived);
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitExtendedDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
     */
    @Override
    protected void visitExtendedDocument(ExtendedDocument artifact) {
        super.visitExtendedDocument(artifact);
        String extendedType = getProperty(jcrNode, JCRConstants.SRAMP_EXTENDED_TYPE);
        String contentType = getProperty(jcrNode, JCRConstants.JCR_CONTENT_MIME_TYPE);
        String contentLength = String.valueOf(getPropertyLength(jcrNode, JCRConstants.JCR_CONTENT_DATA));

        artifact.setExtendedType(extendedType);
        if (contentType != null && contentLength != null) {
            artifact.getOtherAttributes().put(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME, contentLength);
            artifact.getOtherAttributes().put(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME, contentType);
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
                    getRelationship("directsOrchestrationProcess", OrchestrationProcessTarget.class));
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        super.visit(artifact);

        artifact.setTargetNamespace(getProperty(jcrNode, JCRConstants.SRAMP_TARGET_NAMESPACE));

        try {
            artifact.getImportedXsds().addAll(getRelationships("importedXsds", XsdDocumentTarget.class));
            artifact.getIncludedXsds().addAll(getRelationships("includedXsds", XsdDocumentTarget.class));
            artifact.getRedefinedXsds().addAll(getRelationships("redefinedXsds", XsdDocumentTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
     */
    @Override
    public void visit(AttributeDeclaration artifact) {
        super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
     */
    @Override
    public void visit(ComplexTypeDeclaration artifact) {
        super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
     */
    @Override
    public void visit(ElementDeclaration artifact) {
        super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
     */
    @Override
    public void visit(SimpleTypeDeclaration artifact) {
        super.visit(artifact);

        artifact.setNamespace(getProperty(jcrNode, JCRConstants.SRAMP_NAMESPACE));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        super.visit(artifact);

        artifact.setTargetNamespace(getProperty(jcrNode, JCRConstants.SRAMP_TARGET_NAMESPACE));

        try {
            artifact.getImportedXsds().addAll(getRelationships("importedXsds", XsdDocumentTarget.class));
            artifact.getIncludedXsds().addAll(getRelationships("includedXsds", XsdDocumentTarget.class));
            artifact.getRedefinedXsds().addAll(getRelationships("redefinedXsds", XsdDocumentTarget.class));
            artifact.getImportedWsdls().addAll(getRelationships("importedWsdls", WsdlDocumentTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
     */
    @Override
    public void visit(Part artifact) {
        super.visit(artifact);
        try {
            artifact.setElement(getRelationship("element", ElementDeclarationTarget.class));
            artifact.setType(getRelationship("type", XsdTypeTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding)
     */
    @Override
    public void visit(SoapBinding artifact) {
        super.visit(artifact);
        artifact.setStyle(getProperty(jcrNode, JCRConstants.SRAMP_STYLE));
        artifact.setTransport(getProperty(jcrNode, JCRConstants.SRAMP_TRANSPORT));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
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
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress)
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
            artifact.getDescribedBy().addAll(getRelationships("describedBy", BaseArtifactTarget.class));
            artifact.getUses().addAll(getRelationships("uses", BaseArtifactTarget.class));
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
            artifact.setInterfaceDefinedBy(getRelationship("interfaceDefinedBy", DerivedArtifactTarget.class));
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
        String relNodeName = JCRConstants.SRAMP_RELATIONSHIPS + ":" + relationshipType;
        if (this.jcrNode.hasNode(relNodeName)) {
            Node relationshipNode = this.jcrNode.getNode(relNodeName);
            NodeIterator targetNodes = relationshipNode.getNodes();
            while (targetNodes.hasNext()) {
                Node targetNode = targetNodes.nextNode();
                Value relationshipTarget = targetNode.getProperty(JCRConstants.SRAMP_TARGET_ARTIFACT).getValue();
                String targetType = null;
                if (targetNode.hasProperty(JCRConstants.SRAMP_TARGET_TYPE)) {
                    targetType = targetNode.getProperty(JCRConstants.SRAMP_TARGET_TYPE).getValue().getString();
                }

                T t = createTarget(targetClass, relationshipTarget);
                Target target = (Target) t;
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
                setOtherAttributes(targetNode, target.getOtherAttributes());
                rval.add(t);
            }
        }
        return rval;
    }

    private <T> T createTarget(Class<T> targetClass, Value relationshipTarget) throws Exception {
        T t = targetClass.newInstance();
        Target target = (Target) t;
        Node targetedNode = referenceResolver.resolveReference(relationshipTarget);
        if (targetedNode != null) {
            String targetedUuid = targetedNode.getProperty(JCRConstants.SRAMP_UUID).getString();
            target.setValue(targetedUuid);
            // Making a few design decisions here.  1.) Don't attempt to set this when *persisting* into
            // the repo.  The model/type of the targeted artifact aren't guaranteed to be there.  2.)
            // Don't try to pass baseUrl in through the server resources.  ArtifactToFullAtomEntryVisitor
            // will later prepend it.
            ArtifactType targetedArtifactType = ArtifactType.valueOf(
                    targetedNode.getProperty(JCRConstants.SRAMP_ARTIFACT_TYPE).getValue().getString());
            String type;
            if (ExtendedArtifactType.class.isAssignableFrom(targetedArtifactType.getArtifactType().getTypeClass())
                    || ExtendedDocument.class.isAssignableFrom(targetedArtifactType.getArtifactType().getTypeClass())) {
                type = targetedNode.getProperty(JCRConstants.SRAMP_EXTENDED_TYPE).getValue().getString();
            } else {
                type = targetedArtifactType.getType();
            }
            String href = String.format("%1$s/%2$s/%3$s", targetedArtifactType.getModel(), type, targetedUuid);
            target.setHref(href);
        }
        return t;
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
         * Resolves a JCR reference into an s-ramp artifact Node.
         * @param reference a JCR reference
         * @return the Node of an s-ramp artifact (or null if it fails to resolve)
         */
        public Node resolveReference(Value reference);

    }

}
