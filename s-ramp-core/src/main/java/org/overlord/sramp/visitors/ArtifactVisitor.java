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
package org.overlord.sramp.visitors;

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
import org.s_ramp.xmlns._2010.s_ramp.Document;
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
import org.s_ramp.xmlns._2010.s_ramp.Task;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.WsdlExtension;
import org.s_ramp.xmlns._2010.s_ramp.WsdlService;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * An interface for visiting an S-RAMP artifact.  This interface contains a visit method
 * for each type of artifact defined by S-RAMP.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ArtifactVisitor {

	// Core
	public void visit(Document artifact);
	public void visit(XmlDocument artifact);

	// XSD
	public void visit(XsdDocument artifact);
	public void visit(AttributeDeclaration artifact);
	public void visit(ElementDeclaration artifact);
	public void visit(SimpleTypeDeclaration artifact);
	public void visit(ComplexTypeDeclaration artifact);

	// Policy
	public void visit(PolicyDocument artifact);
	public void visit(PolicyExpression artifact);
	public void visit(PolicyAttachment artifact);

	// SOAP WSDL
	public void visit(SoapAddress artifact);
	public void visit(SoapBinding artifact);

	// WSDL
	public void visit(WsdlDocument artifact);
	public void visit(WsdlService artifact);
	public void visit(Port artifact);
	public void visit(WsdlExtension artifact);
	public void visit(Part artifact);
	public void visit(Message artifact);
	public void visit(Fault artifact);
	public void visit(PortType artifact);
	public void visit(Operation artifact);
	public void visit(OperationInput artifact);
	public void visit(OperationOutput artifact);
	public void visit(Binding artifact);
	public void visit(BindingOperation artifact);
	public void visit(BindingOperationInput artifact);
	public void visit(BindingOperationOutput artifact);
	public void visit(BindingOperationFault artifact);

	// Service Implementation
	public void visit(Organization artifact);
	public void visit(ServiceEndpoint artifact);
	public void visit(ServiceInstance artifact);
	public void visit(ServiceOperation artifact);

	// User Defined
	public void visit(UserDefinedArtifactType artifact);

	// SOA
	public void visit(Actor artifact);
	public void visit(Choreography artifact);
	public void visit(ChoreographyProcess artifact);
	public void visit(Collaboration artifact);
	public void visit(CollaborationProcess artifact);
	public void visit(Composition artifact);
	public void visit(Effect artifact);
	public void visit(Element artifact);
	public void visit(Event artifact);
	public void visit(InformationType artifact);
	public void visit(Orchestration artifact);
	public void visit(OrchestrationProcess artifact);
	public void visit(Policy artifact);
	public void visit(PolicySubject artifact);
	public void visit(org.s_ramp.xmlns._2010.s_ramp.Process artifact);
	public void visit(Service artifact);
	public void visit(ServiceContract artifact);
	public void visit(ServiceComposition artifact);
	public void visit(ServiceInterface artifact);
	public void visit(org.s_ramp.xmlns._2010.s_ramp.System artifact);
	public void visit(Task artifact);
}
