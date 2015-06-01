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
package org.artificer.test.client;

import org.apache.commons.io.IOUtils;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.mappers.RdfToOntologyMapper;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.ontology.ArtificerOntology;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.fail;

/**
 * Unit test for the bulk of the s-ramp client features.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
public class ArtificerAtomApiClientTest extends AbstractClientTest {

	/**
	 * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testUploadArtifact() throws Exception {
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			ArtificerAtomApiClient client = client();
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#createArtifact(BaseArtifactType)}.
     */
    @Test
    public void testCreateArtifact() throws Exception {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("TestArtifact");
        artifact.setName("My Test Artifact");
        artifact.setDescription("Description of my test artifact.");
        ArtificerAtomApiClient client = client();
        BaseArtifactType createdArtifact = client.createArtifact(artifact);
        Assert.assertNotNull(artifact);
        Assert.assertEquals("My Test Artifact", createdArtifact.getName());
        Assert.assertEquals("Description of my test artifact.", createdArtifact.getDescription());
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
     */
    @Test
    public void testExtendedDocumentArtifact() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            ArtificerAtomApiClient client = client();
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.ExtendedDocument("TestDocument"), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            Assert.assertEquals(BaseArtifactEnum.EXTENDED_DOCUMENT, artifact.getArtifactType());
            Assert.assertEquals(ExtendedDocument.class, artifact.getClass());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#getArtifactMetaData(ArtifactType, String)}
     */
    @Test
    public void testGetArtifactMetaData() throws Exception {
        String uuid = null;
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            ArtificerAtomApiClient client = client();
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now test that we can fetch the meta-data using the artifact type and UUID
        {
            ArtificerAtomApiClient client = client();
            BaseArtifactType metaData = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid);
            Assert.assertNotNull(metaData);
            Assert.assertEquals(artifactFileName, metaData.getName());
        }
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#getArtifactMetaData(String)}
     */
    @Test
    public void testGetArtifactMetaDataNoType() throws Exception {
        String uuid = null;
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            ArtificerAtomApiClient client = client();
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now test that we can fetch the meta-data using just the UUID
        {
            ArtificerAtomApiClient client = client();
            BaseArtifactType metaData = client.getArtifactMetaData(uuid);
            Assert.assertNotNull(metaData);
            Assert.assertEquals(artifactFileName, metaData.getName());
        }
    }


	/**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
     */
    @Test
    public void testUploadArtifactAndContent() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            ArtificerAtomApiClient client = client();
            XsdDocument xsdDocument = new XsdDocument();
            xsdDocument.setName(artifactFileName);
            xsdDocument.setUuid(uuid); 
            BaseArtifactType artifact = client.uploadArtifact(xsdDocument, is);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            Assert.assertEquals(uuid, artifact.getUuid()); 
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

	/**
	 * Test method for {@link org.artificer.client.ArtificerAtomApiClient#getArtifactContent(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetArtifactContent() throws Exception {
		ArtificerAtomApiClient client = client();
		String uuid = null;

		// First, upload an artifact so we have some content to get
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
		} finally {
			is.close();
		}

		// Now get the content.
		InputStream content = client.getArtifactContent(ArtifactType.XsdDocument(), uuid.toString());
		try {
			Assert.assertNotNull(content);
			BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			String line1 = reader.readLine();
			String line2 = reader.readLine();
			Assert.assertTrue("Unexpected content found.", line1.startsWith("<?xml version=\"1.0\""));
			Assert.assertTrue("Unexpected content found.", line2.startsWith("<xsd:schema"));
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactMetaData() throws Exception {
		ArtificerAtomApiClient client = client();
		String uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
			xsdDoc = (XsdDocument) artifact;
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now update the description
		xsdDoc.setDescription("** DESCRIPTION UPDATED **");
		client.updateArtifactMetaData(xsdDoc);

		// Now verify
		BaseArtifactType artifact = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid.toString());
		Assert.assertEquals("** DESCRIPTION UPDATED **", artifact.getDescription());
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactContent() throws Exception {
		ArtificerAtomApiClient client = client();
		String uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
			xsdDoc = (XsdDocument) artifact;
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now update the artifact content
		is = this.getClass().getResourceAsStream("/sample-files/xsd/PO-updated.xsd");
		try {
			client.updateArtifactContent(xsdDoc, is);
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now verify
		BaseArtifactType artifact = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid.toString());
		xsdDoc = (XsdDocument) artifact;
		Assert.assertEquals(new Long(2583), xsdDoc.getContentSize());
	}

	/**
	 * Test method for {@link org.artificer.client.ArtificerAtomApiClient#query(java.lang.String, int, int, java.lang.String, boolean)}.
	 */
	@Test
	public void testQuery() throws Exception {
		ArtificerAtomApiClient client = client();
		String uuid = null;

		// First add an artifact so we have something to search for
		String artifactFileName = "PO.xsd";
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now search for all XSDs
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument", 0, 50, "name", false);
		boolean uuidFound = false;
		for (ArtifactSummary entry : rset) {
			if (entry.getUuid().equals(uuid))
				uuidFound = true;
		}
		Assert.assertTrue("Failed to find the artifact we just added!", uuidFound);
	}

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#query(String, int, int, String, boolean, java.util.Collection)
     */
    @Test
    public void testQueryWithPropertyName() throws Exception {
        ArtificerAtomApiClient client = client();
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();

            // Set a couple of custom properties and update
            ArtificerModelUtils.setCustomProperty(artifact, "prop1", "foo");
            ArtificerModelUtils.setCustomProperty(artifact, "prop2", "bar");
            ArtificerModelUtils.setCustomProperty(artifact, "prop3", "baz");
            client.updateArtifactMetaData(artifact);
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for the artifact and request one of the custom
        // properties be returned in the result set.
        Set<String> propertyNames = new HashSet<String>();
        propertyNames.add("prop1");
        propertyNames.add("prop2");
        QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument[@uuid='"+uuid+"']", 0, 50, "name", false, propertyNames);
        Assert.assertEquals("Expected a single artifact returned.", 1, rset.size());
        ArtifactSummary summary = rset.get(0);
        Assert.assertEquals("foo", summary.getCustomPropertyValue("prop1"));
        Assert.assertEquals("bar", summary.getCustomPropertyValue("prop2"));
        Assert.assertNull("I didn't ask for 'prop3' to be returned!", summary.getCustomPropertyValue("prop3"));
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#buildQuery(String)
     */
    @Test
    public void testBuildQuery() throws Exception {
        ArtificerAtomApiClient client = client();
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for the XSD by its UUID
        QueryResultSet rset = client.buildQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]").parameter(uuid)
                .count(1).query();
        Assert.assertTrue("Failed to find the artifact we just added!", rset.size() == 1);

        // Do a couple of date-based queries here
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp < ?]")
                .parameter(new Date(System.currentTimeMillis() + 86400000L))
                .count(1).query();
        Assert.assertTrue("Failed to find an artifact by lastModifiedTimestamp!", rset.size() == 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp > ?]")
                .parameter(new Date(System.currentTimeMillis() + 86400000L))
                .count(1).query();
        Assert.assertTrue("Found an artifact by lastModifiedTimestamp, but should *not* have!", rset.size() == 0);

        // Now by DateTime
        Calendar endOfToday = Calendar.getInstance();
        endOfToday.set(Calendar.HOUR_OF_DAY, 0);
        endOfToday.set(Calendar.MINUTE, 0);
        endOfToday.set(Calendar.SECOND, 0);
        endOfToday.set(Calendar.MILLISECOND, 0);
        endOfToday.add(Calendar.DAY_OF_YEAR, 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp < ?]")
                .parameter(endOfToday)
                .count(1).query();
        Assert.assertTrue("Failed to find an artifact by lastModifiedTimestamp!", rset.size() == 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp > ?]")
                .parameter(endOfToday)
                .count(1).query();
        Assert.assertTrue("Found an artifact by lastModifiedTimestamp, but should *not* have!", rset.size() == 0);
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#buildQuery(String)
     */
    @Test
    public void testResultSetAttributes() throws Exception {
        ArtificerAtomApiClient client = client();
        for (int i = 0; i < 20; i++) {
            addXmlDoc();
        }
        QueryResultSet rs = client.buildQuery("/s-ramp/core").count(2).startIndex(5).query();
        Assert.assertEquals(20, rs.getTotalResults());
        Assert.assertEquals(2, rs.getItemsPerPage());
        Assert.assertEquals(5, rs.getStartIndex());
    }

	/**
	 * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testQueryError() throws Exception {
		ArtificerAtomApiClient client = client();
		try {
			QueryResultSet rset = client.query("12345", 0, 20, "name", false);
			fail("Expected a remote exception from the s-ramp server, but got: " + rset);
		} catch (ArtificerServerException e) {
			Assert.assertEquals("Invalid artifact set (step 2).", e.getMessage());
		}
	}

	/**
	 * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadBatch(org.artificer.atom.archive.ArtificerArchive)}.
	 */
	@Test
	public void testArchiveUpload() throws Exception {
		// First, create an s-ramp archive
		ArtificerArchive archive = null;
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			archive = new ArtificerArchive();

			String artifactFileName = "PO.xsd";
			is1 = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("PO.xsd");
			metaData.setVersion("1.1");
			metaData.setDescription("This is a test description (XSD).");
			archive.addEntry("schemas/PO.xsd", metaData, is1);

			artifactFileName = "PO.xml";
			is2 = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
			metaData = new XsdDocument();
			metaData.setName("PO.xml");
			metaData.setVersion("1.2");
			metaData.setDescription("This is a test description (XML).");
			archive.addEntry("core/PO.xml", metaData, is2);
		} catch (Exception e) {
			ArtificerArchive.closeQuietly(archive);
			throw e;
		} finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}

		try {
			// Now use the s-ramp atom api client to upload the s-ramp archive
			ArtificerAtomApiClient client = client();
			Map<String, ?> results = client.uploadBatch(archive);
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.keySet().contains("schemas/PO.xsd"));
			Assert.assertTrue(results.keySet().contains("core/PO.xml"));

			XsdDocument xsdDoc = (XsdDocument) results.get("schemas/PO.xsd");
			Assert.assertNotNull(xsdDoc);
			Assert.assertEquals("PO.xsd", xsdDoc.getName());
			Assert.assertEquals("1.1", xsdDoc.getVersion());

			XmlDocument xmlDoc = (XmlDocument) results.get("core/PO.xml");
			Assert.assertNotNull(xmlDoc);
			Assert.assertEquals("PO.xml", xmlDoc.getName());
			Assert.assertEquals("1.2", xmlDoc.getVersion());
		} finally {
			ArtificerArchive.closeQuietly(archive);
		}
	}

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadBatch(org.artificer.atom.archive.ArtificerArchive)}.
     */
    @Test
    public void testArchiveUpload_Empty() throws Exception {
        // First, create an s-ramp archive
        ArtificerArchive archive = null;
        try {
            archive = new ArtificerArchive();
        } catch (Exception e) {
            ArtificerArchive.closeQuietly(archive);
            throw e;
        } finally {
        }

        try {
            // Now use the s-ramp atom api client to upload the s-ramp archive
            ArtificerAtomApiClient client = client();
            Map<String, ?> results = client.uploadBatch(archive);
            Assert.assertTrue(results.isEmpty());
        } finally {
            ArtificerArchive.closeQuietly(archive);
        }
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadBatch(org.artificer.atom.archive.ArtificerArchive)}.
     */
    @Test
    public void testArchiveUpload_AtomOnly() throws Exception {
        // First, create an s-ramp archive
        ArtificerArchive archive = null;
        try {
            archive = new ArtificerArchive();
            ExtendedArtifactType nonDocArtifact = new ExtendedArtifactType();
            nonDocArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            nonDocArtifact.setExtendedType("TestArtifact");
            nonDocArtifact.setName("My Test Artifact");

            archive.addEntry("myLogicalArtifact", nonDocArtifact, null);
        } catch (Exception e) {
            ArtificerArchive.closeQuietly(archive);
            throw e;
        } finally {
        }

        try {
            // Now use the s-ramp atom api client to upload the s-ramp archive
            ArtificerAtomApiClient client = client();
            
            Map<String, ?> results = client.uploadBatch(archive);
            Assert.assertFalse(results.isEmpty());
            Assert.assertEquals(1, results.size());

            QueryResultSet resultSet = client.buildQuery("/s-ramp/ext").query();
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());

            resultSet = client.buildQuery("/s-ramp/ext/TestArtifact").query();
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());

            resultSet = client.buildQuery("/s-ramp/ext/TestArtifact[@name = 'My Test Artifact']").query();
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());
        } finally {
            ArtificerArchive.closeQuietly(archive);
        }
    }

	/**
	 * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadBatch(org.artificer.atom.archive.ArtificerArchive)}.
	 */
	@Test
	public void testArchiveUploadWithError() throws Exception {
		// First, create an s-ramp archive
		ArtificerArchive archive = null;
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			archive = new ArtificerArchive();

			String artifactFileName = "PO.xsd";
			is1 = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("PO.xsd");
			metaData.setVersion("1.1");
			metaData.setDescription("This is a test description (XSD).");
			archive.addEntry("schemas/PO.xsd", metaData, is1);

			artifactFileName = "PO.xml";
			metaData = new XsdDocument();
			metaData.setName("PO.xml");
			metaData.setVersion("1.2");
			metaData.setDescription("This is a test description (XML).");
			archive.addEntry("core/PO.xml", metaData, null);
		} catch (Exception e) {
			ArtificerArchive.closeQuietly(archive);
			throw e;
		} finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}

		try {
			// Now use the s-ramp atom api client to upload the s-ramp archive
			ArtificerAtomApiClient client = client();
			Map<String, ?> results = client.uploadBatch(archive);
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.keySet().contains("schemas/PO.xsd"));
			Assert.assertTrue(results.keySet().contains("core/PO.xml"));

			XsdDocument xsdDoc = (XsdDocument) results.get("schemas/PO.xsd");
			Assert.assertNotNull(xsdDoc);
			Assert.assertEquals("PO.xsd", xsdDoc.getName());
			Assert.assertEquals("1.1", xsdDoc.getVersion());

			Exception xmlError = (Exception) results.get("core/PO.xml");
			Assert.assertNotNull(xmlError);
		} finally {
			ArtificerArchive.closeQuietly(archive);
		}
	}

	/**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#uploadOntology(InputStream)}.
     */
    @Test
    public void testUploadOntology() throws Exception {
        ArtificerOntology ontology = uploadOntology();
        
        // delete it to prevent OntologyAlreadyExistsException
        client().deleteOntology(ontology.getUuid());
    }
    
    private ArtificerOntology uploadOntology() throws Exception {
        String ontologyFileName = "colors.owl.xml";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/ontologies/" + ontologyFileName);
        Assert.assertNotNull(is);
        try {
            ArtificerAtomApiClient client = client();
            RDF rdf = client.uploadOntology(is);
            Assert.assertNotNull(rdf);
            ArtificerOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
            Assert.assertNotNull(ontology);
            Assert.assertEquals("http://www.example.org/colors.owl", ontology.getBase());
            Assert.assertNotNull(ontology.getUuid());
            
            return ontology;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#getOntologies()}.
     */
    @Test
    public void testGetOntologies() throws Exception {
        ArtificerAtomApiClient client = client();
        List<OntologySummary> ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertTrue(ontologies.isEmpty());
        // Re-use another test to upload an ontology
        ArtificerOntology ontology = uploadOntology();

        // Now go again with data there.
        ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertFalse(ontologies.isEmpty());
        Assert.assertEquals(1, ontologies.size());
        OntologySummary ontologySummary = ontologies.get(0);
        Assert.assertEquals("http://www.example.org/colors.owl", ontologySummary.getBase());
        Assert.assertEquals("Colors ontology", ontologySummary.getComment());
        Assert.assertEquals("Colors", ontologySummary.getId());
        Assert.assertEquals("Colors", ontologySummary.getLabel());
        Assert.assertNotNull(ontologySummary.getUuid());
        
        // delete it to prevent OntologyAlreadyExistsException
        client().deleteOntology(ontology.getUuid());
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#getOntology(String)}.
     */
    @Test
    public void testGetOntology() throws Exception {
        ArtificerAtomApiClient client = client();
        RDF rdf = null;
        try {
            rdf = client.getOntology("INVALID_UUID");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().contains("No ontology found"));
        }
        Assert.assertNull(rdf);

        // Re-use another test to upload an ontology
        uploadOntology();

        // Now go again with data there.
        List<OntologySummary> ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertFalse(ontologies.isEmpty());
        Assert.assertEquals(1, ontologies.size());
        OntologySummary ontologySummary = ontologies.get(0);
        String uuid = ontologySummary.getUuid();

        rdf = client.getOntology(uuid);
        Assert.assertNotNull(rdf);
        ArtificerOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
        Assert.assertNotNull(ontology);
        Assert.assertEquals("http://www.example.org/colors.owl", ontology.getBase());
        Assert.assertNotNull(ontology.getUuid());
        
        // delete it to prevent OntologyAlreadyExistsException
        client().deleteOntology(ontology.getUuid());
    }

    /**
     * Test method for {@link org.artificer.client.ArtificerAtomApiClient#query(String, int, int, String, boolean, java.util.Collection)
     * 
     * https://issues.jboss.org/browse/SRAMP-389
     */
    @Test
    public void testQueryWithPropertyName_SRAMP389() throws Exception {
        ArtificerAtomApiClient client = client();

        // First add a bunch of artifacts so we can search for them.
        for (int count = 0; count < 10; count++) {
            String artifactFileName = "PO-" + count + ".xsd";
            InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/PO.xsd");
            try {
                BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
                Assert.assertNotNull(artifact);
                Assert.assertEquals(artifactFileName, artifact.getName());

                // Set some custom properties and then update.
                ArtificerModelUtils.setCustomProperty(artifact, "count", String.valueOf(count));
                ArtificerModelUtils.setCustomProperty(artifact, "prop1", "foo");
                ArtificerModelUtils.setCustomProperty(artifact, "test", "SRAMP-389");
                client.updateArtifactMetaData(artifact);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }

        // Now search for the artifacts and request some of the custom
        // properties be returned in the result set.
        QueryResultSet rset = client.buildQuery("/s-ramp[@test = 'SRAMP-389']")
                .propertyName("count").propertyName("prop1")
                .orderBy("createdTimestamp").descending().query();
        StringBuilder builder = new StringBuilder();
        System.out.println("----- Query done, iterating result set");
        long start = System.currentTimeMillis();
        for (ArtifactSummary artifactSummary : rset) {
            String prop = artifactSummary.getCustomPropertyValue("count");
            builder.append(prop);
            builder.append("|");
        }
        long end = System.currentTimeMillis();
        System.out.println("----- Done iterating in: " + (end-start) + "ms");
        Assert.assertEquals("9|8|7|6|5|4|3|2|1|0|", builder.toString());
    }
    
    @Test
    public void testFullTextQuery() throws Exception {
        ArtificerAtomApiClient client = client();
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for all XSDs
        QueryResultSet rset = client.query("/s-ramp[xp2:matches(., 'Purchase order schema')]");
        boolean uuidFound = false;
        for (ArtifactSummary entry : rset) {
            if (entry.getUuid().equals(uuid))
                uuidFound = true;
        }
        Assert.assertTrue("Failed to find the artifact we just added!", uuidFound);
    }

}
