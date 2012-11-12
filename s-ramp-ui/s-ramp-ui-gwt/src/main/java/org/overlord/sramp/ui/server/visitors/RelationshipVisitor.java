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
package org.overlord.sramp.ui.server.visitors;

import java.util.Collection;

import org.overlord.sramp.ui.shared.beans.ArtifactDetails;
import org.overlord.sramp.ui.shared.beans.RelationshipDetails;
import org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Binding;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperation;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactTarget;
import org.s_ramp.xmlns._2010.s_ramp.Fault;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.Operation;
import org.s_ramp.xmlns._2010.s_ramp.OperationInput;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.Part;
import org.s_ramp.xmlns._2010.s_ramp.Port;
import org.s_ramp.xmlns._2010.s_ramp.PortType;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.Target;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.WsdlService;

/**
 * Visitor used to gather up all the artifact's relationships and add them
 * to the {@link ArtifactDetails} bean.
 *
 * TODO support 0 minimum cardinality relationships!
 *
 * @author eric.wittmann@redhat.com
 */
public class RelationshipVisitor extends HierarchicalArtifactVisitorAdapter {

	private ArtifactDetails details;

	/**
	 * Constructor.
	 * @param details
	 */
	public RelationshipVisitor(ArtifactDetails details) {
		this.details = details;
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitBase(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	protected void visitBase(BaseArtifactType artifact) {
		for (Relationship rel : artifact.getRelationship()) {
			String relationshipType = rel.getRelationshipType();
			for (Target target : rel.getRelationshipTarget()) {
				addRelationship(relationshipType, target);
			}
		}
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visitDerived(org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType)
	 */
	@Override
	protected void visitDerived(DerivedArtifactType artifact) {
		DocumentArtifactTarget relatedDocument = artifact.getRelatedDocument();
		addRelationship("relatedDocument", relatedDocument);
	}


	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		super.visit(artifact);
		addRelationships("importedXsds", artifact.getImportedXsds());
		addRelationships("includedXsds", artifact.getIncludedXsds());
		addRelationships("importedWsdls", artifact.getImportedWsdls());
	}

	/**
	 * Message has references to all its {@link Part}s.
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Message)
	 */
	@Override
	public void visit(Message artifact) {
		super.visit(artifact);
		addRelationships("part", artifact.getPart());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Part)
	 */
	@Override
	public void visit(Part artifact) {
		super.visit(artifact);
		addRelationship("element", artifact.getElement());
		addRelationship("type", artifact.getType());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		super.visit(artifact);
		addRelationships("operation", artifact.getOperation());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		super.visit(artifact);
		addRelationship("input", artifact.getInput());
		addRelationship("output", artifact.getOutput());
		addRelationships("fault", artifact.getFault());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		super.visit(artifact);
		addRelationship("message", artifact.getMessage());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		super.visit(artifact);
		addRelationship("message", artifact.getMessage());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		super.visit(artifact);
		addRelationship("message", artifact.getMessage());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		super.visit(artifact);
		addRelationships("bindingOperation", artifact.getBindingOperation());
		addRelationship("portType", artifact.getPortType());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		super.visit(artifact);
		addRelationship("input", artifact.getInput());
		addRelationship("output", artifact.getOutput());
		addRelationships("fault", artifact.getFault());
		addRelationship("operation", artifact.getOperation());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {
		super.visit(artifact);
		addRelationships("port", artifact.getPort());
	}

	/**
	 * @see org.overlord.sramp.visitors.HierarchicalArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.Port)
	 */
	@Override
	public void visit(Port artifact) {
		super.visit(artifact);
		addRelationship("binding", artifact.getBinding());
	}

	/**
	 * Adds a relationship to the details.
	 * @param relationshipType
	 * @param target
	 */
	protected void addRelationship(String relationshipType, Target target) {
		if (target != null) {
			RelationshipDetails rd = new RelationshipDetails(relationshipType);
			rd.setTargetUuid(target.getValue());
			rd.setHref(target.getHref());
			this.details.addRelationship(rd);
		}
	}

	/**
	 * Adds a collection of relationships to the details.
	 * @param relationshipName
	 * @param targets
	 */
	protected void addRelationships(String relationshipName, Collection<? extends Target> targets) {
		for (Target target : targets) {
			addRelationship(relationshipName, target);
		}
	}

}
