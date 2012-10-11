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
package org.overlord.sramp.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.SrampModelUtils;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.client.ClientRequest;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;


/**
 * Unit test for the {@link BatchResource} class.
 *
 * TODO add a test for 409 Conflict (e.g. update an artifact that doesn't exist)
 *
 * @author eric.wittmann@redhat.com
 */
public class BatchResourceTest extends AbstractResourceTest {

	/**
	 * Test method for {@link org.overlord.sramp.atom.services.BatchResource#zipPackage(java.lang.String, java.io.InputStream)}.
	 */
	@Test
	public void testZipPackage() throws Exception {
		SrampArchive archive = null;
		InputStream xsd1ContentStream = null;
		InputStream xsd2ContentStream = null;
		File zipFile = null;
		InputStream zipStream = null;

		try {
			// Create a test s-ramp archive
			archive = new SrampArchive();
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
			ClientRequest request = new ClientRequest(generateURL("/s-ramp"));
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
				BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(entry);
				artyMap.put(id, artifact);
			}

			Assert.assertTrue(artyMap.keySet().contains("<schemas/PO.xsd@package>"));
			Assert.assertTrue(artyMap.keySet().contains("<schemas/XMLSchema.xsd@package>"));

			// Asertions for artifact 1
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
			SrampArchive.closeQuietly(archive);
			IOUtils.closeQuietly(zipStream);
			FileUtils.deleteQuietly(zipFile);
		}

		// Verify by querying
		// Do a query using GET with query params
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());
		Set<String> artyNames = new HashSet<String>();
		for (Entry entry : feed.getEntries()) {
			artyNames.add(entry.getTitle());
		}
		Assert.assertEquals(2, artyNames.size());
		Assert.assertTrue(artyNames.contains("PO.xsd"));
		Assert.assertTrue(artyNames.contains("XMLSchema.xsd"));
	}

	/**
	 * Test method for {@link org.overlord.sramp.atom.services.BatchResource#zipPackage(java.lang.String, java.io.InputStream)}.
	 *
	 * This also tests the zipPackage method of the {@link BatchResource} class, but it is
	 * more thorough.  It tests adding new content, updating existing content, etc.
	 */
	@Test
	public void testZipPackage_Multi() throws Exception {
		SrampArchive archive = null;
		InputStream xsd1ContentStream = null;
		InputStream wsdlContentStream = null;
		File zipFile = null;
		InputStream zipStream = null;

		WsdlDocument wsdlDoc = createWsdlArtifact();
		XmlDocument xmlDoc = createXmlArtifact();

		String xsdUuid = null;
		String wsdlUuid = null;
		String xmlUuid = null;

		try {
			// Create a test s-ramp archive
			archive = new SrampArchive();

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
			SrampModelUtils.setCustomProperty(metaData, "foo", "bar");
			archive.addEntry("wsdl/sample.wsdl", metaData, wsdlContentStream);
			// Update an existing XML document (meta-data only)
			metaData = xmlDoc;
			metaData.setVersion("3.0");
			SrampModelUtils.setCustomProperty(metaData, "far", "baz");
			archive.addEntry("core/PO.xml", metaData, null);

			zipFile = archive.pack();
			zipStream = FileUtils.openInputStream(zipFile);

			// Now POST the archive to the s-ramp repository (POST to /s-ramp as application/zip)
			ClientRequest request = new ClientRequest(generateURL("/s-ramp"));
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

			// Asertions for artifact 1 (PO.xsd)
			HttpResponseBean httpResp = respMap.get("<schemas/PO.xsd@package>");
			Assert.assertEquals(201, httpResp.getCode());
			Assert.assertEquals("Created", httpResp.getStatus());
			Entry entry = (Entry) httpResp.getBody();
			BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertEquals("PO.xsd", artifact.getName());
			Assert.assertNull(artifact.getVersion());
			Assert.assertEquals(new Long(2376), ((XsdDocument) artifact).getContentSize());
			xsdUuid = artifact.getUuid();

			// Asertions for artifact 2 (sample.wsdl)
			httpResp = respMap.get("<wsdl/sample.wsdl@package>");
			Assert.assertEquals(201, httpResp.getCode());
			Assert.assertEquals("Created", httpResp.getStatus());
			entry = (Entry) httpResp.getBody();
			artifact = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertEquals("sample.wsdl", artifact.getName());
			Assert.assertEquals("2.0", artifact.getVersion());
			Assert.assertEquals(new Long(2455), ((WsdlDocument) artifact).getContentSize());
			wsdlUuid = artifact.getUuid();

			// Asertions for artifact 3 (PO.xml)
			httpResp = respMap.get("<core/PO.xml@package>");
			Assert.assertEquals(201, httpResp.getCode());
			Assert.assertEquals("Created", httpResp.getStatus());
			entry = (Entry) httpResp.getBody();
			artifact = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertEquals("PO.xml", artifact.getName());
			Assert.assertEquals("3.0", artifact.getVersion());
			Assert.assertEquals(new Long(825), ((XmlDocument) artifact).getContentSize());
			xmlUuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(xsd1ContentStream);
			IOUtils.closeQuietly(wsdlContentStream);
			SrampArchive.closeQuietly(archive);
			IOUtils.closeQuietly(zipStream);
			FileUtils.deleteQuietly(zipFile);
		}

		// Verify by querying
		// Do a query using GET with query params
		Map<String, BaseArtifactType> artyMap = new HashMap<String, BaseArtifactType>();
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Assert.assertEquals(1, feed.getEntries().size());
		for (Entry entry : feed.getEntries()) {
			request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + entry.getId().toString()));
			BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(request.get(Entry.class).getEntity());
			artyMap.put(artifact.getUuid(), artifact);
		}
		request = new ClientRequest(generateURL("/s-ramp/wsdl/WsdlDocument"));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(1, feed.getEntries().size());
		for (Entry entry : feed.getEntries()) {
			request = new ClientRequest(generateURL("/s-ramp/wsdl/WsdlDocument/" + entry.getId().toString()));
			BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(request.get(Entry.class).getEntity());
			artyMap.put(artifact.getUuid(), artifact);
		}
		request = new ClientRequest(generateURL("/s-ramp/core/XmlDocument"));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(1, feed.getEntries().size());
		for (Entry entry : feed.getEntries()) {
			request = new ClientRequest(generateURL("/s-ramp/core/XmlDocument/" + entry.getId().toString()));
			BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(request.get(Entry.class).getEntity());
			artyMap.put(artifact.getUuid(), artifact);
		}

		Assert.assertEquals(3, artyMap.size());

		// Asertions for artifact 1 (PO.xsd)
		BaseArtifactType artifact = artyMap.get(xsdUuid);
		Assert.assertEquals("PO.xsd", artifact.getName());
		Assert.assertNull(artifact.getVersion());
		Assert.assertEquals(new Long(2376), ((XsdDocument) artifact).getContentSize());

		// Asertions for artifact 2 (sample.wsdl)
		artifact = artyMap.get(wsdlUuid);
		Assert.assertEquals("sample.wsdl", artifact.getName());
		Assert.assertEquals("2.0", artifact.getVersion());
		Assert.assertEquals(new Long(2455), ((WsdlDocument) artifact).getContentSize());

		// Asertions for artifact 3 (PO.xml)
		artifact = artyMap.get(xmlUuid);
		Assert.assertEquals("PO.xml", artifact.getName());
		Assert.assertEquals("3.0", artifact.getVersion());
		Assert.assertEquals(new Long(825), ((XmlDocument) artifact).getContentSize());
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
			ClientRequest request = new ClientRequest(generateURL("/s-ramp/core/XmlDocument"));
			request.header("Slug", artifactFileName);
			request.body("application/xml", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(artifactFileName, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof XmlDocument);
			XmlDocument doc = (XmlDocument) arty;
			Assert.assertEquals(artifactFileName, doc.getName());
			Assert.assertEquals(Long.valueOf(825), doc.getContentSize());
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
			Assert.assertEquals(Long.valueOf(1642), doc.getContentSize());
			Assert.assertEquals("application/xml", doc.getContentType());
			return doc;
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
