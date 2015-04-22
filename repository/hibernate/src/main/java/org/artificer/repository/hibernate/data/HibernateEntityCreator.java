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
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.common.visitors.HierarchicalArtifactVisitor;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerWsdlDerivedArtifact;
import org.artificer.repository.hibernate.entity.ArtificerWsdlDocumentArtifact;
import org.artificer.repository.hibernate.entity.ArtificerXsdDocumentArtifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

/**
 * @author Brett Meyer.
 */
public class HibernateEntityCreator extends HierarchicalArtifactVisitor {

    private ArtificerArtifact artificerArtifact;

    /**
     * Visit and create the correct subclass of ArtificerArtifact.  Note that we pass in ArtifactType, rather than the
     * actual BaseArtifactType.  We need to create the true, most-specific type.  Ex: Someone could pass in
     * a Document (instead of XmlDocument) with "XmlDocument" as the artifact type.  In that instance, we should still
     * create an actual XmlDocument!
     *
     * @param artifactType
     * @return ArtificerArtifact
     * @throws Exception
     */
    public static ArtificerArtifact visit(ArtifactType artifactType) throws Exception {
        HibernateEntityCreator visitor = new HibernateEntityCreator();
        ArtifactVisitorHelper.visitArtifact(visitor, artifactType.newArtifactInstance());

        visitor.throwError();

        return visitor.getArtificerArtifact();
    }

    protected ArtificerArtifact getArtificerArtifact() {
        return artificerArtifact;
    }

    @Override
    protected void visitBase(BaseArtifactType artifact) {
        // The last resort.
        try {
            initArtificerArtifact(ArtificerArtifact.class);
            super.visitBase(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    protected void visitDocument(DocumentArtifactType artifact) {
        try {
            initArtificerArtifact(ArtificerDocumentArtifact.class);
            super.visitDocument(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
     */
    @Override
    protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        try {
            initArtificerArtifact(ArtificerWsdlDerivedArtifact.class);
            super.visitWsdlDerived(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    @Override
    public void visit(ExtendedDocument artifact) {
        try {
            initArtificerArtifact(ArtificerDocumentArtifact.class);
            super.visit(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        try {
            initArtificerArtifact(ArtificerXsdDocumentArtifact.class);
            super.visit(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    /**
     * @see org.artificer.common.visitors.HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        try {
            initArtificerArtifact(ArtificerWsdlDocumentArtifact.class);
            super.visit(artifact);
        } catch (Exception e) {
            error = e;
        }
    }

    private void initArtificerArtifact(Class<? extends ArtificerArtifact> clazz) throws Exception {
        if (artificerArtifact == null) {
            artificerArtifact = clazz.newInstance();
        }
    }
}
