/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Binding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationFaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BindingOperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Fault;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.FaultTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Operation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationInput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.OperationTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Part;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Port;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PortType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapAddress;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SoapBinding;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactTypeEnum;

import java.util.List;


/**
 * Full test of persisting a wsdl document, complete with derived content and
 * relationships.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRWsdlDocumentPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

	@Test
	public void testWsdlDocument() throws Exception {
        String uuid = addArtifact("/sample-files/wsdl/", "jcr-sample.wsdl", new WsdlDocument(),
                BaseArtifactEnum.WSDL_DOCUMENT).getUuid();

		WsdlDocument wsdl = (WsdlDocument) getArtifactByUUID(uuid);
		Assert.assertNotNull(wsdl);
		Assert.assertEquals("jcr-sample.wsdl", wsdl.getName());
		Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", wsdl.getTargetNamespace());
		// Make sure all of the derived artifacts were properly created.
		SimpleTypeDeclaration keywordType = (SimpleTypeDeclaration)
				assertSingleArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, "keywordType");
		ElementDeclaration findElement = (ElementDeclaration)
				assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "find");
		ElementDeclaration findResponseElement = (ElementDeclaration)
				assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "findResponse");
		Message findRequestMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findRequest");
		Message findResponseMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findResponse");
		Message findRequestSimpleMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findRequestSimple");
		Message findResponseSimpleMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findResponseSimple");
		Message faultMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "faultMessage");
		PortType samplePortType = (PortType)
				assertSingleArtifact(ArtifactTypeEnum.PortType, "SamplePortType");
		Operation findOp = (Operation) assertSingleArtifact(ArtifactTypeEnum.Operation, "find");
		Operation findSimpleOp = (Operation) assertSingleArtifact(ArtifactTypeEnum.Operation, "findSimple");
		Fault errorFault = (Fault) assertSingleArtifact(ArtifactTypeEnum.Fault, "errorFault");
		Fault unknownFault = (Fault) assertSingleArtifact(ArtifactTypeEnum.Fault, "unknownFault");
		Binding binding = (Binding) assertSingleArtifact(ArtifactTypeEnum.Binding, "SampleBinding");
		WsdlService service = (WsdlService) assertSingleArtifact(ArtifactTypeEnum.WsdlService, "SampleService");

		// findRequestMessage assertions
		Part part = (Part) getArtifactByTarget(findRequestMessage.getPart().get(0));
		Assert.assertNull(part.getType());
		ElementDeclaration elem = (ElementDeclaration) getArtifactByTarget(part.getElement());
		Assert.assertEquals(findElement.getUuid(), elem.getUuid());
		// findResponseMessage assertions
		part = (Part) getArtifactByTarget(findResponseMessage.getPart().get(0));
		Assert.assertNull(part.getType());
		elem = (ElementDeclaration) getArtifactByTarget(part.getElement());
		Assert.assertEquals(findResponseElement.getUuid(), elem.getUuid());
		// findRequestSimpleMessage assertions
		part = (Part) getArtifactByTarget(findRequestSimpleMessage.getPart().get(0));
		Assert.assertNull(part.getElement());
		SimpleTypeDeclaration type = (SimpleTypeDeclaration) getArtifactByTarget(part.getType());
		Assert.assertEquals(keywordType.getUuid(), type.getUuid());
		// findResponseSimpleMessage assertions
		part = (Part) getArtifactByTarget(findResponseSimpleMessage.getPart().get(0));
		Assert.assertNull(part.getType());
		elem = (ElementDeclaration) getArtifactByTarget(part.getElement());
		Assert.assertEquals(findResponseElement.getUuid(), elem.getUuid());
		// faultMessage assertions
		part = (Part) getArtifactByTarget(faultMessage.getPart().get(0));
		Assert.assertNull(part.getType());
		Assert.assertNull(part.getElement());

		// port type + all operations
		List<OperationTarget> operations = samplePortType.getOperation();
		Assert.assertNotNull(operations);
		Assert.assertEquals(2, operations.size());
		// find operation
		Operation operation = assertHasOperation(samplePortType, "find");
		Assert.assertEquals(findOp.getUuid(), operation.getUuid());
		OperationInput input = (OperationInput) getArtifactByTarget(operation.getInput());
		Assert.assertEquals("findRequest", input.getNCName());
		Message msg = (Message) getArtifactByTarget(input.getMessage());
		Assert.assertEquals(findRequestMessage.getUuid(), msg.getUuid());
		OperationOutput output = (OperationOutput) getArtifactByTarget(operation.getOutput());
		Assert.assertEquals("findResponse", output.getNCName());
		msg = (Message) getArtifactByTarget(output.getMessage());
		Assert.assertEquals(findResponseMessage.getUuid(), msg.getUuid());
		List<FaultTarget> faults = operation.getFault();
		Assert.assertEquals(2, faults.size());
		Fault fault = assertHasFault(operation, "errorFault");
		Assert.assertEquals(errorFault.getUuid(), fault.getUuid());
		msg = (Message) getArtifactByTarget(fault.getMessage());
		Assert.assertEquals(faultMessage.getUuid(), msg.getUuid());
		fault = assertHasFault(operation, "unknownFault");
		Assert.assertEquals(unknownFault.getUuid(), fault.getUuid());
		// findSimple operation
		operation = assertHasOperation(samplePortType, "findSimple");
		Assert.assertEquals(findSimpleOp.getUuid(), operation.getUuid());
		input = (OperationInput) getArtifactByTarget(operation.getInput());
		output = (OperationOutput) getArtifactByTarget(operation.getOutput());
		faults = operation.getFault();
		Assert.assertEquals(0, faults.size());

		// binding
		Assert.assertEquals(1, binding.getExtension().size());
		SoapBinding soapBinding = (SoapBinding) getArtifactByTarget(binding.getExtension().get(0));
		Assert.assertNotNull(soapBinding);
		Assert.assertEquals("document", soapBinding.getStyle());
		Assert.assertEquals("http://schemas.xmlsoap.org/soap/http", soapBinding.getTransport());
		PortType pt = (PortType) getArtifactByTarget(binding.getPortType());
		Assert.assertNotNull(pt);
		Assert.assertEquals(samplePortType.getUuid(), pt.getUuid());

		// binding operations
		BindingOperation bindingOperation = assertHasOperation(binding, "find");
		BindingOperationInput bindingInput = (BindingOperationInput) getArtifactByTarget(bindingOperation.getInput());
		Assert.assertEquals("findRequest", bindingInput.getNCName());
		BindingOperationOutput bindingOutput = (BindingOperationOutput) getArtifactByTarget(bindingOperation.getOutput());
		Assert.assertEquals("findResponse", bindingOutput.getNCName());
		List<BindingOperationFaultTarget> bfaults = bindingOperation.getFault();
		Assert.assertEquals(2, bfaults.size());
		assertHasFault(bindingOperation, "errorFault");
		assertHasFault(bindingOperation, "unknownFault");
		Operation op = (Operation) getArtifactByTarget(bindingOperation.getOperation());
		Assert.assertNotNull(op);
		Assert.assertEquals(findOp.getUuid(), op.getUuid());

		// service
		Assert.assertEquals(1, service.getPort().size());
		Port port = (Port) getArtifactByTarget(service.getPort().get(0));
		Assert.assertNotNull(port);
		Binding b = (Binding) getArtifactByTarget(port.getBinding());
		Assert.assertEquals(binding.getUuid(), b.getUuid());
		Assert.assertEquals(1, port.getExtension().size());
		SoapAddress soapAddress = (SoapAddress) getArtifactByTarget(port.getExtension().get(0));
		Assert.assertNotNull(soapAddress);
		Assert.assertEquals("http://localhost:8080/sample/sampleEP", soapAddress.getSoapLocation());
	}

	/**
	 * Asserts that the operation contains a valid reference to a fault with
	 * the given name.  Returns the fault or throws if any assertions fail.
	 * @param operation
	 * @param faultName
	 * @throws Exception
	 */
	private Fault assertHasFault(Operation operation, String faultName) throws Exception {
		List<FaultTarget> faults = operation.getFault();
		for (FaultTarget t : faults) {
			Fault fault = (Fault) getArtifactByTarget(t);
			if (fault.getNCName().equals(faultName)) {
				return fault;
			}
		}
		Assert.fail("Failed to find fault with name: " + faultName);
		return null;
	}

	/**
	 * Asserts that the operation contains a valid reference to a fault with
	 * the given name.  Returns the fault or throws if any assertions fail.
	 * @param operation
	 * @param faultName
	 * @throws Exception
	 */
	private BindingOperationFault assertHasFault(BindingOperation operation, String faultName) throws Exception {
		List<BindingOperationFaultTarget> faults = operation.getFault();
		for (BindingOperationFaultTarget t : faults) {
			BindingOperationFault fault = (BindingOperationFault) getArtifactByTarget(t);
			if (fault.getNCName().equals(faultName)) {
				return fault;
			}
		}
		Assert.fail("Failed to find fault with name: " + faultName);
		return null;
	}

	/**
	 * Asserts that the port type contains a valid reference to an operation with
	 * the given name.  Returns the operation or throws if any assertions fail.
	 * @param portType
	 * @param operationName
	 * @throws Exception
	 */
	private Operation assertHasOperation(PortType portType, String operationName) throws Exception {
		List<OperationTarget> operation = portType.getOperation();
		for (OperationTarget t : operation) {
			Operation op = (Operation) getArtifactByTarget(t);
			if (op.getNCName().equals(operationName)) {
				return op;
			}
		}
		Assert.fail("Failed to find operation with name: " + operationName);
		return null;
	}

	/**
	 * Asserts that the port type contains a valid reference to an operation with
	 * the given name.  Returns the operation or throws if any assertions fail.
	 * @param binding
	 * @param operationName
	 * @throws Exception
	 */
	private BindingOperation assertHasOperation(Binding binding, String operationName) throws Exception {
		List<BindingOperationTarget> operation = binding.getBindingOperation();
		for (BindingOperationTarget t : operation) {
			BindingOperation op = (BindingOperation) getArtifactByTarget(t);
			if (op.getNCName().equals(operationName)) {
				return op;
			}
		}
		Assert.fail("Failed to find operation with name: " + operationName);
		return null;
	}

    @Test
    public void testWsdlDocumentWithExternalRefs() throws Exception {
        String xsdUuid = addArtifact("/sample-files/wsdl/", "jcr-sample-externalrefs.xsd", new XsdDocument(),
                BaseArtifactEnum.XSD_DOCUMENT).getUuid();
        String uuid = addArtifact("/sample-files/wsdl/", "jcr-sample-externalrefs.wsdl", new WsdlDocument(),
                BaseArtifactEnum.WSDL_DOCUMENT).getUuid();

        XsdDocument xsd = (XsdDocument) getArtifactByUUID(xsdUuid);
        Assert.assertNotNull(xsd);
        Assert.assertEquals("urn:s-ramp:test:jcr-sample-externalrefs:types", xsd.getTargetNamespace());

        WsdlDocument wsdl = (WsdlDocument) getArtifactByUUID(uuid);
        Assert.assertNotNull(wsdl);
        Assert.assertEquals("jcr-sample-externalrefs.wsdl", wsdl.getName());
        Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", wsdl.getTargetNamespace());

        ElementDeclaration extInput = (ElementDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "extInput");
        ComplexTypeDeclaration extOutputType = (ComplexTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ComplexTypeDeclaration, "extOutputType");
        SimpleTypeDeclaration extSimpleType = (SimpleTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, "extSimpleType");
        Message findRequestMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequest");
        Message findResponseMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findResponse");
        Message findRequestSimpleMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequestSimple");

        // findRequestMessage assertions
        Part part = (Part) getArtifactByTarget(findRequestMessage.getPart().get(0));
        Assert.assertNull(part.getType());
        ElementDeclaration elem = (ElementDeclaration) getArtifactByTarget(part.getElement());
        Assert.assertEquals(extInput.getUuid(), elem.getUuid());
        // findResponseMessage assertions
        part = (Part) getArtifactByTarget(findResponseMessage.getPart().get(0));
        Assert.assertNull(part.getElement());
        ComplexTypeDeclaration complexType = (ComplexTypeDeclaration) getArtifactByTarget(part.getType());
        Assert.assertEquals(extOutputType.getUuid(), complexType.getUuid());
        // findRequestSimpleMessage assertions
        part = (Part) getArtifactByTarget(findRequestSimpleMessage.getPart().get(0));
        Assert.assertNull(part.getElement());
        SimpleTypeDeclaration type = (SimpleTypeDeclaration) getArtifactByTarget(part.getType());
        Assert.assertEquals(extSimpleType.getUuid(), type.getUuid());
    }
}
