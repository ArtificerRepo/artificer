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

import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.test.TestUtils;

/**
 * Tests the s-ramp query features of the atom api binding.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryResourceTest extends AbstractNoAuditingResourceTest {

	/**
	 * @throws Exception
	 */
	@Test
	public void testQueries() throws Exception {
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
		ClientRequest request = clientRequest("/s-ramp?query=xsd/XsdDocument"); //$NON-NLS-1$
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		int uuidsFound = 0;
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			if (uuids.contains(entryUuid))
				uuidsFound++;
		}
		Assert.assertEquals(numEntries, uuidsFound);

		// Do it again with POST (multipart form data)
		request = clientRequest("/s-ramp"); //$NON-NLS-1$
		MultipartFormDataOutput formData = new MultipartFormDataOutput();
		formData.addFormData("query", "xsd/XsdDocument", MediaType.TEXT_PLAIN_TYPE); //$NON-NLS-1$ //$NON-NLS-2$
		request.body(MediaType.MULTIPART_FORM_DATA_TYPE, formData);
		response = request.post(Feed.class);
		feed = response.getEntity();
		uuidsFound = 0;
		for (Entry entry : feed.getEntries()) {
			String entryUuid = entry.getId().toString();
			if (uuids.contains(entryUuid))
				uuidsFound++;
		}
		Assert.assertEquals(numEntries, uuidsFound);

		// Do a query using GET with multiple documents and using the query params
		String stampVal = UUID.randomUUID().toString();
		Set<String> allTidxVals = new HashSet<String>();
		for (int i=0; i<5; i++) {
			String propname = "tidx"; //$NON-NLS-1$
			String propval = String.valueOf(i);
			doAddXsd("foo", "bar", "stamp", stampVal, propname, propval); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			allTidxVals.add(propval);
		}
		// Verify that 5 documents can be found
		String query = String.format("xsd/XsdDocument[@stamp%%3D'%1$s']", stampVal); //$NON-NLS-1$
		request = clientRequest("/s-ramp?query=" + query); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(5, feed.getEntries().size());
		// Verify that we can choose to return only 2 of them
		query = String.format("xsd/XsdDocument[@stamp%%3D'%1$s']", stampVal); //$NON-NLS-1$
		request = clientRequest("/s-ramp?startIndex=0&count=2&query=" + query); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());
		// Verify that we can return all and bring back the two custom properties
		query = String.format("xsd/XsdDocument[@stamp%%3D'%1$s']", stampVal); //$NON-NLS-1$
		request = clientRequest("/s-ramp?propertyName=tidx&propertyName=stamp&query=" + query); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(5, feed.getEntries().size());
		Set<String> actualTidxVals = new HashSet<String>();
		for (Entry entry : feed.getEntries()) {
			Artifact arty = entry.getAnyOtherJAXBObject(Artifact.class);
			if (arty != null) {
				XsdDocument xsdDoc = arty.getXsdDocument();
				List<Property> properties = xsdDoc.getProperty();
				for (Property prop : properties) {
					if ("tidx".equals(prop.getPropertyName())) { //$NON-NLS-1$
						actualTidxVals.add(prop.getPropertyValue());
					}
				}
			}
		}
		Assert.assertEquals(allTidxVals, actualTidxVals);
	}

	/**
     * @throws Exception
     */
    @Test
    public void testSearchResultAttributes() throws Exception {
        
        int numEntries = 20;
        for (int i = 0; i < numEntries; i++) {
            addJpegDocument("photo" + i + ".jpg"); //$NON-NLS-1$ //$NON-NLS-2$
        }

        // Default query
        ClientRequest request = clientRequest("/s-ramp?query=ext"); //$NON-NLS-1$
        ClientResponse<Feed> response = request.get(Feed.class);
        Feed feed = response.getEntity();
        Object startIndexAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_START_INDEX_QNAME);
        Object itemsPerPageAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_ITEMS_PER_PAGE_QNAME);
        Object totalResultsAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        Assert.assertNotNull("The startIndex attribute wasn't returned!", startIndexAttr); //$NON-NLS-1$
        Assert.assertNotNull("The itemsPerPage attribute wasn't returned!", itemsPerPageAttr); //$NON-NLS-1$
        Assert.assertNotNull("The totalResults attribute wasn't returned!", totalResultsAttr); //$NON-NLS-1$
        int startIndex = Integer.parseInt(String.valueOf(startIndexAttr));
        int itemsPerPage = Integer.parseInt(String.valueOf(itemsPerPageAttr));
        int totalResults = Integer.parseInt(String.valueOf(totalResultsAttr));
        Assert.assertEquals(0, startIndex);
        Assert.assertEquals(100, itemsPerPage);
        Assert.assertEquals(20, totalResults);

        // Query with some params
        request = clientRequest("/s-ramp?query=ext&startIndex=5&count=2"); //$NON-NLS-1$
        response = request.get(Feed.class);
        feed = response.getEntity();
        startIndexAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_START_INDEX_QNAME);
        itemsPerPageAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_ITEMS_PER_PAGE_QNAME);
        totalResultsAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        Assert.assertNotNull("The startIndex attribute wasn't returned!", startIndexAttr); //$NON-NLS-1$
        Assert.assertNotNull("The itemsPerPage attribute wasn't returned!", itemsPerPageAttr); //$NON-NLS-1$
        Assert.assertNotNull("The totalResults attribute wasn't returned!", totalResultsAttr); //$NON-NLS-1$
        startIndex = Integer.parseInt(String.valueOf(startIndexAttr));
        itemsPerPage = Integer.parseInt(String.valueOf(itemsPerPageAttr));
        totalResults = Integer.parseInt(String.valueOf(totalResultsAttr));
        Assert.assertEquals(5, startIndex);
        Assert.assertEquals(2, itemsPerPage);
        Assert.assertEquals(20, totalResults);
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
	 * @throws Exception
	 */
	@Test
	public void testQueriesExtended() throws Exception {	    
		addJpegDocument("photo1.jpg"); //$NON-NLS-1$
		addJpegDocument("photo2.jpg"); //$NON-NLS-1$

		ClientRequest request = clientRequest("/s-ramp?query=ext/JpgDocument"); //$NON-NLS-1$
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Object totalResultsAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int total = Integer.parseInt(String.valueOf(totalResultsAttr));
		Assert.assertEquals(2, total);

		request = clientRequest("/s-ramp/ext/JpgDocument"); //$NON-NLS-1$
		response = request.get(Feed.class);
		feed = response.getEntity();
		totalResultsAttr = feed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        total = Integer.parseInt(String.valueOf(totalResultsAttr));
        Assert.assertEquals(2, total);
	}

	/**
	 * Adds a JPEG to the repo.
	 * @param fname
	 * @throws Exception
	 */
	public void addJpegDocument(String fname) throws Exception {
		// Add the jpg to the repository
		String artifactFileName = "photo.jpg"; //$NON-NLS-1$
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/ext/" + artifactFileName); //$NON-NLS-1$
		try {
			ClientRequest request = clientRequest("/s-ramp/ext/JpgDocument"); //$NON-NLS-1$
			request.header("Slug", fname); //$NON-NLS-1$
			request.body("application/octet-stream", contentStream); //$NON-NLS-1$

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(fname, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof ExtendedDocument);
			ExtendedDocument doc = (ExtendedDocument) arty;
			Assert.assertEquals(fname, doc.getName());
			Assert.assertEquals("JpgDocument", doc.getExtendedType()); //$NON-NLS-1$
			Assert.assertEquals(Long.valueOf(2398), Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME)));
			Assert.assertEquals("image/jpeg", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME)); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
