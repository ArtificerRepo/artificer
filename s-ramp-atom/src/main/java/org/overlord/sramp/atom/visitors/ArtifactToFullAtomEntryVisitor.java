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
package org.overlord.sramp.atom.visitors;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;

import java.util.List;

/**
 * Visitor used to convert an artifact to an Atom entry.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactToFullAtomEntryVisitor extends ArtifactToSummaryAtomEntryVisitor {

	public ArtifactToFullAtomEntryVisitor(String baseUrl) {
	    super(baseUrl);
	}

	@Override
	protected void visitBase(BaseArtifactType artifact) {
		for (Relationship relationship : artifact.getRelationship()) {
			for (Target target : relationship.getRelationshipTarget()) {
				setTargetHref(target);
			}
		}
		super.visitBase(artifact);
	}

	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
		setTargetHref(artifact.getRelatedDocument());
		super.visitDerived(artifact);
	}

	@Override
	protected void visitWsdlDerived(WsdlDerivedArtifactType artifact) {
		setTargetHref(artifact.getExtension());
		super.visitWsdlDerived(artifact);
	}

	@Override
	protected void visitServiceImplementation(ServiceImplementationModelType artifact) {
		setTargetHref(artifact.getDocumentation());
		super.visitServiceImplementation(artifact);
	}

	@Override
	protected void visitSoa(SoaModelType artifact) {
		setTargetHref(artifact.getDocumentation());
		super.visitSoa(artifact);
	}

	@Override
	protected void visitElement(Element artifact) {
		setTargetHref(artifact.getRepresents());
		setTargetHref(artifact.getUses());
		setTargetHref(artifact.getPerforms());
		setTargetHref(artifact.getDirectsOrchestration());
		setTargetHref(artifact.getDirectsOrchestrationProcess());
		setTargetHref(artifact.getGenerates());
		setTargetHref(artifact.getRespondsTo());
		super.visitElement(artifact);
	}

	@Override
	protected void visitActor(Actor artifact) {
		setTargetHref(artifact.getDoes());
		setTargetHref(artifact.getSetsPolicy());
		super.visitActor(artifact);
	}

	@Override
	public void visit(XsdDocument artifact) {
		setTargetHref(artifact.getImportedXsds());
		setTargetHref(artifact.getIncludedXsds());
		setTargetHref(artifact.getRedefinedXsds());
		super.visit(artifact);
	}

	@Override
	public void visit(WsdlDocument artifact) {
		setTargetHref(artifact.getImportedXsds());
		setTargetHref(artifact.getIncludedXsds());
		setTargetHref(artifact.getRedefinedXsds());
		setTargetHref(artifact.getImportedWsdls());
		super.visit(artifact);
	}

	@Override
	public void visit(Message artifact) {
		setTargetHref(artifact.getPart());
		super.visit(artifact);
	}

	@Override
	public void visit(Part artifact) {
		setTargetHref(artifact.getElement());
		setTargetHref(artifact.getType());
		super.visit(artifact);
	}

	@Override
	public void visit(PortType artifact) {
		setTargetHref(artifact.getOperation());
		super.visit(artifact);
	}

	@Override
	public void visit(Operation artifact) {
		setTargetHref(artifact.getInput());
		setTargetHref(artifact.getOutput());
		setTargetHref(artifact.getFault());
		super.visit(artifact);
	}

	@Override
	public void visit(OperationInput artifact) {
		setTargetHref(artifact.getMessage());
		super.visit(artifact);
	}

	@Override
	public void visit(OperationOutput artifact) {
		setTargetHref(artifact.getMessage());
		super.visit(artifact);
	}

	@Override
	public void visit(Fault artifact) {
		setTargetHref(artifact.getMessage());
		super.visit(artifact);
	}

	@Override
	public void visit(Binding artifact) {
		setTargetHref(artifact.getBindingOperation());
		setTargetHref(artifact.getPortType());
		super.visit(artifact);
	}

	@Override
	public void visit(BindingOperation artifact) {
		setTargetHref(artifact.getInput());
		setTargetHref(artifact.getOutput());
		setTargetHref(artifact.getFault());
		setTargetHref(artifact.getOperation());
		super.visit(artifact);
	}

	@Override
	public void visit(WsdlService artifact) {
		setTargetHref(artifact.getPort());
		super.visit(artifact);
	}

	@Override
	public void visit(Port artifact) {
		setTargetHref(artifact.getBinding());
		super.visit(artifact);
	}

	@Override
	public void visit(ServiceEndpoint artifact) {
		setTargetHref(artifact.getEndpointDefinedBy());
		super.visit(artifact);
	}

	@Override
	public void visit(ServiceInstance artifact) {
		setTargetHref(artifact.getDescribedBy());
		setTargetHref(artifact.getUses());
		super.visit(artifact);
	}

	@Override
	public void visit(ServiceOperation artifact) {
		setTargetHref(artifact.getOperationDefinedBy());
		super.visit(artifact);
	}

	@Override
	public void visit(Policy artifact) {
		setTargetHref(artifact.getAppliesTo());
		super.visit(artifact);
	}

	@Override
	public void visit(ServiceInterface artifact) {
		setTargetHref(artifact.getInterfaceDefinedBy());
		setTargetHref(artifact.getHasOperation());
		setTargetHref(artifact.getHasOutput());
		setTargetHref(artifact.getHasInput());
		setTargetHref(artifact.getIsInterfaceOf());
		super.visit(artifact);
	}

	@Override
	public void visit(ServiceContract artifact) {
		setTargetHref(artifact.getInvolvesParty());
		setTargetHref(artifact.getSpecifies());
		super.visit(artifact);
	}

	@Override
	public void visit(Organization artifact) {
		setTargetHref(artifact.getProvides());
		super.visit(artifact);
	}

	@Override
	public void visit(Service artifact) {
		setTargetHref(artifact.getHasContract());
		setTargetHref(artifact.getHasInterface());
		setTargetHref(artifact.getHasInstance());
		super.visit(artifact);
	}

	private void setTargetHref(List<? extends Target> targets) {
		for (Target target : targets) {
			setTargetHref(target);
		}
	}

	private void setTargetHref(Target target) {
		if (target != null) {
			// Prepend baseUrl to generic Relationship href
			target.setHref(baseUrl + "/s-ramp/" + target.getHref());
		}
	}

	@Override
	protected boolean includeArtifact() {
		return true;
	}

	@Override
	protected BaseArtifactType createIncludedArtifact(BaseArtifactType artifact)
			throws InstantiationException, IllegalAccessException {
		return artifact;
	}

}
