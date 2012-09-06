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

import java.lang.reflect.Method;

import org.s_ramp.xmlns._2010.s_ramp.Actor;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
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
	XmlDocument("core", "XML Document", XmlDocument.class, "application/xml", BaseArtifactEnum.XML_DOCUMENT),
	// XSD
	XsdDocument("xsd", "XML Schema", XsdDocument.class, "application/xml", BaseArtifactEnum.XSD_DOCUMENT),
	AttributeDeclaration("xsd", "XML Schema Attribute Declaration", AttributeDeclaration.class, null, BaseArtifactEnum.ATTRIBUTE_DECLARATION),
	ElementDeclaration("xsd", "XML Schema Element Declaration", ElementDeclaration.class, null, BaseArtifactEnum.ELEMENT_DECLARATION),
	SimpleTypeDeclaration("xsd", "XML Schema Simple Type Declaration", SimpleTypeDeclaration.class, null, BaseArtifactEnum.SIMPLE_TYPE_DECLARATION),
	ComplexTypeDeclaration("xsd", "XML Schema Complex Type Declaration", ComplexTypeDeclaration.class, null, BaseArtifactEnum.COMPLEX_TYPE_DECLARATION),
	// Policy
	PolicyDocument("policy", "Policy", PolicyDocument.class, "application/xml", BaseArtifactEnum.POLICY_DOCUMENT),
	PolicyExpression("policy", "Policy Expression", PolicyExpression.class, null, BaseArtifactEnum.POLICY_EXPRESSION),
	PolicyAttachment("policy", "Policy Attachment", PolicyAttachment.class, null, BaseArtifactEnum.POLICY_ATTACHMENT),
	// SOAP
	SoapAddress("soapWsdl", "SOAP Address", SoapAddress.class, null, BaseArtifactEnum.SOAP_ADDRESS),
	SoapBinding("soapWsdl", "SOAP Binding", SoapBinding.class, null, BaseArtifactEnum.SOAP_BINDING),
	// WSDL
	WsdlDocument("wsdl", "WSDL", WsdlDocument.class, "application/xml", BaseArtifactEnum.WSDL_DOCUMENT),
	WsdlService("wsdl", "WSDL Service", WsdlService.class, null, BaseArtifactEnum.WSDL_SERVICE),
	Port("wsdl", "WSDL Port", Port.class, null, BaseArtifactEnum.PORT),
	WsdlExtension("wsdl", "WSDL Extension", WsdlExtension.class, null, BaseArtifactEnum.WSDL_EXTENSION),
	Part("wsdl", "WSDL Part", Part.class, null, BaseArtifactEnum.PART),
	Message("wsdl", "WSDL Message", Message.class, null, BaseArtifactEnum.MESSAGE),
	Fault("wsdl", "WSDL Fault", Fault.class, null, BaseArtifactEnum.FAULT),
	PortType("wsdl", "WSDL Port Type", PortType.class, null, BaseArtifactEnum.PORT_TYPE),
	Operation("wsdl", "WSDL Operation", Operation.class, null, BaseArtifactEnum.OPERATION),
	OperationInput("wsdl", "WSDL Operation Input", OperationInput.class, null, BaseArtifactEnum.OPERATION_INPUT),
	OperationOutput("wsdl", "WSDL Operation Output", OperationOutput.class, null, BaseArtifactEnum.OPERATION_OUTPUT),
	Binding("wsdl", "WSDL Binding", Binding.class, null, BaseArtifactEnum.BINDING),
	BindingOperation("wsdl", "WSDL Binding Operation", BindingOperation.class, null, BaseArtifactEnum.BINDING_OPERATION),
	BindingOperationInput("wsdl", "WSDL Binding Operation Input", BindingOperationInput.class, null, BaseArtifactEnum.BINDING_OPERATION_INPUT),
	BindingOperationOutput("wsdl", "WSDL Binding Operation Output", BindingOperationOutput.class, null, BaseArtifactEnum.BINDING_OPERATION_OUTPUT),
	BindingOperationFault("wsdl", "WSDL Binding Operation Fault", BindingOperationFault.class, null, BaseArtifactEnum.BINDING_OPERATION_FAULT),
	// Service Implementation
	Organization("serviceImplementation", "Organization", Organization.class, null, BaseArtifactEnum.ORGANIZATION),
	ServiceEndpoint("serviceImplementation", "Service Endpoint", ServiceEndpoint.class, null, BaseArtifactEnum.SERVICE_ENDPOINT),
	ServiceInstance("serviceImplementation", "Service Instance", ServiceInstance.class, null, BaseArtifactEnum.SERVICE_INSTANCE),
	ServiceOperation("serviceImplementation", "Service Operation", ServiceOperation.class, null, BaseArtifactEnum.SERVICE_OPERATION),
	// User Defined
	UserDefined("user", "User Defined", UserDefinedArtifactType.class, null, BaseArtifactEnum.USER_DEFINED_ARTIFACT_TYPE), // TODO how are user defined types contributed/registered?
	// SOA
	HumanActor("soa", "SOA Human Actor", Actor.class, null, BaseArtifactEnum.ACTOR),
	Choreography("soa", "SOA Choreography", Choreography.class, null, BaseArtifactEnum.CHOREOGRAPHY),
	ChoreographyProcess("soa", "SOA Choreography Process", ChoreographyProcess.class, null, BaseArtifactEnum.CHOREOGRAPHY_PROCESS),
	Collaboration("soa", "SOA Collaboration", Collaboration.class, null, BaseArtifactEnum.COLLABORATION),
	CollaborationProcess("soa", "SOA Collaboration Process", CollaborationProcess.class, null, BaseArtifactEnum.COLLABORATION_PROCESS),
	Composition("soa", "SOA Composition", Composition.class, null, BaseArtifactEnum.COMPOSITION),
	Effect("soa", "SOA Effect", Effect.class, null, BaseArtifactEnum.EFFECT),
	Element("soa", "SOA Element", Element.class, null, BaseArtifactEnum.ELEMENT),
	Event("soa", "SOA Event", Event.class, null, BaseArtifactEnum.EVENT),
	InformationType("soa", "SOA Information Type", InformationType.class, null, BaseArtifactEnum.INFORMATION_TYPE),
	Orchestration("soa", "SOA Orchestration", Orchestration.class, null, BaseArtifactEnum.ORCHESTRATION),
	OrchestrationProcess("soa", "SOA Orchestration Process", OrchestrationProcess.class, null, BaseArtifactEnum.ORCHESTRATION_PROCESS),
	Policy("soa", "SOA Policy", Policy.class, null, BaseArtifactEnum.POLICY),
	PolicySubject("soa", "SOA Policy Subject", PolicySubject.class, null, BaseArtifactEnum.POLICY_SUBJECT),
	Process("soa", "SOA Process", org.s_ramp.xmlns._2010.s_ramp.Process.class, null, BaseArtifactEnum.PROCESS),
	Service("soa", "SOA Service", Service.class, null, BaseArtifactEnum.SERVICE),
	ServiceContract("soa", "SOA Service Contract", ServiceContract.class, null, BaseArtifactEnum.SERVICE_CONTRACT),
	ServiceComposition("soa", "SOA Service Composition", ServiceComposition.class, null, BaseArtifactEnum.SERVICE_COMPOSITION),
	ServiceInterface("soa", "SOA Service Interface", ServiceInterface.class, null, BaseArtifactEnum.SERVICE_INTERFACE),
	System("soa", "SOA System", org.s_ramp.xmlns._2010.s_ramp.System.class, null, BaseArtifactEnum.SYSTEM),
	Task("soa", "SOA Task", Task.class, null, BaseArtifactEnum.TASK)
	;

	private final String model;
	private final String label;
	private final Class<? extends BaseArtifactType> typeClass;
    private final String mimeType;
    private final BaseArtifactEnum apiType;

	/**
	 * Constructor.
	 * @param model the S-RAMP Artifact Model that this Artifact Type is a part of
	 * @param label a human friendly label for the artifact type
	 * @param typeClass the class that implements this Artifact Type
	 * @param mimeType the mime-type of the artifact
	 */
	private ArtifactType(String model, String label, Class<? extends BaseArtifactType> typeClass,
			String mimeType, BaseArtifactEnum apiType) {
		this.model = model;
		this.label = label;
		this.typeClass = typeClass;
		this.mimeType = mimeType;
		this.apiType = apiType;
	}

	/**
	 * @return the artifact model
	 */
	public String getModel() {
		return model;
	}

	/**
	 * @return the artifact type
	 */
	public String getType() {
		return name();
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @return the mimeType
	 */
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * @return the s-ramp API type
	 */
	public BaseArtifactEnum getApiType() {
		return apiType;
	}

	/**
	 * Called to unwrap the S-RAMP artifact from its wrapper.
	 * @param artifactWrapper the S-RAMP artifact wrapper
	 * @return the specific artifact based on type
	 */
	public BaseArtifactType unwrap(Artifact artifactWrapper) {
		try {
			Method method = Artifact.class.getMethod("get" + this.name());
			return (BaseArtifactType) method.invoke(artifactWrapper);
		} catch (Exception e) {
			throw new RuntimeException("Failed to unwrap artifact for type: " + this.name(), e);
		}
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
		BaseArtifactEnum apiType = artifact.getArtifactType();
		if (apiType != null) {
			return valueOf(apiType);
		}
		ArtifactType[] values = values();
		for (ArtifactType artifactType : values) {
			if (artifactType.getTypeClass().equals(artifact.getClass())) {
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from artifact class: " + artifact.getClass());
	}

	/**
	 * Figures out the type from the s-ramp API type.
	 * @param apiType
	 */
	public static ArtifactType valueOf(BaseArtifactEnum apiType) {
		ArtifactType[] values = values();
		for (ArtifactType artifactType : values) {
			if (artifactType.getApiType() == apiType) {
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from S-RAMP API type: " + apiType.value());
	}

}
