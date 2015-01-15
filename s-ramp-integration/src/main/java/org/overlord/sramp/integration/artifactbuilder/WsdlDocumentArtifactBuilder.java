/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.integration.artifactbuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Brett Meyer
 */
public class WsdlDocumentArtifactBuilder extends XsdDocumentArtifactBuilder {
    
   private String targetNS;

    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);

        namespaceContext.addMapping("wsdl", "http://schemas.xmlsoap.org/wsdl/"); //$NON-NLS-1$ //$NON-NLS-2$
        namespaceContext.addMapping("soap", "http://schemas.xmlsoap.org/wsdl/soap/"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    protected void derive() throws IOException {
        targetNS = rootElement.getAttribute("targetNamespace"); //$NON-NLS-1$
        
        if (getPrimaryArtifact() instanceof WsdlDocument) {
            ((WsdlDocument) getPrimaryArtifact()).setTargetNamespace(targetNS);
        }
        
        try {
            // Get derived content from all of the schemas embedded in this WSDL
            NodeList schemas = (NodeList) this.query(rootElement, "./wsdl:types/xsd:schema", XPathConstants.NODESET); //$NON-NLS-1$
            for (int idx = 0; idx < schemas.getLength(); idx++) {
                Element schema = (Element) schemas.item(idx);
                
                deriveXsd(schema);
                String xsdTargetNS = schema.getAttribute("targetNamespace");
                processImportedXsds(((WsdlDocument) getPrimaryArtifact()).getImportedXsds(), schema, xsdTargetNS);
                processIncludedXsds(((WsdlDocument) getPrimaryArtifact()).getIncludedXsds(), schema, xsdTargetNS);
                processRedefinedXsds(((WsdlDocument) getPrimaryArtifact()).getRedefinedXsds(), schema, xsdTargetNS);
            }
            
            processWsdlImports();

            processMessages();
            processPortTypes();
            processBindings();
            processServices();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void processMessages() throws XPathExpressionException {
        // Get all the WSDL messages and add them (and their parts) to the list
        NodeList messages = (NodeList) this.query(rootElement, "./wsdl:message", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < messages.getLength(); idx++) {
            Element messageElem = (Element) messages.item(idx);
            if (messageElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = messageElem.getAttribute("name"); //$NON-NLS-1$
                Message message = new Message();
                message.setUuid(UUID.randomUUID().toString());
                message.setArtifactType(BaseArtifactEnum.MESSAGE);
                message.setName(name);
                message.setNamespace(targetNS);
                message.setNCName(name);
                derivedArtifacts.add(message);

                Collection<Part> parts = processParts(messageElem);
                for (Part part : parts) {
                    PartTarget target = new PartTarget();
                    target.setValue(part.getUuid());
                    target.setArtifactType(PartEnum.PART);
                    message.getPart().add(target);
                }
            }
        }
    }

    private Collection<Part> processParts(Element messageElem) throws XPathExpressionException {
        Collection<Part> rval = new LinkedList<Part>();

        // Get all the parts and add them to the list
        NodeList parts = (NodeList) this.query(messageElem, "./wsdl:part", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < parts.getLength(); idx++) {
            Element partElem = (Element) parts.item(idx);
            if (partElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = partElem.getAttribute("name"); //$NON-NLS-1$
                Part part = new Part();
                part.setUuid(UUID.randomUUID().toString());
                part.setArtifactType(BaseArtifactEnum.PART);
                part.setName(name);
                part.setNamespace(targetNS);
                part.setNCName(name);
                derivedArtifacts.add(part);
                rval.add(part);

                if (partElem.hasAttribute("element")) { //$NON-NLS-1$
                    String encodedQName = partElem.getAttribute("element"); //$NON-NLS-1$
                    QName qname = resolveQName(partElem, targetNS, encodedQName);
                    ElementDeclaration elementRef = derivedArtifacts.lookupElement(qname);
                    ElementDeclarationTarget elementTarget = new ElementDeclarationTarget();
                    elementTarget.setArtifactType(ElementDeclarationEnum.ELEMENT_DECLARATION);
                    if (elementRef != null) {
                        elementTarget.setValue(elementRef.getUuid());
                    } else {
                        relationshipSources.add(new QNameRelationshipSource(qname, elementTarget, null,
                                ArtifactTypeEnum.ElementDeclaration.getModel(), ArtifactTypeEnum.ElementDeclaration.getType()));
                    }
                    part.setElement(elementTarget);
                } else if (partElem.hasAttribute("type")) { //$NON-NLS-1$
                    String encodedQName = partElem.getAttribute("type"); //$NON-NLS-1$
                    QName qname = resolveQName(partElem, targetNS, encodedQName);
                    XsdType typeRef = derivedArtifacts.lookupType(qname);
                    XsdTypeTarget typeTarget = new XsdTypeTarget();
                    typeTarget.setArtifactType(XsdTypeEnum.XSD_TYPE);
                    if (typeRef != null) {
                        typeTarget.setValue(typeRef.getUuid());
                    } else {
                        relationshipSources.add(new QNameRelationshipSource(qname, typeTarget, null,
                                ArtifactTypeEnum.XsdType.getModel(),
                                ArtifactTypeEnum.SimpleTypeDeclaration.getType(), ArtifactTypeEnum.ComplexTypeDeclaration.getType()));
                    }
                    part.setType(typeTarget);
                }
            }
        }
        return rval;
    }

    private void processPortTypes() throws XPathExpressionException {
        // Get all the port types and add them to the list
        NodeList portTypes = (NodeList) this.query(rootElement, "./wsdl:portType", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < portTypes.getLength(); idx++) {
            Element portTypeElem = (Element) portTypes.item(idx);
            if (portTypeElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = portTypeElem.getAttribute("name"); //$NON-NLS-1$
                PortType portType = new PortType();
                portType.setUuid(UUID.randomUUID().toString());
                portType.setArtifactType(BaseArtifactEnum.PORT_TYPE);
                portType.setName(name);
                portType.setNamespace(targetNS);
                portType.setNCName(name);
                derivedArtifacts.add(portType);

                Collection<Operation> operations = processOperations(portTypeElem);
                for (Operation operation : operations) {
                    OperationTarget target = new OperationTarget();
                    target.setValue(operation.getUuid());
                    target.setArtifactType(OperationEnum.OPERATION);
                    portType.getOperation().add(target);
                }
            }
        }
    }

    private Collection<Operation> processOperations(Element portTypeElem) throws XPathExpressionException {
        Collection<Operation> rval = new LinkedList<Operation>();

        // Get all the operations and add them to the list
        NodeList operations = (NodeList) this.query(portTypeElem, "./wsdl:operation", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < operations.getLength(); idx++) {
            Element operationElem = (Element) operations.item(idx);
            if (operationElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = operationElem.getAttribute("name"); //$NON-NLS-1$
                Operation operation = new Operation();
                operation.setUuid(UUID.randomUUID().toString());
                operation.setArtifactType(BaseArtifactEnum.OPERATION);
                operation.setName(name);
                operation.setNamespace(targetNS);
                operation.setNCName(name);
                derivedArtifacts.add(operation);
                rval.add(operation);

                OperationInput operationInput = processOperationInput(operationElem);
                OperationOutput operationOutput = processOperationOutput(operationElem);
                Collection<Fault> operationFaults = processOperationFaults(operationElem);

                if (operationInput != null) {
                    OperationInputTarget target = new OperationInputTarget();
                    target.setValue(operationInput.getUuid());
                    target.setArtifactType(OperationInputEnum.OPERATION_INPUT);
                    operation.setInput(target);
                }
                if (operationOutput != null) {
                    OperationOutputTarget target = new OperationOutputTarget();
                    target.setValue(operationOutput.getUuid());
                    target.setArtifactType(OperationOutputEnum.OPERATION_OUTPUT);
                    operation.setOutput(target);
                }
                if (operationFaults != null) {
                    for (Fault fault : operationFaults) {
                        FaultTarget target = new FaultTarget();
                        target.setValue(fault.getUuid());
                        target.setArtifactType(FaultEnum.FAULT);
                        operation.getFault().add(target);
                    }
                }
            }
        }
        return rval;
    }

    private OperationInput processOperationInput(Element operationElem) throws XPathExpressionException {
        OperationInput rval = null;

        Element inputElem = (Element) this.query(operationElem, "./wsdl:input", XPathConstants.NODE); //$NON-NLS-1$
        if (inputElem != null) {
            OperationInput input = new OperationInput();
            input.setUuid(UUID.randomUUID().toString());
            input.setArtifactType(BaseArtifactEnum.OPERATION_INPUT);
            String name = null;
            derivedArtifacts.add(input);

            if (inputElem.hasAttribute("message")) { //$NON-NLS-1$
                String encodedMsgQname = inputElem.getAttribute("message"); //$NON-NLS-1$
                QName msgQname = resolveQName(inputElem, targetNS, encodedMsgQname);
                name = msgQname.getLocalPart();
                Message message = derivedArtifacts.lookupMessage(msgQname);
                MessageTarget target = new MessageTarget();
                target.setArtifactType(MessageEnum.MESSAGE);
                if (message != null) {
                    target.setValue(message.getUuid());
                } else {
                    relationshipSources.add(new QNameRelationshipSource(msgQname, target, null,
                            ArtifactTypeEnum.Message.getModel(), ArtifactTypeEnum.Message.getType()));
                }
                input.setMessage(target);
            }
            if (inputElem.hasAttribute("name")) { //$NON-NLS-1$
                name = inputElem.getAttribute("name"); //$NON-NLS-1$
                input.setNCName(name);
            }

            input.setName(name);
            input.setNamespace(targetNS);

            rval = input;
        }
        return rval;
    }

    private OperationOutput processOperationOutput(Element operationElem) throws XPathExpressionException {
        OperationOutput rval = null;

        Element outputElem = (Element) this.query(operationElem, "./wsdl:output", XPathConstants.NODE); //$NON-NLS-1$
        if (outputElem != null) {
            OperationOutput output = new OperationOutput();
            output.setUuid(UUID.randomUUID().toString());
            output.setArtifactType(BaseArtifactEnum.OPERATION_OUTPUT);
            String name = null;
            derivedArtifacts.add(output);

            if (outputElem.hasAttribute("message")) { //$NON-NLS-1$
                String encodedMsgQname = outputElem.getAttribute("message"); //$NON-NLS-1$
                QName msgQname = resolveQName(outputElem, targetNS, encodedMsgQname);
                name = msgQname.getLocalPart();
                Message message = derivedArtifacts.lookupMessage(msgQname);
                MessageTarget target = new MessageTarget();
                target.setArtifactType(MessageEnum.MESSAGE);
                if (message != null) {
                    target.setValue(message.getUuid());
                } else {
                    relationshipSources.add(new QNameRelationshipSource(msgQname, target, null,
                            ArtifactTypeEnum.Message.getModel(), ArtifactTypeEnum.Message.getType()));
                }
                output.setMessage(target);
            }
            if (outputElem.hasAttribute("name")) { //$NON-NLS-1$
                name = outputElem.getAttribute("name"); //$NON-NLS-1$
                output.setNCName(name);
            }

            output.setName(name);
            output.setNamespace(targetNS);

            rval = output;
        }
        return rval;
    }

    private Collection<Fault> processOperationFaults(Element operationElem) throws XPathExpressionException {
        Collection<Fault> rval = new LinkedList<Fault>();

        NodeList faults = (NodeList) this.query(operationElem, "./wsdl:fault", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < faults.getLength(); idx++) {
            Element faultElem = (Element) faults.item(idx);
            Fault fault = new Fault();
            fault.setUuid(UUID.randomUUID().toString());
            fault.setArtifactType(BaseArtifactEnum.FAULT);

            String name = null;
            derivedArtifacts.add(fault);
            rval.add(fault);

            if (faultElem.hasAttribute("message")) { //$NON-NLS-1$
                String encodedMsgQname = faultElem.getAttribute("message"); //$NON-NLS-1$
                QName msgQname = resolveQName(faultElem, targetNS, encodedMsgQname);
                name = msgQname.getLocalPart();
                Message message = derivedArtifacts.lookupMessage(msgQname);
                MessageTarget target = new MessageTarget();
                target.setArtifactType(MessageEnum.MESSAGE);
                if (message != null) {
                    target.setValue(message.getUuid());
                } else {
                    relationshipSources.add(new QNameRelationshipSource(msgQname, target, null,
                            ArtifactTypeEnum.Message.getModel(), ArtifactTypeEnum.Message.getType()));
                }
                fault.setMessage(target);
            }
            if (faultElem.hasAttribute("name")) { //$NON-NLS-1$
                name = faultElem.getAttribute("name"); //$NON-NLS-1$
                fault.setNCName(name);
            }

            fault.setName(name);
            fault.setNamespace(targetNS);
        }
        return rval;
    }

    private void processBindings() throws XPathExpressionException {
        // Get all the bindings and add them to the list
        NodeList bindings = (NodeList) this.query(rootElement, "./wsdl:binding", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < bindings.getLength(); idx++) {
            Element bindingElem = (Element) bindings.item(idx);
            if (bindingElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = bindingElem.getAttribute("name"); //$NON-NLS-1$
                Binding binding = new Binding();
                binding.setUuid(UUID.randomUUID().toString());
                binding.setArtifactType(BaseArtifactEnum.BINDING);
                binding.setName(name);
                binding.setNamespace(targetNS);
                binding.setNCName(name);
                derivedArtifacts.add(binding);

                // Resolve the referenced port type and create a relationship to it.
                PortType portType = null;
                if (bindingElem.hasAttribute("type")) { //$NON-NLS-1$
                    String portTypeEncodedQName = bindingElem.getAttribute("type"); //$NON-NLS-1$
                    QName portTypeQName = resolveQName(bindingElem, targetNS, portTypeEncodedQName);
                    portType = derivedArtifacts.lookupPortType(portTypeQName);
                    PortTypeTarget target = new PortTypeTarget();
                    target.setArtifactType(PortTypeEnum.PORT_TYPE);
                    if (portType != null) {
                        target.setValue(portType.getUuid());
                    } else {
                        relationshipSources.add(new QNameRelationshipSource(portTypeQName, target, null,
                                ArtifactTypeEnum.PortType.getModel(), ArtifactTypeEnum.PortType.getType()));
                    }
                    binding.setPortType(target);
                }

                // Process all the wsdl:operation children
                Collection<BindingOperation> bindingOperations = processBindingOperations(bindingElem, portType);
                for (BindingOperation bindingOperation : bindingOperations) {
                    BindingOperationTarget target = new BindingOperationTarget();
                    target.setValue(bindingOperation.getUuid());
                    target.setArtifactType(BindingOperationEnum.BINDING_OPERATION);
                    binding.getBindingOperation().add(target);
                }

                // Process soap extensions
                NodeList soapBindings = (NodeList) this.query(bindingElem, "./soap:binding", XPathConstants.NODESET); //$NON-NLS-1$
                for (int jdx = 0; jdx < bindings.getLength(); jdx++) {
                    Element soapBindingElem = (Element) soapBindings.item(jdx);
                    // Note: I ran into a case where the xpath returned some nodes but calls to item()
                    // returned null!  This is madness and I couldn't explain it.  The s-ramp-demos-switchyard
                    // demo currently causes this to happen.
                    if (soapBindingElem == null)
                        continue;
                    SoapBinding soapBinding = new SoapBinding();
                    soapBinding.setUuid(UUID.randomUUID().toString());
                    soapBinding.setArtifactType(BaseArtifactEnum.SOAP_BINDING);
                    soapBinding.setName("soap:binding"); //$NON-NLS-1$
                    soapBinding.setNamespace(soapBindingElem.getNamespaceURI());
                    soapBinding.setNCName(soapBindingElem.getLocalName());
                    soapBinding.setStyle(soapBindingElem.getAttribute("style")); //$NON-NLS-1$
                    soapBinding.setTransport(soapBindingElem.getAttribute("transport")); //$NON-NLS-1$
                    derivedArtifacts.add(soapBinding);

                    WsdlExtensionTarget target = new WsdlExtensionTarget();
                    target.setArtifactType(WsdlExtensionEnum.WSDL_EXTENSION);
                    target.setValue(soapBinding.getUuid());
                    binding.getExtension().add(target);
                }
            }
        }
    }

    private Collection<BindingOperation> processBindingOperations(Element bindingElem, PortType portType) throws XPathExpressionException {
        Collection<BindingOperation> rval = new LinkedList<BindingOperation>();

        // Get all the binding operations and add them to the list
        NodeList bindingOperations = (NodeList) this.query(bindingElem, "./wsdl:operation", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < bindingOperations.getLength(); idx++) {
            Element bindingOperationElem = (Element) bindingOperations.item(idx);
            if (bindingOperationElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = bindingOperationElem.getAttribute("name"); //$NON-NLS-1$
                BindingOperation bindingOperation = new BindingOperation();
                bindingOperation.setUuid(UUID.randomUUID().toString());
                bindingOperation.setArtifactType(BaseArtifactEnum.BINDING_OPERATION);
                bindingOperation.setName(name);
                bindingOperation.setNamespace(targetNS);
                bindingOperation.setNCName(name);
                derivedArtifacts.add(bindingOperation);
                rval.add(bindingOperation);

                if (portType != null) {
                    QName portTypeQName = new QName(portType.getNamespace(), portType.getName());
                    Operation operation = derivedArtifacts.lookupOperation(portTypeQName, name);
                    OperationTarget opTarget = new OperationTarget();
                    opTarget.setValue(operation.getUuid());
                    opTarget.setArtifactType(OperationEnum.OPERATION);
                    bindingOperation.setOperation(opTarget);
                }

                BindingOperationInput bindingOperationInput = processBindingOperationInput(bindingOperationElem);
                BindingOperationOutput bindingOperationOutput = processBindingOperationOutput(bindingOperationElem);
                Collection<BindingOperationFault> bindingOperationFaults = processBindingOperationFaults(bindingOperationElem);

                if (bindingOperationInput != null) {
                    BindingOperationInputTarget target = new BindingOperationInputTarget();
                    target.setValue(bindingOperationInput.getUuid());
                    target.setArtifactType(BindingOperationInputEnum.BINDING_OPERATION_INPUT);
                    bindingOperation.setInput(target);
                }
                if (bindingOperationOutput != null) {
                    BindingOperationOutputTarget target = new BindingOperationOutputTarget();
                    target.setValue(bindingOperationOutput.getUuid());
                    target.setArtifactType(BindingOperationOutputEnum.BINDING_OPERATION_OUTPUT);
                    bindingOperation.setOutput(target);
                }
                for (BindingOperationFault fault : bindingOperationFaults) {
                    BindingOperationFaultTarget target = new BindingOperationFaultTarget();
                    target.setValue(fault.getUuid());
                    target.setArtifactType(BindingOperationFaultEnum.BINDING_OPERATION_FAULT);
                    bindingOperation.getFault().add(target);
                }
            }
        }
        return rval;
    }

    private BindingOperationInput processBindingOperationInput(Element operationElem) throws XPathExpressionException {
        BindingOperationInput rval = null;

        Element inputElem = (Element) this.query(operationElem, "./wsdl:input", XPathConstants.NODE); //$NON-NLS-1$
        if (inputElem != null) {
            BindingOperationInput bindingOperationInput = new BindingOperationInput();
            bindingOperationInput.setUuid(UUID.randomUUID().toString());
            bindingOperationInput.setArtifactType(BaseArtifactEnum.BINDING_OPERATION_INPUT);
            String name = "wsdl:input"; //$NON-NLS-1$
            if (inputElem.hasAttribute("name")) { //$NON-NLS-1$
                name = inputElem.getAttribute("name"); //$NON-NLS-1$
                bindingOperationInput.setNCName(name);
            }
            bindingOperationInput.setName(name);
            bindingOperationInput.setNamespace(targetNS);
            derivedArtifacts.add(bindingOperationInput);

            rval = bindingOperationInput;
        }
        return rval;
    }

    private BindingOperationOutput processBindingOperationOutput(Element operationElem) throws XPathExpressionException {
        BindingOperationOutput rval = null;

        Element outputElem = (Element) this.query(operationElem, "./wsdl:output", XPathConstants.NODE); //$NON-NLS-1$
        if (outputElem != null) {
            BindingOperationOutput bindingOperationOutput = new BindingOperationOutput();
            bindingOperationOutput.setUuid(UUID.randomUUID().toString());
            bindingOperationOutput.setArtifactType(BaseArtifactEnum.BINDING_OPERATION_OUTPUT);
            String name = "wsdl:output"; //$NON-NLS-1$
            if (outputElem.hasAttribute("name")) { //$NON-NLS-1$
                name = outputElem.getAttribute("name"); //$NON-NLS-1$
                bindingOperationOutput.setNCName(name);
            }
            bindingOperationOutput.setName(name);
            bindingOperationOutput.setNamespace(targetNS);
            derivedArtifacts.add(bindingOperationOutput);

            rval = bindingOperationOutput;
        }
        return rval;
    }

    private Collection<BindingOperationFault> processBindingOperationFaults(Element operationElem) throws XPathExpressionException {
        Collection<BindingOperationFault> rval = new LinkedList<BindingOperationFault>();

        NodeList faults = (NodeList) this.query(operationElem, "./wsdl:fault", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < faults.getLength(); idx++) {
            Element faultElem = (Element) faults.item(idx);
            BindingOperationFault bindingOperationFault = new BindingOperationFault();
            bindingOperationFault.setUuid(UUID.randomUUID().toString());
            bindingOperationFault.setArtifactType(BaseArtifactEnum.BINDING_OPERATION_FAULT);

            String name = "wsdl:fault"; //$NON-NLS-1$
            if (faultElem.hasAttribute("name")) { //$NON-NLS-1$
                name = faultElem.getAttribute("name"); //$NON-NLS-1$
                bindingOperationFault.setNCName(name);
            }

            bindingOperationFault.setName(name);
            bindingOperationFault.setNamespace(targetNS);
            derivedArtifacts.add(bindingOperationFault);
            rval.add(bindingOperationFault);
        }
        return rval;
    }

    private void processServices() throws XPathExpressionException {
        // Get all the bindings and add them to the list
        NodeList services = (NodeList) this.query(rootElement, "./wsdl:service", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < services.getLength(); idx++) {
            Element serviceElem = (Element) services.item(idx);
            WsdlService service = new WsdlService();
            service.setUuid(UUID.randomUUID().toString());
            service.setArtifactType(BaseArtifactEnum.WSDL_SERVICE);
            service.setNamespace(targetNS);
            if (serviceElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = serviceElem.getAttribute("name"); //$NON-NLS-1$
                service.setName(name);
                service.setNCName(name);
            } else {
                service.setName("wsdl:service"); //$NON-NLS-1$
            }
            derivedArtifacts.add(service);

            Collection<Port> ports = processPorts(serviceElem);
            for (Port port : ports) {
                PortTarget target = new PortTarget();
                target.setValue(port.getUuid());
                target.setArtifactType(PortEnum.PORT);
                service.getPort().add(target);
            }
        }
    }

    /**
     * Processes the ports for a service.
     * @param derivedArtifacts
     * @param primaryArtifact
     * @param serviceElem
     * @param xpath
     * @throws XPathExpressionException
     */
    private Collection<Port> processPorts(Element serviceElem) throws XPathExpressionException {
        Collection<Port> rval = new LinkedList<Port>();

        NodeList ports = (NodeList) this.query(serviceElem, "./wsdl:port", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < ports.getLength(); idx++) {
            Element portElem = (Element) ports.item(idx);
            Port port = new Port();
            port.setUuid(UUID.randomUUID().toString());
            port.setArtifactType(BaseArtifactEnum.PORT);
            port.setNamespace(targetNS);

            if (portElem.hasAttribute("name")) { //$NON-NLS-1$
                String name = portElem.getAttribute("name"); //$NON-NLS-1$
                port.setNCName(name);
                port.setName(name);
            } else {
                port.setName("wsdl:port"); //$NON-NLS-1$
            }

            if (portElem.hasAttribute("binding")) { //$NON-NLS-1$
                String bindingEncodedQName = portElem.getAttribute("binding"); //$NON-NLS-1$
                QName bindingQName = resolveQName(portElem, targetNS, bindingEncodedQName);
                Binding binding = derivedArtifacts.lookupBinding(bindingQName);
                BindingTarget target = new BindingTarget();
                target.setArtifactType(BindingEnum.BINDING);
                if (binding != null) {
                    target.setValue(binding.getUuid());
                } else {
                    relationshipSources.add(new QNameRelationshipSource(bindingQName, target, null,
                            ArtifactTypeEnum.Binding.getModel(), ArtifactTypeEnum.Binding.getType()));
                }
                port.setBinding(target);
            }

            derivedArtifacts.add(port);
            rval.add(port);

            NodeList soapAddresses = (NodeList) this.query(portElem, "./soap:address", XPathConstants.NODESET); //$NON-NLS-1$
            for (int jdx = 0; jdx < soapAddresses.getLength(); jdx++) {
                Element soapAddressElem = (Element) soapAddresses.item(jdx);
                SoapAddress soapAddress = new SoapAddress();
                soapAddress.setUuid(UUID.randomUUID().toString());
                soapAddress.setArtifactType(BaseArtifactEnum.SOAP_ADDRESS);
                soapAddress.setName("soap:address"); //$NON-NLS-1$
                soapAddress.setNCName(soapAddressElem.getLocalName());
                soapAddress.setNamespace(soapAddressElem.getNamespaceURI());
                soapAddress.setSoapLocation(soapAddressElem.getAttribute("location")); //$NON-NLS-1$
                derivedArtifacts.add(soapAddress);

                WsdlExtensionTarget target = new WsdlExtensionTarget();
                target.setArtifactType(WsdlExtensionEnum.WSDL_EXTENSION);
                target.setValue(soapAddress.getUuid());
                port.getExtension().add(target);
            }
        }
        return rval;
    }
    
    private void processWsdlImports() throws XPathExpressionException {
        WsdlDocument wsdlDocument;
        if (getPrimaryArtifact() instanceof WsdlDocument) {
            wsdlDocument = (WsdlDocument) getPrimaryArtifact();
        } else {
            return;
        }
        
        NodeList nodes = (NodeList) this.query(rootElement, ".//wsdl:import", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (node.hasAttribute("namespace")) { //$NON-NLS-1$
                String namespace = node.getAttribute("namespace");
                String location = node.getAttribute("location");
                stripPath(location);
                WsdlDocument wsdlDocumentRef = derivedArtifacts.lookupWsdlDocument(namespace);
                WsdlDocumentTarget wsdlDocumentTarget = new WsdlDocumentTarget();
                wsdlDocumentTarget.setArtifactType(WsdlDocumentEnum.WSDL_DOCUMENT);
                if (wsdlDocumentRef != null) {
                    wsdlDocumentTarget.setValue(wsdlDocumentRef.getUuid());
                } else {
                    relationshipSources.add(new NamespaceRelationshipSource(namespace, location, wsdlDocumentTarget,
                            wsdlDocument.getImportedWsdls(), ArtifactTypeEnum.WsdlDocument.getModel(),
                            ArtifactTypeEnum.WsdlDocument.getType()));
                }
                wsdlDocument.getImportedWsdls().add(wsdlDocumentTarget);
            }
        }
    }

    /**
     * Resolves an encoded QName into a {@link QName}.
     * @param context
     * @param defaultNamespace
     * @param encodedQName
     */
    private QName resolveQName(Element context, String defaultNamespace, String encodedQName) {
        int idx = encodedQName.indexOf(":"); //$NON-NLS-1$
        if (idx == -1) {
            return new QName(defaultNamespace, encodedQName);
        }
        String prefix = encodedQName.substring(0, idx);
        String localPart = encodedQName.substring(idx + 1);
        String namespace = resolveNamespaceByPrefix(context, prefix);
        if (namespace == null) {
            return new QName(null, encodedQName);
        } else {
            return new QName(namespace, localPart, prefix);
        }
    }

    /**
     * Given a prefix, returns the namespace mapped to that prefix based on the
     * given XML context.
     * @param context
     * @param prefix
     */
    private String resolveNamespaceByPrefix(Element context, String prefix) {
        String nsDecl = "xmlns:" + prefix; //$NON-NLS-1$
        Element elem = context;
        String ns = null;
        while (elem != null) {
            if (elem.hasAttribute(nsDecl)) {
                ns = elem.getAttribute(nsDecl);
                break;
            }
            if (elem.getParentNode().getNodeType() == Node.ELEMENT_NODE) {
                elem = (Element) elem.getParentNode();
            } else {
                elem = null;
            }
        }
        return ns;
    }

}
