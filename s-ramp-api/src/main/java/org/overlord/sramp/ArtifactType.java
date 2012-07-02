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
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
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
 * An enum representing all of the Artifact Types defined by S-RAMP.
 * 
 * @author eric.wittmann@redhat.com
 */
public enum ArtifactType {

	// Core
	Document("core", "Document", DocumentArtifactType.class), 
	XmlDocument("core", "XML Document", XmlDocument.class),
	// XSD
	XsdDocument("xsd", "XML Schema", XsdDocument.class),
	AttributeDeclaration("xsd", "XML Schema Attribute Declaration", AttributeDeclaration.class),
	ElementDeclaration("xsd", "XML Schema Element Declaration", ElementDeclaration.class),
	SimpleTypeDeclaration("xsd", "XML Schema Simple Type Declaration", SimpleTypeDeclaration.class),
	ComplexTypeDeclaration("xsd", "XML Schema Complex Type Declaration", ComplexTypeDeclaration.class),
	// Policy
	PolicyDocument("policy", "Policy", PolicyDocument.class),
	PolicyExpression("policy", "Policy Expression", PolicyExpression.class),
	PolicyAttachment("policy", "Policy Attachment", PolicyAttachment.class),
	// SOAP
	SoapAddress("soapWsdl", "SOAP Address", SoapAddress.class),
	SoapBinding("soapWsdl", "SOAP Binding", SoapBinding.class),
	// WSDL
	WsdlDocument("wsdl", "WSDL", WsdlDocument.class),
	WsdlService("wsdl", "WSDL Service", WsdlService.class),
	Port("wsdl", "WSDL Port", Port.class),
	WsdlExtension("wsdl", "WSDL Extension", WsdlExtension.class),
	Part("wsdl", "WSDL Part", Part.class),
	Message("wsdl", "WSDL Message", Message.class),
	Fault("wsdl", "WSDL Fault", Fault.class),
	PortType("wsdl", "WSDL Port Type", PortType.class),
	Operation("wsdl", "WSDL Operation", Operation.class),
	OperationInput("wsdl", "WSDL Operation Input", OperationInput.class),
	OperationOutput("wsdl", "WSDL Operation Output", OperationOutput.class),
	Binding("wsdl", "WSDL Binding", Binding.class),
	BindingOperation("wsdl", "WSDL Binding Operation", BindingOperation.class),
	BindingOperationInput("wsdl", "WSDL Binding Operation Input", BindingOperationInput.class),
	BindingOperationOutput("wsdl", "WSDL Binding Operation Output", BindingOperationOutput.class),
	BindingOperationFault("wsdl", "WSDL Binding Operation Fault", BindingOperationFault.class),
	// Service Implementation
	Organization("serviceImplementation", "Organization", Organization.class),
	ServiceEndpoint("serviceImplementation", "Service Endpoint", ServiceEndpoint.class),
	ServiceInstance("serviceImplementation", "Service Instance", ServiceInstance.class),
	ServiceOperation("serviceImplementation", "Service Operation", ServiceOperation.class),
	// User Defined
	UserDefined("user", "User Defined", UserDefinedArtifactType.class), // TODO how are user defined types contributed/registered?
	// SOA
	HumanActor("soa", "SOA Human Actor", Actor.class),
	Choreography("soa", "SOA Choreography", Choreography.class),
	ChoreographyProcess("soa", "SOA Choreography Process", ChoreographyProcess.class),
	Collaboration("soa", "SOA Collaboration", Collaboration.class),
	CollaborationProcess("soa", "SOA Collaboration Process", CollaborationProcess.class),
	Composition("soa", "SOA Composition", Composition.class),
	Effect("soa", "SOA Effect", Effect.class),
	Element("soa", "SOA Element", Element.class),
	Event("soa", "SOA Event", Event.class),
	InformationType("soa", "SOA Information Type", InformationType.class),
	Orchestration("soa", "SOA Orchestration", Orchestration.class),
	OrchestrationProcess("soa", "SOA Orchestration Process", OrchestrationProcess.class),
	Policy("soa", "SOA Policy", Policy.class),
	PolicySubject("soa", "SOA Policy Subject", PolicySubject.class),
	Process("soa", "SOA Process", org.s_ramp.xmlns._2010.s_ramp.Process.class),
	Service("soa", "SOA Service", Service.class),
	ServiceContract("soa", "SOA Service Contract", ServiceContract.class),
	ServiceComposition("soa", "SOA Service Composition", ServiceComposition.class),
	ServiceInterface("soa", "SOA Service Interface", ServiceInterface.class),
	System("soa", "SOA System", org.s_ramp.xmlns._2010.s_ramp.System.class),
	Task("soa", "SOA Task", Task.class)
	;

	private final String model;
	private final String label;
	private final Class<? extends BaseArtifactType> typeClass;

	/**
	 * Constructor.
	 * @param model the S-RAMP Artifact Model that this Artifact Type is a part of
	 * @param label a human friendly label for the artifact type
	 * @param typeClass the class that implements this Artifact Type
	 */
	private ArtifactType(String model, String label, Class<? extends BaseArtifactType> typeClass) {
		this.model = model;
		this.label = label;
		this.typeClass = typeClass;
	}

	/**
	 * @return the model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the typeClass
	 */
	public Class<? extends BaseArtifactType> getTypeClass() {
		return typeClass;
	}
	
	/**
	 * Figures out the type from the artifact instance.
	 * @param artifact
	 */
	public static ArtifactType valueOf(BaseArtifactType artifact) {
		ArtifactType[] values = values();
		for (ArtifactType artifactType : values) {
			if (artifactType.getTypeClass().equals(artifact.getClass())) {
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from artifact class: " + artifact.getClass());
	}
	

}
