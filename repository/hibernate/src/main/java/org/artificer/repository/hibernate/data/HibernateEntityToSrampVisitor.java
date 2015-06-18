/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.repository.hibernate.data;

import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.repository.hibernate.HibernateEntityFactory;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerComment;
import org.artificer.repository.hibernate.entity.ArtificerDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerProperty;
import org.artificer.repository.hibernate.entity.ArtificerRelationship;
import org.artificer.common.query.RelationshipType;
import org.artificer.repository.hibernate.entity.ArtificerTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ActorTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
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

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps ORM entities to S-RAMP artifacts.
 *
 * @author Brett Meyer.
 */
public class HibernateEntityToSrampVisitor extends HierarchicalArtifactVisitor {

    private final ArtificerArtifact artificerArtifact;
    private final ArtifactType artifactType;
    private final boolean includeAssociations;

    public static BaseArtifactType visit(ArtificerArtifact artificerArtifact,
            boolean includeAssociations) throws Exception {
        return visit(artificerArtifact,
                ArtifactType.valueOf(artificerArtifact.getType(), artificerArtifact.isDocument()), includeAssociations);
    }

    public static BaseArtifactType visit(ArtificerArtifact artificerArtifact, ArtifactType artifactType,
            boolean includeAssociations) throws Exception {
        if (artifactType.isExtendedType() && artificerArtifact.isDocument()) {
            artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
        }
        BaseArtifactType srampArtifact = artifactType.newArtifactInstance();

        HibernateEntityToSrampVisitor visitor = new HibernateEntityToSrampVisitor(artificerArtifact, artifactType,
                includeAssociations);
        ArtifactVisitorHelper.visitArtifact(visitor, srampArtifact);

        visitor.throwError();

        return srampArtifact;
    }

    public HibernateEntityToSrampVisitor(ArtificerArtifact artificerArtifact, ArtifactType artifactType,
            boolean includeAssociations) {
        this.artificerArtifact = artificerArtifact;
        this.artifactType = artifactType;
        this.includeAssociations = includeAssociations;
    }

    @Override
    protected void visitBase(BaseArtifactType srampArtifact) {
        super.visitBase(srampArtifact);

        try {
            srampArtifact.setDescription(artificerArtifact.getDescription());
            srampArtifact.setArtifactType(artifactType.getArtifactType().getApiType());
            srampArtifact.setCreatedBy(artificerArtifact.getCreatedBy().getUsername());
            XMLGregorianCalendar createdTimestamp = HibernateEntityFactory.calendar(artificerArtifact.getCreatedBy().getLastActionTime());
            srampArtifact.setCreatedTimestamp(createdTimestamp);
            srampArtifact.setLastModifiedBy(artificerArtifact.getModifiedBy().getUsername());
            XMLGregorianCalendar modifiedTimestamp = HibernateEntityFactory.calendar(artificerArtifact.getModifiedBy().getLastActionTime());
            srampArtifact.setLastModifiedTimestamp(modifiedTimestamp);
            srampArtifact.setName(artificerArtifact.getName());
            srampArtifact.setUuid(artificerArtifact.getUuid());
            srampArtifact.setVersion(artificerArtifact.getVersion());

            // Map in all the s-ramp extended properties.
            for (ArtificerProperty artificerProperty : artificerArtifact.getProperties()) {
                if (artificerProperty.isCustom()) {
                    Property srampProp = new Property();
                    srampProp.setPropertyName(artificerProperty.getKey());
                    srampProp.setPropertyValue(artificerProperty.getValue());
                    srampArtifact.getProperty().add(srampProp);
                }
            }

            if (includeAssociations) {
                // Map in the classifications
                for (String uri : artificerArtifact.getClassifiers()) {
                    srampArtifact.getClassifiedBy().add(uri);
                }

                visitGenericRelationships(srampArtifact);
                visitComments(srampArtifact);
            }
        } catch (Exception e) {
            error = e;
        }
    }

    private void visitGenericRelationships(BaseArtifactType srampArtifact) throws Exception {
        for (ArtificerRelationship artificerRelationship : artificerArtifact.getRelationships()) {
            if (artificerRelationship.getType() == RelationshipType.GENERIC) {
                Relationship srampRelationship = new Relationship();
                srampRelationship.setRelationshipType(artificerRelationship.getName());
                for (ArtificerTarget artificerTarget : artificerRelationship.getTargets()) {
                    Target srampTarget = createTarget(Target.class, artificerTarget);
                    srampRelationship.getRelationshipTarget().add(srampTarget);
                }

                setOtherAttributes(artificerRelationship.getOtherAttributes(), srampRelationship.getOtherAttributes());

                srampArtifact.getRelationship().add(srampRelationship);
            }
        }
    }

    private void setOtherAttributes(Map<String, String> artificerAttributes, Map<QName, String> srampAttributes)
            throws Exception {
        for (String key : artificerAttributes.keySet()) {
            srampAttributes.put(QName.valueOf(key), artificerAttributes.get(key));
        }
    }

    private void visitComments(BaseArtifactType srampArtifact) throws Exception {
        for (ArtificerComment artificerComment : artificerArtifact.getComments()) {
            Comment srampComment = new Comment();

            srampComment.setCreatedBy(artificerComment.getCreatedBy().getUsername());
            XMLGregorianCalendar createdTimestamp = HibernateEntityFactory.calendar(artificerComment.getCreatedBy().getLastActionTime());
            srampComment.setCreatedTimestamp(createdTimestamp);
            srampComment.setText(artificerComment.getText());

            srampArtifact.getComment().add(srampComment);
        }
    }

    @Override
    protected void visitDerived(DerivedArtifactType srampArtifact) {
        super.visitDerived(srampArtifact);
        try {
            if (includeAssociations) {
                ArtificerArtifact derivedFrom = artificerArtifact.getDerivedFrom();
                if (derivedFrom != null) {
                    DocumentArtifactTarget target = new DocumentArtifactTarget();
                    ArtifactType derivedFromType = ArtifactType.valueOf(derivedFrom.getType(), derivedFrom.isDocument());
                    DocumentArtifactEnum documentArtifactEnum = DocumentArtifactEnum.fromValue(derivedFromType.getArtifactType().getApiType());
                    target.setArtifactType(documentArtifactEnum);
                    target.setValue(derivedFrom.getUuid());
                    srampArtifact.setRelatedDocument(target);
                }
            }
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

        artifact.setNamespace(getProperty("namespace"));
        try {
            artifact.getExtension().addAll(getRelationships(
                    "extension", artificerArtifact, WsdlExtensionTarget.class));
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
        artifact.setNCName(getProperty("ncName"));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType)
     */
    @Override
    protected void visitDocument(DocumentArtifactType artifact) {
        super.visitDocument(artifact);

        ArtificerDocumentArtifact artificerDocumentArtifact = (ArtificerDocumentArtifact) artificerArtifact;
        artifact.setContentSize(artificerDocumentArtifact.getContentSize());
        artifact.setContentType(artificerDocumentArtifact.getMimeType());
        artifact.setContentHash(artificerDocumentArtifact.getContentHash());
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitXmlDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
     */
    @Override
    protected void visitXmlDocument(XmlDocument artifact) {
        super.visitXmlDocument(artifact);
        artifact.setContentEncoding(artificerArtifact.getContentEncoding());
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitExtended(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    protected void visitExtended(ExtendedArtifactType artifact) {
        super.visitExtended(artifact);

        String extendedType = artificerArtifact.getType();
        boolean extendedDerived = artificerArtifact.isDerived();
        artifact.setExtendedType(extendedType);
        artifact.getOtherAttributes().put(ArtificerConstants.SRAMP_DERIVED_QNAME, extendedDerived + "");
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitExtendedDocument(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
     */
    @Override
    protected void visitExtendedDocument(ExtendedDocument artifact) {
        super.visitExtendedDocument(artifact);

        ArtificerDocumentArtifact artificerDocumentArtifact = (ArtificerDocumentArtifact) artificerArtifact;
        String extendedType = artificerDocumentArtifact.getType();
        String contentType = artificerDocumentArtifact.getMimeType();
        long contentLength = artificerDocumentArtifact.getContentSize();

        artifact.setExtendedType(extendedType);
        if (contentType != null) {
            artifact.getOtherAttributes().put(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME, contentLength + "");
            artifact.getOtherAttributes().put(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME, contentType);
        }
    }

    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        super.visitServiceImplementation(artifact);
        try {
            artifact.getDocumentation().addAll(getRelationships(
                    "documentation", artificerArtifact, DocumentArtifactTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void visitSoa(SoaModelType artifact) {
        super.visitSoa(artifact);
        try {
            artifact.getDocumentation().addAll(getRelationships(
                    "documentation", artificerArtifact, DocumentArtifactTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void visitElement(Element artifact) {
        super.visitElement(artifact);
        try {
            artifact.getRepresents().addAll(getRelationships(
                    "represents", artificerArtifact, ElementTarget.class));
            artifact.getUses().addAll(getRelationships(
                    "uses", artificerArtifact, ElementTarget.class));
            artifact.getPerforms().addAll(getRelationships(
                    "performs", artificerArtifact, ServiceTarget.class));
            artifact.setDirectsOrchestration(getRelationship(
                    "directsOrchestration", artificerArtifact, OrchestrationTarget.class));
            artifact.setDirectsOrchestrationProcess(getRelationship(
                    "directsOrchestrationProcess", artificerArtifact, OrchestrationProcessTarget.class));
            artifact.getGenerates().addAll(getRelationships(
                    "generates", artificerArtifact, EventTarget.class));
            artifact.getRespondsTo().addAll(getRelationships(
                    "respondsTo", artificerArtifact, EventTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void visitActor(Actor artifact) {
        super.visitActor(artifact);
        try {
            artifact.getDoes().addAll(getRelationships(
                    "does", artificerArtifact, TaskTarget.class));
            artifact.getSetsPolicy().addAll(getRelationships(
                    "setsPolicy", artificerArtifact, PolicyTarget.class));
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

        artifact.setTargetNamespace(getProperty("targetNamespace"));

        try {
            artifact.getImportedXsds().addAll(getRelationships(
                    "importedXsds", artificerArtifact, XsdDocumentTarget.class));
            artifact.getIncludedXsds().addAll(getRelationships(
                    "includedXsds", artificerArtifact, XsdDocumentTarget.class));
            artifact.getRedefinedXsds().addAll(getRelationships(
                    "redefinedXsds", artificerArtifact, XsdDocumentTarget.class));
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
        artifact.setNamespace(getProperty("namespace"));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
     */
    @Override
    public void visit(ComplexTypeDeclaration artifact) {
        super.visit(artifact);
        artifact.setNamespace(getProperty("namespace"));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
     */
    @Override
    public void visit(ElementDeclaration artifact) {
        super.visit(artifact);
        artifact.setNamespace(getProperty("namespace"));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
     */
    @Override
    public void visit(SimpleTypeDeclaration artifact) {
        super.visit(artifact);
        artifact.setNamespace(getProperty("namespace"));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        super.visit(artifact);

        artifact.setTargetNamespace(getProperty("targetNamespace"));

        try {
            artifact.getImportedXsds().addAll(getRelationships(
                    "importedXsds", artificerArtifact, XsdDocumentTarget.class));
            artifact.getIncludedXsds().addAll(getRelationships(
                    "includedXsds", artificerArtifact, XsdDocumentTarget.class));
            artifact.getRedefinedXsds().addAll(getRelationships(
                    "redefinedXsds", artificerArtifact, XsdDocumentTarget.class));
            artifact.getImportedWsdls().addAll(getRelationships(
                    "importedWsdls", artificerArtifact, WsdlDocumentTarget.class));
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
            artifact.getPart().addAll(getRelationships(
                    "part", artificerArtifact, PartTarget.class));
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
            artifact.setElement(getRelationship(
                    "element", artificerArtifact, ElementDeclarationTarget.class));
            artifact.setType(getRelationship(
                    "type", artificerArtifact, XsdTypeTarget.class));
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
            artifact.getOperation().addAll(getRelationships(
                    "operation", artificerArtifact, OperationTarget.class));
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
            artifact.setInput(getRelationship(
                    "input", artificerArtifact, OperationInputTarget.class));
            artifact.setOutput(getRelationship(
                    "output", artificerArtifact, OperationOutputTarget.class));
            artifact.getFault().addAll(getRelationships(
                    "fault", artificerArtifact, FaultTarget.class));
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
            artifact.setMessage(getRelationship(
                    "message", artificerArtifact, MessageTarget.class));
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
            artifact.setMessage(getRelationship(
                    "message", artificerArtifact, MessageTarget.class));
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
            artifact.setMessage(getRelationship(
                    "message", artificerArtifact, MessageTarget.class));
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
            artifact.getBindingOperation().addAll(getRelationships(
                    "bindingOperation", artificerArtifact, BindingOperationTarget.class));
            artifact.setPortType(getRelationship(
                    "portType", artificerArtifact, PortTypeTarget.class));
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
        artifact.setStyle(getProperty("style"));
        artifact.setTransport(getProperty("transport"));
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
     */
    @Override
    public void visit(BindingOperation artifact) {
        super.visit(artifact);
        try {
            artifact.setInput(getRelationship(
                    "input", artificerArtifact, BindingOperationInputTarget.class));
            artifact.setOutput(getRelationship(
                    "output", artificerArtifact, BindingOperationOutputTarget.class));
            artifact.getFault().addAll(getRelationships(
                    "fault", artificerArtifact, BindingOperationFaultTarget.class));
            artifact.setOperation(getRelationship(
                    "operation", artificerArtifact, OperationTarget.class));
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
            artifact.getPort().addAll(getRelationships(
                    "port", artificerArtifact, PortTarget.class));
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
            artifact.setBinding(getRelationship(
                    "binding", artificerArtifact, BindingTarget.class));
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
        artifact.setSoapLocation(getProperty("soapLocation"));
    }

    @Override
    public void visit(ServiceEndpoint artifact) {
        super.visit(artifact);
        try {
            artifact.setEndpointDefinedBy(getRelationship(
                    "endpointDefinedBy", artificerArtifact, DerivedArtifactTarget.class));
            artifact.setUrl(getProperty("url"));
            // These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            artifact.setEnd(getProperty("end"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceInstance artifact) {
        super.visit(artifact);
        try {
            artifact.getDescribedBy().addAll(getRelationships(
                    "describedBy", artificerArtifact, BaseArtifactTarget.class));
            artifact.getUses().addAll(getRelationships(
                    "uses", artificerArtifact, BaseArtifactTarget.class));
            // These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            artifact.setEnd(getProperty("end"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceOperation artifact) {
        super.visit(artifact);
        try {
            artifact.setOperationDefinedBy(getRelationship(
                    "operationDefinedBy", artificerArtifact, DerivedArtifactTarget.class));
            // These have to currently be added on the subclass visitors, as they're not currently
            // on ServiceImplementationModelType itself.
            artifact.setEnd(getProperty("end"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Policy artifact) {
        super.visit(artifact);
        try {
            artifact.getAppliesTo().addAll(getRelationships(
                    "appliesTo", artificerArtifact, PolicySubjectTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceInterface artifact) {
        super.visit(artifact);
        try {
            artifact.setInterfaceDefinedBy(getRelationship(
                    "interfaceDefinedBy", artificerArtifact, DerivedArtifactTarget.class));
            artifact.setHasOperation(getRelationship(
                    "hasOperation", artificerArtifact, ServiceOperationTarget.class));
            artifact.getHasOutput().addAll(getRelationships(
                    "hasOutput", artificerArtifact, InformationTypeTarget.class));
            artifact.getHasInput().addAll(getRelationships(
                    "hasInput", artificerArtifact, InformationTypeTarget.class));
            artifact.getIsInterfaceOf().addAll(getRelationships(
                    "isInterfaceOf", artificerArtifact, ServiceTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceContract artifact) {
        super.visit(artifact);
        try {
            artifact.getInvolvesParty().addAll(getRelationships(
                    "involvesParty", artificerArtifact, ActorTarget.class));
            artifact.getSpecifies().addAll(getRelationships(
                    "specifies", artificerArtifact, EffectTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Organization artifact) {
        super.visit(artifact);
        try {
            artifact.getProvides().addAll(getRelationships(
                    "provides", artificerArtifact, ServiceImplementationModelTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(Service artifact) {
        super.visit(artifact);
        try {
            artifact.getHasContract().addAll(getRelationships(
                    "hasContract", artificerArtifact, ServiceContractTarget.class));
            artifact.getHasInterface().addAll(getRelationships(
                    "hasInterface", artificerArtifact, ServiceInterfaceTarget.class));
            artifact.setHasInstance(getRelationship(
                    "hasInstance", artificerArtifact, ServiceInstanceTarget.class));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String getProperty(String key) {
        for (ArtificerProperty artificerProperty : artificerArtifact.getProperties()) {
            if (artificerProperty.getKey().equals(key)) {
                return artificerProperty.getValue();
            }
        }
        return null;
    }

    private <T> T getRelationship(ArtificerTarget artificerTarget, Class<T> targetClass) throws Exception {
        T t = createTarget(targetClass, artificerTarget);
        Target target = (Target) t;
        // Use reflection to set the 'artifact type' attribute found on
        // most (all?) targets.  Unfortunately, the method and field are
        // redefined in each subclass of Target.
        // Get ^^^ changed in the spec!
        try {
            Method m = targetClass.getMethod("getArtifactType");
            Class<?> mc = m.getReturnType();
            m = mc.getMethod("valueOf", String.class);
            Object o = m.invoke(null, artificerTarget.getTargetType());
            m = targetClass.getMethod("setArtifactType", o.getClass());
            m.invoke(target, o);
        } catch (Exception e) {
            // eat it
        }
        return t;
    }

    private <T> T getRelationship(String relationshipName, ArtificerArtifact artifact,
            Class<T> targetClass) throws Exception {
        List<T> rval = getRelationships(relationshipName, artifact, targetClass);
        // TODO: Throw something if more than one?
        return rval.size() == 0 ? null : rval.get(0);
    }

    private <T> List<T> getRelationships(String relationshipName, ArtificerArtifact artifact,
            Class<T> targetClass) throws Exception {
        List<T> rval = new ArrayList<T>();
        if (includeAssociations) {
            for (ArtificerRelationship relationship : artifact.getRelationships()) {
                if (relationshipName.equalsIgnoreCase(relationship.getName())) {
                    for (ArtificerTarget artificerTarget : relationship.getTargets()) {
                        T t = getRelationship(artificerTarget, targetClass);
                        rval.add(t);
                    }
                }
            }
        }
        return rval;
    }

    private <T> T createTarget(Class<T> targetClass, ArtificerTarget artificerTarget) throws Exception {
        T t = targetClass.newInstance();
        Target target = (Target) t;
        if (artificerTarget != null) {
            String targetedUuid = artificerTarget.getTarget().getUuid();
            target.setValue(targetedUuid);
            String href = String.format("%1$s/%2$s/%3$s", artificerTarget.getTarget().getModel(),
                    artificerTarget.getTarget().getType(), targetedUuid);
            target.setHref(href);
            setOtherAttributes(artificerTarget.getOtherAttributes(), target.getOtherAttributes());
        }
        return t;
    }
}
