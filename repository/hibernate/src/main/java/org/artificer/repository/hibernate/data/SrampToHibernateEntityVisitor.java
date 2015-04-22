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

import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtifactType;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.hibernate.HibernateEntityFactory;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerProperty;
import org.artificer.repository.hibernate.entity.ArtificerUser;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import java.net.URI;
import java.util.Collection;

/**
 * @author Brett Meyer.
 */
public class SrampToHibernateEntityVisitor extends HierarchicalArtifactVisitor {

    private ArtificerArtifact artificerArtifact;
    private final ArtifactType artifactType;
    private final ClassificationHelper classificationHelper;

    public static ArtificerArtifact visit(BaseArtifactType srampArtifact,
            ArtifactType artifactType, ClassificationHelper classificationHelper) throws Exception {
        // If the artifact is not known, allow the HibernateEntityCreator visit and create the appropriate entity type.
        ArtificerArtifact artificerArtifact = HibernateEntityCreator.visit(artifactType);
        return visit(srampArtifact, artificerArtifact, artifactType, classificationHelper);
    }

    public static ArtificerArtifact visit(BaseArtifactType srampArtifact, ArtificerArtifact artificerArtifact,
            ArtifactType artifactType, ClassificationHelper classificationHelper) throws Exception {
        SrampToHibernateEntityVisitor visitor = new SrampToHibernateEntityVisitor(artificerArtifact, artifactType, classificationHelper);
        ArtifactVisitorHelper.visitArtifact(visitor, srampArtifact);

        visitor.throwError();

        return artificerArtifact;
    }

    public SrampToHibernateEntityVisitor(ArtificerArtifact artificerArtifact, ArtifactType artifactType,
            ClassificationHelper classificationHelper) {
        this.artificerArtifact = artificerArtifact;
        this.artifactType = artifactType;
        this.classificationHelper = classificationHelper;

        artificerArtifact.getClassifiers().clear();
        artificerArtifact.getNormalizedClassifiers().clear();
        artificerArtifact.getProperties().clear();
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
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitDocument(DocumentArtifactType artifact) {
        super.visitDocument(artifact);
        try {
            ArtificerDocumentArtifact artificerDocumentArtifact = (ArtificerDocumentArtifact) artificerArtifact;
            artificerDocumentArtifact.setMimeType(artifactType.getMimeType());
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
            createProperty("namespace", artifact.getNamespace());
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
            createProperty("ncName", artifact.getNCName());
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitExtended(ExtendedArtifactType artifact) {
        // override the type with extendedType
        artificerArtifact.setType(artifact.getExtendedType());
    }

    @Override
    protected void visitExtendedDocument(ExtendedDocument artifact) {
        // override the type with extendedType
        artificerArtifact.setType(artifact.getExtendedType());
    }

    /**
     * Updates the basic s-ramp meta data.
     * @param srampArtifact
     * @throws Exception
     */
    private void updateArtifactMetaData(BaseArtifactType srampArtifact) throws Exception {
        artificerArtifact.setUuid(srampArtifact.getUuid());
        artificerArtifact.setModel(artifactType.getModel());

        // may have already been set...
        if (StringUtils.isBlank(artificerArtifact.getType())) {
            artificerArtifact.setType(artifactType.getType());
        }

        ArtificerUser createdBy = HibernateEntityFactory.user(srampArtifact.getCreatedBy(), srampArtifact.getCreatedTimestamp());
        artificerArtifact.setCreatedBy(createdBy);
        ArtificerUser modifiedBy = HibernateEntityFactory.user(srampArtifact.getLastModifiedBy(), srampArtifact.getLastModifiedTimestamp());
        artificerArtifact.setModifiedBy(modifiedBy);

        if (srampArtifact.getName() != null)
            artificerArtifact.setName(srampArtifact.getName());
        else
            artificerArtifact.setName(srampArtifact.getClass().getSimpleName());

        artificerArtifact.setDescription(srampArtifact.getDescription());
        artificerArtifact.setVersion(srampArtifact.getVersion());
    }

    /**
     * Updates the classifications.
     *
     * @param artifact
     * @throws Exception
     */
    private void updateClassifications(BaseArtifactType artifact) throws Exception {
        Collection<URI> classifiers = classificationHelper.resolveAll(artifact.getClassifiedBy());
        Collection<URI> normalizedClassifiers = classificationHelper.normalizeAll(classifiers);
        for (URI classifier : classifiers) {
            artificerArtifact.getClassifiers().add(classifier.toString());
        }
        for (URI normalizedClassifier : normalizedClassifiers) {
            artificerArtifact.getNormalizedClassifiers().add(normalizedClassifier.toString());
        }
    }

    /**
     * Updates the custom s-ramp properties.
     * @param artifact
     * @throws Exception
     */
    private void updateArtifactProperties(BaseArtifactType artifact) throws Exception {
        for (org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property property : artifact.getProperty()) {
            createProperty(property.getPropertyName(), property.getPropertyValue(), true);
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        super.visit(artifact);
        try {
            createProperty("targetNamespace", artifact.getTargetNamespace());
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
            createProperty("namespace", artifact.getNamespace());
            createProperty("ncName", artifact.getNCName());
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
            createProperty("namespace", artifact.getNamespace());
            createProperty("ncName", artifact.getNCName());
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
            createProperty("namespace", artifact.getNamespace());
            createProperty("ncName", artifact.getNCName());
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
            createProperty("namespace", artifact.getNamespace());
            createProperty("ncName", artifact.getNCName());
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
            createProperty("targetNamespace", artifact.getTargetNamespace());
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding)
     */
    @Override
    public void visit(SoapBinding artifact) {
        super.visit(artifact);
        try {
            createProperty("style", artifact.getStyle());
            createProperty("transport", artifact.getTransport());
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
            createProperty("soapLocation", artifact.getSoapLocation());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        super.visitServiceImplementation(artifact);
        try {
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceEndpoint artifact) {
        super.visit(artifact);
        try {
            createProperty("url", artifact.getUrl());
            createProperty("end", artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceInstance artifact) {
        super.visit(artifact);
        try {
            createProperty("end", artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visit(ServiceOperation artifact) {
        super.visit(artifact);
        try {
            createProperty("end", artifact.getEnd());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createProperty(String key, String value) {
        createProperty(key, value, false);
    }

    private void createProperty(String key, String value, boolean isCustom) {
        ArtificerProperty artificerProperty = new ArtificerProperty();
        artificerProperty.setKey(key);
        artificerProperty.setValue(value);
        artificerProperty.setCustom(isCustom);
        artificerProperty.setOwner(artificerArtifact);
        artificerArtifact.getProperties().add(artificerProperty);
    }
}
