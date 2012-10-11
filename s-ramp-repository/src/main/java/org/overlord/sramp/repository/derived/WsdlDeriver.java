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
package org.overlord.sramp.repository.derived;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.UUID;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.Node;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.overlord.sramp.query.xpath.StaticNamespaceContext;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactTarget;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.ElementEnum;
import org.s_ramp.xmlns._2010.s_ramp.ElementTarget;
import org.s_ramp.xmlns._2010.s_ramp.Fault;
import org.s_ramp.xmlns._2010.s_ramp.FaultEnum;
import org.s_ramp.xmlns._2010.s_ramp.FaultTarget;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.MessageEnum;
import org.s_ramp.xmlns._2010.s_ramp.MessageTarget;
import org.s_ramp.xmlns._2010.s_ramp.Operation;
import org.s_ramp.xmlns._2010.s_ramp.OperationEnum;
import org.s_ramp.xmlns._2010.s_ramp.OperationInput;
import org.s_ramp.xmlns._2010.s_ramp.OperationInputEnum;
import org.s_ramp.xmlns._2010.s_ramp.OperationInputTarget;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutputEnum;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutputTarget;
import org.s_ramp.xmlns._2010.s_ramp.OperationTarget;
import org.s_ramp.xmlns._2010.s_ramp.Part;
import org.s_ramp.xmlns._2010.s_ramp.PartEnum;
import org.s_ramp.xmlns._2010.s_ramp.PartTarget;
import org.s_ramp.xmlns._2010.s_ramp.PortType;
import org.s_ramp.xmlns._2010.s_ramp.XsdType;
import org.s_ramp.xmlns._2010.s_ramp.XsdTypeEnum;
import org.s_ramp.xmlns._2010.s_ramp.XsdTypeTarget;
import org.w3c.dom.Document;
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
public class WsdlDeriver implements ArtifactDeriver {

	/**
	 * Constructor.
	 */
	public WsdlDeriver() {
	}

	/**
	 * @see org.overlord.sramp.repository.derived.ArtifactDeriver#derive(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, java.io.InputStream)
	 */
	@Override
	public Collection<DerivedArtifactType> derive(BaseArtifactType artifact, InputStream content) throws IOException {
		IndexedArtifactCollection derivedArtifacts = new IndexedArtifactCollection();

		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(content);
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			StaticNamespaceContext nsCtx = new StaticNamespaceContext();
			nsCtx.addMapping("xs", "http://www.w3.org/2001/XMLSchema");
			nsCtx.addMapping("xsd", "http://www.w3.org/2001/XMLSchema");
			nsCtx.addMapping("wsdl", "http://schemas.xmlsoap.org/wsdl/");
			xpath.setNamespaceContext(nsCtx);

			Element definitionsElem = document.getDocumentElement();
			processDefinitions(derivedArtifacts, artifact, definitionsElem, xpath);

			// Set the relatedTo relationship for all derived artifacts
			for (DerivedArtifactType derivedArtifact : derivedArtifacts) {
				if (derivedArtifact.getRelatedDocument() == null) {
					DocumentArtifactTarget related = new DocumentArtifactTarget();
					related.setValue(artifact.getUuid());
					related.setArtifactType(DocumentArtifactEnum.fromValue(artifact.getArtifactType()));
					derivedArtifact.setRelatedDocument(related);
				}
			}
		} catch (Exception e) {
			throw new IOException(e);
		}

		return derivedArtifacts;
	}

	/**
	 * Process the entire wsdl for all derived content.
	 * @param derivedArtifacts
	 * @param artifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	public static void processDefinitions(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType artifact, Element definitions, XPath xpath) throws XPathExpressionException {

		// Get derived content from all of the schemas embedded in this WSDL
		XPathExpression expr = xpath.compile("./wsdl:types/xsd:schema");
		NodeList schemas = (NodeList) expr.evaluate(definitions, XPathConstants.NODESET);
		for (int idx = 0; idx < schemas.getLength(); idx++) {
			Element schema = (Element) schemas.item(idx);
			XsdDeriver.processSchema(derivedArtifacts, artifact, schema, xpath);
		}

		processMessages(derivedArtifacts, artifact, definitions, xpath);
		processPortTypes(derivedArtifacts, artifact, definitions, xpath);
	}

	/**
	 * Processes the messages found in the WSDL.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param definitions
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private static void processMessages(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element definitions, XPath xpath) throws XPathExpressionException {
		String targetNS = definitions.getAttribute("targetNamespace");

		// Get all the WSDL messages and add them (and their parts) to the list
		XPathExpression expr = xpath.compile("./wsdl:message");
		NodeList messages = (NodeList) expr.evaluate(definitions, XPathConstants.NODESET);
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
	private static Collection<Part> processParts(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element messageElem, XPath xpath) throws XPathExpressionException {
		Collection<Part> rval = new LinkedList<Part>();
		String targetNS = messageElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		// Get all the parts and add them to the list
		XPathExpression expr = xpath.compile("./wsdl:part");
		NodeList parts = (NodeList) expr.evaluate(messageElem, XPathConstants.NODESET);
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
					if (elementRef != null) {
						ElementTarget elementTarget = new ElementTarget();
						elementTarget.setValue(elementRef.getUuid());
						elementTarget.setArtifactType(ElementEnum.ELEMENT);
						part.setElement(elementTarget);
					}
				} else if (partElem.hasAttribute("type")) {
					String encodedQName = partElem.getAttribute("type");
					QName qname = resolveQName(partElem, targetNS, encodedQName);
					XsdType typeRef = derivedArtifacts.lookupType(qname);
					if (typeRef != null) {
						XsdTypeTarget typeTarget = new XsdTypeTarget();
						typeTarget.setValue(typeRef.getUuid());
						typeTarget.setArtifactType(XsdTypeEnum.XSD_TYPE);
						part.setType(typeTarget);
					}
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
	private static void processPortTypes(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element definitions, XPath xpath) throws XPathExpressionException {
		String targetNS = definitions.getAttribute("targetNamespace");

		// Get all the port types and add them to the list
		XPathExpression expr = xpath.compile("./wsdl:portType");
		NodeList portTypes = (NodeList) expr.evaluate(definitions, XPathConstants.NODESET);
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
	private static Collection<Operation> processOperations(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element portTypeElem, XPath xpath) throws XPathExpressionException {
		Collection<Operation> rval = new LinkedList<Operation>();
		String targetNS = portTypeElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		// Get all the parts and add them to the list
		XPathExpression expr = xpath.compile("./wsdl:operation");
		NodeList operations = (NodeList) expr.evaluate(portTypeElem, XPathConstants.NODESET);
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
	private static OperationInput processOperationInput(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		OperationInput rval = null;
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		XPathExpression expr = xpath.compile("./wsdl:input");
		Element inputElem = (Element) expr.evaluate(operationElem, XPathConstants.NODE);
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
				if (message != null) {
					MessageTarget target = new MessageTarget();
					target.setValue(message.getUuid());
					target.setArtifactType(MessageEnum.MESSAGE);
					input.setMessage(target);
				}
			}
			if (inputElem.hasAttribute("name")) {
				name = inputElem.getAttribute("name");
				input.setNCName(name);
				input.setInputNCName(name);
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
	private static OperationOutput processOperationOutput(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		OperationOutput rval = null;
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		XPathExpression expr = xpath.compile("./wsdl:output");
		Element outputElem = (Element) expr.evaluate(operationElem, XPathConstants.NODE);
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
				if (message != null) {
					MessageTarget target = new MessageTarget();
					target.setValue(message.getUuid());
					target.setArtifactType(MessageEnum.MESSAGE);
					output.setMessage(target);
				}
			}
			if (outputElem.hasAttribute("name")) {
				name = outputElem.getAttribute("name");
				output.setNCName(name);
				output.setOutputNCName(name);
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
	private static Collection<Fault> processOperationFaults(IndexedArtifactCollection derivedArtifacts,
			BaseArtifactType sourceArtifact, Element operationElem, XPath xpath) throws XPathExpressionException {
		Collection<Fault> rval = new LinkedList<Fault>();
		String targetNS = operationElem.getOwnerDocument().getDocumentElement().getAttribute("targetNamespace");

		XPathExpression expr = xpath.compile("./wsdl:fault");
		NodeList faults = (NodeList) expr.evaluate(operationElem, XPathConstants.NODESET);
		for (int idx = 0; idx < faults.getLength(); idx++) {
			Element faultElem = (Element) faults.item(idx);
			Fault fault = new Fault();
			fault.setUuid(UUID.randomUUID().toString());
			fault.setArtifactType(BaseArtifactEnum.FAULT);

			String ncname = null;
			derivedArtifacts.add(fault);
			rval.add(fault);

			if (faultElem.hasAttribute("message")) {
				String encodedMsgQname = faultElem.getAttribute("message");
				QName msgQname = resolveQName(faultElem, targetNS, encodedMsgQname);
				ncname = msgQname.getLocalPart();
				Message message = derivedArtifacts.lookupMessage(msgQname);
				if (message != null) {
					MessageTarget target = new MessageTarget();
					target.setValue(message.getUuid());
					target.setArtifactType(MessageEnum.MESSAGE);
					fault.setMessage(target);
				}
			}
			if (faultElem.hasAttribute("name")) {
				ncname = faultElem.getAttribute("name");
			}

			fault.setName(ncname);
			fault.setNCName(ncname);
			fault.setNamespace(targetNS);
		}
		return rval;
	}

	/**
	 * Resolves an encoded QName into a {@link QName}.
	 * @param context
	 * @param defaultNamespace
	 * @param encodedQName
	 */
	private static QName resolveQName(Element context, String defaultNamespace, String encodedQName) {
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
	private static String resolveNamespaceByPrefix(Element context, String prefix) {
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

}
