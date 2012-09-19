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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.err.SrampAtomExceptionMapper;
import org.overlord.sramp.atom.providers.HttpResponseProvider;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

import test.org.overlord.sramp.repository.jcr.JCRRepositoryCleaner;

/**
 * Unit test for the {@link BatchResource} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class BatchResourceTest extends BaseResourceTest {

	@Before
	public void setUp() throws Exception {
		// bring up the embedded container with the BatchResource deployed.
		getProviderFactory().registerProvider(SrampAtomExceptionMapper.class);
		getProviderFactory().registerProvider(HttpResponseProvider.class);
		dispatcher.getRegistry().addPerRequestResource(BatchResource.class);
		dispatcher.getRegistry().addPerRequestResource(FeedResource.class);
        new JCRRepositoryCleaner().clean();
	}

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
			archive = new SrampArchive();

			xsd1ContentStream = this.getClass().getResourceAsStream("/sample-files/xsd/PO.xsd");
			BaseArtifactType metaData = new XsdDocument();
			archive.addEntry("schemas/PO.xsd", metaData, xsd1ContentStream);
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
			metaData.setName("PO.xsd");
			metaData.setUuid(UUID.randomUUID().toString());
			xsd2ContentStream = this.getClass().getResourceAsStream("/sample-files/xsd/XMLSchema.xsd");
			metaData = new XsdDocument();
			metaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
			metaData.setName("XMLSchema.xsd");
			metaData.setUuid(UUID.randomUUID().toString());
			metaData.setVersion("1.0");
			archive.addEntry("schemas/XMLSchema.xsd", metaData, xsd2ContentStream);

			zipFile = archive.pack();
			zipStream = FileUtils.openInputStream(zipFile);

			// Now POST the archive to the s-ramp repository (POST to /s-ramp as application/zip)
			ClientRequest request = new ClientRequest(generateURL("/s-ramp"));
			request.body(MediaType.APPLICATION_ZIP, zipStream);
			ClientResponse<MultipartInput> clientResponse = request.post(MultipartInput.class);

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

			arty = artyMap.get("<schemas/XMLSchema.xsd@package>");
			Assert.assertNotNull(arty);
		} finally {
			IOUtils.closeQuietly(xsd1ContentStream);
			IOUtils.closeQuietly(xsd2ContentStream);
			SrampArchive.closeQuietly(archive);
			if (zipFile != null)
				zipFile.delete();
			IOUtils.closeQuietly(zipStream);
		}

		// Verify by querying
		// Do a query using GET with query params
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());
		Map<String, BaseArtifactType> artyMap = new HashMap<String, BaseArtifactType>();
		for (Entry entry : feed.getEntries()) {
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			artyMap.put(arty.getName(), arty);
		}
		Assert.assertEquals(2, artyMap.size());
		BaseArtifactType poArty = artyMap.get("PO.xsd");
		Assert.assertNotNull(poArty);
		BaseArtifactType xmlSchemaArty = artyMap.get("XMLSchema.xsd");
		Assert.assertNotNull(xmlSchemaArty);
	}

}
