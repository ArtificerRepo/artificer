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
package org.overlord.sramp.test.server.atom.services;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.test.TestUtils;

/**
 * Tests the s-ramp query features of the atom api binding.
 *
 * @author eric.wittmann@redhat.com
 */
public class FeedResourceTest extends AbstractNoAuditingResourceTest {

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
		ClientRequest request = clientRequest("/s-ramp/xsd/XsdDocument"); //$NON-NLS-1$
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
		request = clientRequest("/s-ramp/xsd/XsdDocument?startPage=2&count=2"); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertTrue("Expected 2 entries.", feed.getEntries().size() == 2); //$NON-NLS-1$
	}

	/**
	 * Adds an XSD to the repo by POSTing the content to /s-ramp/xsd/XsdDocument.
	 */
	private Entry doAddXsd(String ... properties) throws Exception {
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
			request = clientRequest("/s-ramp/xsd/XsdDocument/" + uuid); //$NON-NLS-1$
			request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry);
			request.put(Void.class);
		}

		return entry;
	}

	/**
	 * Tests an extended type feed.
	 * @throws Exception
	 */
	@Test
	public void testExtendedTypeFeed() throws Exception {
		// Add some pkg entries
		Set<String> pkgUuids = new HashSet<String>();
		for (int i = 0; i < 5; i++) {
			Entry entry = doAddExtended("PkgDocument", "/sample-files/ext/defaultPackage.pkg"); //$NON-NLS-1$ //$NON-NLS-2$
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			pkgUuids.add(uuid);
		}
		// Add some bpmn entries
		Set<String> bpmnUuids = new HashSet<String>();
		for (int i = 0; i < 3; i++) {
			Entry entry = doAddExtended("BpmnDocument", "/sample-files/ext/Evaluation.bpmn"); //$NON-NLS-1$ //$NON-NLS-2$
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			bpmnUuids.add(uuid);
		}

		// Test the feed of the pkg docs
		ClientRequest request = clientRequest("/s-ramp/ext/PkgDocument"); //$NON-NLS-1$
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			pkgUuids.remove(entryUuid);
		}
		assertEquals("The feed did not contain the expected PkgDocument entries.", 0, pkgUuids.size());

		// Test the feed of the pkg docs
		request = clientRequest("/s-ramp/ext/BpmnDocument"); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			bpmnUuids.remove(entryUuid);
		}
        assertEquals("The feed did not contain the expected BpmnDocument entries.", 0, pkgUuids.size());
	}

	/**
	 * Adds a extended artifact to the repo by POSTing the content to /s-ramp/ext/???.
	 * @param extendedType
	 * @throws Exception
	 */
	private Entry doAddExtended(String extendedType, String testFilePath) throws Exception {
		ClientRequest request = clientRequest("/s-ramp/ext/" + extendedType); //$NON-NLS-1$

		File f = new File(testFilePath);
		String artifactFileName = f.getName();
		InputStream contentStream = this.getClass().getResourceAsStream(testFilePath);
		if (contentStream == null)
			throw new NullPointerException("Failed to find: " + testFilePath); //$NON-NLS-1$
		try {
			request.header("Slug", artifactFileName); //$NON-NLS-1$
			request.body(MediaType.APPLICATION_OCTET_STREAM, contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			return entry;
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	/**
	 * Tests the model feed.
	 * @throws Exception
	 */
	@Test
	public void testModelFeed() throws Exception {
		// Add 5 XSD entries
		Set<String> xsdUuids = new HashSet<String>();
		for (int i = 0; i < 5; i++) {
			Entry entry = doAddXsd();
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			xsdUuids.add(uuid);
		}
		// Add some pkg entries
		Set<String> pkgUuids = new HashSet<String>();
		for (int i = 0; i < 5; i++) {
			Entry entry = doAddExtended("PkgDocument", "/sample-files/ext/defaultPackage.pkg"); //$NON-NLS-1$ //$NON-NLS-2$
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			pkgUuids.add(uuid);
		}
		// Add some bpmn entries
		Set<String> bpmnUuids = new HashSet<String>();
		for (int i = 0; i < 3; i++) {
			Entry entry = doAddExtended("BpmnDocument", "/sample-files/ext/Evaluation.bpmn"); //$NON-NLS-1$ //$NON-NLS-2$
			URI entryId = entry.getId();
			String uuid = entryId.toString();
			bpmnUuids.add(uuid);
		}

		// Do a query for *just* the Extended types - there should be 5+3=8 of them
		ClientRequest request = clientRequest("/s-ramp/ext"); //$NON-NLS-1$
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		int uuidsFound = 0;
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			if (pkgUuids.contains(entryUuid) || bpmnUuids.contains(entryUuid))
				uuidsFound++;
		}
		Assert.assertEquals(8, uuidsFound);

		// Make sure the query params work
		request = clientRequest("/s-ramp/xsd?startPage=1&count=2"); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertTrue("Expected 2 entries.", feed.getEntries().size() == 2); //$NON-NLS-1$
	}

}
