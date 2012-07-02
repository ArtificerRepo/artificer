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
package org.overlord.sramp;

import org.s_ramp.xmlns._2010.s_ramp.Actor;
import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Binding;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperation;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationFault;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationInput;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.Choreography;
import org.s_ramp.xmlns._2010.s_ramp.ChoreographyProcess;
import org.s_ramp.xmlns._2010.s_ramp.Collaboration;
import org.s_ramp.xmlns._2010.s_ramp.CollaborationProcess;
import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Composition;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Effect;
import org.s_ramp.xmlns._2010.s_ramp.Element;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Event;
import org.s_ramp.xmlns._2010.s_ramp.Fault;
import org.s_ramp.xmlns._2010.s_ramp.InformationType;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.Operation;
import org.s_ramp.xmlns._2010.s_ramp.OperationInput;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.Orchestration;
import org.s_ramp.xmlns._2010.s_ramp.OrchestrationProcess;
import org.s_ramp.xmlns._2010.s_ramp.Organization;
import org.s_ramp.xmlns._2010.s_ramp.Part;
import org.s_ramp.xmlns._2010.s_ramp.Policy;
import org.s_ramp.xmlns._2010.s_ramp.PolicyAttachment;
import org.s_ramp.xmlns._2010.s_ramp.PolicyDocument;
import org.s_ramp.xmlns._2010.s_ramp.PolicyExpression;
import org.s_ramp.xmlns._2010.s_ramp.PolicySubject;
import org.s_ramp.xmlns._2010.s_ramp.Port;
import org.s_ramp.xmlns._2010.s_ramp.PortType;
import org.s_ramp.xmlns._2010.s_ramp.Process;
import org.s_ramp.xmlns._2010.s_ramp.Service;
import org.s_ramp.xmlns._2010.s_ramp.ServiceComposition;
import org.s_ramp.xmlns._2010.s_ramp.ServiceContract;
import org.s_ramp.xmlns._2010.s_ramp.ServiceEndpoint;
import org.s_ramp.xmlns._2010.s_ramp.ServiceInstance;
import org.s_ramp.xmlns._2010.s_ramp.ServiceInterface;
import org.s_ramp.xmlns._2010.s_ramp.ServiceOperation;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.SoapAddress;
import org.s_ramp.xmlns._2010.s_ramp.SoapBinding;
import org.s_ramp.xmlns._2010.s_ramp.System;
import org.s_ramp.xmlns._2010.s_ramp.Task;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.WsdlExtension;
import org.s_ramp.xmlns._2010.s_ramp.WsdlService;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * A base class for visitors that are only interested in implementing a subset
 * of the methods in the interface.
 * 
 * @author eric.wittmann@redhat.com
 */
public abstract class ArtifactVisitorAdapter implements ArtifactVisitor {

	/**
	 * Default constructor.
	 */
	public ArtifactVisitorAdapter() {
	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType)
	 */
	@Override
	public void visit(DocumentArtifactType artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.XmlDocument)
	 */
	@Override
	public void visit(XmlDocument artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration)
	 */
	@Override
	public void visit(AttributeDeclaration artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration)
	 */
	@Override
	public void visit(ElementDeclaration artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration)
	 */
	@Override
	public void visit(SimpleTypeDeclaration artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration)
	 */
	@Override
	public void visit(ComplexTypeDeclaration artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicyDocument)
	 */
	@Override
	public void visit(PolicyDocument artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicyExpression)
	 */
	@Override
	public void visit(PolicyExpression artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicyAttachment)
	 */
	@Override
	public void visit(PolicyAttachment artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.SoapAddress)
	 */
	@Override
	public void visit(SoapAddress artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.SoapBinding)
	 */
	@Override
	public void visit(SoapBinding artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlDocument)
	 */
	@Override
	public void visit(WsdlDocument artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlService)
	 */
	@Override
	public void visit(WsdlService artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Port)
	 */
	@Override
	public void visit(Port artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.WsdlExtension)
	 */
	@Override
	public void visit(WsdlExtension artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Part)
	 */
	@Override
	public void visit(Part artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Message)
	 */
	@Override
	public void visit(Message artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Fault)
	 */
	@Override
	public void visit(Fault artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PortType)
	 */
	@Override
	public void visit(PortType artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Operation)
	 */
	@Override
	public void visit(Operation artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.OperationInput)
	 */
	@Override
	public void visit(OperationInput artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.OperationOutput)
	 */
	@Override
	public void visit(OperationOutput artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Binding)
	 */
	@Override
	public void visit(Binding artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperation)
	 */
	@Override
	public void visit(BindingOperation artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperationInput)
	 */
	@Override
	public void visit(BindingOperationInput artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperationOutput)
	 */
	@Override
	public void visit(BindingOperationOutput artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.BindingOperationFault)
	 */
	@Override
	public void visit(BindingOperationFault artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Organization)
	 */
	@Override
	public void visit(Organization artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceEndpoint)
	 */
	@Override
	public void visit(ServiceEndpoint artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceInstance)
	 */
	@Override
	public void visit(ServiceInstance artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceOperation)
	 */
	@Override
	public void visit(ServiceOperation artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType)
	 */
	@Override
	public void visit(UserDefinedArtifactType artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Actor)
	 */
	@Override
	public void visit(Actor artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Choreography)
	 */
	@Override
	public void visit(Choreography artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ChoreographyProcess)
	 */
	@Override
	public void visit(ChoreographyProcess artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Collaboration)
	 */
	@Override
	public void visit(Collaboration artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.CollaborationProcess)
	 */
	@Override
	public void visit(CollaborationProcess artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Composition)
	 */
	@Override
	public void visit(Composition artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Effect)
	 */
	@Override
	public void visit(Effect artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Element)
	 */
	@Override
	public void visit(Element artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Event)
	 */
	@Override
	public void visit(Event artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.InformationType)
	 */
	@Override
	public void visit(InformationType artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Orchestration)
	 */
	@Override
	public void visit(Orchestration artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.OrchestrationProcess)
	 */
	@Override
	public void visit(OrchestrationProcess artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Policy)
	 */
	@Override
	public void visit(Policy artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.PolicySubject)
	 */
	@Override
	public void visit(PolicySubject artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Process)
	 */
	@Override
	public void visit(Process artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Service)
	 */
	@Override
	public void visit(Service artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceContract)
	 */
	@Override
	public void visit(ServiceContract artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceComposition)
	 */
	@Override
	public void visit(ServiceComposition artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.ServiceInterface)
	 */
	@Override
	public void visit(ServiceInterface artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.System)
	 */
	@Override
	public void visit(System artifact) {

	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.Task)
	 */
	@Override
	public void visit(Task artifact) {

	}

}
