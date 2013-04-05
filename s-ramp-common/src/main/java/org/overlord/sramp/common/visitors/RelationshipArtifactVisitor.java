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
package org.overlord.sramp.common.visitors;

import java.util.List;

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

/**
 * Visits all of the artifact's relationships.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class RelationshipArtifactVisitor extends HierarchicalArtifactVisitorAdapter {

    /**
     * Constructor.
     */
    public RelationshipArtifactVisitor() {
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
     */
    @Override
    protected void visitBase(BaseArtifactType artifact) {
        List<Relationship> relationships = artifact.getRelationship();
        if (relationships != null) {
            for (Relationship relationship : relationships) {
                String type = relationship.getRelationshipType();
                List<Target> targets = relationship.getRelationshipTarget();
                visitRelationships(type, targets);
            }
        }
        super.visitBase(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType)
     */
    @Override
    protected void visitDerived(DerivedArtifactType artifact) {
        DocumentArtifactTarget relatedDocument = artifact.getRelatedDocument();
        visitRelationship("relatedDocument", relatedDocument);
        super.visitDerived(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
     */
    @Override
    protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        visitRelationships("extension", artifact.getExtension());
        super.visitWsdlDerived(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitSoa(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType)
     */
    @Override
    protected void visitSoa(SoaModelType artifact) {
        visitRelationships("documentation", artifact.getDocumentation());
        super.visitSoa(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visitServiceImplementation(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType)
     */
    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        visitRelationships("documentation", artifact.getDocumentation());
        super.visitServiceImplementation(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract)
     */
    @Override
    public void visit(ServiceContract artifact) {
        visitRelationships("involvesParty", artifact.getInvolvesParty());
        visitRelationships("specifies", artifact.getSpecifies());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
     */
    @Override
    public void visit(BindingOperation artifact) {
        visitRelationships("fault", artifact.getFault());
        visitRelationship("input", artifact.getInput());
        visitRelationship("output", artifact.getOutput());
        visitRelationship("operation", artifact.getOperation());

        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor)
     */
    @Override
    public void visit(Actor artifact) {
        visitRelationships("does", artifact.getDoes());
        visitRelationships("setsPolicy", artifact.getSetsPolicy());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
     */
    @Override
    public void visit(Binding artifact) {
        visitRelationships("bindingOperation", artifact.getBindingOperation());
        visitRelationship("portType", artifact.getPortType());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element)
     */
    @Override
    public void visit(Element artifact) {
        visitRelationships("represents", artifact.getRepresents());
        visitRelationships("uses", artifact.getUses());
        visitRelationships("performs", artifact.getPerforms());
        visitRelationship("directsOrchestration", artifact.getDirectsOrchestration());
        visitRelationship("directsOrchestrationProcess", artifact.getDirectsOrchestrationProcess());
        visitRelationships("generates", artifact.getGenerates());
        visitRelationships("respondsTo", artifact.getRespondsTo());

        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
     */
    @Override
    public void visit(Fault artifact) {
        visitRelationship("message", artifact.getMessage());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
     */
    @Override
    public void visit(Message artifact) {
        visitRelationships("part", artifact.getPart());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
     */
    @Override
    public void visit(Operation artifact) {
        visitRelationship("input", artifact.getInput());
        visitRelationship("output", artifact.getOutput());
        visitRelationships("fault", artifact.getFault());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
     */
    @Override
    public void visit(OperationInput artifact) {
        visitRelationship("message", artifact.getMessage());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
     */
    @Override
    public void visit(OperationOutput artifact) {
        visitRelationship("message", artifact.getMessage());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization)
     */
    @Override
    public void visit(Organization artifact) {
        visitRelationships("provides", artifact.getProvides());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
     */
    @Override
    public void visit(Part artifact) {
        visitRelationship("type", artifact.getType());
        visitRelationship("element", artifact.getElement());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy)
     */
    @Override
    public void visit(Policy artifact) {
        visitRelationships("appliesTo", artifact.getAppliesTo());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment)
     */
    @Override
    public void visit(PolicyAttachment artifact) {
        visitRelationships("appliesTo", artifact.getAppliesTo());
        visitRelationships("policies", artifact.getPolicies());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
     */
    @Override
    public void visit(Port artifact) {
        visitRelationship("binding", artifact.getBinding());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
     */
    @Override
    public void visit(PortType artifact) {
        visitRelationships("operation", artifact.getOperation());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service)
     */
    @Override
    public void visit(Service artifact) {
        visitRelationships("hasContract", artifact.getHasContract());
        visitRelationships("hasInterface", artifact.getHasInterface());
        visitRelationship("hasInstance", artifact.getHasInstance());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint)
     */
    @Override
    public void visit(ServiceEndpoint artifact) {
        visitRelationship("endpointDefinedBy", artifact.getEndpointDefinedBy());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance)
     */
    @Override
    public void visit(ServiceInstance artifact) {
        visitRelationships("uses", artifact.getUses());
        visitRelationships("describedBy", artifact.getDescribedBy());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface)
     */
    @Override
    public void visit(ServiceInterface artifact) {
        visitRelationship("hasOperation", artifact.getHasOperation());
        visitRelationships("hasOutput", artifact.getHasOutput());
        visitRelationships("hasInput", artifact.getHasInput());
        visitRelationships("isInterfaceOf", artifact.getIsInterfaceOf());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation)
     */
    @Override
    public void visit(ServiceOperation artifact) {
        visitRelationship("operationDefinedBy", artifact.getOperationDefinedBy());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        visitRelationships("importedXsds", artifact.getImportedXsds());
        visitRelationships("includedXsds", artifact.getIncludedXsds());
        visitRelationships("redefinedXsds", artifact.getRedefinedXsds());
        visitRelationships("importedWsdls", artifact.getImportedWsdls());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
     */
    @Override
    public void visit(WsdlService artifact) {
        visitRelationships("port", artifact.getPort());
        super.visit(artifact);
    }

    /**
     * @see org.overlord.sramp.common.visitors.HierarchicalArtifactVisitorAdapter#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        visitRelationships("importedXsds", artifact.getImportedXsds());
        visitRelationships("includedXsds", artifact.getIncludedXsds());
        visitRelationships("redefinedXsds", artifact.getRedefinedXsds());
        super.visit(artifact);
    }

    /**
     * Visits a collection of relationships.
     * @param type
     * @param targets
     */
    protected void visitRelationships(String type, List<? extends Target> targets) {
        for (Target target : targets) {
            visitRelationship(type, target);
        }
    }

    /**
     * Called to visit a relationship.
     * @param type
     * @param target
     */
    protected abstract void visitRelationship(String type, Target target);

}
