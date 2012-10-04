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
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.repository.jcr.JCRRepositoryCleaner;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

import test.org.overlord.sramp.atom.TestUtils;

/**
 * Tests the s-ramp query features of the atom api binding.
 *
 * @author eric.wittmann@redhat.com
 */
public class FeedResourceTest extends BaseResourceTest {

	@BeforeClass
	public static void cleanIndex() {
		try {
			FileUtils.deleteDirectory(new File("target/repos"));
		} catch (Throwable t) {
		}
	}

	@Before
	public void setUp() throws Exception {
		dispatcher.getRegistry().addPerRequestResource(ArtifactResource.class);
		dispatcher.getRegistry().addPerRequestResource(FeedResource.class);
		dispatcher.getRegistry().addPerRequestResource(QueryResource.class);
        new JCRRepositoryCleaner().clean();
	}

	/**
	 * Tests the artifact feed.
	 * @throws Exception
	 */
	@Test
	public void testArtifactFeed() throws Exception {
		int numEntries = 10;

		// Add some entries
		Set<String> uuids = new HashSet<String>();
		for (int i = 0; i < numEntries; i++) {
			Entry entry = doAddXsd();
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			uuids.add(uuid);
		}

		// Do a query using GET with query params
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		int uuidsFound = 0;
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			if (uuids.contains(entryUuid))
				uuidsFound++;
		}
		Assert.assertEquals(numEntries, uuidsFound);

		// Make sure the query params work
		request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument?page=2&pageSize=2"));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertTrue("Expected 2 entries.", feed.getEntries().size() == 2);
	}

	/**
	 * Adds an XSD to the repo by POSTing the content to /s-ramp/xsd/XsdDocument.
	 */
	private Entry doAddXsd(String ... properties) throws Exception {
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
		Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
		Assert.assertEquals(Long.valueOf(2376), artifact.getXsdDocument().getContentSize());

		if (properties.length > 0) {
			Artifact srampArtifactWrapper = entry.getAnyOtherJAXBObject(Artifact.class);
			XsdDocument xsdDocument = srampArtifactWrapper.getXsdDocument();
			for (int i = 0; i < properties.length; i+=2) {
				String propname = properties[i];
				String propvalue = properties[i+1];
				Property prop = new Property();
				prop.setPropertyName(propname);
				prop.setPropertyValue(propvalue);
				xsdDocument.getProperty().add(prop);
			}
			String uuid = xsdDocument.getUuid();
			entry.setAnyOtherJAXBObject(srampArtifactWrapper);
			request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument/" + uuid));
			request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry);
			request.put(Void.class);
		}

		return entry;
	}

	/**
	 * Tests a user-defined type feed.
	 * @throws Exception
	 */
	@Test
	public void testUserDefinedTypeFeed() throws Exception {
		// Add some pkg entries
		Set<String> pkgUuids = new HashSet<String>();
		for (int i = 0; i < 5; i++) {
			Entry entry = doAddUserDefined("PkgDocument", "/sample-files/user/defaultPackage.pkg");
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			pkgUuids.add(uuid);
		}
		// Add some bpmn entries
		Set<String> bpmnUuids = new HashSet<String>();
		for (int i = 0; i < 3; i++) {
			Entry entry = doAddUserDefined("BpmnDocument", "/sample-files/user/Evaluation.bpmn");
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			bpmnUuids.add(uuid);
		}

		// Test the feed of the pkg docs
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/PkgDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Set<String> actualPkgUuids = new HashSet<String>();
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			actualPkgUuids.add(entryUuid);
		}
		Assert.assertEquals(pkgUuids, actualPkgUuids);

		// Test the feed of the pkg docs
		request = new ClientRequest(generateURL("/s-ramp/user/BpmnDocument"));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Set<String> actualBpmnUuids = new HashSet<String>();
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			actualBpmnUuids.add(entryUuid);
		}
		Assert.assertEquals(bpmnUuids, actualBpmnUuids);
	}

	/**
	 * Adds a user-defined artifact to the repo by POSTing the content to /s-ramp/user/???.
	 * @param userType
	 * @throws Exception
	 */
	private Entry doAddUserDefined(String userType, String testFilePath) throws Exception {
		ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/" + userType));

		File f = new File(testFilePath);
		String artifactFileName = f.getName();
		InputStream contentStream = this.getClass().getResourceAsStream(testFilePath);
		if (contentStream == null)
			throw new NullPointerException("Failed to find: " + testFilePath);
		try {
			request.header("Slug", artifactFileName);
			request.body(MediaType.APPLICATION_OCTET_STREAM, contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			return entry;
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
