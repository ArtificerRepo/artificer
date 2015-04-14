/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.common.visitors;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import java.util.List;

/**
 * Visits all of the artifact's relationships.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class RelationshipArtifactVisitor extends HierarchicalArtifactVisitor {

    /**
     * Constructor.
     */
    public RelationshipArtifactVisitor() {
    }

    /**
     * @see HierarchicalArtifactVisitor#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
     */
    @Override
    protected void visitBase(BaseArtifactType artifact) {
        List<Relationship> relationships = artifact.getRelationship();
        if (relationships != null) {
            for (Relationship relationship : relationships) {
                String type = relationship.getRelationshipType();
                List<Target> targets = relationship.getRelationshipTarget();
                visitRelationships(type, targets, true);
            }
        }
        super.visitBase(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
     */
    @Override
    protected void visitDerived(DerivedArtifactType artifact) {
        DocumentArtifactTarget relatedDocument = artifact.getRelatedDocument();
        visitRelationship("relatedDocument", relatedDocument, false); //$NON-NLS-1$
        super.visitDerived(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
     */
    @Override
    protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        visitRelationships("extension", artifact.getExtension(), false); //$NON-NLS-1$
        super.visitWsdlDerived(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitSoa(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType)
     */
    @Override
    protected void visitSoa(SoaModelType artifact) {
        visitRelationships("documentation", artifact.getDocumentation(), false); //$NON-NLS-1$
        super.visitSoa(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitServiceImplementation(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType)
     */
    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        visitRelationships("documentation", artifact.getDocumentation(), false); //$NON-NLS-1$
        super.visitServiceImplementation(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract)
     */
    @Override
    public void visit(ServiceContract artifact) {
        visitRelationships("involvesParty", artifact.getInvolvesParty(), false); //$NON-NLS-1$
        visitRelationships("specifies", artifact.getSpecifies(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
     */
    @Override
    public void visit(BindingOperation artifact) {
        visitRelationships("fault", artifact.getFault(), false); //$NON-NLS-1$
        visitRelationship("input", artifact.getInput(), false); //$NON-NLS-1$
        visitRelationship("output", artifact.getOutput(), false); //$NON-NLS-1$
        visitRelationship("operation", artifact.getOperation(), false); //$NON-NLS-1$

        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor)
     */
    @Override
    public void visit(Actor artifact) {
        visitRelationships("does", artifact.getDoes(), false); //$NON-NLS-1$
        visitRelationships("setsPolicy", artifact.getSetsPolicy(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
     */
    @Override
    public void visit(Binding artifact) {
        visitRelationships("bindingOperation", artifact.getBindingOperation(), false); //$NON-NLS-1$
        visitRelationship("portType", artifact.getPortType(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element)
     */
    @Override
    public void visit(Element artifact) {
        visitRelationships("represents", artifact.getRepresents(), false); //$NON-NLS-1$
        visitRelationships("uses", artifact.getUses(), false); //$NON-NLS-1$
        visitRelationships("performs", artifact.getPerforms(), false); //$NON-NLS-1$
        visitRelationship("directsOrchestration", artifact.getDirectsOrchestration(), false); //$NON-NLS-1$
        visitRelationship("directsOrchestrationProcess", artifact.getDirectsOrchestrationProcess(), false); //$NON-NLS-1$
        visitRelationships("generates", artifact.getGenerates(), false); //$NON-NLS-1$
        visitRelationships("respondsTo", artifact.getRespondsTo(), false); //$NON-NLS-1$

        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
     */
    @Override
    public void visit(Fault artifact) {
        visitRelationship("message", artifact.getMessage(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
     */
    @Override
    public void visit(Message artifact) {
        visitRelationships("part", artifact.getPart(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
     */
    @Override
    public void visit(Operation artifact) {
        visitRelationship("input", artifact.getInput(), false); //$NON-NLS-1$
        visitRelationship("output", artifact.getOutput(), false); //$NON-NLS-1$
        visitRelationships("fault", artifact.getFault(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
     */
    @Override
    public void visit(OperationInput artifact) {
        visitRelationship("message", artifact.getMessage(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
     */
    @Override
    public void visit(OperationOutput artifact) {
        visitRelationship("message", artifact.getMessage(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization)
     */
    @Override
    public void visit(Organization artifact) {
        visitRelationships("provides", artifact.getProvides(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
     */
    @Override
    public void visit(Part artifact) {
        visitRelationship("type", artifact.getType(), false); //$NON-NLS-1$
        visitRelationship("element", artifact.getElement(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy)
     */
    @Override
    public void visit(Policy artifact) {
        visitRelationships("appliesTo", artifact.getAppliesTo(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment)
     */
    @Override
    public void visit(PolicyAttachment artifact) {
        visitRelationships("appliesTo", artifact.getAppliesTo(), false); //$NON-NLS-1$
        visitRelationships("policies", artifact.getPolicies(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
     */
    @Override
    public void visit(Port artifact) {
        visitRelationship("binding", artifact.getBinding(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
     */
    @Override
    public void visit(PortType artifact) {
        visitRelationships("operation", artifact.getOperation(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service)
     */
    @Override
    public void visit(Service artifact) {
        visitRelationships("hasContract", artifact.getHasContract(), false); //$NON-NLS-1$
        visitRelationships("hasInterface", artifact.getHasInterface(), false); //$NON-NLS-1$
        visitRelationship("hasInstance", artifact.getHasInstance(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint)
     */
    @Override
    public void visit(ServiceEndpoint artifact) {
        visitRelationship("endpointDefinedBy", artifact.getEndpointDefinedBy(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance)
     */
    @Override
    public void visit(ServiceInstance artifact) {
        visitRelationships("uses", artifact.getUses(), false); //$NON-NLS-1$
        visitRelationships("describedBy", artifact.getDescribedBy(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface)
     */
    @Override
    public void visit(ServiceInterface artifact) {
        visitRelationship("hasOperation", artifact.getHasOperation(), false); //$NON-NLS-1$
        visitRelationships("hasOutput", artifact.getHasOutput(), false); //$NON-NLS-1$
        visitRelationships("hasInput", artifact.getHasInput(), false); //$NON-NLS-1$
        visitRelationships("isInterfaceOf", artifact.getIsInterfaceOf(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation)
     */
    @Override
    public void visit(ServiceOperation artifact) {
        visitRelationship("operationDefinedBy", artifact.getOperationDefinedBy(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        visitRelationships("importedXsds", artifact.getImportedXsds(), false); //$NON-NLS-1$
        visitRelationships("includedXsds", artifact.getIncludedXsds(), false); //$NON-NLS-1$
        visitRelationships("redefinedXsds", artifact.getRedefinedXsds(), false); //$NON-NLS-1$
        visitRelationships("importedWsdls", artifact.getImportedWsdls(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
     */
    @Override
    public void visit(WsdlService artifact) {
        visitRelationships("port", artifact.getPort(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        visitRelationships("importedXsds", artifact.getImportedXsds(), false); //$NON-NLS-1$
        visitRelationships("includedXsds", artifact.getIncludedXsds(), false); //$NON-NLS-1$
        visitRelationships("redefinedXsds", artifact.getRedefinedXsds(), false); //$NON-NLS-1$
        super.visit(artifact);
    }

    /**
     * Visits a collection of relationships.
     * @param type
     * @param targets
     */
    protected void visitRelationships(String type, List<? extends Target> targets, boolean generic) {
        for (Target target : targets) {
            visitRelationship(type, target, generic);
        }
    }

    /**
     * Called to visit a relationship.
     * @param type
     * @param target
     */
    protected abstract void visitRelationship(String type, Target target, boolean generic);

}
