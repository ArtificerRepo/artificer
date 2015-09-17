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
package org.artificer.test.server.atom.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.artificer.common.MediaType;
import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.beans.HttpResponseBean;
import org.artificer.client.ClientRequest;
import org.artificer.common.ArtificerModelUtils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Unit test for the {@link org.artificer.server.atom.services.BatchResource} class.
 *
 * TODO add a test for 409 Conflict (e.g. update an artifact that doesn't exist)
 *
 * @author eric.wittmann@redhat.com
 */
public class BatchResourceTest extends AbstractResourceTest {

	@Test
	public void testZipPackage() throws Exception {
		ArtificerArchive archive = null;
		InputStream xsd1ContentStream = null;
		InputStream xsd2ContentStream = null;
		File zipFile = null;
		InputStream zipStream = null;
		ClientRequest request = null;

		try {
			// Create a test s-ramp archive
			archive = new ArtificerArchive();
			xsd1ContentStream = this.getClass().getResourceAsStream("/sample-files/xsd/PO.xsd");
			BaseArtifactType metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
			metaData.setName("PO.xsd");
			archive.addEntry("schemas/PO.xsd", metaData, xsd1ContentStream);
			xsd2ContentStream = this.getClass().getResourceAsStream("/sample-files/xsd/XMLSchema.xsd");
			metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
			metaData.setName("XMLSchema.xsd");
			metaData.setVersion("1.0");
			archive.addEntry("schemas/XMLSchema.xsd", metaData, xsd2ContentStream);

			zipFile = archive.pack();
			zipStream = FileUtils.openInputStream(zipFile);

			// Now POST the archive to the s-ramp repository (POST to /s-ramp as application/zip)
			request = clientRequest("/s-ramp");
			request.body(MediaType.APPLICATION_ZIP, zipStream);
			ClientResponse<MultipartInput> clientResponse = request.post(MultipartInput.class);

			// Process the response - it should be multipart/mixed with each part being
			// itself an http response with a code, content-id, and an s-ramp atom entry
			// body
			MultipartInput response = clientResponse.getEntity();
			List<InputPart> parts = response.getParts();
			Map<String, BaseArtifactType> artyMap = new HashMap<String, BaseArtifactType>();
			for (InputPart part : parts) {
				String id = part.getHeaders().getFirst("Content-ID");
				HttpResponseBean rbean = part.getBody(HttpResponseBean.class, null);
				Assert.assertEquals(201, rbean.getCode());
				Entry entry = (Entry) rbean.getBody();
				BaseArtifactType artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
				artyMap.put(id, artifact);
			}

			Assert.assertTrue(artyMap.keySet().contains("<schemas/PO.xsd@package>"));
			Assert.assertTrue(artyMap.keySet().contains("<schemas/XMLSchema.xsd@package>"));

			// Assertions for artifact 1
			BaseArtifactType arty = artyMap.get("<schemas/PO.xsd@package>");
			Assert.assertNotNull(arty);
			Assert.assertEquals("PO.xsd", arty.getName());
			Assert.assertNull(arty.getVersion());

			arty = artyMap.get("<schemas/XMLSchema.xsd@package>");
			Assert.assertNotNull(arty);
			Assert.assertEquals("XMLSchema.xsd", arty.getName());
			Assert.assertEquals("1.0", arty.getVersion());
		} finally {
			IOUtils.closeQuietly(xsd1ContentStream);
			IOUtils.closeQuietly(xsd2ContentStream);
			ArtificerArchive.closeQuietly(archive);
			IOUtils.closeQuietly(zipStream);
			FileUtils.deleteQuietly(zipFile);
		}

		// Verify by querying
		// Do a query using GET with query params
		request = clientRequest("/s-ramp/xsd/XsdDocument");
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());
		Set<String> artyNames = new HashSet<String>();
		for (Entry entry : feed.getEntries()) {
			artyNames.add(entry.getTitle());
		}
		Assert.assertTrue(artyNames.contains("PO.xsd"));
		Assert.assertTrue(artyNames.contains("XMLSchema.xsd"));
	}

	/**
	 * This also tests the zipPackage method of the {@link org.artificer.server.atom.services.BatchResource} class, but it is
	 * more thorough.  It tests adding new content, updating existing content, etc.
	 */
	@Test
	public void testZipPackage_Multi() throws Exception {
		ArtificerArchive archive = null;
		InputStream xsd1ContentStream = null;
		InputStream wsdlContentStream = null;
		File zipFile = null;
		InputStream zipStream = null;
        ClientRequest request = null;

		WsdlDocument wsdlDoc = createWsdlArtifact();
		XmlDocument xmlDoc = createXmlArtifact();

		String xsdUuid = null;
		String wsdlUuid = null;
		String xmlUuid = null;

		try {
			// Create a test s-ramp archive
			archive = new ArtificerArchive();

			// A new XSD document
			xsd1ContentStream = this.getClass().getResourceAsStream("/sample-files/xsd/PO.xsd");
			BaseArtifactType metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
			metaData.setUuid(UUID.randomUUID().toString()); // will be ignored
			metaData.setName("PO.xsd");
			archive.addEntry("schemas/PO.xsd", metaData, xsd1ContentStream);
			// Update an existing WSDL document (content and meta-data)
			wsdlContentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/sample-updated.wsdl");
			metaData = wsdlDoc;
			metaData.setVersion("2.0");
			ArtificerModelUtils.setCustomProperty(metaData, "foo", "bar");
			archive.addEntry("wsdl/sample.wsdl", metaData, wsdlContentStream);
			// Update an existing XML document (meta-data only)
			metaData = xmlDoc;
			metaData.setVersion("3.0");
			ArtificerModelUtils.setCustomProperty(metaData, "far", "baz");
			archive.addEntry("core/PO.xml", metaData, null);

			zipFile = archive.pack();
			zipStream = FileUtils.openInputStream(zipFile);

			// Now POST the archive to the s-ramp repository (POST to /s-ramp as application/zip)
			request = clientRequest("/s-ramp");
			request.body(MediaType.APPLICATION_ZIP, zipStream);
			ClientResponse<MultipartInput> clientResponse = request.post(MultipartInput.class);

			// Process the response - it should be multipart/mixed with each part being
			// itself an http response with a code, content-id, and an s-ramp atom entry
			// body
			MultipartInput response = clientResponse.getEntity();
			List<InputPart> parts = response.getParts();
			Map<String, HttpResponseBean> respMap = new HashMap<String, HttpResponseBean>();
			for (InputPart part : parts) {
				String id = part.getHeaders().getFirst("Content-ID");
				HttpResponseBean rbean = part.getBody(HttpResponseBean.class, null);
				respMap.put(id, rbean);
			}

			// Should be three responses.
			Assert.assertEquals(3, respMap.size());
			Assert.assertTrue(respMap.keySet().contains("<schemas/PO.xsd@package>"));
			Assert.assertTrue(respMap.keySet().contains("<wsdl/sample.wsdl@package>"));
			Assert.assertTrue(respMap.keySet().contains("<core/PO.xml@package>"));

			// Assertions for artifact 1 (PO.xsd)
			HttpResponseBean httpResp = respMap.get("<schemas/PO.xsd@package>");
			Assert.assertEquals(201, httpResp.getCode());
			Assert.assertEquals("Created", httpResp.getStatus());
			Entry entry = (Entry) httpResp.getBody();
			BaseArtifactType artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertEquals("PO.xsd", artifact.getName());
			Assert.assertNull(artifact.getVersion());
			Long size = ((XsdDocument) artifact).getContentSize();
            Assert.assertTrue(size >= 2376L);
			xsdUuid = artifact.getUuid();

			// Assertions for artifact 2 (sample.wsdl)
			httpResp = respMap.get("<wsdl/sample.wsdl@package>");
			Assert.assertEquals(200, httpResp.getCode());
			Assert.assertEquals("OK", httpResp.getStatus());
			entry = (Entry) httpResp.getBody();
			artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertEquals("sample.wsdl", artifact.getName());
			Assert.assertEquals("2.0", artifact.getVersion());
			wsdlUuid = artifact.getUuid();

			// Assertions for artifact 3 (PO.xml)
			httpResp = respMap.get("<core/PO.xml@package>");
			Assert.assertEquals(200, httpResp.getCode());
			Assert.assertEquals("OK", httpResp.getStatus());
			entry = (Entry) httpResp.getBody();
			artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertEquals("PO.xml", artifact.getName());
			Assert.assertEquals("3.0", artifact.getVersion());
			xmlUuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(xsd1ContentStream);
			IOUtils.closeQuietly(wsdlContentStream);
			ArtificerArchive.closeQuietly(archive);
			IOUtils.closeQuietly(zipStream);
			FileUtils.deleteQuietly(zipFile);
		}

		// Verify by querying
		// Do a query using GET with query params
		Map<String, BaseArtifactType> artyMap = new HashMap<String, BaseArtifactType>();
		request = clientRequest("/s-ramp/xsd/XsdDocument");
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Assert.assertEquals(1, feed.getEntries().size());
		for (Entry entry : feed.getEntries()) {
		    String uuid = entry.getId().toString().replace("urn:uuid:", "");
			request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid);
			BaseArtifactType artifact = ArtificerAtomUtils.unwrapSrampArtifact(request.get(Entry.class).getEntity());
			artyMap.put(artifact.getUuid(), artifact);
		}
		request = clientRequest("/s-ramp/wsdl/WsdlDocument");
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(1, feed.getEntries().size());
		for (Entry entry : feed.getEntries()) {
		    String uuid = entry.getId().toString().replace("urn:uuid:", "");
            request = clientRequest("/s-ramp/wsdl/WsdlDocument/" + uuid);
			BaseArtifactType artifact = ArtificerAtomUtils.unwrapSrampArtifact(request.get(Entry.class).getEntity());
			artyMap.put(artifact.getUuid(), artifact);
		}
		request = clientRequest("/s-ramp/core/XmlDocument");
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(1, feed.getEntries().size());
		for (Entry entry : feed.getEntries()) {
		    String uuid = entry.getId().toString().replace("urn:uuid:", "");
            request = clientRequest("/s-ramp/core/XmlDocument/" + uuid);
			BaseArtifactType artifact = ArtificerAtomUtils.unwrapSrampArtifact(request.get(Entry.class).getEntity());
			artyMap.put(artifact.getUuid(), artifact);
		}

		Assert.assertEquals(3, artyMap.size());

		// Assertions for artifact 1 (PO.xsd)
		BaseArtifactType artifact = artyMap.get(xsdUuid);
		Assert.assertEquals("PO.xsd", artifact.getName());
		Assert.assertNull(artifact.getVersion());

		// Assertions for artifact 2 (sample.wsdl)
		artifact = artyMap.get(wsdlUuid);
		Assert.assertEquals("sample.wsdl", artifact.getName());
		Assert.assertEquals("2.0", artifact.getVersion());

		// Assertions for artifact 3 (PO.xml)
		artifact = artyMap.get(xmlUuid);
		Assert.assertEquals("PO.xml", artifact.getName());
		Assert.assertEquals("3.0", artifact.getVersion());
	}

	/**
	 * Creates a WSDL artifact in the s-ramp repository.
	 * @return the new artifact
	 * @throws Exception
	 */
	private XmlDocument createXmlArtifact() throws Exception {
		String artifactFileName = "PO.xml";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
		try {
			ClientRequest request = clientRequest("/s-ramp/core/XmlDocument");
			request.header("Slug", artifactFileName);
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof XmlDocument);
			XmlDocument doc = (XmlDocument) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Long size = doc.getContentSize();
            Assert.assertTrue(size >= 825L);
			Assert.assertEquals("application/xml", doc.getContentType());
			return doc;
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	/**
	 * Creates a WSDL artifact in the s-ramp repository.
	 * @return the new artifact
	 * @throws Exception
	 */
	private WsdlDocument createWsdlArtifact() throws Exception {
		String artifactFileName = "sample.wsdl";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName);
		try {
			ClientRequest request = clientRequest("/s-ramp/wsdl/WsdlDocument");
			request.header("Slug", artifactFileName);
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = ArtificerAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof WsdlDocument);
			WsdlDocument doc = (WsdlDocument) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Assert.assertEquals("application/xml", doc.getContentType());
			return doc;
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
