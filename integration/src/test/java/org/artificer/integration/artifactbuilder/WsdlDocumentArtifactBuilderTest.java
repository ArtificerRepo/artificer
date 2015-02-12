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
package org.artificer.integration.artifactbuilder;

import java.io.InputStream;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.NamedWsdlDerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlExtension;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.artificer.common.ArtifactContent;

/**
 * Unit test for the {@link WsdlDocumentArtifactBuilder} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class WsdlDocumentArtifactBuilderTest {

	@Test
	public void testDeriverWsdl() throws Exception {
		DatatypeFactory dtFactory = DatatypeFactory.newInstance();

		WsdlDocumentArtifactBuilder builder = new WsdlDocumentArtifactBuilder();
		WsdlDocument testSrcArtifact = new WsdlDocument();
		testSrcArtifact.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
		testSrcArtifact.setUuid(UUID.randomUUID().toString());
		testSrcArtifact.setName("sample.wsdl"); //$NON-NLS-1$
		testSrcArtifact.setVersion("2012/09"); //$NON-NLS-1$
		testSrcArtifact.setContentEncoding("UTF-8"); //$NON-NLS-1$
		testSrcArtifact.setContentType("application/xml"); //$NON-NLS-1$
		testSrcArtifact.setContentSize(92779L);
		testSrcArtifact.setCreatedBy("anonymous"); //$NON-NLS-1$
		XMLGregorianCalendar xmlGC = dtFactory.newXMLGregorianCalendar(new GregorianCalendar());
		testSrcArtifact.setCreatedTimestamp(xmlGC);
		testSrcArtifact.setDescription("Sample WSDL."); //$NON-NLS-1$
		testSrcArtifact.setLastModifiedBy("anonymous"); //$NON-NLS-1$
		testSrcArtifact.setLastModifiedTimestamp(xmlGC);

		InputStream testSrcContent = null;
		try {
			testSrcContent = getClass().getResourceAsStream("/sample-files/wsdl/deriver.wsdl"); //$NON-NLS-1$
			Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(testSrcArtifact,
                    new ArtifactContent("deriver.wsdl", testSrcContent)).getDerivedArtifacts();
                            Assert.assertNotNull(derivedArtifacts);
			Assert.assertEquals(35, derivedArtifacts.size());

			// Index the results by artifact type and name
			Map<QName, DerivedArtifactType> index = new HashMap<QName, DerivedArtifactType>();
			for (BaseArtifactType da : derivedArtifacts) {
			    DerivedArtifactType artifact = (DerivedArtifactType) da;
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
			DerivedArtifactType artifact = index.get(new QName(BaseArtifactEnum.MESSAGE.toString(), "findRequest")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findRequest", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("findRequest", ((Message) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((Message) artifact).getNamespace()); //$NON-NLS-1$
			Message message = (Message) artifact;
			Assert.assertEquals(1, message.getPart().size());

			// Find the element decl named 'findResponse'
			artifact = index.get(new QName(BaseArtifactEnum.ELEMENT_DECLARATION.toString(), "findResponse")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findResponse", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("findResponse", ((ElementDeclaration) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl/types", ((ElementDeclaration) artifact).getNamespace()); //$NON-NLS-1$

			// Find the simple type named 'keywordType'
			artifact = index.get(new QName(BaseArtifactEnum.SIMPLE_TYPE_DECLARATION.toString(), "keywordType")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("keywordType", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("keywordType", ((SimpleTypeDeclaration) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl/types", ((SimpleTypeDeclaration) artifact).getNamespace()); //$NON-NLS-1$
			String typeUuid = artifact.getUuid();

			// Find the part named 'keyword'
			artifact = index.get(new QName(BaseArtifactEnum.PART.toString(), "keyword")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("keyword", artifact.getName()); //$NON-NLS-1$
			Part part = (Part) artifact;
			Assert.assertEquals("keyword", part.getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", part.getNamespace()); //$NON-NLS-1$
			Assert.assertNotNull(part.getType());
			Assert.assertEquals(typeUuid, part.getType().getValue());

			// Find the port type named 'SamplePortType'
			artifact = index.get(new QName(BaseArtifactEnum.PORT_TYPE.toString(), "SamplePortType")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SamplePortType", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("SamplePortType", ((PortType) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((PortType) artifact).getNamespace()); //$NON-NLS-1$
			PortType portType = (PortType) artifact;
			Assert.assertEquals(2, portType.getOperation().size());

			// Find the operation named 'find'
			artifact = index.get(new QName(BaseArtifactEnum.OPERATION.toString(), "find")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("find", artifact.getName()); //$NON-NLS-1$
			Operation operation = (Operation) artifact;
			Assert.assertEquals("find", operation.getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", operation.getNamespace()); //$NON-NLS-1$
			Assert.assertNotNull(operation.getInput());
			Assert.assertNotNull(operation.getOutput());
			Assert.assertNotNull(operation.getFault());
			Assert.assertEquals(2, operation.getFault().size());

			// Find the operation input named 'findRequest'
			artifact = index.get(new QName(BaseArtifactEnum.OPERATION_INPUT.toString(), "findRequest")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findRequest", artifact.getName()); //$NON-NLS-1$
			OperationInput operationInput = (OperationInput) artifact;
			Assert.assertEquals("findRequest", operationInput.getNCName()); //$NON-NLS-1$
			Assert.assertNotNull(operationInput.getMessage());
			Assert.assertNotNull(operationInput.getMessage().getValue());
			Assert.assertEquals(
					index.get(new QName(BaseArtifactEnum.MESSAGE.toString(), "findRequest")).getUuid(), //$NON-NLS-1$
					operationInput.getMessage().getValue());

			// Find the operation output named 'findResponse'
			artifact = index.get(new QName(BaseArtifactEnum.OPERATION_OUTPUT.toString(), "findResponse")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findResponse", artifact.getName()); //$NON-NLS-1$
			OperationOutput operationOutput = (OperationOutput) artifact;
			Assert.assertEquals("findResponse", operationOutput.getNCName()); //$NON-NLS-1$
			Assert.assertNotNull(operationOutput.getMessage());
			Assert.assertNotNull(operationOutput.getMessage().getValue());
			Assert.assertEquals(
					index.get(new QName(BaseArtifactEnum.MESSAGE.toString(), "findResponse")).getUuid(), //$NON-NLS-1$
					operationOutput.getMessage().getValue());

			// Find the binding named 'SampleBinding'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING.toString(), "SampleBinding")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SampleBinding", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("SampleBinding", ((Binding) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((Binding) artifact).getNamespace()); //$NON-NLS-1$
			Binding binding = (Binding) artifact;
			Assert.assertEquals(2, binding.getBindingOperation().size());
			Assert.assertEquals(1, binding.getExtension().size());

			// Find the document style soap:binding
			artifact = index.get(new QName(BaseArtifactEnum.SOAP_BINDING.toString(), "binding")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("soap:binding", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("binding", ((SoapBinding) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", ((SoapBinding) artifact).getNamespace()); //$NON-NLS-1$
			SoapBinding soapBinding = (SoapBinding) artifact;
			Assert.assertEquals("document", soapBinding.getStyle()); //$NON-NLS-1$
			Assert.assertEquals("http://schemas.xmlsoap.org/soap/http", soapBinding.getTransport()); //$NON-NLS-1$
			Assert.assertEquals(binding.getExtension().get(0).getValue(), soapBinding.getUuid());

			// Find the binding operation named 'find'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING_OPERATION.toString(), "find")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("find", artifact.getName()); //$NON-NLS-1$
			BindingOperation bindingOperation = (BindingOperation) artifact;
			Assert.assertEquals("find", bindingOperation.getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", bindingOperation.getNamespace()); //$NON-NLS-1$
			Assert.assertNotNull(bindingOperation.getInput());
			Assert.assertNotNull(bindingOperation.getOutput());
			Assert.assertNotNull(bindingOperation.getFault());
			Assert.assertEquals(2, bindingOperation.getFault().size());
			Assert.assertNotNull(bindingOperation.getOperation());
			Assert.assertEquals(operation.getUuid(), bindingOperation.getOperation().getValue());

			// Find the binding operation input named 'findRequest'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING_OPERATION_INPUT.toString(), "findRequest")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findRequest", artifact.getName()); //$NON-NLS-1$
			BindingOperationInput bindingOperationInput = (BindingOperationInput) artifact;
			Assert.assertEquals("findRequest", bindingOperationInput.getNCName()); //$NON-NLS-1$
			Assert.assertEquals(bindingOperation.getInput().getValue(), bindingOperationInput.getUuid());

			// Find the binding operation output named 'findResponse'
			artifact = index.get(new QName(BaseArtifactEnum.BINDING_OPERATION_OUTPUT.toString(), "findResponse")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("findResponse", artifact.getName()); //$NON-NLS-1$
			BindingOperationOutput bindingOperationOutput = (BindingOperationOutput) artifact;
			Assert.assertEquals("findResponse", bindingOperationOutput.getNCName()); //$NON-NLS-1$
			Assert.assertEquals(bindingOperation.getOutput().getValue(), bindingOperationOutput.getUuid());

			// Find the service named 'SampleService'
			artifact = index.get(new QName(BaseArtifactEnum.WSDL_SERVICE.toString(), "SampleService")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SampleService", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("SampleService", ((WsdlService) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", ((WsdlService) artifact).getNamespace()); //$NON-NLS-1$
			WsdlService service = (WsdlService) artifact;
			Assert.assertEquals(1, service.getPort().size());

			// Find the port named 'SamplePort'
			artifact = index.get(new QName(BaseArtifactEnum.PORT.toString(), "SamplePort")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("SamplePort", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("SamplePort", ((Port) artifact).getNCName()); //$NON-NLS-1$
			Port port = (Port) artifact;
			Assert.assertNotNull(port.getBinding());
			Assert.assertEquals(port.getBinding().getValue(), binding.getUuid());
			Assert.assertEquals(1, port.getExtension().size());

			// Find the soap:address
			artifact = index.get(new QName(BaseArtifactEnum.SOAP_ADDRESS.toString(), "address")); //$NON-NLS-1$
			Assert.assertNotNull(artifact);
			Assert.assertEquals("soap:address", artifact.getName()); //$NON-NLS-1$
			Assert.assertEquals("address", ((SoapAddress) artifact).getNCName()); //$NON-NLS-1$
			Assert.assertEquals("http://schemas.xmlsoap.org/wsdl/soap/", ((SoapAddress) artifact).getNamespace()); //$NON-NLS-1$
			SoapAddress soapAddress = (SoapAddress) artifact;
			Assert.assertEquals("http://localhost:8080/sample/sampleEP", soapAddress.getSoapLocation()); //$NON-NLS-1$
			Assert.assertEquals(port.getExtension().get(0).getValue(), soapAddress.getUuid());

		} finally {
			IOUtils.closeQuietly(testSrcContent);
		}
	}


	@Test
	public void testHumanTaskWsdl() throws Exception {
		DatatypeFactory dtFactory = DatatypeFactory.newInstance();

		WsdlDocumentArtifactBuilder builder = new WsdlDocumentArtifactBuilder();
		WsdlDocument testSrcArtifact = new WsdlDocument();
		testSrcArtifact.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
		testSrcArtifact.setUuid(UUID.randomUUID().toString());
		testSrcArtifact.setName("ws-humantask-api.wsdl"); //$NON-NLS-1$
		testSrcArtifact.setVersion("200803"); //$NON-NLS-1$
		testSrcArtifact.setContentEncoding("UTF-8"); //$NON-NLS-1$
		testSrcArtifact.setContentType("application/xml"); //$NON-NLS-1$
		testSrcArtifact.setContentSize(92779L);
		testSrcArtifact.setCreatedBy("anonymous"); //$NON-NLS-1$
		XMLGregorianCalendar xmlGC = dtFactory.newXMLGregorianCalendar(new GregorianCalendar());
		testSrcArtifact.setCreatedTimestamp(xmlGC);
		testSrcArtifact.setDescription("Human Task WSDL."); //$NON-NLS-1$
		testSrcArtifact.setLastModifiedBy("anonymous"); //$NON-NLS-1$
		testSrcArtifact.setLastModifiedTimestamp(xmlGC);

		InputStream testSrcContent = null;
		try {
			testSrcContent = getClass().getResourceAsStream("/sample-files/wsdl/ws-humantask-api.wsdl"); //$NON-NLS-1$
			Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(testSrcArtifact,
                    new ArtifactContent("ws-humantask-api.wsdl", testSrcContent)).getDerivedArtifacts();
                            Assert.assertNotNull(derivedArtifacts);
			Assert.assertEquals(850, derivedArtifacts.size());
		} finally {
			IOUtils.closeQuietly(testSrcContent);
		}
	}

}
