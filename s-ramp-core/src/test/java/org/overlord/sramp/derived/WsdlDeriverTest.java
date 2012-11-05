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
package org.overlord.sramp.derived;

import java.io.InputStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.Binding;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperation;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationInput;
import org.s_ramp.xmlns._2010.s_ramp.BindingOperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.Message;
import org.s_ramp.xmlns._2010.s_ramp.NamedWsdlDerivedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Operation;
import org.s_ramp.xmlns._2010.s_ramp.OperationInput;
import org.s_ramp.xmlns._2010.s_ramp.OperationOutput;
import org.s_ramp.xmlns._2010.s_ramp.Part;
import org.s_ramp.xmlns._2010.s_ramp.Port;
import org.s_ramp.xmlns._2010.s_ramp.PortType;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.SoapAddress;
import org.s_ramp.xmlns._2010.s_ramp.SoapBinding;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.WsdlExtension;
import org.s_ramp.xmlns._2010.s_ramp.WsdlService;

/**
 * Unit test for the {@link WsdlDeriver} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class WsdlDeriverTest {

	/**
	 * Test method for {@link org.overlord.sramp.repository.derived.XsdDeriver#derive(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, java.io.InputStream)}.
	 */
	@Test
	public void testDeriverWsdl() throws Exception {
		DatatypeFactory dtFactory = DatatypeFactory.newInstance();

		WsdlDeriver deriver = new WsdlDeriver();
		WsdlDocument testSrcArtifact = new WsdlDocument();
		testSrcArtifact.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
		testSrcArtifact.setUuid(UUID.randomUUID().toString());
		testSrcArtifact.setName("sample.wsdl");
		testSrcArtifact.setVersion("2012/09");
		testSrcArtifact.setContentEncoding("UTF-8");
		testSrcArtifact.setContentType("application/xml");
		testSrcArtifact.setContentSize(92779L);
		testSrcArtifact.setCreatedBy("anonymous");
		XMLGregorianCalendar xmlGC = dtFactory.newXMLGregorianCalendar(new GregorianCalendar());
		testSrcArtifact.setCreatedTimestamp(xmlGC);
		testSrcArtifact.setDescription("Sample WSDL.");
		testSrcArtifact.setLastModifiedBy("anonymous");
		testSrcArtifact.setLastModifiedTimestamp(xmlGC);

		InputStream testSrcContent = null;
		try {
			testSrcContent = getClass().getResourceAsStream("/sample-files/wsdl/deriver.wsdl");
			Collection<DerivedArtifactType> derivedArtifacts = deriver.derive(testSrcArtifact, testSrcContent);
			Assert.assertNotNull(derivedArtifacts);
			Assert.assertEquals(35, derivedArtifacts.size());

			// Index the results by artifact type and name
			Map<QName, DerivedArtifactType> index = new HashMap<QName, DerivedArtifactType>();
			for (DerivedArtifactType artifact : derivedArtifacts) {
				if (artifact instanceof NamedWsdlDerivedArtifactType) {
					NamedWsdlDerivedArtifactType arty = (NamedWsdlDerivedArtifactType) artifact;
					if (arty.getNCName() != null)
						index.put(new QName(arty.getArtifactType().toString(), arty.getNCName()), artifact);
				} else if (artifact instanceof WsdlExtension) {
					WsdlExtension arty = (WsdlExtension) artifact;
					index.put(new QName(arty.getArtifactType().toString(), arty.getNCName()), arty);
				} else if (artifact instanceof ElementDeclaration) {
					ElementDeclaration arty = (ElementDeclaration) artifact;
					index.put(new QName(arty.getArtifactType().toString(), arty.getNCName()), artifact);
				} else if (artifact instanceof AttributeDeclaration) {
					AttributeDeclaration arty = (AttributeDeclaration) artifact;
					index.put(new QName(arty.getArtifactType().toString(), arty.getNCName()), artifact);
				} else if (artifact instanceof SimpleTypeDeclaration) {
					SimpleTypeDeclaration arty = (SimpleTypeDeclaration) artifact;
					index.put(new QName(arty.getArtifactType().toString(), arty.getNCName()), artifact);
				} else if (artifact instanceof ComplexTypeDeclaration) {
					ComplexTypeDeclaration arty = (ComplexTypeDeclaration) artifact;
					index.put(new QName(arty.getArtifactType().toString(), arty.getNCName()), artifact);
				}
			}

			// Do some specific assertions
			////////////////////////////////////////////

			// Find the message named 'findRequest'
			DerivedArtifactType artifact = index.get(new QName(BaseArtifactEnum.MESSAGE.toString(), "findRequest"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findRequest", artifact.getName());
			Assert.assertEquals("findRequest", ((Message) artifact).getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((Message) artifact).getNamespace());
			Message message = (Message) artifact;
			Assert.assertEquals(1, message.getPart().size());

			// Find the element decl named 'findResponse'
			artifact = index.get(new QName(BaseArtifactEnum.ELEMENT_DECLARATION.toString(), "findResponse"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findResponse", artifact.getName());
			Assert.assertEquals("findResponse", ((ElementDeclaration) artifact).getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl/types", ((ElementDeclaration) artifact).getNamespace());

			// Find the simple type named 'keywordType'
			artifact = index.get(new QName(BaseArtifactEnum.SIMPLE_TYPE_DECLARATION.toString(), "keywordType"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("keywordType", artifact.getName());
			Assert.assertEquals("keywordType", ((SimpleTypeDeclaration) artifact).getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl/types", ((SimpleTypeDeclaration) artifact).getNamespace());
			String typeUuid = artifact.getUuid();

			// Find the part named 'keyword'
			artifact = index.get(new QName(BaseArtifactEnum.PART.toString(), "keyword"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("keyword", artifact.getName());
			Part part = (Part) artifact;
			Assert.assertEquals("keyword", part.getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", part.getNamespace());
			Assert.assertNotNull(part.getType());
			Assert.assertEquals(typeUuid, part.getType().getValue());

			// Find the port type named 'SamplePortType'
			artifact = index.get(new QName(BaseArtifactEnum.PORT_TYPE.toString(), "SamplePortType"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SamplePortType", artifact.getName());
			Assert.assertEquals("SamplePortType", ((PortType) artifact).getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((PortType) artifact).getNamespace());
			PortType portType = (PortType) artifact;
			Assert.assertEquals(2, portType.getOperation().size());

			// Find the operation named 'find'
			artifact = index.get(new QName(BaseArtifactEnum.OPERATION.toString(), "find"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("find", artifact.getName());
			Operation operation = (Operation) artifact;
			Assert.assertEquals("find", operation.getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", operation.getNamespace());
			Assert.assertNotNull(operation.getInput());
			Assert.assertNotNull(operation.getOutput());
			Assert.assertNotNull(operation.getFault());
			Assert.assertEquals(2, operation.getFault().size());

			// Find the operation input named 'findRequest'
			artifact = index.get(new QName(BaseArtifactEnum.OPERATION_INPUT.toString(), "findRequest"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findRequest", artifact.getName());
			OperationInput operationInput = (OperationInput) artifact;
			Assert.assertEquals("findRequest", operationInput.getInputNCName());
			Assert.assertEquals("findRequest", operationInput.getNCName());
			Assert.assertNotNull(operationInput.getMessage());
			Assert.assertNotNull(operationInput.getMessage().getValue());
			Assert.assertEquals(
					index.get(new QName(BaseArtifactEnum.MESSAGE.toString(), "findRequest")).getUuid(),
					operationInput.getMessage().getValue());

			// Find the operation output named 'findResponse'
			artifact = index.get(new QName(BaseArtifactEnum.OPERATION_OUTPUT.toString(), "findResponse"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findResponse", artifact.getName());
			OperationOutput operationOutput = (OperationOutput) artifact;
			Assert.assertEquals("findResponse", operationOutput.getOutputNCName());
			Assert.assertEquals("findResponse", operationOutput.getNCName());
			Assert.assertNotNull(operationOutput.getMessage());
			Assert.assertNotNull(operationOutput.getMessage().getValue());
			Assert.assertEquals(
					index.get(new QName(BaseArtifactEnum.MESSAGE.toString(), "findResponse")).getUuid(),
					operationOutput.getMessage().getValue());

			// Find the binding named 'SampleBinding'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING.toString(), "SampleBinding"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SampleBinding", artifact.getName());
			Assert.assertEquals("SampleBinding", ((Binding) artifact).getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((Binding) artifact).getNamespace());
			Binding binding = (Binding) artifact;
			Assert.assertEquals(2, binding.getBindingOperation().size());
			Assert.assertEquals(1, binding.getExtension().size());

			// Find the document style soap:binding
			artifact = index.get(new QName(BaseArtifactEnum.SOAP_BINDING.toString(), "binding"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("soap:binding", artifact.getName());
			Assert.assertEquals("binding", ((SoapBinding) artifact).getNCName());
			Assert.assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", ((SoapBinding) artifact).getNamespace());
			SoapBinding soapBinding = (SoapBinding) artifact;
			Assert.assertEquals("document", soapBinding.getStyle());
			Assert.assertEquals("http://schemas.xmlsoap.org/soap/http", soapBinding.getTransport());
			Assert.assertEquals(binding.getExtension().get(0).getValue(), soapBinding.getUuid());

			// Find the binding operation named 'find'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING_OPERATION.toString(), "find"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("find", artifact.getName());
			BindingOperation bindingOperation = (BindingOperation) artifact;
			Assert.assertEquals("find", bindingOperation.getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", bindingOperation.getNamespace());
			Assert.assertNotNull(bindingOperation.getInput());
			Assert.assertNotNull(bindingOperation.getOutput());
			Assert.assertNotNull(bindingOperation.getFault());
			Assert.assertEquals(2, bindingOperation.getFault().size());
			Assert.assertNotNull(bindingOperation.getOperation());
			Assert.assertEquals(operation.getUuid(), bindingOperation.getOperation().getValue());

			// Find the binding operation input named 'findRequest'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING_OPERATION_INPUT.toString(), "findRequest"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findRequest", artifact.getName());
			BindingOperationInput bindingOperationInput = (BindingOperationInput) artifact;
			Assert.assertEquals("findRequest", bindingOperationInput.getNCName());
			Assert.assertEquals(bindingOperation.getInput().getValue(), bindingOperationInput.getUuid());

			// Find the binding operation output named 'findResponse'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING_OPERATION_OUTPUT.toString(), "findResponse"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findResponse", artifact.getName());
			BindingOperationOutput bindingOperationOutput = (BindingOperationOutput) artifact;
			Assert.assertEquals("findResponse", bindingOperationOutput.getNCName());
			Assert.assertEquals(bindingOperation.getOutput().getValue(), bindingOperationOutput.getUuid());

			// Find the service named 'SampleService'
			artifact = index.get(new QName(BaseArtifactEnum.WSDL_SERVICE.toString(), "SampleService"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SampleService", artifact.getName());
			Assert.assertEquals("SampleService", ((WsdlService) artifact).getNCName());
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((WsdlService) artifact).getNamespace());
			WsdlService service = (WsdlService) artifact;
			Assert.assertEquals(1, service.getPort().size());

			// Find the port named 'SamplePort'
			artifact = index.get(new QName(BaseArtifactEnum.PORT.toString(), "SamplePort"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SamplePort", artifact.getName());
			Assert.assertEquals("SamplePort", ((Port) artifact).getNCName());
			Port port = (Port) artifact;
			Assert.assertNotNull(port.getBinding());
			Assert.assertEquals(port.getBinding().getValue(), binding.getUuid());
			Assert.assertEquals(1, port.getExtension().size());

			// Find the soap:address
			artifact = index.get(new QName(BaseArtifactEnum.SOAP_ADDRESS.toString(), "address"));
			Assert.assertNotNull(artifact);
			Assert.assertEquals("soap:address", artifact.getName());
			Assert.assertEquals("address", ((SoapAddress) artifact).getNCName());
			Assert.assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", ((SoapAddress) artifact).getNamespace());
			SoapAddress soapAddress = (SoapAddress) artifact;
			Assert.assertEquals("http://localhost:8080/sample/sampleEP", soapAddress.getSoapLocation());
			Assert.assertEquals(port.getExtension().get(0).getValue(), soapAddress.getUuid());

		} finally {
			IOUtils.closeQuietly(testSrcContent);
		}
	}


	/**
	 * Test method for {@link org.overlord.sramp.repository.derived.XsdDeriver#derive(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, java.io.InputStream)}.
	 */
	@Test
	public void testHumanTaskWsdl() throws Exception {
		DatatypeFactory dtFactory = DatatypeFactory.newInstance();

		WsdlDeriver deriver = new WsdlDeriver();
		WsdlDocument testSrcArtifact = new WsdlDocument();
		testSrcArtifact.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
		testSrcArtifact.setUuid(UUID.randomUUID().toString());
		testSrcArtifact.setName("ws-humantask-api.wsdl");
		testSrcArtifact.setVersion("200803");
		testSrcArtifact.setContentEncoding("UTF-8");
		testSrcArtifact.setContentType("application/xml");
		testSrcArtifact.setContentSize(92779L);
		testSrcArtifact.setCreatedBy("anonymous");
		XMLGregorianCalendar xmlGC = dtFactory.newXMLGregorianCalendar(new GregorianCalendar());
		testSrcArtifact.setCreatedTimestamp(xmlGC);
		testSrcArtifact.setDescription("Human Task WSDL.");
		testSrcArtifact.setLastModifiedBy("anonymous");
		testSrcArtifact.setLastModifiedTimestamp(xmlGC);

		InputStream testSrcContent = null;
		try {
			testSrcContent = getClass().getResourceAsStream("/sample-files/wsdl/ws-humantask-api.wsdl");
			Collection<DerivedArtifactType> derivedArtifacts = deriver.derive(testSrcArtifact, testSrcContent);
			Assert.assertNotNull(derivedArtifacts);
			Assert.assertEquals(850, derivedArtifacts.size());
		} finally {
			IOUtils.closeQuietly(testSrcContent);
		}
	}

}
