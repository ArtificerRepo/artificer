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
package org.artificer.common.visitors;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Task;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtension;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

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

	// Extended
	public void visit(ExtendedArtifactType artifact);
    public void visit(ExtendedDocument artifact);

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
	public void visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process artifact);
	public void visit(Service artifact);
	public void visit(ServiceContract artifact);
	public void visit(ServiceComposition artifact);
	public void visit(ServiceInterface artifact);
	public void visit(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System artifact);
	public void visit(Task artifact);
}
