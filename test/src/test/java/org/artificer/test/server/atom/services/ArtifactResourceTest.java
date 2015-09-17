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
package org.artificer.test.server.atom.services;

import org.apache.commons.io.IOUtils;
import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.providers.ArtificerServerExceptionProvider;
import org.artificer.client.ClientRequest;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.MediaType;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.test.TestUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

/**
 * Test of the jax-rs resource that handles Artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactResourceTest extends AbstractResourceTest {

    String uuid = null;

	/**
	 * @throws Exception
	 */
	@Test
	public void testDerivedArtifactCreate() throws Exception {
		// Making a client call to the actual XsdDocument implementation running in
		// an embedded container.
		ClientRequest request = clientRequest("/s-ramp/xsd/ElementDeclaration");

		// read the XsdDocument from file
		String artifactFileName = "PO.xsd";
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		String xmltext = TestUtils.convertStreamToString(POXsd);
		POXsd.close();

		request.header("Slug", artifactFileName);
		request.body(MediaType.APPLICATION_XML, xmltext);

		try {
			request.post(String.class);
			Assert.fail("Expected an error here.");
		} catch (ArtificerServerException e) {
			Assert.assertEquals("Failed to create artifact because \"ElementDeclaration\" is a derived type.", e.getMessage());
			String stack = ArtificerServerExceptionProvider.getRootStackTrace(e);
			Assert.assertTrue(stack.contains("ArtifactResource.create"));
		}
	}

	/**
	 * Tests adding a PDF document.
	 * @throws Exception
	 */
	@Test
	public void testPDFDocument() throws Exception {
		// Add the PDF to the repository
		String artifactFileName = "sample.pdf";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
		//String uuid = null;
		try {
			ClientRequest request = clientRequest("/s-ramp/core/Document");
			request.header("Slug", artifactFileName);
			request.body("application/pdf", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof Document);
			Document doc = (Document) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Assert.assertEquals(Long.valueOf(218882), doc.getContentSize());
			Assert.assertEquals("application/pdf", doc.getContentType());
			uuid = doc.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Make sure we can query it now
		ClientRequest request = clientRequest("/s-ramp/core/Document/" + uuid);
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertTrue(arty instanceof Document);
		Document doc = (Document) arty;
		Assert.assertEquals(artifactFileName, doc.getName());
		Assert.assertEquals(Long.valueOf(218882), doc.getContentSize());
		Assert.assertEquals("sample.pdf", doc.getName());
		Assert.assertEquals("application/pdf", doc.getContentType());
        //Obtain the content for visual inspection
        ClientRequest request2 = clientRequest("/s-ramp/core/Document/" + uuid + "/media");
        ClientResponse<InputStream> response2 = request2.get(InputStream.class);
        if (response2.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                + response.getStatus());
        }
        InputStream in = response2.getEntity();
        File file = new File("target/SRAMP-sample.pdf");
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
        artifact.setExtendedType("FooApplication");
        artifact.setName("Extended Artifact Name");
        artifact.setDescription("Extended Artifact Description");
        ClientRequest request = clientRequest("/s-ramp/ext/FooApplication");
        Entry requestEntry = ArtificerAtomUtils.wrapSrampArtifact(artifact);
        request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, requestEntry);

        ClientResponse<Entry> response = request.post(Entry.class);

        Entry responseEntry = response.getEntity();
        Assert.assertEquals("Extended Artifact Name", responseEntry.getTitle());
        BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(responseEntry);
        Assert.assertTrue(arty instanceof ExtendedArtifactType);
        ExtendedArtifactType extArty = (ExtendedArtifactType) arty;
        Assert.assertEquals("Extended Artifact Name", extArty.getName());
        Assert.assertEquals("Extended Artifact Description", extArty.getDescription());
        uuid = extArty.getUuid();

        // Make sure we can query it now
        request = clientRequest("/s-ramp/ext/FooApplication/" + uuid);
        response = request.get(Entry.class);

        Entry entry = response.getEntity();
        arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedArtifactType);
        extArty = (ExtendedArtifactType) arty;
        Assert.assertEquals("Extended Artifact Name", extArty.getName());
        Assert.assertEquals("Extended Artifact Description", extArty.getDescription());
    }

	/**
     * Tests adding a BRMS Pkg document.
     * @throws Exception
     */
    @Test
    public void testBrmsPkgDocument() throws Exception {
        // Add the pkg to the repository
        String artifactFileName = "defaultPackage.pkg";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/BrmsPkgDocument");
            request.header("Slug", artifactFileName);
            request.body("application/octet-stream", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("BrmsPkgDocument", doc.getExtendedType());
            Assert.assertEquals(Long.valueOf(17043), Long.valueOf(doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME)));
            Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/BrmsPkgDocument/" + uuid);
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(17043), Long.valueOf(doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME)));
        Assert.assertEquals("defaultPackage.pkg", doc.getName());
        Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));
    }

    /**
     * Tests adding a JPG document.
     * @throws Exception
     */
    @Test
    public void testJPGDocument() throws Exception {
        // Add the jpg to the repository
        String artifactFileName = "photo.jpg";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/JpgDocument");
            request.header("Slug", artifactFileName);
            request.body("application/octet-stream", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("JpgDocument", doc.getExtendedType());
            Assert.assertEquals(Long.valueOf(2398), Long.valueOf(doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME)));
            Assert.assertEquals("image/jpeg", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/JpgDocument/" + uuid);
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(2398), Long.valueOf(doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME)));
        Assert.assertEquals("photo.jpg", doc.getName());
        Assert.assertEquals("image/jpeg", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));

        //Obtain the content for visual inspection
        ClientRequest request2 = clientRequest("/s-ramp/ext/JpgDocument/" + uuid + "/media");
        ClientResponse<InputStream> response2 = request2.get(InputStream.class);
        if (response2.getStatus() != 200) {
            throw new RuntimeException("Failed : HTTP error code : "
                + response.getStatus());
        }
        InputStream in = response2.getEntity();
        File file = new File("target/SRAMP-photo.jpg");
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
        String artifactFileName = "Evaluation.bpmn";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/BpmnDocument");
            request.header("Slug", artifactFileName);
            request.body("application/xml", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("BpmnDocument", doc.getExtendedType());
            long size = Long.valueOf(doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME));
            Assert.assertTrue(size >= 12482L);
            Assert.assertEquals("application/xml", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/BpmnDocument/" + uuid);
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals("Evaluation.bpmn", doc.getName());
        Assert.assertEquals("application/xml", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));

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
        String artifactFileName = "Sample.wsla";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/ext/WslaDocument");
            request.header("Slug", artifactFileName);
            request.body("application/xml", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof ExtendedDocument);
            ExtendedDocument doc = (ExtendedDocument) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("WslaDocument", doc.getExtendedType());
            long size = Long.valueOf(doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_SIZE_QNAME));
            Assert.assertTrue(size >= 6556L);
            Assert.assertEquals("application/xml", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = clientRequest("/s-ramp/ext/WslaDocument/" + uuid);
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof ExtendedDocument);
        ExtendedDocument doc = (ExtendedDocument) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals("Sample.wsla", doc.getName());
        Assert.assertEquals("application/xml", doc.getOtherAttributes().get(ArtificerConstants.SRAMP_CONTENT_TYPE_QNAME));

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
		String artifactFileName = "sample.wsdl";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName);
		String uuid = null;
		ClientRequest request = null;
		
		try {
			request = clientRequest("/s-ramp/wsdl/WsdlDocument");
			request.header("Slug", artifactFileName);
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
            Long size = doc.getContentSize();
            Assert.assertTrue(size >= 1642L);
			Assert.assertEquals("application/xml", doc.getContentType());
			uuid = doc.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Make sure we can query it now
		request = clientRequest("/s-ramp/wsdl/WsdlDocument/" + uuid);
		ClientResponse<Entry> response = request.get(Entry.class);
		Entry entry = response.getEntity();
		BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertNotNull(arty);
		Assert.assertTrue(arty instanceof WsdlDocument);
		WsdlDocument wsdlDoc = (WsdlDocument) arty;
		Assert.assertEquals("sample.wsdl", wsdlDoc.getName());

		// Make sure we can query the derived content
		ClientRequest frequest = clientRequest("/s-ramp/wsdl/Message");
		ClientResponse<Feed> fresponse = frequest.get(Feed.class);
		Feed feed = fresponse.getEntity();
		Object totalResultsAttr = feed.getExtensionAttributes().get(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int total = Integer.parseInt(String.valueOf(totalResultsAttr));
		Assert.assertNotNull(feed);
		Assert.assertEquals(2, total);
		String findReqMsgUuid = null;
		for (Entry atomEntry : feed.getEntries()) {
			if ("findRequest".equals(atomEntry.getTitle())) {
				findReqMsgUuid = atomEntry.getId().toString().replace("urn:uuid:", "");
			}
		}
		Assert.assertNotNull(findReqMsgUuid);

		// Get the full meta data for the derived Message
		request = clientRequest("/s-ramp/wsdl/Message/" + findReqMsgUuid);
		response = request.get(Entry.class);
		entry = response.getEntity();
		arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertNotNull(arty);
		Assert.assertTrue(arty instanceof Message);
		Message message = (Message) arty;
		Assert.assertEquals("findRequest", message.getNCName());
		Assert.assertEquals("http://ewittman.redhat.com/sample/2012/09/wsdl/sample.wsdl", message.getNamespace());
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
	        ClientRequest request = clientRequest("/s-ramp/core/XmlDocument");

	        MultipartRelatedOutput output = new MultipartRelatedOutput();

	        XmlDocument xmlDocument = new XmlDocument();
	        xmlDocument.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
	        xmlDocument.setCreatedBy("kurt");
	        xmlDocument.setDescription("In depth description of this XML document");
	        xmlDocument.setName("PO.xml");
	        xmlDocument.setUuid(uuid); 
	        xmlDocument.setVersion("1.0");

	        Entry atomEntry = new Entry();
	        Artifact arty = new Artifact();
	        arty.setXmlDocument(xmlDocument);
	        atomEntry.setAnyOtherJAXBObject(arty);

	        MediaType mediaType = new MediaType("application", "atom+xml");
	        output.addPart(atomEntry, mediaType);

	        String artifactFileName = "PO.xml";
	        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
	        MediaType mediaType2 = new MediaType("application", "xml");
	        output.addPart(contentStream, mediaType2);

	        request.body(MultipartConstants.MULTIPART_RELATED, output);

            ClientResponse<Entry> response = request.post(Entry.class);
//
            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
            Assert.assertEquals(uuid,artifact.getXmlDocument().getUuid()); 
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
		String artifactFileName = "sample.wsdl";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName);
		try {
			ClientRequest request = clientRequest("/s-ramp/wsdl/WsdlDocument");
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals("newartifact.wsdl", entry.getTitle());
			BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals("newartifact.wsdl", doc.getName());
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
		String artifactFileName = "PO.xsd";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		ClientRequest request = null;
		
		try {
			request = clientRequest("/s-ramp/xsd/XsdDocument");
			request.body("application/xml", contentStream);
			ClientResponse<Entry> response = request.post(Entry.class);
			response.getEntity();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Now let's query for the derived artifacts
        request = clientRequest("/s-ramp/xsd/ElementDeclaration");
        ClientResponse<Feed> response = request.get(Feed.class);
        Feed feed = response.getEntity();
        Object totalResultsAttr = feed.getExtensionAttributes().get(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int total = Integer.parseInt(String.valueOf(totalResultsAttr));
        Assert.assertEquals(2, total);
        // TODO: This needs reworked.  Depending on what other tests have run and what's in the repo, the query may
        // return the max 100 entries and leave off what we're looking for.  Query using the titles?
//        Map<String, Entry> entryMap = new HashMap<String, Entry>();
//        for (Entry entry : feed.getEntries()) {
//        	entryMap.put(entry.getTitle(), entry);
//        }
//        Entry purchaseOrder = entryMap.get("purchaseOrder");
//        Assert.assertNotNull(purchaseOrder);
//        Entry comment = entryMap.get("comment");
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

		deleteXsdEntry(entryId);
		verifyEntryDeleted(entryId);
	}

	/**
	 * Adds an XSD to the repo by POSTing the content to /s-ramp/xsd/XsdDocument.
	 */
	private Entry doAddXsd() throws Exception {
		// Making a client call to the actual XsdDocument implementation running in
		// an embedded container.
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument");

		// read the XsdDocument from file
		String artifactFileName = "PO.xsd";
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		String xmltext = TestUtils.convertStreamToString(POXsd);
		POXsd.close();

		request.header("Slug", artifactFileName);
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
		String uuid = entryId.toString().replace("urn:uuid:", "");

		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid);
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
		String uuid = entryId.toString().replace("urn:uuid:", "");

		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid + "/media");
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

		String artifactFileName = "PO.xsd";
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
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
		XsdDocument xsdDocument = (XsdDocument) ArtificerAtomUtils.unwrapSrampArtifact(entry);
		String uuid = xsdDocument.getUuid();
		xsdDocument.setDescription("** Updated description! **");
		ArtificerModelUtils.setCustomProperty(xsdDocument, "my.property", "Hello World");

		Artifact arty = new Artifact();
		arty.setXsdDocument(xsdDocument);
		entry.setAnyOtherJAXBObject(arty);

		// Now PUT the changed entry into the repo
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid);
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
		Assert.assertEquals("** Updated description! **", xsdDocument.getDescription());
		Assert.assertEquals("Hello World", ArtificerModelUtils.getCustomProperty(xsdDocument, "my.property"));
		Assert.assertNull(ArtificerModelUtils.getCustomProperty(xsdDocument, "my.missing.property"));
		Assert.assertNull(ArtificerModelUtils.getGenericRelationship(xsdDocument, "MissingRel"));
	}

	/**
	 * Confirms that the content was updated.
	 * @param content
	 * @throws IOException
	 */
	private void verifyContentUpdated(String content) throws IOException {
		Assert.assertNotNull(content);

		InputStream xsdStream = this.getClass().getResourceAsStream("/sample-files/xsd/PO-updated.xsd");
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
		String uuid = entryId.toString().replace("urn:uuid:", "");
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid);
		request.delete(Void.class);
	}

	/**
	 * Verify that the artifact was really deleted.
	 * @throws Exception
	 */
	private void verifyEntryDeleted(URI entryId) throws Exception {
		String uuid = entryId.toString().replace("urn:uuid:", "");

		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid);
		try {
			request.get(String.class);
			Assert.fail("Expected an 'Artifact not found.' error here.");
		} catch (ArtificerServerException e) {
			Assert.assertTrue(e.getMessage().contains("No artifact found"));
		}
	}

}
