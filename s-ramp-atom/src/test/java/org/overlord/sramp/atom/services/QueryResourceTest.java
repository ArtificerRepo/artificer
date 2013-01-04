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
import org.overlord.sramp.SrampConstants;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.UserDefinedArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

import test.org.overlord.sramp.atom.TestUtils;

/**
 * Tests the s-ramp query features of the atom api binding.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryResourceTest extends AbstractResourceTest {

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
		ClientRequest request = new ClientRequest(generateURL("/s-ramp?query=xsd/XsdDocument"));
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
		request = new ClientRequest(generateURL("/s-ramp"));
		MultipartFormDataOutput formData = new MultipartFormDataOutput();
		formData.addFormData("query", "xsd/XsdDocument", MediaType.TEXT_PLAIN_TYPE);
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
			String propname = "tidx";
			String propval = String.valueOf(i);
			doAddXsd("foo", "bar", "stamp", stampVal, propname, propval);
			allTidxVals.add(propval);
		}
		// Verify that 5 documents can be found
		String query = String.format("xsd/XsdDocument[@stamp%%3D'%1$s']", stampVal);
		request = new ClientRequest(generateURL("/s-ramp?query=" + query));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(5, feed.getEntries().size());
		// Verify that we can choose to return only 2 of them
		query = String.format("xsd/XsdDocument[@stamp%%3D'%1$s']", stampVal);
		request = new ClientRequest(generateURL("/s-ramp?startIndex=0&count=2&query=" + query));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());
		// Verify that we can return all and bring back the two custom properties
		query = String.format("xsd/XsdDocument[@stamp%%3D'%1$s']", stampVal);
		request = new ClientRequest(generateURL("/s-ramp?propertyName=tidx&propertyName=stamp&query=" + query));
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
					if ("tidx".equals(prop.getPropertyName())) {
						actualTidxVals.add(prop.getPropertyValue());
					}
				}
			}
		}
		// TODO restore this assertion once this is fixed:  https://issues.jboss.org/browse/RESTEASY-761
		//		Assert.assertEquals(allTidxVals, actualTidxVals);
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
	 * @throws Exception
	 */
	@Test
	public void testQueriesUserDefined() throws Exception {
		addJpegDocument("photo1.jpg");
		addJpegDocument("photo2.jpg");

		ClientRequest request = new ClientRequest(generateURL("/s-ramp?query=user/JpgDocument"));
		ClientResponse<Feed> response = request.get(Feed.class);
		Feed feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());

		request = new ClientRequest(generateURL("/s-ramp/user/JpgDocument"));
		response = request.get(Feed.class);
		feed = response.getEntity();
		Assert.assertEquals(2, feed.getEntries().size());
	}

	public void addJpegDocument(String fname) throws Exception {
		// Add the jpg to the repository
		String artifactFileName = "photo.jpg";
		InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/user/" + artifactFileName);
		try {
			ClientRequest request = new ClientRequest(generateURL("/s-ramp/user/JpgDocument"));
			request.header("Slug", fname);
			request.body("application/octet-stream", contentStream);

			ClientResponse<Entry> response = request.post(Entry.class);

			Entry entry = response.getEntity();
			Assert.assertEquals(fname, entry.getTitle());
			BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
			Assert.assertTrue(arty instanceof UserDefinedArtifactType);
			UserDefinedArtifactType doc = (UserDefinedArtifactType) arty;
			Assert.assertEquals(fname, doc.getName());
			Assert.assertEquals("JpgDocument", doc.getUserType());
			Assert.assertEquals(Long.valueOf(2966447), Long.valueOf(doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME)));
			Assert.assertEquals("application/octet-stream", doc.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_TYPE_QNAME));
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

}
