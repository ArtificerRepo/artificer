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
package org.overlord.sramp.test.client;

import static org.junit.Assert.fail;

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

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.mappers.RdfToOntologyMapper;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * Unit test for the bulk of the s-ramp client features.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
public class SrampAtomApiClientTest extends AbstractNoAuditingClientTest {

	/**
	 * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testUploadArtifact() throws Exception {
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
		try {
			SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

    /**
     * Test method for {@link SrampAtomApiClient#createArtifact(BaseArtifactType)}.
     */
    @Test
    public void testCreateArtifact() throws Exception {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("TestArtifact"); //$NON-NLS-1$
        artifact.setName("My Test Artifact"); //$NON-NLS-1$
        artifact.setDescription("Description of my test artifact."); //$NON-NLS-1$
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
        BaseArtifactType createdArtifact = client.createArtifact(artifact);
        Assert.assertNotNull(artifact);
        Assert.assertEquals("My Test Artifact", createdArtifact.getName()); //$NON-NLS-1$
        Assert.assertEquals("Description of my test artifact.", createdArtifact.getDescription()); //$NON-NLS-1$
    }

    /**
     * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
     */
    @Test
    public void testExtendedDocumentArtifact() throws Exception {
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        try {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.ExtendedDocument("TestDocument"), is, artifactFileName); //$NON-NLS-1$
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            Assert.assertEquals(BaseArtifactEnum.EXTENDED_DOCUMENT, artifact.getArtifactType());
            Assert.assertEquals(ExtendedDocument.class, artifact.getClass());
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#getArtifactMetaData(ArtifactType, String)}
     */
    @Test
    public void testGetArtifactMetaData() throws Exception {
        String uuid = null;
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        try {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now test that we can fetch the meta-data using the artifact type and UUID
        {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            BaseArtifactType metaData = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid);
            Assert.assertNotNull(metaData);
            Assert.assertEquals(artifactFileName, metaData.getName());
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#getArtifactMetaData(String)}
     */
    @Test
    public void testGetArtifactMetaDataNoType() throws Exception {
        String uuid = null;
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        try {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now test that we can fetch the meta-data using just the UUID
        {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            BaseArtifactType metaData = client.getArtifactMetaData(uuid);
            Assert.assertNotNull(metaData);
            Assert.assertEquals(artifactFileName, metaData.getName());
        }
    }


	/**
     * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
     */
    @Test
    public void testUploadArtifactAndContent() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        try {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            XsdDocument xsdDocument = new XsdDocument();
            xsdDocument.setName(artifactFileName);
            xsdDocument.setUuid(uuid); //$NON-NLS-1$
            BaseArtifactType artifact = client.uploadArtifact(xsdDocument, is);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            Assert.assertEquals(uuid, artifact.getUuid()); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

	/**
	 * Test method for {@link SrampAtomApiClient#getArtifactContent(java.lang.String, java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testGetArtifactContent() throws Exception {
		SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
		String uuid = null;

		// First, upload an artifact so we have some content to get
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
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
			Assert.assertTrue("Unexpected content found.", line1.startsWith("<?xml version=\"1.0\"")); //$NON-NLS-1$ //$NON-NLS-2$
			Assert.assertTrue("Unexpected content found.", line2.startsWith("<xsd:schema")); //$NON-NLS-1$ //$NON-NLS-2$
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactMetaData() throws Exception {
		SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
		String uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
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
		xsdDoc.setDescription("** DESCRIPTION UPDATED **"); //$NON-NLS-1$
		client.updateArtifactMetaData(xsdDoc);

		// Now verify
		BaseArtifactType artifact = client.getArtifactMetaData(ArtifactType.XsdDocument(), uuid.toString());
		Assert.assertEquals("** DESCRIPTION UPDATED **", artifact.getDescription()); //$NON-NLS-1$
	}

	/**
	 * Tests updating an artifact.
	 * @throws Exception
	 */
	public void testUpdateArtifactContent() throws Exception {
		SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
		String uuid = null;
		XsdDocument xsdDoc = null;

		// First, upload an artifact so we have some content to update
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
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
		is = this.getClass().getResourceAsStream("/sample-files/xsd/PO-updated.xsd"); //$NON-NLS-1$
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
	 * Test method for {@link SrampAtomApiClient#query(java.lang.String, int, int, java.lang.String, boolean)}.
	 */
	@Test
	public void testQuery() throws Exception {
		SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
		String uuid = null;

		// First add an artifact so we have something to search for
		String artifactFileName = "PO.xsd"; //$NON-NLS-1$
		InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
		try {
			BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
			Assert.assertNotNull(artifact);
			Assert.assertEquals(artifactFileName, artifact.getName());
			uuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(is);
		}

		// Now search for all XSDs
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument", 0, 50, "name", false); //$NON-NLS-1$ //$NON-NLS-2$
		boolean uuidFound = false;
		for (ArtifactSummary entry : rset) {
			if (entry.getUuid().equals(uuid))
				uuidFound = true;
		}
		Assert.assertTrue("Failed to find the artifact we just added!", uuidFound); //$NON-NLS-1$
	}

    /**
     * Test method for {@link SrampAtomApiClient#query(String, int, int, String, boolean, java.util.Collection)
     */
    @Test
    public void testQueryWithPropertyName() throws Exception {
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();

            // Set a couple of custom properties and update
            SrampModelUtils.setCustomProperty(artifact, "prop1", "foo"); //$NON-NLS-1$ //$NON-NLS-2$
            SrampModelUtils.setCustomProperty(artifact, "prop2", "bar"); //$NON-NLS-1$ //$NON-NLS-2$
            SrampModelUtils.setCustomProperty(artifact, "prop3", "baz"); //$NON-NLS-1$ //$NON-NLS-2$
            client.updateArtifactMetaData(artifact);
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for the artifact and request one of the custom
        // properties be returned in the result set.
        Set<String> propertyNames = new HashSet<String>();
        propertyNames.add("prop1"); //$NON-NLS-1$
        propertyNames.add("prop2"); //$NON-NLS-1$
        QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument[@uuid='"+uuid+"']", 0, 50, "name", false, propertyNames); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Assert.assertEquals("Expected a single artifact returned.", 1, rset.size()); //$NON-NLS-1$
        ArtifactSummary summary = rset.get(0);
        Assert.assertEquals("foo", summary.getCustomPropertyValue("prop1")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals("bar", summary.getCustomPropertyValue("prop2")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertNull("I didn't ask for 'prop3' to be returned!", summary.getCustomPropertyValue("prop3")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Test method for {@link SrampAtomApiClient#buildQuery(String)
     */
    @Test
    public void testBuildQuery() throws Exception {
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
        String uuid = null;

        // First add an artifact so we have something to search for
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        try {
            BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
            Assert.assertNotNull(artifact);
            Assert.assertEquals(artifactFileName, artifact.getName());
            uuid = artifact.getUuid();
        } finally {
            IOUtils.closeQuietly(is);
        }

        // Now search for the XSD by its UUID
        QueryResultSet rset = client.buildQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]").parameter(uuid) //$NON-NLS-1$
                .count(1).query();
        Assert.assertTrue("Failed to find the artifact we just added!", rset.size() == 1); //$NON-NLS-1$

        // Do a couple of date-based queries here
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp < ?]") //$NON-NLS-1$
                .parameter(new Date(System.currentTimeMillis() + 86400000L))
                .count(1).query();
        Assert.assertTrue("Failed to find an artifact by lastModifiedTimestamp!", rset.size() == 1); //$NON-NLS-1$
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp > ?]") //$NON-NLS-1$
                .parameter(new Date(System.currentTimeMillis() + 86400000L))
                .count(1).query();
        Assert.assertTrue("Found an artifact by lastModifiedTimestamp, but should *not* have!", rset.size() == 0); //$NON-NLS-1$

        // Now by DateTime
        Calendar endOfToday = Calendar.getInstance();
        endOfToday.set(Calendar.HOUR_OF_DAY, 0);
        endOfToday.set(Calendar.MINUTE, 0);
        endOfToday.set(Calendar.SECOND, 0);
        endOfToday.set(Calendar.MILLISECOND, 0);
        endOfToday.add(Calendar.DAY_OF_YEAR, 1);
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp < ?]") //$NON-NLS-1$
                .parameter(endOfToday)
                .count(1).query();
        Assert.assertTrue("Failed to find an artifact by lastModifiedTimestamp!", rset.size() == 1); //$NON-NLS-1$
        rset = client.buildQuery("/s-ramp[@lastModifiedTimestamp > ?]") //$NON-NLS-1$
                .parameter(endOfToday)
                .count(1).query();
        Assert.assertTrue("Found an artifact by lastModifiedTimestamp, but should *not* have!", rset.size() == 0); //$NON-NLS-1$
    }

    /**
     * Test method for {@link SrampAtomApiClient#buildQuery(String)
     */
    @Test
    public void testResultSetAttributes() throws Exception {
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
        for (int i = 0; i < 20; i++) {
            addXmlDoc();
        }
        QueryResultSet rs = client.buildQuery("/s-ramp/core").count(2).startIndex(5).query(); //$NON-NLS-1$
        Assert.assertEquals(20, rs.getTotalResults());
        Assert.assertEquals(2, rs.getItemsPerPage());
        Assert.assertEquals(5, rs.getStartIndex());
    }

	/**
	 * Test method for {@link SrampAtomApiClient#uploadArtifact(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)}.
	 */
	@Test
	public void testQueryError() throws Exception {
		SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
		try {
			QueryResultSet rset = client.query("12345", 0, 20, "name", false); //$NON-NLS-1$ //$NON-NLS-2$
			fail("Expected a remote exception from the s-ramp server, but got: " + rset); //$NON-NLS-1$
		} catch (SrampAtomException e) {
			Assert.assertEquals("Invalid artifact set (step 2).", e.getMessage()); //$NON-NLS-1$
		}
	}

	/**
	 * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
	 */
	@Test
	public void testArchiveUpload() throws Exception {
		// First, create an s-ramp archive
		SrampArchive archive = null;
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			archive = new SrampArchive();

			String artifactFileName = "PO.xsd"; //$NON-NLS-1$
			is1 = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("PO.xsd"); //$NON-NLS-1$
			metaData.setVersion("1.1"); //$NON-NLS-1$
			metaData.setDescription("This is a test description (XSD)."); //$NON-NLS-1$
			archive.addEntry("schemas/PO.xsd", metaData, is1); //$NON-NLS-1$

			artifactFileName = "PO.xml"; //$NON-NLS-1$
			is2 = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
			metaData = new XsdDocument();
			metaData.setName("PO.xml"); //$NON-NLS-1$
			metaData.setVersion("1.2"); //$NON-NLS-1$
			metaData.setDescription("This is a test description (XML)."); //$NON-NLS-1$
			archive.addEntry("core/PO.xml", metaData, is2); //$NON-NLS-1$
		} catch (Exception e) {
			SrampArchive.closeQuietly(archive);
			throw e;
		} finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}

		try {
			// Now use the s-ramp atom api client to upload the s-ramp archive
			SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
			Map<String, ?> results = client.uploadBatch(archive);
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.keySet().contains("schemas/PO.xsd")); //$NON-NLS-1$
			Assert.assertTrue(results.keySet().contains("core/PO.xml")); //$NON-NLS-1$

			XsdDocument xsdDoc = (XsdDocument) results.get("schemas/PO.xsd"); //$NON-NLS-1$
			Assert.assertNotNull(xsdDoc);
			Assert.assertEquals("PO.xsd", xsdDoc.getName()); //$NON-NLS-1$
			Assert.assertEquals("1.1", xsdDoc.getVersion()); //$NON-NLS-1$

			XmlDocument xmlDoc = (XmlDocument) results.get("core/PO.xml"); //$NON-NLS-1$
			Assert.assertNotNull(xmlDoc);
			Assert.assertEquals("PO.xml", xmlDoc.getName()); //$NON-NLS-1$
			Assert.assertEquals("1.2", xmlDoc.getVersion()); //$NON-NLS-1$
		} finally {
			SrampArchive.closeQuietly(archive);
		}
	}

    /**
     * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
     */
    @Test
    public void testArchiveUpload_Empty() throws Exception {
        // First, create an s-ramp archive
        SrampArchive archive = null;
        try {
            archive = new SrampArchive();
        } catch (Exception e) {
            SrampArchive.closeQuietly(archive);
            throw e;
        } finally {
        }

        try {
            // Now use the s-ramp atom api client to upload the s-ramp archive
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            Map<String, ?> results = client.uploadBatch(archive);
            Assert.assertTrue(results.isEmpty());
        } finally {
            SrampArchive.closeQuietly(archive);
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
     */
    @Test
    public void testArchiveUpload_AtomOnly() throws Exception {
        // First, create an s-ramp archive
        SrampArchive archive = null;
        try {
            archive = new SrampArchive();
            ExtendedArtifactType nonDocArtifact = new ExtendedArtifactType();
            nonDocArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            nonDocArtifact.setExtendedType("TestArtifact"); //$NON-NLS-1$
            nonDocArtifact.setName("My Test Artifact"); //$NON-NLS-1$

            archive.addEntry("myLogicalArtifact", nonDocArtifact, null); //$NON-NLS-1$
        } catch (Exception e) {
            SrampArchive.closeQuietly(archive);
            throw e;
        } finally {
        }

        try {
            // Now use the s-ramp atom api client to upload the s-ramp archive
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            
            Map<String, ?> results = client.uploadBatch(archive);
            Assert.assertFalse(results.isEmpty());
            Assert.assertEquals(1, results.size());

            QueryResultSet resultSet = client.buildQuery("/s-ramp/ext").query(); //$NON-NLS-1$
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());

            resultSet = client.buildQuery("/s-ramp/ext/TestArtifact").query(); //$NON-NLS-1$
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());

            resultSet = client.buildQuery("/s-ramp/ext/TestArtifact[@name = 'My Test Artifact']").query(); //$NON-NLS-1$
            Assert.assertNotNull(resultSet);
            Assert.assertEquals(1, resultSet.getTotalResults());
        } finally {
            SrampArchive.closeQuietly(archive);
        }
    }

	/**
	 * Test method for {@link SrampAtomApiClient#uploadBatch(SrampArchive)}.
	 */
	@Test
	public void testArchiveUploadWithError() throws Exception {
		// First, create an s-ramp archive
		SrampArchive archive = null;
		InputStream is1 = null;
		InputStream is2 = null;
		try {
			archive = new SrampArchive();

			String artifactFileName = "PO.xsd"; //$NON-NLS-1$
			is1 = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("PO.xsd"); //$NON-NLS-1$
			metaData.setVersion("1.1"); //$NON-NLS-1$
			metaData.setDescription("This is a test description (XSD)."); //$NON-NLS-1$
			archive.addEntry("schemas/PO.xsd", metaData, is1); //$NON-NLS-1$

			artifactFileName = "PO.xml"; //$NON-NLS-1$
			metaData = new XsdDocument();
			metaData.setName("PO.xml"); //$NON-NLS-1$
			metaData.setVersion("1.2"); //$NON-NLS-1$
			metaData.setDescription("This is a test description (XML)."); //$NON-NLS-1$
			archive.addEntry("core/PO.xml", metaData, null); //$NON-NLS-1$
		} catch (Exception e) {
			SrampArchive.closeQuietly(archive);
			throw e;
		} finally {
			IOUtils.closeQuietly(is1);
			IOUtils.closeQuietly(is2);
		}

		try {
			// Now use the s-ramp atom api client to upload the s-ramp archive
			SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
			Map<String, ?> results = client.uploadBatch(archive);
			Assert.assertEquals(2, results.size());
			Assert.assertTrue(results.keySet().contains("schemas/PO.xsd")); //$NON-NLS-1$
			Assert.assertTrue(results.keySet().contains("core/PO.xml")); //$NON-NLS-1$

			XsdDocument xsdDoc = (XsdDocument) results.get("schemas/PO.xsd"); //$NON-NLS-1$
			Assert.assertNotNull(xsdDoc);
			Assert.assertEquals("PO.xsd", xsdDoc.getName()); //$NON-NLS-1$
			Assert.assertEquals("1.1", xsdDoc.getVersion()); //$NON-NLS-1$

			Exception xmlError = (Exception) results.get("core/PO.xml"); //$NON-NLS-1$
			Assert.assertNotNull(xmlError);
		} finally {
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
     * Test method for {@link SrampAtomApiClient#uploadOntology(InputStream)}.
     */
    @Test
    public void testUploadOntology() throws Exception {
        SrampOntology ontology = uploadOntology();
        
        // delete it to prevent OntologyAlreadyExistsException
        client("/s-ramp").deleteOntology(ontology.getUuid());
    }
    
    private SrampOntology uploadOntology() throws Exception {
        String ontologyFileName = "colors.owl.xml"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/ontologies/" + ontologyFileName); //$NON-NLS-1$
        Assert.assertNotNull(is);
        try {
            SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
            RDF rdf = client.uploadOntology(is);
            Assert.assertNotNull(rdf);
            SrampOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
            Assert.assertNotNull(ontology);
            Assert.assertEquals("http://www.example.org/colors.owl", ontology.getBase()); //$NON-NLS-1$
            Assert.assertNotNull(ontology.getUuid());
            
            return ontology;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * Test method for {@link SrampAtomApiClient#getOntologies()}.
     */
    @Test
    public void testGetOntologies() throws Exception {
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
        List<OntologySummary> ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertTrue(ontologies.isEmpty());
        // Re-use another test to upload an ontology
        SrampOntology ontology = uploadOntology();

        // Now go again with data there.
        ontologies = client.getOntologies();
        Assert.assertNotNull(ontologies);
        Assert.assertFalse(ontologies.isEmpty());
        Assert.assertEquals(1, ontologies.size());
        OntologySummary ontologySummary = ontologies.get(0);
        Assert.assertEquals("http://www.example.org/colors.owl", ontologySummary.getBase()); //$NON-NLS-1$
        Assert.assertEquals("Colors ontology", ontologySummary.getComment()); //$NON-NLS-1$
        Assert.assertEquals("Colors", ontologySummary.getId()); //$NON-NLS-1$
        Assert.assertEquals("Colors", ontologySummary.getLabel()); //$NON-NLS-1$
        Assert.assertNotNull(ontologySummary.getUuid());
        
        // delete it to prevent OntologyAlreadyExistsException
        client("/s-ramp").deleteOntology(ontology.getUuid());
    }

    /**
     * Test method for {@link SrampAtomApiClient#getOntology(String)}.
     */
    @Test
    public void testGetOntology() throws Exception {
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$
        RDF rdf = null;
        try {
            rdf = client.getOntology("INVALID_UUID"); //$NON-NLS-1$
        } catch (Exception e) {
            Assert.assertEquals("No ontology found with UUID: INVALID_UUID", e.getMessage()); //$NON-NLS-1$
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
        SrampOntology ontology = RdfToOntologyMapper.rdf2ontology(rdf);
        Assert.assertNotNull(ontology);
        Assert.assertEquals("http://www.example.org/colors.owl", ontology.getBase()); //$NON-NLS-1$
        Assert.assertNotNull(ontology.getUuid());
        
        // delete it to prevent OntologyAlreadyExistsException
        client("/s-ramp").deleteOntology(ontology.getUuid());
    }

    /**
     * Test method for {@link SrampAtomApiClient#query(String, int, int, String, boolean, java.util.Collection)
     * 
     * https://issues.jboss.org/browse/SRAMP-389
     */
    @Test
    public void testQueryWithPropertyName_SRAMP389() throws Exception {
        SrampAtomApiClient client = client("/s-ramp"); //$NON-NLS-1$

        // First add a bunch of artifacts so we can search for them.
        for (int count = 0; count < 10; count++) {
            String artifactFileName = "PO-" + count + ".xsd"; //$NON-NLS-1$ //$NON-NLS-2$
            InputStream is = this.getClass().getResourceAsStream("/sample-files/xsd/PO.xsd"); //$NON-NLS-1$
            try {
                BaseArtifactType artifact = client.uploadArtifact(ArtifactType.XsdDocument(), is, artifactFileName);
                Assert.assertNotNull(artifact);
                Assert.assertEquals(artifactFileName, artifact.getName());

                // Set some custom properties and then update.
                SrampModelUtils.setCustomProperty(artifact, "count", String.valueOf(count)); //$NON-NLS-1$
                SrampModelUtils.setCustomProperty(artifact, "prop1", "foo"); //$NON-NLS-1$ //$NON-NLS-2$
                SrampModelUtils.setCustomProperty(artifact, "test", "SRAMP-389"); //$NON-NLS-1$ //$NON-NLS-2$
                client.updateArtifactMetaData(artifact);
            } finally {
                IOUtils.closeQuietly(is);
            }
        }

        // Now search for the artifacts and request some of the custom
        // properties be returned in the result set.
        QueryResultSet rset = client.buildQuery("/s-ramp[@test = 'SRAMP-389']") //$NON-NLS-1$
                .propertyName("count").propertyName("prop1") //$NON-NLS-1$ //$NON-NLS-2$
                .orderBy("createdTimestamp").descending().query(); //$NON-NLS-1$
        StringBuilder builder = new StringBuilder();
        System.out.println("----- Query done, iterating result set"); //$NON-NLS-1$
        long start = System.currentTimeMillis();
        for (ArtifactSummary artifactSummary : rset) {
            String prop = artifactSummary.getCustomPropertyValue("count"); //$NON-NLS-1$
            builder.append(prop);
            builder.append("|"); //$NON-NLS-1$
        }
        long end = System.currentTimeMillis();
        System.out.println("----- Done iterating in: " + (end-start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals("9|8|7|6|5|4|3|2|1|0|", builder.toString()); //$NON-NLS-1$
    }

}
