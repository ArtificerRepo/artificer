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
package org.overlord.sramp.repository.jcr.modeshape;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;



/**
 * Full test of persisting a wsdl document, complete with derived content and
 * relationships.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRWsdlDocumentPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

	@Test
	public void testWsdlDocument() throws Exception {
        String uuid = addWsdlArtifact("jcr-sample.wsdl", new WsdlDocument(), BaseArtifactEnum.WSDL_DOCUMENT); //$NON-NLS-1$

		WsdlDocument wsdl = (WsdlDocument) getArtifactByUUID(uuid);
		Assert.assertNotNull(wsdl);
		Assert.assertEquals("jcr-sample.wsdl", wsdl.getName()); //$NON-NLS-1$
		Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", wsdl.getTargetNamespace()); //$NON-NLS-1$
		// Make sure all of the derived artifacts were properly created.
		SimpleTypeDeclaration keywordType = (SimpleTypeDeclaration)
				assertSingleArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, "keywordType"); //$NON-NLS-1$
		ElementDeclaration findElement = (ElementDeclaration)
				assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "find"); //$NON-NLS-1$
		ElementDeclaration findResponseElement = (ElementDeclaration)
				assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "findResponse"); //$NON-NLS-1$
		Message findRequestMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findRequest"); //$NON-NLS-1$
		Message findResponseMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findResponse"); //$NON-NLS-1$
		Message findRequestSimpleMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findRequestSimple"); //$NON-NLS-1$
		Message findResponseSimpleMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "findResponseSimple"); //$NON-NLS-1$
		Message faultMessage = (Message)
				assertSingleArtifact(ArtifactTypeEnum.Message, "faultMessage"); //$NON-NLS-1$
		PortType samplePortType = (PortType)
				assertSingleArtifact(ArtifactTypeEnum.PortType, "SamplePortType"); //$NON-NLS-1$
		Operation findOp = (Operation) assertSingleArtifact(ArtifactTypeEnum.Operation, "find"); //$NON-NLS-1$
		Operation findSimpleOp = (Operation) assertSingleArtifact(ArtifactTypeEnum.Operation, "findSimple"); //$NON-NLS-1$
		Fault errorFault = (Fault) assertSingleArtifact(ArtifactTypeEnum.Fault, "errorFault"); //$NON-NLS-1$
		Fault unknownFault = (Fault) assertSingleArtifact(ArtifactTypeEnum.Fault, "unknownFault"); //$NON-NLS-1$
		Binding binding = (Binding) assertSingleArtifact(ArtifactTypeEnum.Binding, "SampleBinding"); //$NON-NLS-1$
		WsdlService service = (WsdlService) assertSingleArtifact(ArtifactTypeEnum.WsdlService, "SampleService"); //$NON-NLS-1$

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
		Operation operation = assertHasOperation(samplePortType, "find"); //$NON-NLS-1$
		Assert.assertEquals(findOp.getUuid(), operation.getUuid());
		OperationInput input = (OperationInput) getArtifactByTarget(operation.getInput());
		Assert.assertEquals("findRequest", input.getNCName()); //$NON-NLS-1$
		Message msg = (Message) getArtifactByTarget(input.getMessage());
		Assert.assertEquals(findRequestMessage.getUuid(), msg.getUuid());
		OperationOutput output = (OperationOutput) getArtifactByTarget(operation.getOutput());
		Assert.assertEquals("findResponse", output.getNCName()); //$NON-NLS-1$
		msg = (Message) getArtifactByTarget(output.getMessage());
		Assert.assertEquals(findResponseMessage.getUuid(), msg.getUuid());
		List<FaultTarget> faults = operation.getFault();
		Assert.assertEquals(2, faults.size());
		Fault fault = assertHasFault(operation, "errorFault"); //$NON-NLS-1$
		Assert.assertEquals(errorFault.getUuid(), fault.getUuid());
		msg = (Message) getArtifactByTarget(fault.getMessage());
		Assert.assertEquals(faultMessage.getUuid(), msg.getUuid());
		fault = assertHasFault(operation, "unknownFault"); //$NON-NLS-1$
		Assert.assertEquals(unknownFault.getUuid(), fault.getUuid());
		// findSimple operation
		operation = assertHasOperation(samplePortType, "findSimple"); //$NON-NLS-1$
		Assert.assertEquals(findSimpleOp.getUuid(), operation.getUuid());
		input = (OperationInput) getArtifactByTarget(operation.getInput());
		output = (OperationOutput) getArtifactByTarget(operation.getOutput());
		faults = operation.getFault();
		Assert.assertEquals(0, faults.size());

		// binding
		Assert.assertEquals(1, binding.getExtension().size());
		SoapBinding soapBinding = (SoapBinding) getArtifactByTarget(binding.getExtension().get(0));
		Assert.assertNotNull(soapBinding);
		Assert.assertEquals("document", soapBinding.getStyle()); //$NON-NLS-1$
		Assert.assertEquals("http://schemas.xmlsoap.org/soap/http", soapBinding.getTransport()); //$NON-NLS-1$
		PortType pt = (PortType) getArtifactByTarget(binding.getPortType());
		Assert.assertNotNull(pt);
		Assert.assertEquals(samplePortType.getUuid(), pt.getUuid());

		// binding operations
		BindingOperation bindingOperation = assertHasOperation(binding, "find"); //$NON-NLS-1$
		BindingOperationInput bindingInput = (BindingOperationInput) getArtifactByTarget(bindingOperation.getInput());
		Assert.assertEquals("findRequest", bindingInput.getNCName()); //$NON-NLS-1$
		BindingOperationOutput bindingOutput = (BindingOperationOutput) getArtifactByTarget(bindingOperation.getOutput());
		Assert.assertEquals("findResponse", bindingOutput.getNCName()); //$NON-NLS-1$
		List<BindingOperationFaultTarget> bfaults = bindingOperation.getFault();
		Assert.assertEquals(2, bfaults.size());
		assertHasFault(bindingOperation, "errorFault"); //$NON-NLS-1$
		assertHasFault(bindingOperation, "unknownFault"); //$NON-NLS-1$
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
		Assert.assertEquals("http://localhost:8080/sample/sampleEP", soapAddress.getSoapLocation()); //$NON-NLS-1$
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
		Assert.fail("Failed to find fault with name: " + faultName); //$NON-NLS-1$
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
		Assert.fail("Failed to find fault with name: " + faultName); //$NON-NLS-1$
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
		Assert.fail("Failed to find operation with name: " + operationName); //$NON-NLS-1$
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
		Assert.fail("Failed to find operation with name: " + operationName); //$NON-NLS-1$
		return null;
	}

	/**
	 * Gets an artifact by a {@link Target}.
	 * @param target
	 * @throws Exception
	 */
	private BaseArtifactType getArtifactByTarget(Target target) throws Exception {
	    Assert.assertNotNull("Missing target/relationship.", target); //$NON-NLS-1$
		return getArtifactByUUID(target.getValue());
	}

	/**
	 * Ensures that a single artifact exists of the given type and name.
	 * @param type
	 * @param name
	 * @throws Exception
	 */
	private BaseArtifactType assertSingleArtifact(ArtifactTypeEnum type, String name) throws Exception {
		String q = String.format("/s-ramp/%1$s/%2$s[@name = ?]", type.getModel(), type.getType()); //$NON-NLS-1$
		SrampQuery query = queryManager.createQuery(q);
		query.setString(name);
		ArtifactSet artifactSet = null;
		try {
			artifactSet = query.executeQuery();
			Assert.assertEquals(1, artifactSet.size());
			BaseArtifactType arty = artifactSet.iterator().next();
			Assert.assertEquals(name, arty.getName());
			return arty;
		} finally {
			if (artifactSet != null)
				artifactSet.close();
		}
	}

	/**
	 * Gets a single artifact by UUID.
	 * @param uuid
	 * @throws Exception
	 */
	private BaseArtifactType getArtifactByUUID(String uuid) throws Exception {
		SrampQuery query = queryManager.createQuery("/s-ramp[@uuid = ?]"); //$NON-NLS-1$
		query.setString(uuid);
		ArtifactSet artifactSet = null;
		try {
			artifactSet = query.executeQuery();
			Assert.assertEquals(1, artifactSet.size());
			return artifactSet.iterator().next();
		} finally {
			if (artifactSet != null)
				artifactSet.close();
		}
	}


    @Test
    public void testWsdlDocumentWithExternalRefs() throws Exception {
        String xsdUuid = addWsdlArtifact("jcr-sample-externalrefs.xsd", new XsdDocument(), BaseArtifactEnum.XSD_DOCUMENT); //$NON-NLS-1$
        String uuid = addWsdlArtifact("jcr-sample-externalrefs.wsdl", new WsdlDocument(), BaseArtifactEnum.WSDL_DOCUMENT); //$NON-NLS-1$

        XsdDocument xsd = (XsdDocument) getArtifactByUUID(xsdUuid);
        Assert.assertNotNull(xsd);
        Assert.assertEquals("urn:s-ramp:test:jcr-sample-externalrefs:types", xsd.getTargetNamespace()); //$NON-NLS-1$

        WsdlDocument wsdl = (WsdlDocument) getArtifactByUUID(uuid);
        Assert.assertNotNull(wsdl);
        Assert.assertEquals("jcr-sample-externalrefs.wsdl", wsdl.getName()); //$NON-NLS-1$
        Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", wsdl.getTargetNamespace()); //$NON-NLS-1$

        ElementDeclaration extInput = (ElementDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ElementDeclaration, "extInput"); //$NON-NLS-1$
        ComplexTypeDeclaration extOutputType = (ComplexTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.ComplexTypeDeclaration, "extOutputType"); //$NON-NLS-1$
        SimpleTypeDeclaration extSimpleType = (SimpleTypeDeclaration)
                assertSingleArtifact(ArtifactTypeEnum.SimpleTypeDeclaration, "extSimpleType"); //$NON-NLS-1$
        Message findRequestMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequest"); //$NON-NLS-1$
        Message findResponseMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findResponse"); //$NON-NLS-1$
        Message findRequestSimpleMessage = (Message)
                assertSingleArtifact(ArtifactTypeEnum.Message, "findRequestSimple"); //$NON-NLS-1$

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

    /**
     * Adds an artifact to the repo.
     * @param fileName
     * @param document
     * @param type
     * @throws SrampException
     */
    private String addWsdlArtifact(String fileName, XmlDocument document, BaseArtifactEnum type) throws SrampException {
        String artifactFileName = fileName;
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName); //$NON-NLS-1$

        String uuid = null;
        try {
            document.setArtifactType(type);
            document.setName(artifactFileName);
            document.setContentType("application/xml"); //$NON-NLS-1$
            // Persist the artifact
            BaseArtifactType artifact = persistenceManager.persistArtifact(document, contentStream);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        return uuid;
    }


}
