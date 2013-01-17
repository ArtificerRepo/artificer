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
package org.overlord.sramp.common;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

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
 * An enum representing all of the Artifact Types defined by S-RAMP.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ArtifactTypeEnum {

	// Core
	Document("core", "Document", Document.class, BaseArtifactEnum.DOCUMENT, false),
	XmlDocument("core", "XML Document", XmlDocument.class, BaseArtifactEnum.XML_DOCUMENT, false),
	// XSD
	XsdDocument("xsd", "XML Schema", XsdDocument.class, BaseArtifactEnum.XSD_DOCUMENT, false),
	AttributeDeclaration("xsd", "XML Schema Attribute Declaration", AttributeDeclaration.class, BaseArtifactEnum.ATTRIBUTE_DECLARATION, true),
	ElementDeclaration("xsd", "XML Schema Element Declaration", ElementDeclaration.class, BaseArtifactEnum.ELEMENT_DECLARATION, true),
	SimpleTypeDeclaration("xsd", "XML Schema Simple Type Declaration", SimpleTypeDeclaration.class, BaseArtifactEnum.SIMPLE_TYPE_DECLARATION, true),
	ComplexTypeDeclaration("xsd", "XML Schema Complex Type Declaration", ComplexTypeDeclaration.class, BaseArtifactEnum.COMPLEX_TYPE_DECLARATION, true),
	// Policy
	PolicyDocument("policy", "Policy", PolicyDocument.class, BaseArtifactEnum.POLICY_DOCUMENT, false),
	PolicyExpression("policy", "Policy Expression", PolicyExpression.class, BaseArtifactEnum.POLICY_EXPRESSION, true),
	PolicyAttachment("policy", "Policy Attachment", PolicyAttachment.class, BaseArtifactEnum.POLICY_ATTACHMENT, true),
	// SOAP
	SoapAddress("soapWsdl", "SOAP Address", SoapAddress.class, BaseArtifactEnum.SOAP_ADDRESS, true),
	SoapBinding("soapWsdl", "SOAP Binding", SoapBinding.class, BaseArtifactEnum.SOAP_BINDING, true),
	// WSDL
	WsdlDocument("wsdl", "WSDL", WsdlDocument.class, BaseArtifactEnum.WSDL_DOCUMENT, false),
	WsdlService("wsdl", "WSDL Service", WsdlService.class, BaseArtifactEnum.WSDL_SERVICE, true),
	Port("wsdl", "WSDL Port", Port.class, BaseArtifactEnum.PORT, true),
	WsdlExtension("wsdl", "WSDL Extension", WsdlExtension.class, BaseArtifactEnum.WSDL_EXTENSION, true),
	Part("wsdl", "WSDL Part", Part.class, BaseArtifactEnum.PART, true),
	Message("wsdl", "WSDL Message", Message.class, BaseArtifactEnum.MESSAGE, true),
	Fault("wsdl", "WSDL Fault", Fault.class, BaseArtifactEnum.FAULT, true),
	PortType("wsdl", "WSDL Port Type", PortType.class, BaseArtifactEnum.PORT_TYPE, true),
	Operation("wsdl", "WSDL Operation", Operation.class, BaseArtifactEnum.OPERATION, true),
	OperationInput("wsdl", "WSDL Operation Input", OperationInput.class, BaseArtifactEnum.OPERATION_INPUT, true),
	OperationOutput("wsdl", "WSDL Operation Output", OperationOutput.class, BaseArtifactEnum.OPERATION_OUTPUT, true),
	Binding("wsdl", "WSDL Binding", Binding.class, BaseArtifactEnum.BINDING, true),
	BindingOperation("wsdl", "WSDL Binding Operation", BindingOperation.class, BaseArtifactEnum.BINDING_OPERATION, true),
	BindingOperationInput("wsdl", "WSDL Binding Operation Input", BindingOperationInput.class, BaseArtifactEnum.BINDING_OPERATION_INPUT, true),
	BindingOperationOutput("wsdl", "WSDL Binding Operation Output", BindingOperationOutput.class, BaseArtifactEnum.BINDING_OPERATION_OUTPUT, true),
	BindingOperationFault("wsdl", "WSDL Binding Operation Fault", BindingOperationFault.class, BaseArtifactEnum.BINDING_OPERATION_FAULT, true),
	// Service Implementation
	Organization("serviceImplementation", "Organization", Organization.class, BaseArtifactEnum.ORGANIZATION, true),
	ServiceEndpoint("serviceImplementation", "Service Endpoint", ServiceEndpoint.class, BaseArtifactEnum.SERVICE_ENDPOINT, true),
	ServiceInstance("serviceImplementation", "Service Instance", ServiceInstance.class, BaseArtifactEnum.SERVICE_INSTANCE, true),
	ServiceOperation("serviceImplementation", "Service Operation", ServiceOperation.class, BaseArtifactEnum.SERVICE_OPERATION, true),
	// User Defined
	UserDefinedArtifactType("user", "User Defined", UserDefinedArtifactType.class, BaseArtifactEnum.USER_DEFINED_ARTIFACT_TYPE, false), // TODO how are user defined types contributed/registered?
	// SOA
	HumanActor("soa", "SOA Human Actor", Actor.class, BaseArtifactEnum.ACTOR, true),
	Choreography("soa", "SOA Choreography", Choreography.class, BaseArtifactEnum.CHOREOGRAPHY, true),
	ChoreographyProcess("soa", "SOA Choreography Process", ChoreographyProcess.class, BaseArtifactEnum.CHOREOGRAPHY_PROCESS, true),
	Collaboration("soa", "SOA Collaboration", Collaboration.class, BaseArtifactEnum.COLLABORATION, true),
	CollaborationProcess("soa", "SOA Collaboration Process", CollaborationProcess.class, BaseArtifactEnum.COLLABORATION_PROCESS, true),
	Composition("soa", "SOA Composition", Composition.class, BaseArtifactEnum.COMPOSITION, true),
	Effect("soa", "SOA Effect", Effect.class, BaseArtifactEnum.EFFECT, true),
	Element("soa", "SOA Element", Element.class, BaseArtifactEnum.ELEMENT, true),
	Event("soa", "SOA Event", Event.class, BaseArtifactEnum.EVENT, true),
	InformationType("soa", "SOA Information Type", InformationType.class, BaseArtifactEnum.INFORMATION_TYPE, true),
	Orchestration("soa", "SOA Orchestration", Orchestration.class, BaseArtifactEnum.ORCHESTRATION, true),
	OrchestrationProcess("soa", "SOA Orchestration Process", OrchestrationProcess.class, BaseArtifactEnum.ORCHESTRATION_PROCESS, true),
	Policy("soa", "SOA Policy", Policy.class, BaseArtifactEnum.POLICY, true),
	PolicySubject("soa", "SOA Policy Subject", PolicySubject.class, BaseArtifactEnum.POLICY_SUBJECT, true),
	Process("soa", "SOA Process", org.s_ramp.xmlns._2010.s_ramp.Process.class, BaseArtifactEnum.PROCESS, true),
	Service("soa", "SOA Service", Service.class, BaseArtifactEnum.SERVICE, true),
	ServiceContract("soa", "SOA Service Contract", ServiceContract.class, BaseArtifactEnum.SERVICE_CONTRACT, true),
	ServiceComposition("soa", "SOA Service Composition", ServiceComposition.class, BaseArtifactEnum.SERVICE_COMPOSITION, true),
	ServiceInterface("soa", "SOA Service Interface", ServiceInterface.class, BaseArtifactEnum.SERVICE_INTERFACE, true),
	System("soa", "SOA System", org.s_ramp.xmlns._2010.s_ramp.System.class, BaseArtifactEnum.SYSTEM, true),
	Task("soa", "SOA Task", Task.class, BaseArtifactEnum.TASK, true)
	;

	private final static Set<String> enumValueIndex = new HashSet<String>();
	static {
		ArtifactTypeEnum[] values = ArtifactTypeEnum.values();
		for (ArtifactTypeEnum value : values) {
			enumValueIndex.add(value.name());
		}
	}

	private final String model;
	private final String label;
	private final Class<? extends BaseArtifactType> typeClass;
    private final BaseArtifactEnum apiType;
    private final boolean derived;

	/**
	 * Constructor.
	 * @param model the S-RAMP Artifact Model that this Artifact Type is a part of
	 * @param label a human friendly label for the artifact type
	 * @param typeClass the class that implements this Artifact Type
	 * @param apiType the type from the s-ramp API
	 */
	private ArtifactTypeEnum(String model, String label, Class<? extends BaseArtifactType> typeClass,
			BaseArtifactEnum apiType, boolean derived) {
		this.model = model;
		this.label = label;
		this.typeClass = typeClass;
		this.apiType = apiType;
		this.derived = derived;
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
	 * Returns true if the artifact type is a derived type.
	 */
	public boolean isDerived() {
		return this.derived;
	}

	/**
	 * Figures out the type from the artifact instance.
	 * @param artifact
	 */
	public static ArtifactTypeEnum valueOf(BaseArtifactType artifact) {
		BaseArtifactEnum apiType = artifact.getArtifactType();
		if (apiType != null) {
			return valueOf(apiType);
		}
		ArtifactTypeEnum[] values = values();
		for (ArtifactTypeEnum artifactType : values) {
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
	public static ArtifactTypeEnum valueOf(BaseArtifactEnum apiType) {
		ArtifactTypeEnum[] values = values();
		for (ArtifactTypeEnum artifactType : values) {
			if (artifactType.getApiType() == apiType) {
				return artifactType;
			}
		}
		throw new RuntimeException("Could not determine Artifact Type from S-RAMP API type: " + apiType.value());
	}

	/**
	 * Returns true if the given string is one of the valid enum values.
	 * @param artifactType
	 * @return true if the string is a valid enum value
	 */
	public static boolean hasEnum(String artifactType) {
		return enumValueIndex.contains(artifactType);
	}

}
