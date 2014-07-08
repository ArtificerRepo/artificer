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
package org.overlord.sramp.test.server.atom.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput;
import org.jgroups.util.UUID;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Message;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.PartTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.providers.SrampAtomExceptionProvider;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.test.TestUtils;

/**
 * Test of the jax-rs resource that handles Artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactResourceTest extends AbstractNoAuditingResourceTest {

    String uuid = null;

	/**
	 * @throws Exception
	 */
	@Test
	public void testDerivedArtifactCreate() throws Exception {
		// Making a client call to the actual XsdDocument implementation running in
		// an embedded container.
		ClientRequest request = clientRequest("/s-ramp/xsd/ElementDeclaration"); //$NON-NLS-1$

		// read the XsdDocument from file
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
		String xmltext = TestUtils.convertStreamToString(POXsd);
		POXsd.close();

		request.header("Slug", artifactFileName); //$NON-NLS-1$
		request.body(MediaType.APPLICATION_XML, xmltext);

		try {
			request.post(String.class);
			Assert.fail("Expected an error here."); //$NON-NLS-1$
		} catch (SrampAtomException e) {
			Assert.assertEquals("Failed to create artifact because \"ElementDeclaration\" is a derived type.", e.getMessage()); //$NON-NLS-1$
			String stack = SrampAtomExceptionProvider.getRootStackTrace(e);
			Assert.assertTrue(stack.contains("org.overlord.sramp.server.atom.services.ArtifactResource.create")); //$NON-NLS-1$
		}
	}

	/**
	 * Tests adding a PDF document.
	 * @throws Exception
	 */
	@Test
	public void testPDFDocument() throws Exception {
		// Add the PDF to the repository
		String artifactFileName = "sample.pdf"; //$NON-NLS-1$
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
		//String uuid = null;
		try {
			ClientRequest request = clientRequest("/s-ramp/core/Document"); //$NON-NLS-1$
			request.header("Slug", artifactFileName); //$NON-NLS-1$
			request.body("application/pdf", contentStream); //$NON-NLS-1$

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof Document);
			Document doc = (Document) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Assert.assertEquals(Long.valueOf(218882), doc.getContentSize());
			Assert.assertEquals("application/pdf", doc.getContentType()); //$NON-NLS-1$
			uuid = doc.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Make sure we can query it now
		ClientRequest request = clientRequest("/s-ramp/core/Document/" + uuid); //$NON-NLS-1$
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertTrue(arty instanceof Document);
		Document doc = (Document) arty;
		Assert.assertEquals(artifactFileName, doc.getName());
		Assert.assertEquals(Long.valueOf(218882), doc.getContentSize());
		Assert.assertEquals("sample.pdf", doc.getName()); //$NON-NLS-1$
		Assert.assertEquals("application/pdf", doc.getContentType()); //$NON-NLS-1$
        //Obtain the content for visual inspection
        ClientRequest request2 = clientRequest("/s-ramp/core/Document/" + uuid + "/media"); //$NON-NLS-1$ //$NON-NLS-2$
        ClientResponse<InputStream> response2 = request2.get(InputStream.class);
        if (response2.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " //$NON-NLS-1$
                + response.getStatus());
        }
        InputStream in = response2.getEntity();
        File file = new File("target/SRAMP-sample.pdf"); //$NON-NLS-1$
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        out.flush();
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
	}

    /**
     * Tests adding an extended artifact type (no document content).
     * @throws Exception
     */
    @Test
    public void testExtendedArtifactType() throws Exception {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("FooApplication"); //$NON-NLS-1$
        artifact.setName("Extended Artifact Name"); //$NON-NLS-1$
        artifact.setDescription("Extended Artifact Description"); //$NON-NLS-1$
        ClientRequest request = clientRequest("/s-ramp/ext/FooApplication"); //$NON-NLS-1$
        Entry requestEntry = SrampAtomUtils.wrapSrampArtifact(artifact);
        request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, requestEntry);

        ClientResponse<Entry> response = request.post(Entry.class);

        Entry responseEntry = response.getEntity();
        Assert.assertEquals("Extended Artifact Name", responseEntry.getTitle()); //$NON-NLS-1$
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(responseEntry);
        Assert.assertTrue(arty instanceof ExtendedArtifactType);
        ExtendedArtifactType extArty = (ExtendedArtifactType) arty;
        Assert.assertEquals("Extended Artifact Name", extArty.getName()); //$NON-NLS-1$
        Assert.assertEquals("Extended Artifact Description", extArty.getDescription()); //$NON-NLS-1$
        uuid = extArty.getUuid();

        // Make sure we can query it now
        request = clientRequest("/s-ramp/ext/FooApplication/" + uuid); //$NON-NLS-1$
        response = request.get(Entry.class);

        Entry entry = response.getEntity();
        arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedArtifactType);
        extArty = (ExtendedArtifactType) arty;
        Assert.assertEquals("Extended Artifact Name", extArty.getName()); //$NON-NLS-1$
        Assert.assertEquals("Extended Artifact Description", extArty.getDescription()); //$NON-NLS-1$
    }

	/**
     * Tests adding a BRMS Pkg document.
     * @throws Exception
     */
    @Test
    public void testBrmsPkgDocument() throws Exception {
        // Add the pkg to the repository
        String artifactFileName = "defaultPackage.pkg"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName); //$NON-NLS-1$
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/BrmsPkgDocument"); //$NON-NLS-1$
            request.header("Slug", artifactFileName); //$NON-NLS-1$
            request.body("application/octet-stream", contentStream); //$NON-NLS-1$

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("BrmsPkgDocument", doc.getExtendedType()); //$NON-NLS-1$
            Assert.assertEquals(Long.valueOf(17043), Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME)));
            Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/BrmsPkgDocument/" + uuid); //$NON-NLS-1$
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(17043), Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME)));
        Assert.assertEquals("defaultPackage.pkg", doc.getName()); //$NON-NLS-1$
        Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$
    }

    /**
     * Tests adding a JPG document.
     * @throws Exception
     */
    @Test
    public void testJPGDocument() throws Exception {
        // Add the jpg to the repository
        String artifactFileName = "photo.jpg"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName); //$NON-NLS-1$
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/JpgDocument"); //$NON-NLS-1$
            request.header("Slug", artifactFileName); //$NON-NLS-1$
            request.body("application/octet-stream", contentStream); //$NON-NLS-1$

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("JpgDocument", doc.getExtendedType()); //$NON-NLS-1$
            Assert.assertEquals(Long.valueOf(2398), Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME)));
            Assert.assertEquals("image/jpeg", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/JpgDocument/" + uuid); //$NON-NLS-1$
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(2398), Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME)));
        Assert.assertEquals("photo.jpg", doc.getName()); //$NON-NLS-1$
        Assert.assertEquals("image/jpeg", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$

        //Obtain the content for visual inspection
        ClientRequest request2 = clientRequest("/s-ramp/ext/JpgDocument/" + uuid + "/media"); //$NON-NLS-1$ //$NON-NLS-2$
        ClientResponse<InputStream> response2 = request2.get(InputStream.class);
        if (response2.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " //$NON-NLS-1$
                + response.getStatus());
        }
        InputStream in = response2.getEntity();
        File file = new File("target/SRAMP-photo.jpg"); //$NON-NLS-1$
        OutputStream out = new FileOutputStream(file);
        IOUtils.copy(in, out);
        out.flush();
        IOUtils.closeQuietly(in);
        IOUtils.closeQuietly(out);
    }

	/**
     * Tests adding a BPMN Process Definition document.
     * @throws Exception
     */
    @Test
    public void testBpmnExtendedDocumentCreate() throws Exception {
        // Add the BPMN process to the repository
        String artifactFileName = "Evaluation.bpmn"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName); //$NON-NLS-1$
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/BpmnDocument"); //$NON-NLS-1$
            request.header("Slug", artifactFileName); //$NON-NLS-1$
            request.body("application/xml", contentStream); //$NON-NLS-1$

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("BpmnDocument", doc.getExtendedType()); //$NON-NLS-1$
            long size = Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME));
            Assert.assertTrue(size >= 12482L);
            Assert.assertEquals("application/xml", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/BpmnDocument/" + uuid); //$NON-NLS-1$
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals("Evaluation.bpmn", doc.getName()); //$NON-NLS-1$
        Assert.assertEquals("application/xml", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$

        ClientResponse<String> content = request.get(String.class);
        String c = content.getEntity();
        Assert.assertNotNull(c);
//        System.out.println("Content=" + content.getEntity());
    }

    /**
     * Tests adding a BPMN Process Definition document.
     * @throws Exception
     */
    @Test
    public void testWslaExtendedDocumentCreate() throws Exception {
        // Add the BPMN process to the repository
        String artifactFileName = "Sample.wsla"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName); //$NON-NLS-1$
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/WslaDocument"); //$NON-NLS-1$
            request.header("Slug", artifactFileName); //$NON-NLS-1$
            request.body("application/xml", contentStream); //$NON-NLS-1$

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("WslaDocument", doc.getExtendedType()); //$NON-NLS-1$
            long size = Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME));
            Assert.assertTrue(size >= 6556L);
            Assert.assertEquals("application/xml", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/WslaDocument/" + uuid); //$NON-NLS-1$
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals("Sample.wsla", doc.getName()); //$NON-NLS-1$
        Assert.assertEquals("application/xml", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$

        ClientResponse<String> content = request.get(String.class);
        String c = content.getEntity();
        Assert.assertNotNull(c);
//        System.out.println("Content=" + content.getEntity());
    }

	/**
	 * Tests adding a wsdl document.
	 * @throws Exception
	 */
	@Test
	public void testWsdlDocumentCreate() throws Exception {
		// Add the PDF to the repository
		String artifactFileName = "sample.wsdl"; //$NON-NLS-1$
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName); //$NON-NLS-1$
		String uuid = null;
		ClientRequest request = null;
		
		try {
			request = clientRequest("/s-ramp/wsdl/WsdlDocument"); //$NON-NLS-1$
			request.header("Slug", artifactFileName); //$NON-NLS-1$
			request.body("application/xml", contentStream); //$NON-NLS-1$

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
            Long size = doc.getContentSize();
            Assert.assertTrue(size >= 1642L);
			Assert.assertEquals("application/xml", doc.getContentType()); //$NON-NLS-1$
			uuid = doc.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Make sure we can query it now
		request = clientRequest("/s-ramp/wsdl/WsdlDocument/" + uuid); //$NON-NLS-1$
		ClientResponse<Entry> response = request.get(Entry.class);
		Entry entry = response.getEntity();
		BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertNotNull(arty);
		Assert.assertTrue(arty instanceof WsdlDocument);
		WsdlDocument wsdlDoc = (WsdlDocument) arty;
		Assert.assertEquals("sample.wsdl", wsdlDoc.getName()); //$NON-NLS-1$

		// Make sure we can query the derived content
		ClientRequest frequest = clientRequest("/s-ramp/wsdl/Message"); //$NON-NLS-1$
		ClientResponse<Feed> fresponse = frequest.get(Feed.class);
		Feed feed = fresponse.getEntity();
		Object totalResultsAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int total = Integer.parseInt(String.valueOf(totalResultsAttr));
		Assert.assertNotNull(feed);
		Assert.assertEquals(2, total);
		String findReqMsgUuid = null;
		for (Entry atomEntry : feed.getEntries()) {
			if ("findRequest".equals(atomEntry.getTitle())) { //$NON-NLS-1$
				findReqMsgUuid = atomEntry.getId().toString();
			}
		}
		Assert.assertNotNull(findReqMsgUuid);

		// Get the full meta data for the derived Message
		request = clientRequest("/s-ramp/wsdl/Message/" + findReqMsgUuid); //$NON-NLS-1$
		response = request.get(Entry.class);
		entry = response.getEntity();
		arty = SrampAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertNotNull(arty);
		Assert.assertTrue(arty instanceof Message);
		Message message = (Message) arty;
		Assert.assertEquals("findRequest", message.getNCName()); //$NON-NLS-1$
		Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", message.getNamespace()); //$NON-NLS-1$
		DocumentArtifactTarget relatedDocumentTarget = message.getRelatedDocument();
		Assert.assertEquals(DocumentArtifactEnum.WSDL_DOCUMENT, relatedDocumentTarget.getArtifactType());
		Assert.assertEquals(uuid, relatedDocumentTarget.getValue());
		List<PartTarget> parts = message.getPart();
		Assert.assertNotNull(parts);
		Assert.assertEquals(1, parts.size());
		Assert.assertEquals(PartEnum.PART, parts.get(0).getArtifactType());
		Assert.assertNotNull(parts.get(0).getValue());
	}

	/**
	 * Tests the multi-part create scenario.
	 */
	@Test
	public void testMultiPartCreate() {
	    String uuid = UUID.randomUUID().toString();
	    
	    try {
	        ClientRequest request = clientRequest("/s-ramp/core/XmlDocument"); //$NON-NLS-1$

	        MultipartRelatedOutput output = new MultipartRelatedOutput();

	        XmlDocument xmlDocument = new XmlDocument();
	        xmlDocument.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
	        xmlDocument.setCreatedBy("kurt"); //$NON-NLS-1$
	        xmlDocument.setDescription("In depth description of this XML document"); //$NON-NLS-1$
	        xmlDocument.setName("PO.xml"); //$NON-NLS-1$
	        xmlDocument.setUuid(uuid); //$NON-NLS-1$
	        xmlDocument.setVersion("1.0"); //$NON-NLS-1$

	        Entry atomEntry = new Entry();
	        Artifact arty = new Artifact();
	        arty.setXmlDocument(xmlDocument);
	        atomEntry.setAnyOtherJAXBObject(arty);

	        MediaType mediaType = new MediaType("application", "atom+xml"); //$NON-NLS-1$ //$NON-NLS-2$
	        output.addPart(atomEntry, mediaType);

	        String artifactFileName = "PO.xml"; //$NON-NLS-1$
	        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
	        MediaType mediaType2 = new MediaType("application", "xml"); //$NON-NLS-1$ //$NON-NLS-2$
	        output.addPart(contentStream, mediaType2);

	        request.body(MultipartConstants.MULTIPART_RELATED, output);

            ClientResponse<Entry> response = request.post(Entry.class);
//
            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
            Assert.assertEquals(uuid,artifact.getXmlDocument().getUuid()); //$NON-NLS-1$
            Long size = artifact.getXmlDocument().getContentSize();
            Assert.assertTrue(size >= 825L);
            Assert.assertEquals(artifactFileName, artifact.getXmlDocument().getName());
	    } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Assert.fail(e.getMessage());
        }
	}

	/**
	 * Tests adding an artifact without a slug.
	 * @throws Exception
	 */
	@Test
	public void testCreateNoSlug() throws Exception {
		// Add the PDF to the repository
		String artifactFileName = "sample.wsdl"; //$NON-NLS-1$
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName); //$NON-NLS-1$
		try {
			ClientRequest request = clientRequest("/s-ramp/wsdl/WsdlDocument"); //$NON-NLS-1$
			request.body("application/xml", contentStream); //$NON-NLS-1$

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals("newartifact.wsdl", entry.getTitle()); //$NON-NLS-1$
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals("newartifact.wsdl", doc.getName()); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	/**
	 * Tests that artifact derivation is happening.
	 * @throws Exception
	 */
	@Test
	public void testArtifactDerivation() throws Exception {
		// Add the PDF to the repository
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
		ClientRequest request = null;
		
		try {
			request = clientRequest("/s-ramp/xsd/XsdDocument"); //$NON-NLS-1$
			request.body("application/xml", contentStream); //$NON-NLS-1$
			ClientResponse<Entry> response = request.post(Entry.class);
			response.getEntity();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Now let's query for the derived artifacts
        request = clientRequest("/s-ramp/xsd/ElementDeclaration"); //$NON-NLS-1$
        ClientResponse<Feed> response = request.get(Feed.class);
        Feed feed = response.getEntity();
        Object totalResultsAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int total = Integer.parseInt(String.valueOf(totalResultsAttr));
        Assert.assertEquals(2, total);
        // TODO: This needs reworked.  Depending on what other tests have run and what's in the repo, the query may
        // return the max 100 entries and leave off what we're looking for.  Query using the titles?
//        Map<String, Entry> entryMap = new HashMap<String, Entry>();
//        for (Entry entry : feed.getEntries()) {
//        	entryMap.put(entry.getTitle(), entry);
//        }
//        Entry purchaseOrder = entryMap.get("purchaseOrder"); //$NON-NLS-1$
//        Assert.assertNotNull(purchaseOrder);
//        Entry comment = entryMap.get("comment"); //$NON-NLS-1$
//        Assert.assertNotNull(comment);
	}

	/**
	 * Does a full test of all the basic Artifact operations.
	 * @throws Exception
	 */
	@Test
	public void testFullPurchaseOrderXSD() throws Exception {
		// Add
		Entry entry = doAddXsd();
		URI entryId = entry.getId();

		// Get
		entry = doGetXsdEntry(entryId);

		// Get artifact content
		String content = doGetXsdContent(entryId);
		verifyXsdContent(content);

		// Update meta data
		doUpdateXsdEntry(entry);
		entry = doGetXsdEntry(entryId);
		verifyEntryUpdated(entry);

		// Update content
		doUpdateXsdContent(entry);
		content = doGetXsdContent(entryId);
		verifyContentUpdated(content);

		deleteXsdEntry(entryId);
		verifyEntryDeleted(entryId);
	}

	/**
	 * Adds an XSD to the repo by POSTing the content to /s-ramp/xsd/XsdDocument.
	 */
	private Entry doAddXsd() throws Exception {
		// Making a client call to the actual XsdDocument implementation running in
		// an embedded container.
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument"); //$NON-NLS-1$

		// read the XsdDocument from file
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
		String xmltext = TestUtils.convertStreamToString(POXsd);
		POXsd.close();

		request.header("Slug", artifactFileName); //$NON-NLS-1$
		request.body(MediaType.APPLICATION_XML, xmltext);

		ClientResponse<Entry> response = request.post(Entry.class);

		Entry entry = response.getEntity();
		Assert.assertEquals(artifactFileName, entry.getTitle());
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		Long size = artifact.getXsdDocument().getContentSize();
        Assert.assertTrue(size >= 2376L);
		Assert.assertEquals(artifactFileName, artifact.getXsdDocument().getName());

		return entry;
	}

	/**
	 * GETs the Atom entry from the repository (to ensure we have the latest).
	 * @param entryId
	 * @throws Exception
	 */
	private Entry doGetXsdEntry(URI entryId) throws Exception {
		// TODO I think the entryId should be of the format urn:{uuid} and we'll need to parse it - this isn't happening right now though
		String uuid = entryId.toString();

		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid); //$NON-NLS-1$
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		Assert.assertNotNull(artifact.getXsdDocument());

		return entry;
	}

	/**
	 * Gets the content for the artifact from the repo.
	 * @param entryId
	 * @throws Exception
	 */
	private String doGetXsdContent(URI entryId) throws Exception {
		String uuid = entryId.toString();

		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid + "/media"); //$NON-NLS-1$ //$NON-NLS-2$
		ClientResponse<String> response = request.get(String.class);

		return response.getEntity();
	}

	/**
	 * Verify that the content returned from the repo is right.
	 * @param content
	 * @throws IOException
	 */
	private void verifyXsdContent(String content) throws IOException {
		Assert.assertNotNull(content);

		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
		try {
			String expectedContent = TestUtils.convertStreamToString(POXsd);
			Assert.assertEquals(expectedContent, content);
		} finally {
			POXsd.close();
		}
	}

	/**
	 * PUTs the Atom entry back into the repository (after making some changes).
	 * @param entry
	 * @throws Exception
	 */
	private void doUpdateXsdEntry(Entry entry) throws Exception {
		// First, make a change to the entry.
		XsdDocument xsdDocument = (XsdDocument) SrampAtomUtils.unwrapSrampArtifact(entry);
		String uuid = xsdDocument.getUuid();
		xsdDocument.setDescription("** Updated description! **"); //$NON-NLS-1$
		SrampModelUtils.setCustomProperty(xsdDocument, "my.property", "Hello World"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampModelUtils.addGenericRelationship(xsdDocument, "NoTargetRel", null); //$NON-NLS-1$

		Artifact arty = new Artifact();
		arty.setXsdDocument(xsdDocument);
		entry.setAnyOtherJAXBObject(arty);

		// Now PUT the changed entry into the repo
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid); //$NON-NLS-1$
		request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry);
		request.put(Void.class);
	}

	/**
	 * Verifies that the entry has been updated, by checking that the s-ramp extended
	 * Atom entry returned contains the classification and custom property set during
	 * the update phase of the test.
	 * @param entry
	 */
	private void verifyEntryUpdated(Entry entry) throws Exception {
		Artifact srampArtifactWrapper = entry.getAnyOtherJAXBObject(Artifact.class);
		XsdDocument xsdDocument = srampArtifactWrapper.getXsdDocument();
		Assert.assertEquals("** Updated description! **", xsdDocument.getDescription()); //$NON-NLS-1$
		Assert.assertEquals("Hello World", SrampModelUtils.getCustomProperty(xsdDocument, "my.property")); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertNull(SrampModelUtils.getCustomProperty(xsdDocument, "my.missing.property")); //$NON-NLS-1$
		Assert.assertNotNull(SrampModelUtils.getGenericRelationship(xsdDocument, "NoTargetRel")); //$NON-NLS-1$
		Assert.assertNull(SrampModelUtils.getGenericRelationship(xsdDocument, "MissingRel")); //$NON-NLS-1$
	}

	/**
	 * Updates the content of the artifact.
	 * @param entry
	 * @throws Exception
	 */
	private void doUpdateXsdContent(Entry entry) throws Exception {
		XsdDocument xsdDocument = (XsdDocument) SrampAtomUtils.unwrapSrampArtifact(entry);
		String uuid = xsdDocument.getUuid();
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid + "/media"); //$NON-NLS-1$ //$NON-NLS-2$

		// read the XsdDocument from file
		String artifactFileName = "PO-updated.xsd"; //$NON-NLS-1$
		InputStream xsdStream = null;
		try {
			xsdStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
			request.body(MediaType.APPLICATION_XML, xsdStream);
			request.put(Void.class);
		} finally {
			IOUtils.closeQuietly(xsdStream);
		}
	}

	/**
	 * Confirms that the content was updated.
	 * @param content
	 * @throws IOException
	 */
	private void verifyContentUpdated(String content) throws IOException {
		Assert.assertNotNull(content);

		InputStream xsdStream = this.getClass().getResourceAsStream("/sample-files/xsd/PO-updated.xsd"); //$NON-NLS-1$
		try {
			String expectedContent = TestUtils.convertStreamToString(xsdStream);
			Assert.assertEquals(expectedContent, content);
		} finally {
			xsdStream.close();
		}
	}

	/**
	 * Delete the XSD entry with the given uuid.
	 * @param entryId
	 * @throws Exception
	 */
	private void deleteXsdEntry(URI entryId) throws Exception {
		String uuid = entryId.toString();
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid); //$NON-NLS-1$
		request.delete(Void.class);
	}

	/**
	 * Verify that the artifact was really deleted.
	 * @throws Exception
	 */
	private void verifyEntryDeleted(URI entryId) throws Exception {
		String uuid = entryId.toString();

		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid); //$NON-NLS-1$
		try {
			request.get(String.class);
			Assert.fail("Expected an 'Artifact not found.' error here."); //$NON-NLS-1$
		} catch (SrampAtomException e) {
			Assert.assertTrue(e.getMessage().startsWith("No artifact found with UUID:")); //$NON-NLS-1$
		}
	}

}
