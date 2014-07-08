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
package org.overlord.sramp.test.server.atom.services;

import java.net.URL;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;
import org.w3._2002._07.owl_.Class;
import org.w3._2002._07.owl_.Ontology;

/**
 * Unit test for the ontology rest api.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyResourceTest extends AbstractNoAuditingResourceTest {

    @Test
    public void testCreate() throws Exception {
        ClientRequest request = clientRequest("/s-ramp/ontology"); //$NON-NLS-1$

        RDF rdf = loadTestRDF("regional"); //$NON-NLS-1$

        request.body(MediaType.APPLICATION_RDF_XML_TYPE, rdf);
        ClientResponse<Entry> response = request.post(Entry.class);
        Entry entry = response.getEntity();

        RDF ontology = SrampAtomUtils.unwrap(entry, RDF.class);

        Assert.assertNotNull(ontology);
        assertEquals(rdf, ontology);
        
        // delete it to prevent OntologyAlreadyExistsException
        deleteOntology(entry.getId().toString());
    }

    /**
     * Unit test for: https://issues.jboss.org/browse/SRAMP-256
     * @throws Exception
     */
    @Test
    public void testCreate_SRAMP256() throws Exception {
        ClientRequest request = clientRequest("/s-ramp/ontology"); //$NON-NLS-1$

        RDF rdf = loadTestRDF("SRAMP-256"); //$NON-NLS-1$

        request.body(MediaType.APPLICATION_RDF_XML_TYPE, rdf);
        try {
            request.post(Entry.class);
            Assert.fail("Expected an ontology validation error."); //$NON-NLS-1$
        } catch (Exception e) {
            Assert.assertEquals("The ontology ID was invalid: SRAMP 256", e.getMessage()); //$NON-NLS-1$
        }
    }

	@Test
	public void testGet() throws Exception {
		ClientRequest request = clientRequest("/s-ramp/ontology"); //$NON-NLS-1$

		// POST the ontology
		RDF rdf = loadTestRDF("regional"); //$NON-NLS-1$
		request.body(MediaType.APPLICATION_RDF_XML_TYPE, rdf);
		ClientResponse<Entry> response = request.post(Entry.class);
		Entry entry = response.getEntity();
		String uuid = entry.getId().toString();

		// GET the ontology
		request = clientRequest("/s-ramp/ontology/" + uuid); //$NON-NLS-1$
		entry = request.get(Entry.class).getEntity();
		RDF ontology = SrampAtomUtils.unwrap(entry, RDF.class);

		Assert.assertNotNull(ontology);
		assertEquals(rdf, ontology);
        
        // delete it to prevent OntologyAlreadyExistsException
        deleteOntology(uuid);
	}

	@Test
	public void testFeed() throws Exception {
		// POST the regional ontology
		ClientRequest request = clientRequest("/s-ramp/ontology"); //$NON-NLS-1$
		RDF rdf = loadTestRDF("regional"); //$NON-NLS-1$
		request.body(MediaType.APPLICATION_RDF_XML_TYPE, rdf);
		request.post(Entry.class).getEntity();
		// POST the colors ontology
		request = clientRequest("/s-ramp/ontology"); //$NON-NLS-1$
		rdf = loadTestRDF("colors"); //$NON-NLS-1$
		request.body(MediaType.APPLICATION_RDF_XML_TYPE, rdf);
		request.post(Entry.class).getEntity();

		// GET the ontology feed
		request = clientRequest("/s-ramp/ontology"); //$NON-NLS-1$
		Feed feed = request.get(Feed.class).getEntity();
		Assert.assertNotNull(feed);
		Assert.assertEquals(2, feed.getEntries().size());
        
        // delete it to prevent OntologyAlreadyExistsException
		for (Entry entry : feed.getEntries()) {
		    deleteOntology(entry.getId().toString());
		}
	}

	/**
	 * Asserts that two ontologies are equal.
	 *
	 * @param expected
	 * @param actual
	 */
	private void assertEquals(RDF expected, RDF actual) {
		Assert.assertNotNull(expected);
		Assert.assertNotNull(actual);

		Ontology expectedOntology = expected.getOntology();
		Ontology actualOntology = actual.getOntology();
		Assert.assertNotNull(expectedOntology);
		Assert.assertNotNull(actualOntology);

		String expectedBase = null;
		String actualBase = null;

		Assert.assertEquals(expectedBase, actualBase);

		Assert.assertEquals(expectedOntology.getID(), actualOntology.getID());
		Assert.assertEquals(expectedOntology.getLabel(), actualOntology.getLabel());
		Assert.assertEquals(expectedOntology.getComment(), actualOntology.getComment());

		List<Class> expectedClasses = expected.getClazz();
		List<Class> actualClasses = actual.getClazz();
		Assert.assertEquals(expectedClasses.size(), actualClasses.size());

		for (int idx = 0; idx < expectedClasses.size(); idx++) {
			Class expectedClass = expectedClasses.get(idx);
			Class actualClass = actualClasses.get(idx);
			assertEquals(expectedClass, actualClass);
		}
	}

	/**
	 * @param expectedClass
	 * @param actualClass
	 */
	private void assertEquals(Class expectedClass, Class actualClass) {
		Assert.assertNotNull(expectedClass);
		Assert.assertNotNull(actualClass);

		Assert.assertEquals(expectedClass.getID(), actualClass.getID());
		Assert.assertEquals(expectedClass.getLabel(), actualClass.getLabel());
		Assert.assertEquals(expectedClass.getComment(), actualClass.getComment());

		if (expectedClass.getSubClassOf() == null) {
			Assert.assertNull(actualClass.getSubClassOf());
		} else {
			Assert.assertNotNull(actualClass.getSubClassOf());
			Assert.assertEquals(expectedClass.getSubClassOf().getResource(), actualClass.getSubClassOf()
					.getResource());
		}
	}

	/**
	 * Loads an RDF by unmarshaling from a test file.
	 *
	 * @param testOwlName
	 */
	private RDF loadTestRDF(String testOwlName) throws Exception {
		URL resourceUrl = getClass().getResource("/ontology-files/" + testOwlName + ".owl.xml"); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.assertNotNull(resourceUrl);
		JAXBContext jaxbContext = JAXBContext.newInstance(RDF.class);
		Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
		return (RDF) unMarshaller.unmarshal(resourceUrl);
	}

    private void deleteOntology(String uuid) throws Exception {
        ClientRequest request = clientRequest("/s-ramp/ontology/" + uuid); //$NON-NLS-1$
        request.delete(Void.class);
    }

}
