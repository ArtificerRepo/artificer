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

import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtificerConstants;
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
        // expansion
        String archiveUuid = artifact.getOtherAttributes().get(ArtificerConstants.ARTIFICER_EXPANDED_FROM_ARCHIVE_UUID_QNAME);
        if (StringUtils.isNotBlank(archiveUuid)) {
            visitRelationship("expandedFromArchive", archiveUuid, false);
        }

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
        visitRelationship("relatedDocument", relatedDocument, false);
        super.visitDerived(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitWsdlDerived(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDerivedArtifactType)
     */
    @Override
    protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
        visitRelationships("extension", artifact.getExtension(), false);
        super.visitWsdlDerived(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitSoa(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoaModelType)
     */
    @Override
    protected void visitSoa(SoaModelType artifact) {
        visitRelationships("documentation", artifact.getDocumentation(), false);
        super.visitSoa(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visitServiceImplementation(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelType)
     */
    @Override
    protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
        visitRelationships("documentation", artifact.getDocumentation(), false);
        super.visitServiceImplementation(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract)
     */
    @Override
    public void visit(ServiceContract artifact) {
        visitRelationships("involvesParty", artifact.getInvolvesParty(), false);
        visitRelationships("specifies", artifact.getSpecifies(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
     */
    @Override
    public void visit(BindingOperation artifact) {
        visitRelationships("fault", artifact.getFault(), false);
        visitRelationship("input", artifact.getInput(), false);
        visitRelationship("output", artifact.getOutput(), false);
        visitRelationship("operation", artifact.getOperation(), false);

        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor)
     */
    @Override
    public void visit(Actor artifact) {
        visitRelationships("does", artifact.getDoes(), false);
        visitRelationships("setsPolicy", artifact.getSetsPolicy(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
     */
    @Override
    public void visit(Binding artifact) {
        visitRelationships("bindingOperation", artifact.getBindingOperation(), false);
        visitRelationship("portType", artifact.getPortType(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element)
     */
    @Override
    public void visit(Element artifact) {
        visitRelationships("represents", artifact.getRepresents(), false);
        visitRelationships("uses", artifact.getUses(), false);
        visitRelationships("performs", artifact.getPerforms(), false);
        visitRelationship("directsOrchestration", artifact.getDirectsOrchestration(), false);
        visitRelationship("directsOrchestrationProcess", artifact.getDirectsOrchestrationProcess(), false);
        visitRelationships("generates", artifact.getGenerates(), false);
        visitRelationships("respondsTo", artifact.getRespondsTo(), false);

        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
     */
    @Override
    public void visit(Fault artifact) {
        visitRelationship("message", artifact.getMessage(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
     */
    @Override
    public void visit(Message artifact) {
        visitRelationships("part", artifact.getPart(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
     */
    @Override
    public void visit(Operation artifact) {
        visitRelationship("input", artifact.getInput(), false);
        visitRelationship("output", artifact.getOutput(), false);
        visitRelationships("fault", artifact.getFault(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
     */
    @Override
    public void visit(OperationInput artifact) {
        visitRelationship("message", artifact.getMessage(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
     */
    @Override
    public void visit(OperationOutput artifact) {
        visitRelationship("message", artifact.getMessage(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization)
     */
    @Override
    public void visit(Organization artifact) {
        visitRelationships("provides", artifact.getProvides(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
     */
    @Override
    public void visit(Part artifact) {
        visitRelationship("type", artifact.getType(), false);
        visitRelationship("element", artifact.getElement(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy)
     */
    @Override
    public void visit(Policy artifact) {
        visitRelationships("appliesTo", artifact.getAppliesTo(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment)
     */
    @Override
    public void visit(PolicyAttachment artifact) {
        visitRelationships("appliesTo", artifact.getAppliesTo(), false);
        visitRelationships("policies", artifact.getPolicies(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
     */
    @Override
    public void visit(Port artifact) {
        visitRelationship("binding", artifact.getBinding(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
     */
    @Override
    public void visit(PortType artifact) {
        visitRelationships("operation", artifact.getOperation(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service)
     */
    @Override
    public void visit(Service artifact) {
        visitRelationships("hasContract", artifact.getHasContract(), false);
        visitRelationships("hasInterface", artifact.getHasInterface(), false);
        visitRelationship("hasInstance", artifact.getHasInstance(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint)
     */
    @Override
    public void visit(ServiceEndpoint artifact) {
        visitRelationship("endpointDefinedBy", artifact.getEndpointDefinedBy(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance)
     */
    @Override
    public void visit(ServiceInstance artifact) {
        visitRelationships("uses", artifact.getUses(), false);
        visitRelationships("describedBy", artifact.getDescribedBy(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface)
     */
    @Override
    public void visit(ServiceInterface artifact) {
        visitRelationship("hasOperation", artifact.getHasOperation(), false);
        visitRelationships("hasOutput", artifact.getHasOutput(), false);
        visitRelationships("hasInput", artifact.getHasInput(), false);
        visitRelationships("isInterfaceOf", artifact.getIsInterfaceOf(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation)
     */
    @Override
    public void visit(ServiceOperation artifact) {
        visitRelationship("operationDefinedBy", artifact.getOperationDefinedBy(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
     */
    @Override
    public void visit(WsdlDocument artifact) {
        visitRelationships("importedXsds", artifact.getImportedXsds(), false);
        visitRelationships("includedXsds", artifact.getIncludedXsds(), false);
        visitRelationships("redefinedXsds", artifact.getRedefinedXsds(), false);
        visitRelationships("importedWsdls", artifact.getImportedWsdls(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
     */
    @Override
    public void visit(WsdlService artifact) {
        visitRelationships("port", artifact.getPort(), false);
        super.visit(artifact);
    }

    /**
     * @see HierarchicalArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
     */
    @Override
    public void visit(XsdDocument artifact) {
        visitRelationships("importedXsds", artifact.getImportedXsds(), false);
        visitRelationships("includedXsds", artifact.getIncludedXsds(), false);
        visitRelationships("redefinedXsds", artifact.getRedefinedXsds(), false);
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

    protected void visitRelationship(String type, String targetUuid, boolean generic) {
        Target target = new Target();
        target.setValue(targetUuid);
        visitRelationship(type, target, generic);
    }

    /**
     * Called to visit a relationship.
     * @param type
     * @param target
     */
    protected abstract void visitRelationship(String type, Target target, boolean generic);

}
