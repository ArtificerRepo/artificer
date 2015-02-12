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
package org.artificer.common;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Actor;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdType;
import org.artificer.common.i18n.Messages;

/**
 * An enum representing all of the Artifact Types defined by S-RAMP.
 *
 * @author eric.wittmann@redhat.com
 */
public enum ArtifactTypeEnum {

    // Core
    Document("core", "Document", Document.class, BaseArtifactEnum.DOCUMENT, false, true), //$NON-NLS-1$ //$NON-NLS-2$
    XmlDocument("core", "XML Document", XmlDocument.class, BaseArtifactEnum.XML_DOCUMENT, false, true), //$NON-NLS-1$ //$NON-NLS-2$
    // XSD
    XsdDocument("xsd", "XML Schema", XsdDocument.class, BaseArtifactEnum.XSD_DOCUMENT, false, true), //$NON-NLS-1$ //$NON-NLS-2$
    AttributeDeclaration("xsd", "XML Schema Attribute Declaration", AttributeDeclaration.class, BaseArtifactEnum.ATTRIBUTE_DECLARATION, true), //$NON-NLS-1$ //$NON-NLS-2$
    ElementDeclaration("xsd", "XML Schema Element Declaration", ElementDeclaration.class, BaseArtifactEnum.ELEMENT_DECLARATION, true), //$NON-NLS-1$ //$NON-NLS-2$
    SimpleTypeDeclaration("xsd", "XML Schema Simple Type Declaration", SimpleTypeDeclaration.class, BaseArtifactEnum.SIMPLE_TYPE_DECLARATION, true), //$NON-NLS-1$ //$NON-NLS-2$
    ComplexTypeDeclaration("xsd", "XML Schema Complex Type Declaration", ComplexTypeDeclaration.class, BaseArtifactEnum.COMPLEX_TYPE_DECLARATION, true), //$NON-NLS-1$ //$NON-NLS-2$
    XsdType("xsd", "XML Schema Type Declaration", XsdType.class, BaseArtifactEnum.XSD_TYPE, true), //$NON-NLS-1$ //$NON-NLS-2$
    // Policy
    PolicyDocument("policy", "Policy", PolicyDocument.class, BaseArtifactEnum.POLICY_DOCUMENT, false, true), //$NON-NLS-1$ //$NON-NLS-2$
    PolicyExpression("policy", "Policy Expression", PolicyExpression.class, BaseArtifactEnum.POLICY_EXPRESSION, true), //$NON-NLS-1$ //$NON-NLS-2$
    PolicyAttachment("policy", "Policy Attachment", PolicyAttachment.class, BaseArtifactEnum.POLICY_ATTACHMENT, true), //$NON-NLS-1$ //$NON-NLS-2$
    // SOAP
    SoapAddress("soapWsdl", "SOAP Address", SoapAddress.class, BaseArtifactEnum.SOAP_ADDRESS, true), //$NON-NLS-1$ //$NON-NLS-2$
    SoapBinding("soapWsdl", "SOAP Binding", SoapBinding.class, BaseArtifactEnum.SOAP_BINDING, true), //$NON-NLS-1$ //$NON-NLS-2$
    // WSDL
    WsdlDocument("wsdl", "WSDL", WsdlDocument.class, BaseArtifactEnum.WSDL_DOCUMENT, false, true), //$NON-NLS-1$ //$NON-NLS-2$
    WsdlService("wsdl", "WSDL Service", WsdlService.class, BaseArtifactEnum.WSDL_SERVICE, true), //$NON-NLS-1$ //$NON-NLS-2$
    Port("wsdl", "WSDL Port", Port.class, BaseArtifactEnum.PORT, true), //$NON-NLS-1$ //$NON-NLS-2$
    WsdlExtension("wsdl", "WSDL Extension", WsdlExtension.class, BaseArtifactEnum.WSDL_EXTENSION, true), //$NON-NLS-1$ //$NON-NLS-2$
    Part("wsdl", "WSDL Part", Part.class, BaseArtifactEnum.PART, true), //$NON-NLS-1$ //$NON-NLS-2$
    Message("wsdl", "WSDL Message", Message.class, BaseArtifactEnum.MESSAGE, true), //$NON-NLS-1$ //$NON-NLS-2$
    Fault("wsdl", "WSDL Fault", Fault.class, BaseArtifactEnum.FAULT, true), //$NON-NLS-1$ //$NON-NLS-2$
    PortType("wsdl", "WSDL Port Type", PortType.class, BaseArtifactEnum.PORT_TYPE, true), //$NON-NLS-1$ //$NON-NLS-2$
    Operation("wsdl", "WSDL Operation", Operation.class, BaseArtifactEnum.OPERATION, true), //$NON-NLS-1$ //$NON-NLS-2$
    OperationInput("wsdl", "WSDL Operation Input", OperationInput.class, BaseArtifactEnum.OPERATION_INPUT, true), //$NON-NLS-1$ //$NON-NLS-2$
    OperationOutput("wsdl", "WSDL Operation Output", OperationOutput.class, BaseArtifactEnum.OPERATION_OUTPUT, true), //$NON-NLS-1$ //$NON-NLS-2$
    Binding("wsdl", "WSDL Binding", Binding.class, BaseArtifactEnum.BINDING, true), //$NON-NLS-1$ //$NON-NLS-2$
    BindingOperation("wsdl", "WSDL Binding Operation", BindingOperation.class, BaseArtifactEnum.BINDING_OPERATION, true), //$NON-NLS-1$ //$NON-NLS-2$
    BindingOperationInput("wsdl", "WSDL Binding Operation Input", BindingOperationInput.class, BaseArtifactEnum.BINDING_OPERATION_INPUT, true), //$NON-NLS-1$ //$NON-NLS-2$
    BindingOperationOutput("wsdl", "WSDL Binding Operation Output", BindingOperationOutput.class, BaseArtifactEnum.BINDING_OPERATION_OUTPUT, true), //$NON-NLS-1$ //$NON-NLS-2$
    BindingOperationFault("wsdl", "WSDL Binding Operation Fault", BindingOperationFault.class, BaseArtifactEnum.BINDING_OPERATION_FAULT, true), //$NON-NLS-1$ //$NON-NLS-2$
    // Service Implementation
    ServiceEndpoint("serviceImplementation", "Service Endpoint", ServiceEndpoint.class, BaseArtifactEnum.SERVICE_ENDPOINT, false), //$NON-NLS-1$ //$NON-NLS-2$
    ServiceInstance("serviceImplementation", "Service Instance", ServiceInstance.class, BaseArtifactEnum.SERVICE_INSTANCE, false), //$NON-NLS-1$ //$NON-NLS-2$
    ServiceOperation("serviceImplementation", "Service Operation", ServiceOperation.class, BaseArtifactEnum.SERVICE_OPERATION, false), //$NON-NLS-1$ //$NON-NLS-2$
    // Extended
    ExtendedArtifactType("ext", "Extended Artifact Type", ExtendedArtifactType.class, BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE, false), //$NON-NLS-1$ //$NON-NLS-2$
    ExtendedDocument("ext", "Extended Document", ExtendedDocument.class, BaseArtifactEnum.EXTENDED_DOCUMENT, false, true), //$NON-NLS-1$ //$NON-NLS-2$
    // SOA
    Actor("soa", "SOA Actor", Actor.class, BaseArtifactEnum.ACTOR, false), //$NON-NLS-1$ //$NON-NLS-2$
    Choreography("soa", "SOA Choreography", Choreography.class, BaseArtifactEnum.CHOREOGRAPHY, false), //$NON-NLS-1$ //$NON-NLS-2$
    ChoreographyProcess("soa", "SOA Choreography Process", ChoreographyProcess.class, BaseArtifactEnum.CHOREOGRAPHY_PROCESS, false), //$NON-NLS-1$ //$NON-NLS-2$
    Collaboration("soa", "SOA Collaboration", Collaboration.class, BaseArtifactEnum.COLLABORATION, false), //$NON-NLS-1$ //$NON-NLS-2$
    CollaborationProcess("soa", "SOA Collaboration Process", CollaborationProcess.class, BaseArtifactEnum.COLLABORATION_PROCESS, false), //$NON-NLS-1$ //$NON-NLS-2$
    Composition("soa", "SOA Composition", Composition.class, BaseArtifactEnum.COMPOSITION, false), //$NON-NLS-1$ //$NON-NLS-2$
    Effect("soa", "SOA Effect", Effect.class, BaseArtifactEnum.EFFECT, false), //$NON-NLS-1$ //$NON-NLS-2$
    Element("soa", "SOA Element", Element.class, BaseArtifactEnum.ELEMENT, false), //$NON-NLS-1$ //$NON-NLS-2$
    Event("soa", "SOA Event", Event.class, BaseArtifactEnum.EVENT, false), //$NON-NLS-1$ //$NON-NLS-2$
    InformationType("soa", "SOA Information Type", InformationType.class, BaseArtifactEnum.INFORMATION_TYPE, false), //$NON-NLS-1$ //$NON-NLS-2$
    Orchestration("soa", "SOA Orchestration", Orchestration.class, BaseArtifactEnum.ORCHESTRATION, false), //$NON-NLS-1$ //$NON-NLS-2$
    OrchestrationProcess("soa", "SOA Orchestration Process", OrchestrationProcess.class, BaseArtifactEnum.ORCHESTRATION_PROCESS, false), //$NON-NLS-1$ //$NON-NLS-2$
    Organization("serviceImplementation", "SOA Organization", Organization.class, BaseArtifactEnum.ORGANIZATION, false), //$NON-NLS-1$ //$NON-NLS-2$
    Policy("soa", "SOA Policy", Policy.class, BaseArtifactEnum.POLICY, false), //$NON-NLS-1$ //$NON-NLS-2$
    PolicySubject("soa", "SOA Policy Subject", PolicySubject.class, BaseArtifactEnum.POLICY_SUBJECT, false), //$NON-NLS-1$ //$NON-NLS-2$
    Process("soa", "SOA Process", org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Process.class, BaseArtifactEnum.PROCESS, false), //$NON-NLS-1$ //$NON-NLS-2$
    Service("soa", "SOA Service", Service.class, BaseArtifactEnum.SERVICE, false), //$NON-NLS-1$ //$NON-NLS-2$
    ServiceContract("soa", "SOA Service Contract", ServiceContract.class, BaseArtifactEnum.SERVICE_CONTRACT, false), //$NON-NLS-1$ //$NON-NLS-2$
    ServiceComposition("soa", "SOA Service Composition", ServiceComposition.class, BaseArtifactEnum.SERVICE_COMPOSITION, false), //$NON-NLS-1$ //$NON-NLS-2$
    ServiceInterface("soa", "SOA Service Interface", ServiceInterface.class, BaseArtifactEnum.SERVICE_INTERFACE, false), //$NON-NLS-1$ //$NON-NLS-2$
    System("soa", "SOA System", org.oasis_open.docs.s_ramp.ns.s_ramp_v1.System.class, BaseArtifactEnum.SYSTEM, false), //$NON-NLS-1$ //$NON-NLS-2$
    Task("soa", "SOA Task", Task.class, BaseArtifactEnum.TASK, false) //$NON-NLS-1$ //$NON-NLS-2$
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
    private final boolean document;

    /**
     * Constructor.
     * @param model the S-RAMP Artifact Model that this Artifact Type is a part of
     * @param label a human friendly label for the artifact type
     * @param typeClass the class that implements this Artifact Type
     * @param apiType the type from the s-ramp API
     * @param derived true if the artifact type is a derived type
     * @param document true if the artifact type is a document type
     */
    private ArtifactTypeEnum(String model, String label, Class<? extends BaseArtifactType> typeClass,
            BaseArtifactEnum apiType, boolean derived, boolean document) {
        this.model = model;
        this.label = label;
        this.typeClass = typeClass;
        this.apiType = apiType;
        this.derived = derived;
        this.document = document;
    }

    /**
     * Constructor.
     * @param model the S-RAMP Artifact Model that this Artifact Type is a part of
     * @param label a human friendly label for the artifact type
     * @param typeClass the class that implements this Artifact Type
     * @param apiType the type from the s-ramp API
     * @param derived true if the artifact type is a derived type
     * @param derived
     */
    private ArtifactTypeEnum(String model, String label, Class<? extends BaseArtifactType> typeClass,
            BaseArtifactEnum apiType, boolean derived) {
        this(model, label, typeClass, apiType, derived, false);
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
            Method method = Artifact.class.getMethod("get" + this.name()); //$NON-NLS-1$
            return (BaseArtifactType) method.invoke(artifactWrapper);
        } catch (Exception e) {
            throw new RuntimeException(Messages.i18n.format("ARTIFACT_UNWRAP_ERROR", this.name()), e); //$NON-NLS-1$
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
     * @return
     */
    public boolean isDocument() {
        return this.document;
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
        throw new RuntimeException(Messages.i18n.format("ARTIFACT_TYPE_FROM_CLASS_ERROR", artifact.getClass())); //$NON-NLS-1$
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
        throw new RuntimeException(Messages.i18n.format("ARTIFACT_TYPE_FROM_APITYPE_ERROR", apiType.value())); //$NON-NLS-1$
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
