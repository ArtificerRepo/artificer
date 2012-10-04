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
package org.overlord.sramp.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.overlord.sramp.SrampConstants;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.err.SrampAtomExceptionMapper;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.jcr.JCRRepositoryCleaner;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Document;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

import test.org.overlord.sramp.atom.TestUtils;

/**
 * Test of the jax-rs resource that handles Artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactResourceTest extends BaseResourceTest {

    String uuid = null;
    
	@BeforeClass
	public void setUp() throws Exception {
		// bring up the embedded container with the ArtifactResource deployed.
		getProviderFactory().registerProvider(SrampAtomExceptionMapper.class);
		dispatcher.getRegistry().addPerRequestResource(ArtifactResource.class);
	}
	
	@Before
	public void clean() {
	    new JCRRepositoryCleaner().clean();
	}
	
	@AfterClass
	public static void cleanup() {
	    PersistenceFactory.newInstance().shutdown();
	}
	/**
	 * @throws Exception
	 */
	@Test
	public void testDerivedArtifactCreate() throws Exception {
		// Making a client call to the actual XsdDocument implementation running in
		// an embedded container.
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/ElementDeclaration"));

		// read the XsdDocument from file
		String artifactFileName = "PO.xsd";
		InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		String xmltext = TestUtils.convertStreamToString(POXsd);
		POXsd.close();

		request.header("Slug", artifactFileName);
		request.body(MediaType.APPLICATION_XML, xmltext);

		ClientResponse<String> response = request.post(String.class);
		Assert.assertEquals(500, response.getStatus());
		String stack = response.getEntity();
		Assert.assertTrue(stack.contains("Failed to create artifact because 'ElementDeclaration' is a derived type."));
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
			ClientRequest request = new ClientRequest(generateURL("/s-ramp/core/Document"));
			request.header("Slug", artifactFileName);
			request.body("application/pdf", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof Document);
			Document doc = (Document) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Assert.assertEquals(Long.valueOf(218882), doc.getContentSize());
			Assert.assertEquals("application/pdf", doc.getContentType());
			uuid = doc.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
		
		PersistenceFactory.newInstance().shutdown();
		setUp();

		// Make sure we can query it now
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/core/Document/" + uuid));
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertTrue(arty instanceof Document);
		Document doc = (Document) arty;
		Assert.assertEquals(artifactFileName, doc.getName());
		Assert.assertEquals(Long.valueOf(218882), doc.getContentSize());
		Assert.assertEquals("sample.pdf", doc.getName());
		Assert.assertEquals("application/pdf", doc.getContentType());
        //Obtain the content for visual inspection
        ClientRequest request2 = new ClientRequest(generateURL("/s-ramp/core/Document/" + uuid + "/media"));
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
     * Tests adding a PDF document.
     * @throws Exception
     */
    @Test
    public void testDroolsPkgDocument() throws Exception {
        // Add the pkg to the repository
        String artifactFileName = "defaultPackage.pkg";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/user/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/DroolsPkgDocument"));
            request.header("Slug", artifactFileName);
            request.body("application/octet-stream", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof UserDefinedArtifactType);
            UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("DroolsPkgDocument", doc.getUserType());
            Assert.assertEquals(Long.valueOf(17043), Long.valueOf(doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_SIZE))));
            Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE)));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/DroolsPkgDocument/" + uuid));
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof UserDefinedArtifactType);
        UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(17043), Long.valueOf(doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_SIZE))));
        Assert.assertEquals("defaultPackage.pkg", doc.getName());
        Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE)));
    }
    
    /**
     * Tests adding a PDF document.
     * @throws Exception
     */
    @Test
    public void testJPGDocument() throws Exception {
        // Add the jpg to the repository
        String artifactFileName = "photo.jpg";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/user/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/JpgDocument"));
            request.header("Slug", artifactFileName);
            request.body("application/octet-stream", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof UserDefinedArtifactType);
            UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("JpgDocument", doc.getUserType());
            Assert.assertEquals(Long.valueOf(2966447), Long.valueOf(doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_SIZE))));
            Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE)));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/JpgDocument/" + uuid));
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof UserDefinedArtifactType);
        UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(2966447), Long.valueOf(doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_SIZE))));
        Assert.assertEquals("photo.jpg", doc.getName());
        Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE)));
        
        //Obtain the content for visual inspection
        ClientRequest request2 = new ClientRequest(generateURL("/s-ramp/user/JpgDocument/" + uuid + "/media"));
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
    public void testBpmnUserDefinedDocumentCreate() throws Exception {
        // Add the BPMN process to the repository
        String artifactFileName = "Evaluation.bpmn";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/user/" + artifactFileName);
        String uuid = null;
        try {
            ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/BpmnDocument"));
            request.header("Slug", artifactFileName);
            request.body("application/xml", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof UserDefinedArtifactType);
            UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
            Assert.assertEquals(artifactFileName, doc.getName());
            Assert.assertEquals("BpmnDocument", doc.getUserType());
            Assert.assertEquals(Long.valueOf(12482), Long.valueOf(doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_SIZE))));
            Assert.assertEquals("application/xml", doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE)));
            uuid = doc.getUuid();
        } finally {
            IOUtils.closeQuietly(contentStream);
        }

        // Make sure we can query it now
        ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/BpmnDocument/" + uuid));
        ClientResponse<Entry> response = request.get(Entry.class);

        Entry entry = response.getEntity();
        BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
        Assert.assertTrue(arty instanceof UserDefinedArtifactType);
        UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
        Assert.assertEquals(artifactFileName, doc.getName());
        Assert.assertEquals(Long.valueOf(12482), Long.valueOf(doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_SIZE))));
        Assert.assertEquals("Evaluation.bpmn", doc.getName());
        Assert.assertEquals("application/xml", doc.getOtherAttributes().get(new QName(SrampConstants.SRAMP_CONTENT_TYPE)));
        
        ClientResponse<String> content = request.get(String.class);
        System.out.println("Content=" + content.getEntity());
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
		try {
			ClientRequest request = new ClientRequest(generateURL("/s-ramp/wsdl/WsdlDocument"));
			request.header("Slug", artifactFileName);
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Assert.assertEquals(Long.valueOf(1643), doc.getContentSize());
			Assert.assertEquals("application/xml", doc.getContentType());
			uuid = doc.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Make sure we can query it now
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/wsdl/WsdlDocument/" + uuid));
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
		Assert.assertNotNull(arty);
		Assert.assertTrue(arty instanceof WsdlDocument);
		WsdlDocument wsdlDoc = (WsdlDocument) arty;
		Assert.assertEquals(Long.valueOf(1643), wsdlDoc.getContentSize());
		Assert.assertEquals("sample.wsdl", wsdlDoc.getName());
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
			ClientRequest request = new ClientRequest(generateURL("/s-ramp/wsdl/WsdlDocument"));
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals("newartifact.wsdl", entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals("newartifact.wsdl", doc.getName());
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
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
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));

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
		Assert.assertEquals(Long.valueOf(2376), artifact.getXsdDocument().getContentSize());
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

		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
		ClientResponse<Entry> response = request.get(Entry.class);

		Entry entry = response.getEntity();
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		Assert.assertNotNull(artifact.getXsdDocument());
		Assert.assertEquals(Long.valueOf(2376), artifact.getXsdDocument().getContentSize());

		return entry;
	}

	/**
	 * Gets the content for the artifact from the repo.
	 * @param entryId
	 * @throws Exception
	 */
	private String doGetXsdContent(URI entryId) throws Exception {
		String uuid = entryId.toString();

		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid + "/media"));
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
		XsdDocument xsdDocument = (XsdDocument) SrampAtomUtils.unwrapSrampArtifact(entry);
		String uuid = xsdDocument.getUuid();
		xsdDocument.setDescription("** Updated description! **");

		Artifact arty = new Artifact();
		arty.setXsdDocument(xsdDocument);
		entry.setAnyOtherJAXBObject(arty);

		// Now PUT the changed entry into the repo
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
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
	}

	/**
	 * Updates the content of the artifact.
	 * @param entry
	 * @throws Exception
	 */
	private void doUpdateXsdContent(Entry entry) throws Exception {
		XsdDocument xsdDocument = (XsdDocument) SrampAtomUtils.unwrapSrampArtifact(entry);
		String uuid = xsdDocument.getUuid();
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid + "/media"));

		// read the XsdDocument from file
		String artifactFileName = "PO-updated.xsd";
		InputStream xsdStream = null;
		try {
			xsdStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
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
		String uuid = entryId.toString();
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
		request.delete(Void.class);
	}

	/**
	 * Verify that the artifact was really deleted.
	 * @throws Exception
	 */
	private void verifyEntryDeleted(URI entryId) throws Exception {
		String uuid = entryId.toString();

		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
		ClientResponse<String> response = request.get(String.class);

		String stacktrace = response.getEntity();
		Assert.assertTrue(stacktrace.contains("Artifact not found."));
	}

}
