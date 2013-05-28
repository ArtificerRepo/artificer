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
package org.overlord.sramp.common.derived;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.soap.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFaultEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.FaultEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.FaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.MessageEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.MessageTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutputEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutputTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTypeEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortTypeTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtensionEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtensionTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdTypeEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdTypeTarget;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates derived content from a WSDL document.  This will create the derived content as
 * defined in the WSDL model found in the s-ramp specification.  The following derived
 * artifact types will (potentially) be created:
 *
 * <ul>
 *   <li>AttributeDeclaration</li>
 *   <li>ElementDeclaration</li>
 *   <li>ComplexTypeDeclaration</li>
 *   <li>SimpleTypeDeclaration</li>
 *   <li>Message</li>
 *   <li>Part</li>
 *   <li>PortType</li>
 *   <li>Operation</li>
 *   <li>OperationInput</li>
 *   <li>OperationOutput</li>
 *   <li>Fault</li>
 *
 *   <li>WsdlService</li>
 *   <li>Port</li>
 *   <li>Binding</li>
 *   <li>BindingOperation</li>
 *   <li>BindingOperationInput</li>
 *   <li>BindingOperationOutput</li>
 *   <li>BindingOperationFault</li>
 *   <li>WsdlExtension</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class WsdlDeriver extends XsdDeriver {

    // Property added to some targets if the target artifact reference cannot be resolved during
    // the derivation phase.  When this happens we mark it as unresolved and try to resolve it
    // during the link phase.
    public static final QName UNRESOLVED_REF = new QName("urn:s-ramp:wsdl-deriver", "unresolvedRef");

    private final WsdlLinker linker = new WsdlLinker();

	/**
	 * Constructor.
	 */
	public WsdlDeriver() {
	}

	/**
	 * @see org.overlord.sramp.common.repository.derived.XsdDeriver#configureNamespaceMappings(org.overlord.sramp.common.query.xpath.StaticNamespaceContext)
	 */
	@Override
	protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
		super.configureNamespaceMappings(namespaceContext);

		namespaceContext.addMapping("wsdl", "http://schemas.xmlsoap.org/wsdl/");
		namespaceContext.addMapping("soap", "http://schemas.xmlsoap.org/wsdl/soap/");
	}

	/**
	 * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#createDerivedArtifactCollection()
	 */
	@Override
	protected Collection<BaseArtifactType> createDerivedArtifactCollection() {
	    return new IndexedArtifactCollection();
	}

	/**
	 * @see org.overlord.sramp.common.repository.derived.XsdDeriver#derive(org.overlord.sramp.common.repository.derived.IndexedArtifactCollection, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)
	 */
	@Override
	protected void derive(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
			Element rootElement, XPath xpath) throws IOException {
		String targetNS = rootElement.getAttribute("targetNamespace");
		((WsdlDocument) artifact).setTargetNamespace(targetNS);

		try {
			processDefinitions((IndexedArtifactCollection) derivedArtifacts, artifact, rootElement, xpath);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Process the entire wsdl for all derived content.
	 * @param derivedArtifacts
	 * @param artifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	public void processDefinitions(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType artifact, Element definitions, XPath xpath) throws XPathExpressionException {

		// Get derived content from all of the schemas embedded in this WSDL
		NodeList schemas = (NodeList) this.query(xpath, definitions, "./wsdl:types/xsd:schema", XPathConstants.NODESET);
		for (int idx = 0; idx < schemas.getLength(); idx++) {
			Element schema = (Element) schemas.item(idx);
			processSchema(derivedArtifacts, artifact, schema, xpath);
		}

		processMessages(derivedArtifacts, artifact, definitions, xpath);
		processPortTypes(derivedArtifacts, artifact, definitions, xpath);
		processBindings(derivedArtifacts, artifact, definitions, xpath);
		processServices(derivedArtifacts, artifact, definitions, xpath);
	}

	/**
	 * Processes the messages found in the WSDL.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processMessages(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element definitions, XPath xpath) throws XPathExpressionException {
		String targetNS = definitions.getAttribute("targetNamespace");

		// Get all the WSDL messages and add them (and their parts) to the list
		NodeList messages = (NodeList) this.query(xpath, definitions, "./wsdl:message", XPathConstants.NODESET);
		for (int idx = 0; idx < messages.getLength(); idx++) {
			Element messageElem = (Element) messages.item(idx);
			if (messageElem.hasAttribute("name")) {
				String name = messageElem.getAttribute("name");
				Message message = new Message();
				message.setUuid(UUID.randomUUID().toString());
				message.setArtifactType(BaseArtifactEnum.MESSAGE);
				message.setName(name);
				message.setNamespace(targetNS);
				message.setNCName(name);
				derivedArtifacts.add(message);

				Collection<Part> parts = processParts(derivedArtifacts, sourceArtifact, messageElem, xpath);
				for (Part part : parts) {
					PartTarget target = new PartTarget();
					target.setValue(part.getUuid());
					target.setArtifactType(PartEnum.PART);
					message.getPart().add(target);
				}
			}
		}
	}

	/**
	 * Processes the parts found in the WSDL message.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param messageElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private Collection<Part> processParts(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element messageElem, XPath xpath) throws XPathExpressionException {
		Collection<Part> rval = new LinkedList<Part>();
		String targetNS = messageElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		// Get all the parts and add them to the list
		NodeList parts = (NodeList) this.query(xpath, messageElem, "./wsdl:part", XPathConstants.NODESET);
		for (int idx = 0; idx < parts.getLength(); idx++) {
			Element partElem = (Element) parts.item(idx);
			if (partElem.hasAttribute("name")) {
				String name = partElem.getAttribute("name");
				Part part = new Part();
				part.setUuid(UUID.randomUUID().toString());
				part.setArtifactType(BaseArtifactEnum.PART);
				part.setName(name);
				part.setNamespace(targetNS);
				part.setNCName(name);
				derivedArtifacts.add(part);
				rval.add(part);

				if (partElem.hasAttribute("element")) {
					String encodedQName = partElem.getAttribute("element");
					QName qname = resolveQName(partElem, targetNS, encodedQName);
					ElementDeclaration elementRef = derivedArtifacts.lookupElement(qname);
                    ElementTarget elementTarget = new ElementTarget();
                    elementTarget.setArtifactType(ElementEnum.ELEMENT);
					if (elementRef != null) {
						elementTarget.setValue(elementRef.getUuid());
					} else {
					    elementTarget.getOtherAttributes().put(UNRESOLVED_REF, qname.toString());
					}
                    part.setElement(elementTarget);
				} else if (partElem.hasAttribute("type")) {
					String encodedQName = partElem.getAttribute("type");
					QName qname = resolveQName(partElem, targetNS, encodedQName);
					XsdType typeRef = derivedArtifacts.lookupType(qname);
                    XsdTypeTarget typeTarget = new XsdTypeTarget();
                    typeTarget.setArtifactType(XsdTypeEnum.XSD_TYPE);
					if (typeRef != null) {
						typeTarget.setValue(typeRef.getUuid());
					} else {
					    typeTarget.getOtherAttributes().put(UNRESOLVED_REF, qname.toString());
					}
                    part.setType(typeTarget);
				}
			}
		}
		return rval;
	}

	/**
	 * Process all the port types found in the WSDL.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processPortTypes(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element definitions, XPath xpath) throws XPathExpressionException {
		String targetNS = definitions.getAttribute("targetNamespace");

		// Get all the port types and add them to the list
		NodeList portTypes = (NodeList) this.query(xpath, definitions, "./wsdl:portType", XPathConstants.NODESET);
		for (int idx = 0; idx < portTypes.getLength(); idx++) {
			Element portTypeElem = (Element) portTypes.item(idx);
			if (portTypeElem.hasAttribute("name")) {
				String name = portTypeElem.getAttribute("name");
				PortType portType = new PortType();
				portType.setUuid(UUID.randomUUID().toString());
				portType.setArtifactType(BaseArtifactEnum.PORT_TYPE);
				portType.setName(name);
				portType.setNamespace(targetNS);
				portType.setNCName(name);
				derivedArtifacts.add(portType);

				Collection<Operation> operations = processOperations(derivedArtifacts, sourceArtifact, portTypeElem, xpath);
				for (Operation operation : operations) {
					OperationTarget target = new OperationTarget();
					target.setValue(operation.getUuid());
					target.setArtifactType(OperationEnum.OPERATION);
					portType.getOperation().add(target);
				}
			}
		}
	}

	/**
	 * Processes the operations found in the WSDL port type.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param portTypeElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private Collection<Operation> processOperations(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element portTypeElem, XPath xpath) throws XPathExpressionException {
		Collection<Operation> rval = new LinkedList<Operation>();
		String targetNS = portTypeElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		// Get all the operations and add them to the list
		NodeList operations = (NodeList) this.query(xpath, portTypeElem, "./wsdl:operation", XPathConstants.NODESET);
		for (int idx = 0; idx < operations.getLength(); idx++) {
			Element operationElem = (Element) operations.item(idx);
			if (operationElem.hasAttribute("name")) {
				String name = operationElem.getAttribute("name");
				Operation operation = new Operation();
				operation.setUuid(UUID.randomUUID().toString());
				operation.setArtifactType(BaseArtifactEnum.OPERATION);
				operation.setName(name);
				operation.setNamespace(targetNS);
				operation.setNCName(name);
				derivedArtifacts.add(operation);
				rval.add(operation);

				OperationInput operationInput = processOperationInput(derivedArtifacts, sourceArtifact, operationElem, xpath);
				OperationOutput operationOutput = processOperationOutput(derivedArtifacts, sourceArtifact, operationElem, xpath);
				Collection<Fault> operationFaults = processOperationFaults(derivedArtifacts, sourceArtifact, operationElem, xpath);

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

	/**
	 * Processes the input for an operation.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param operationElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private OperationInput processOperationInput(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		OperationInput rval = null;
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		Element inputElem = (Element) this.query(xpath, operationElem, "./wsdl:input", XPathConstants.NODE);
		if (inputElem != null) {
			OperationInput input = new OperationInput();
			input.setUuid(UUID.randomUUID().toString());
			input.setArtifactType(BaseArtifactEnum.OPERATION_INPUT);
			String name = null;
			derivedArtifacts.add(input);

			if (inputElem.hasAttribute("message")) {
				String encodedMsgQname = inputElem.getAttribute("message");
				QName msgQname = resolveQName(inputElem, targetNS, encodedMsgQname);
				name = msgQname.getLocalPart();
				Message message = derivedArtifacts.lookupMessage(msgQname);
                MessageTarget target = new MessageTarget();
                target.setArtifactType(MessageEnum.MESSAGE);
				if (message != null) {
					target.setValue(message.getUuid());
				} else {
				    target.getOtherAttributes().put(UNRESOLVED_REF, msgQname.toString());
				}
                input.setMessage(target);
			}
			if (inputElem.hasAttribute("name")) {
				name = inputElem.getAttribute("name");
				input.setNCName(name);
			}

			input.setName(name);
			input.setNamespace(targetNS);

			rval = input;
		}
		return rval;
	}

	/**
	 * Processes the output for an operation.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param operationElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private OperationOutput processOperationOutput(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		OperationOutput rval = null;
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		Element outputElem = (Element) this.query(xpath, operationElem, "./wsdl:output", XPathConstants.NODE);
		if (outputElem != null) {
			OperationOutput output = new OperationOutput();
			output.setUuid(UUID.randomUUID().toString());
			output.setArtifactType(BaseArtifactEnum.OPERATION_OUTPUT);
			String name = null;
			derivedArtifacts.add(output);

			if (outputElem.hasAttribute("message")) {
				String encodedMsgQname = outputElem.getAttribute("message");
				QName msgQname = resolveQName(outputElem, targetNS, encodedMsgQname);
				name = msgQname.getLocalPart();
				Message message = derivedArtifacts.lookupMessage(msgQname);
                MessageTarget target = new MessageTarget();
                target.setArtifactType(MessageEnum.MESSAGE);
				if (message != null) {
					target.setValue(message.getUuid());
				} else {
                    target.getOtherAttributes().put(UNRESOLVED_REF, msgQname.toString());
				}
                output.setMessage(target);
			}
			if (outputElem.hasAttribute("name")) {
				name = outputElem.getAttribute("name");
				output.setNCName(name);
			}

			output.setName(name);
			output.setNamespace(targetNS);

			rval = output;
		}
		return rval;
	}

	/**
	 * Processes the faults for an operation.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param operationElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private Collection<Fault> processOperationFaults(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		Collection<Fault> rval = new LinkedList<Fault>();
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		NodeList faults = (NodeList) this.query(xpath, operationElem, "./wsdl:fault", XPathConstants.NODESET);
		for (int idx = 0; idx < faults.getLength(); idx++) {
			Element faultElem = (Element) faults.item(idx);
			Fault fault = new Fault();
			fault.setUuid(UUID.randomUUID().toString());
			fault.setArtifactType(BaseArtifactEnum.FAULT);

			String name = null;
			derivedArtifacts.add(fault);
			rval.add(fault);

			if (faultElem.hasAttribute("message")) {
				String encodedMsgQname = faultElem.getAttribute("message");
				QName msgQname = resolveQName(faultElem, targetNS, encodedMsgQname);
				name = msgQname.getLocalPart();
				Message message = derivedArtifacts.lookupMessage(msgQname);
                MessageTarget target = new MessageTarget();
                target.setArtifactType(MessageEnum.MESSAGE);
				if (message != null) {
					target.setValue(message.getUuid());
				} else {
                    target.getOtherAttributes().put(UNRESOLVED_REF, msgQname.toString());
				}
                fault.setMessage(target);
			}
			if (faultElem.hasAttribute("name")) {
				name = faultElem.getAttribute("name");
				fault.setNCName(name);
			}

			fault.setName(name);
			fault.setNamespace(targetNS);
		}
		return rval;
	}

	/**
	 * Process all the bindings found in the WSDL.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processBindings(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element definitions, XPath xpath) throws XPathExpressionException {
		String targetNS = definitions.getAttribute("targetNamespace");

		// Get all the bindings and add them to the list
		NodeList bindings = (NodeList) this.query(xpath, definitions, "./wsdl:binding", XPathConstants.NODESET);
		for (int idx = 0; idx < bindings.getLength(); idx++) {
			Element bindingElem = (Element) bindings.item(idx);
			if (bindingElem.hasAttribute("name")) {
				String name = bindingElem.getAttribute("name");
				Binding binding = new Binding();
				binding.setUuid(UUID.randomUUID().toString());
				binding.setArtifactType(BaseArtifactEnum.BINDING);
				binding.setName(name);
				binding.setNamespace(targetNS);
				binding.setNCName(name);
				derivedArtifacts.add(binding);

				// Resolve the referenced port type and create a relationship to it.
				PortType portType = null;
				if (bindingElem.hasAttribute("type")) {
					String portTypeEncodedQName = bindingElem.getAttribute("type");
					QName portTypeQName = resolveQName(bindingElem, targetNS, portTypeEncodedQName);
					portType = derivedArtifacts.lookupPortType(portTypeQName);
                    PortTypeTarget target = new PortTypeTarget();
                    target.setArtifactType(PortTypeEnum.PORT_TYPE);
					if (portType != null) {
						target.setValue(portType.getUuid());
					} else {
	                    target.getOtherAttributes().put(UNRESOLVED_REF, portTypeQName.toString());
					}
                    binding.setPortType(target);
				}

				// Process all the wsdl:operation children
				Collection<BindingOperation> bindingOperations = processBindingOperations(derivedArtifacts,
						sourceArtifact, bindingElem, portType, xpath);
				for (BindingOperation bindingOperation : bindingOperations) {
					BindingOperationTarget target = new BindingOperationTarget();
					target.setValue(bindingOperation.getUuid());
					target.setArtifactType(BindingOperationEnum.BINDING_OPERATION);
					binding.getBindingOperation().add(target);
				}

				// Process soap extensions
				NodeList soapBindings = (NodeList) this.query(xpath, bindingElem, "./soap:binding", XPathConstants.NODESET);
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
					soapBinding.setName("soap:binding");
					soapBinding.setNamespace(soapBindingElem.getNamespaceURI());
					soapBinding.setNCName(soapBindingElem.getLocalName());
					soapBinding.setStyle(soapBindingElem.getAttribute("style"));
					soapBinding.setTransport(soapBindingElem.getAttribute("transport"));
					derivedArtifacts.add(soapBinding);

					WsdlExtensionTarget target = new WsdlExtensionTarget();
					target.setArtifactType(WsdlExtensionEnum.WSDL_EXTENSION);
					target.setValue(soapBinding.getUuid());
					binding.getExtension().add(target);
				}
			}
		}
	}

	/**
	 * Processes the operations found in the WSDL binding.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param bindingElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private Collection<BindingOperation> processBindingOperations(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element bindingElem, PortType portType, XPath xpath) throws XPathExpressionException {
		Collection<BindingOperation> rval = new LinkedList<BindingOperation>();
		String targetNS = bindingElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		// Get all the binding operations and add them to the list
		NodeList bindingOperations = (NodeList) this.query(xpath, bindingElem, "./wsdl:operation", XPathConstants.NODESET);
		for (int idx = 0; idx < bindingOperations.getLength(); idx++) {
			Element bindingOperationElem = (Element) bindingOperations.item(idx);
			if (bindingOperationElem.hasAttribute("name")) {
				String name = bindingOperationElem.getAttribute("name");
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

				BindingOperationInput bindingOperationInput = processBindingOperationInput(derivedArtifacts, sourceArtifact, bindingOperationElem, xpath);
				BindingOperationOutput bindingOperationOutput = processBindingOperationOutput(derivedArtifacts, sourceArtifact, bindingOperationElem, xpath);
				Collection<BindingOperationFault> bindingOperationFaults = processBindingOperationFaults(derivedArtifacts, sourceArtifact, bindingOperationElem, xpath);

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

	/**
	 * Processes the input for a binding operation.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param operationElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private BindingOperationInput processBindingOperationInput(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		BindingOperationInput rval = null;
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		Element inputElem = (Element) this.query(xpath, operationElem, "./wsdl:input", XPathConstants.NODE);
		if (inputElem != null) {
			BindingOperationInput bindingOperationInput = new BindingOperationInput();
			bindingOperationInput.setUuid(UUID.randomUUID().toString());
			bindingOperationInput.setArtifactType(BaseArtifactEnum.BINDING_OPERATION_INPUT);
			String name = "wsdl:input";
			if (inputElem.hasAttribute("name")) {
				name = inputElem.getAttribute("name");
				bindingOperationInput.setNCName(name);
			}
			bindingOperationInput.setName(name);
			bindingOperationInput.setNamespace(targetNS);
			derivedArtifacts.add(bindingOperationInput);

			rval = bindingOperationInput;
		}
		return rval;
	}

	/**
	 * Processes the output for a binding operation.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param operationElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private BindingOperationOutput processBindingOperationOutput(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		BindingOperationOutput rval = null;
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		Element outputElem = (Element) this.query(xpath, operationElem, "./wsdl:output", XPathConstants.NODE);
		if (outputElem != null) {
			BindingOperationOutput bindingOperationOutput = new BindingOperationOutput();
			bindingOperationOutput.setUuid(UUID.randomUUID().toString());
			bindingOperationOutput.setArtifactType(BaseArtifactEnum.BINDING_OPERATION_OUTPUT);
			String name = "wsdl:output";
			if (outputElem.hasAttribute("name")) {
				name = outputElem.getAttribute("name");
				bindingOperationOutput.setNCName(name);
			}
			bindingOperationOutput.setName(name);
			bindingOperationOutput.setNamespace(targetNS);
			derivedArtifacts.add(bindingOperationOutput);

			rval = bindingOperationOutput;
		}
		return rval;
	}

	/**
	 * Processes the faults for a binding operation.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param operationElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private Collection<BindingOperationFault> processBindingOperationFaults(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		Collection<BindingOperationFault> rval = new LinkedList<BindingOperationFault>();
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		NodeList faults = (NodeList) this.query(xpath, operationElem, "./wsdl:fault", XPathConstants.NODESET);
		for (int idx = 0; idx < faults.getLength(); idx++) {
			Element faultElem = (Element) faults.item(idx);
			BindingOperationFault bindingOperationFault = new BindingOperationFault();
			bindingOperationFault.setUuid(UUID.randomUUID().toString());
			bindingOperationFault.setArtifactType(BaseArtifactEnum.BINDING_OPERATION_FAULT);

			String name = "wsdl:fault";
			if (faultElem.hasAttribute("name")) {
				name = faultElem.getAttribute("name");
				bindingOperationFault.setNCName(name);
			}

			bindingOperationFault.setName(name);
			bindingOperationFault.setNamespace(targetNS);
			derivedArtifacts.add(bindingOperationFault);
			rval.add(bindingOperationFault);
		}
		return rval;
	}

	/**
	 * Processes all the services in the wsdl.
	 *
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processServices(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element definitions, XPath xpath) throws XPathExpressionException {
		String targetNS = definitions.getAttribute("targetNamespace");

		// Get all the bindings and add them to the list
		NodeList services = (NodeList) this.query(xpath, definitions, "./wsdl:service", XPathConstants.NODESET);
		for (int idx = 0; idx < services.getLength(); idx++) {
			Element serviceElem = (Element) services.item(idx);
			WsdlService service = new WsdlService();
			service.setUuid(UUID.randomUUID().toString());
			service.setArtifactType(BaseArtifactEnum.WSDL_SERVICE);
			service.setNamespace(targetNS);
			if (serviceElem.hasAttribute("name")) {
				String name = serviceElem.getAttribute("name");
				service.setName(name);
				service.setNCName(name);
			} else {
				service.setName("wsdl:service");
			}
			derivedArtifacts.add(service);

			Collection<Port> ports = processPorts(derivedArtifacts, sourceArtifact, serviceElem, xpath);
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
	 * @param sourceArtifact
	 * @param serviceElem
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private Collection<Port> processPorts(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element serviceElem, XPath xpath) throws XPathExpressionException {
		Collection<Port> rval = new LinkedList<Port>();
		String targetNS = serviceElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		NodeList ports = (NodeList) this.query(xpath, serviceElem, "./wsdl:port", XPathConstants.NODESET);
		for (int idx = 0; idx < ports.getLength(); idx++) {
			Element portElem = (Element) ports.item(idx);
			Port port = new Port();
			port.setUuid(UUID.randomUUID().toString());
			port.setArtifactType(BaseArtifactEnum.PORT);
			port.setNamespace(targetNS);

			if (portElem.hasAttribute("name")) {
				String name = portElem.getAttribute("name");
				port.setNCName(name);
				port.setName(name);
			} else {
				port.setName("wsdl:port");
			}

			if (portElem.hasAttribute("binding")) {
				String bindingEncodedQName = portElem.getAttribute("binding");
				QName bindingQName = resolveQName(portElem, targetNS, bindingEncodedQName);
				Binding binding = derivedArtifacts.lookupBinding(bindingQName);
                BindingTarget target = new BindingTarget();
                target.setArtifactType(BindingEnum.BINDING);
				if (binding != null) {
					target.setValue(binding.getUuid());
				} else {
                    target.getOtherAttributes().put(UNRESOLVED_REF, bindingQName.toString());
				}
                port.setBinding(target);
			}

			derivedArtifacts.add(port);
			rval.add(port);

			NodeList soapAddresses = (NodeList) this.query(xpath, portElem, "./soap:address", XPathConstants.NODESET);
			for (int jdx = 0; jdx < soapAddresses.getLength(); jdx++) {
				Element soapAddressElem = (Element) soapAddresses.item(jdx);
				SoapAddress soapAddress = new SoapAddress();
				soapAddress.setUuid(UUID.randomUUID().toString());
				soapAddress.setArtifactType(BaseArtifactEnum.SOAP_ADDRESS);
				soapAddress.setName("soap:address");
				soapAddress.setNCName(soapAddressElem.getLocalName());
				soapAddress.setNamespace(soapAddressElem.getNamespaceURI());
				soapAddress.setSoapLocation(soapAddressElem.getAttribute("location"));
				derivedArtifacts.add(soapAddress);

				WsdlExtensionTarget target = new WsdlExtensionTarget();
				target.setArtifactType(WsdlExtensionEnum.WSDL_EXTENSION);
				target.setValue(soapAddress.getUuid());
				port.getExtension().add(target);
			}
		}
		return rval;
	}

	/**
	 * Resolves an encoded QName into a {@link QName}.
	 * @param context
	 * @param defaultNamespace
	 * @param encodedQName
	 */
	private QName resolveQName(Element context, String defaultNamespace, String encodedQName) {
		int idx = encodedQName.indexOf(":");
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
		String nsDecl = "xmlns:" + prefix;
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

	/**
	 * @see org.overlord.sramp.common.derived.XsdDeriver#link(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
	 */
	@Override
	public void link(LinkerContext context, BaseArtifactType sourceArtifact,
	        Collection<BaseArtifactType> derivedArtifacts) {
	    super.link(context, sourceArtifact, derivedArtifacts);
	    for (BaseArtifactType derivedArtifact : derivedArtifacts) {
	        linker.link(context, derivedArtifact);
        }
	}

}
