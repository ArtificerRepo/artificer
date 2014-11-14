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
package org.overlord.sramp.common.visitors;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Choreography;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ChoreographyProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Collaboration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.CollaborationProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Composition;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Effect;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Event;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Orchestration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcess;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyExpression;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubject;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceComposition;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtension;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

/**
 * A base class for visitors that are only interested in implementing a subset
 * of the methods in the interface.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractArtifactVisitor implements ArtifactVisitor {

	/**
	 * Default constructor.
	 */
	public AbstractArtifactVisitor() {
	}

	/**
	 * Common visit method for all artifacts.
	 * @param artifact
	 */
	protected void visitBase(BaseArtifactType artifact) {
		// Subclasses can do common visit logic here
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document)
	 */
	@Override
	public void visit(Document artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument)
	 */
	@Override
	public void visit(XmlDocument artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyDocument)
	 */
	@Override
	public void visit(PolicyDocument artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyExpression)
	 */
	@Override
	public void visit(PolicyExpression artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicyAttachment)
	 */
	@Override
	public void visit(PolicyAttachment artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress)
	 */
	@Override
	public void visit(SoapAddress artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding)
	 */
	@Override
	public void visit(SoapBinding artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port)
	 */
	@Override
	public void visit(Port artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtension)
	 */
	@Override
	public void visit(WsdlExtension artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part)
	 */
	@Override
	public void visit(Part artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message)
	 */
	@Override
	public void visit(Message artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault)
	 */
	@Override
	public void visit(Fault artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType)
	 */
	@Override
	public void visit(PortType artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation)
	 */
	@Override
	public void visit(Operation artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding)
	 */
	@Override
	public void visit(Binding artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput)
	 */
	@Override
	public void visit(BindingOperationInput artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput)
	 */
	@Override
	public void visit(BindingOperationOutput artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFault)
	 */
	@Override
	public void visit(BindingOperationFault artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization)
	 */
	@Override
	public void visit(Organization artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint)
	 */
	@Override
	public void visit(ServiceEndpoint artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInstance)
	 */
	@Override
	public void visit(ServiceInstance artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceOperation)
	 */
	@Override
	public void visit(ServiceOperation artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
	 */
	@Override
	public void visit(ExtendedArtifactType artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument)
	 */
	@Override
	public void visit(ExtendedDocument artifact) {
        visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor)
	 */
	@Override
	public void visit(Actor artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Choreography)
	 */
	@Override
	public void visit(Choreography artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ChoreographyProcess)
	 */
	@Override
	public void visit(ChoreographyProcess artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Collaboration)
	 */
	@Override
	public void visit(Collaboration artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.CollaborationProcess)
	 */
	@Override
	public void visit(CollaborationProcess artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Composition)
	 */
	@Override
	public void visit(Composition artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Effect)
	 */
	@Override
	public void visit(Effect artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Element)
	 */
	@Override
	public void visit(Element artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Event)
	 */
	@Override
	public void visit(Event artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.InformationType)
	 */
	@Override
	public void visit(InformationType artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Orchestration)
	 */
	@Override
	public void visit(Orchestration artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OrchestrationProcess)
	 */
	@Override
	public void visit(OrchestrationProcess artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Policy)
	 */
	@Override
	public void visit(Policy artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PolicySubject)
	 */
	@Override
	public void visit(PolicySubject artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process)
	 */
	@Override
	public void visit(Process artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Service)
	 */
	@Override
	public void visit(Service artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceContract)
	 */
	@Override
	public void visit(ServiceContract artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceComposition)
	 */
	@Override
	public void visit(ServiceComposition artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceInterface)
	 */
	@Override
	public void visit(ServiceInterface artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System)
	 */
	@Override
	public void visit(System artifact) {
		visitBase(artifact);
	}

	/**
	 * @see org.overlord.sramp.common.visitors.ArtifactVisitor#visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task)
	 */
	@Override
	public void visit(Task artifact) {
		visitBase(artifact);
	}

}
