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
package org.overlord.sramp.client;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;

import junit.framework.Assert;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Before;
import org.junit.Test;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.err.SrampAtomExceptionMapper;
import org.overlord.sramp.atom.services.ArtifactResource;
import org.overlord.sramp.atom.services.FeedResource;
import org.overlord.sramp.atom.services.QueryResource;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * Unit test for the
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAtomApiClientTest extends BaseResourceTest {

	@Before
	public void setUp() throws Exception {
		getProviderFactory().registerProvider(SrampAtomExceptionMapper.class);
		dispatcher.getRegistry().addPerRequestResource(ArtifactResource.class);
		dispatcher.getRegistry().addPerRequestResource(FeedResource.class);
		dispatcher.getRegistry().addPerRequestResource(QueryResource.class);
	}

	/**
	 * Test method for {@link org.overlord.sramp.client.SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testUploadArtifact() throws Exception {
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
			Entry entry = client.uploadArtifact("xsd", "XsdDocument", is, artifactFileName);
			Assert.assertNotNull(entry);
			Assert.assertEquals(artifactFileName, entry.getTitle());
		} finally {
			is.close();
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.client.SrampAtomApiClient#getArtifactContent(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetArtifactContent() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		URI uuid = null;

		// First, upload an artifact so we have some content to get
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			Entry entry = client.uploadArtifact("xsd", "XsdDocument", is, artifactFileName);
			Assert.assertNotNull(entry);
			Assert.assertEquals(artifactFileName, entry.getTitle());
			uuid = entry.getId();
		} finally {
			is.close();
		}

		// Now get the content.
		InputStream content = client.getArtifactContent("xsd", "XsdDocument", uuid.toString());
		try {
			Assert.assertNotNull(content);
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line1 = reader.readLine();
			String line2 = reader.readLine();
			Assert.assertTrue("Unexpected content found.", line1.startsWith("<?xml version=\"1.0\""));
			Assert.assertTrue("Unexpected content found.", line2.startsWith("<xsd:schema"));
		} finally {
			content.close();
		}
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactMetaData() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		URI uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			Entry entry = client.uploadArtifact("xsd", "XsdDocument", is, artifactFileName);
			Assert.assertNotNull(entry);
			Assert.assertEquals(artifactFileName, entry.getTitle());
			uuid = entry.getId();
			xsdDoc = entry.getAnyOtherJAXBObject(XsdDocument.class);
		} finally {
			is.close();
		}

		// Now update the description
		xsdDoc.setDescription("** DESCRIPTION UPDATED **");
		client.updateArtifactMetaData(xsdDoc);

		// Now verify
		Entry entry = client.getFullArtifactEntry(ArtifactType.XsdDocument, uuid.toString());
		Assert.assertEquals("** DESCRIPTION UPDATED **", entry.getSummary());
	}

	/**
	 * Test method for {@link org.overlord.sramp.client.SrampAtomApiClient#query(java.lang.String, int, int, java.lang.String, boolean)}.
	 */
	@Test
	public void testQuery() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		URI uuid = null;

		// First add an artifact so we have something to search for
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			Entry entry = client.uploadArtifact("xsd", "XsdDocument", is, artifactFileName);
			Assert.assertNotNull(entry);
			Assert.assertEquals(artifactFileName, entry.getTitle());
			uuid = entry.getId();
		} finally {
			is.close();
		}

		// Now search for all XSDs
		Feed feed = client.query("/s-ramp/xsd/XsdDocument", 0, 50, "name", false);
		List<Entry> entries = feed.getEntries();
		boolean uuidFound = false;
		for (Entry entry : entries) {
			if (entry.getId().equals(uuid))
				uuidFound = true;
		}
		Assert.assertTrue("Failed to find the artifact we just added!", uuidFound);
	}

	/**
	 * Test method for {@link org.overlord.sramp.client.SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testQueryError() throws Exception {
		SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
		try {
			Feed feed = client.query("12345", 0, 20, "name", false);
			fail("Expected a remote exception from the s-ramp server, but got: " + feed);
		} catch (SrampServerException e) {
			String remoteTrace = e.getRemoteStackTrace();
			Assert.assertNotNull(remoteTrace);
			Assert.assertTrue(remoteTrace.startsWith("org.overlord.sramp.query.xpath.XPathParserException: Invalid artifact set (step 2)."));
		}
	}

}
