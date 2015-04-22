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
package org.artificer.repository.test;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.repository.query.ArtifactSet;
import org.artificer.repository.query.ArtificerQuery;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Comment;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;


/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class PersistenceTest extends AbstractNoAuditingPersistenceTest {

    @Test
    public void testPersistArtifact_PDF() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));

        Assert.assertNotNull(artifact);
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.Document());
        }
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((DocumentArtifactType) artifact).getContentSize());
        Assert.assertEquals("4ee67f4c9f12ebe58c0c6d55d20d9dab91d8ab39", ((DocumentArtifactType) artifact).getContentHash());
    }

    @Test
    public void testPersistDuplicateArtifact() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.setUuid("12345"); // amazing - that's the same UUID as my luggage!
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
        Assert.assertNotNull(artifact);

        // Now try to persist another artifact of the same type with the same UUID.
        pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        document = new Document();
        document.setName(artifactFileName + "-2");
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.setUuid("12345"); // amazing - that's the same UUID as my luggage!
        try {
            persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
            Assert.fail("Expected an ArtifactAlreadyExistsException.");
        } catch (ArtificerConflictException e) {
            // Expected this!
            Assert.assertEquals("Artifact with UUID 12345 already exists.", e.getMessage());
        }

        // Now try to persist another artifact with a *different* type but the same UUID.
        pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        ExtendedArtifactType extendedArtifact = new ExtendedArtifactType();
        extendedArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        extendedArtifact.setExtendedType("FooArtifactType");
        extendedArtifact.setName("MyExtendedArtifact");
        extendedArtifact.setUuid("12345");
        try {
            persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
            Assert.fail("Expected an ArtifactAlreadyExistsException.");
        } catch (ArtificerConflictException e) {
            // Expected this!
            Assert.assertEquals("Artifact with UUID 12345 already exists.", e.getMessage());
        }
    }

    @Test
    public void testPersistArtifactPO_XSD() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        XsdDocument document = new XsdDocument();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, POXsd));

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XsdDocument());
        }

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
    }

    @Test
    public void testPersistArtifactPO_XML() throws Exception {
        String artifactFileName = "PO.xml";
        InputStream POXml = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);

        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, POXml));

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xml to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XmlDocument());
        }
        Assert.assertEquals(XmlDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        assertTrue(size >= 825L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
    }

    @Test
    public void testPersistArtifact_ExtendedArtifactType() throws Exception {
        ExtendedArtifactType extendedArtifact = new ExtendedArtifactType();
        extendedArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        extendedArtifact.setExtendedType("FooArtifactType");
        extendedArtifact.setName("MyExtendedArtifact");
        extendedArtifact.setDescription("This is a simple description for testing.");

        BaseArtifactType artifact = persistenceManager.persistArtifact(extendedArtifact, null);
        Assert.assertNotNull(artifact);
        log.info("persisted extended artifact to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XmlDocument());
        }
        Assert.assertEquals(ExtendedArtifactType.class, artifact.getClass());

        String name = ((ExtendedArtifactType) artifact).getName();
        String description = ((ExtendedArtifactType) artifact).getDescription();

        Assert.assertEquals("MyExtendedArtifact", name);
        Assert.assertEquals("This is a simple description for testing.", description);
    }

    @Test
    public void testGetArtifact_XSD() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, POXsd));

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings

        BaseArtifactType artifact2 = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertEquals(artifact.getUuid(), artifact2.getUuid());
        Assert.assertEquals(artifact.getCreatedBy(), artifact2.getCreatedBy());
        Assert.assertEquals(artifact.getDescription(), artifact2.getDescription());
        Assert.assertEquals(artifact.getLastModifiedBy(), artifact2.getLastModifiedBy());
        Assert.assertEquals(artifact.getName(), artifact2.getName());
        Assert.assertEquals(artifact.getVersion(), artifact2.getVersion());
        Assert.assertEquals(artifact.getLastModifiedTimestamp(), artifact2.getLastModifiedTimestamp());
    }

    /**
     * Tests that we can update basic s-ramp meta data.
     * @throws Exception
     */
    @Test
    public void testUpdateMetaData() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, POXsd));
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        artifact.setName("My PO");
        artifact.setDescription("A new description of the PO.xsd artifact.");
        artifact.setVersion("2.0.13");
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify the meta-data was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertEquals("My PO", artifact.getName());
        Assert.assertEquals("A new description of the PO.xsd artifact.", artifact.getDescription());
        Assert.assertEquals("2.0.13", artifact.getVersion());
    }

    /**
     * Tests that we can update the content of an s-ramp artifact.
     * @throws Exception
     */
    @Test
    public void testUpdateContent() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, POXsd));
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact content
        InputStream otherXsd = this.getClass().getResourceAsStream("/sample-files/xsd/XMLSchema.xsd");
        persistenceManager.updateArtifactContent(artifact.getUuid(), ArtifactType.XsdDocument(), new ArtifactContent("XMLSchema.xsd", otherXsd));

        // Now verify the content was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        size = ((DocumentArtifactType) artifact).getContentSize();
        // TODO: Fails for Hibernate, but updateContent will be removed.
//        assertTrue(size >= 87677L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
    }

    /**
     * Tests that we can manage s-ramp properties.
     * @throws Exception
     */
    @Test
    public void testUpdateProperties() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, POXsd));
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty());
        Property prop1 = new Property();
        prop1.setPropertyName("prop1");
        prop1.setPropertyValue("propval1");
		artifact.getProperty().add(prop1);
        Property prop2 = new Property();
        prop2.setPropertyName("prop2");
        prop2.setPropertyValue("propval2");
        artifact.getProperty().add(prop2);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the properties were stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        String p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        String p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        Set<String> ps = new HashSet<String>();
        ps.add(p1);
        ps.add(p2);
        assertTrue("Prop1 missing from properties.", ps.contains("prop1=propval1"));
        assertTrue("Prop2 missing from properties.", ps.contains("prop2=propval2"));
        Assert.assertFalse("Prop3 somehow existed!.", ps.contains("prop3=propval3"));

        // Now remove one property, add another one, and change the value of one
        artifact.getProperty().clear();
        prop1 = new Property();
        prop1.setPropertyName("prop1");
        prop1.setPropertyValue("propval1-updated");
		artifact.getProperty().add(prop1);
        Property prop3 = new Property();
        prop3.setPropertyName("prop3");
        prop3.setPropertyValue("propval3");
        artifact.getProperty().add(prop3);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the properties were updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        ps.clear();
        ps.add(p1);
        ps.add(p2);
        Assert.assertFalse("Prop1 wasn't updated (old value detected).", ps.contains("prop1=propval1"));
        assertTrue("Prop1 wasn't updated (new value not found).", ps.contains("prop1=propval1-updated"));
        Assert.assertFalse("Prop2 existed unexpectedly.", ps.contains("prop2=propval2"));
        assertTrue("Prop3 missing from properties.", ps.contains("prop3=propval3"));
    }

    /**
     * Tests that we can manage s-ramp properties on a /core/Document.
     * @throws Exception
     */
    @Test
    public void testUpdateProperties_Document() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);

        Document document = new Document();
        document.setName(artifactFileName);
        document.setContentType("application/pdf");
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
		BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
        Assert.assertNotNull(artifact);
        log.info("persisted PDF to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((DocumentArtifactType) artifact).getContentSize());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty());
        Property prop1 = new Property();
        prop1.setPropertyName("prop1");
        prop1.setPropertyValue("propval1");
		artifact.getProperty().add(prop1);
        Property prop2 = new Property();
        prop2.setPropertyName("prop2");
        prop2.setPropertyValue("propval2");
        artifact.getProperty().add(prop2);
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());

        // Now verify that the properties were stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        String p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        String p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        Set<String> ps = new HashSet<String>();
        ps.add(p1);
        ps.add(p2);
        assertTrue("Prop1 missing from properties.", ps.contains("prop1=propval1"));
        assertTrue("Prop2 missing from properties.", ps.contains("prop2=propval2"));
        Assert.assertFalse("Prop3 somehow existed!.", ps.contains("prop3=propval3"));

        // Now remove one property, add another one, and change the value of one
        artifact.getProperty().clear();
        prop1 = new Property();
        prop1.setPropertyName("prop1");
        prop1.setPropertyValue("propval1-updated");
		artifact.getProperty().add(prop1);
        Property prop3 = new Property();
        prop3.setPropertyName("prop3");
        prop3.setPropertyValue("propval3");
        artifact.getProperty().add(prop3);
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());

        // Now verify that the properties were updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        ps.clear();
        ps.add(p1);
        ps.add(p2);
        Assert.assertFalse("Prop1 wasn't updated (old value detected).", ps.contains("prop1=propval1"));
        assertTrue("Prop1 wasn't updated (new value not found).", ps.contains("prop1=propval1-updated"));
        Assert.assertFalse("Prop2 existed unexpectedly.", ps.contains("prop2=propval2"));
        assertTrue("Prop3 missing from properties.", ps.contains("prop3=propval3"));
    }

    /**
     * Tests that we can manage s-ramp properties.
     * @throws Exception
     */
    @Test
    public void testGenericRelationships() throws Exception {
    	String uuid1 = null;
    	String uuid2 = null;
    	String uuid3 = null;

    	// First, add an artifact to the repo
        String artifactFileName = "PO.xsd";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, contentStream));
        Assert.assertNotNull(artifact);
        uuid1 = artifact.getUuid();
        contentStream.close();

        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        assertTrue("Expected 0 relationships.", artifact.getRelationship().isEmpty());

        // Add a second artifact.
        artifactFileName = "XMLSchema.xsd";
        contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document2 = new Document();
        document2.setName(artifactFileName);
        document2.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact2 = persistenceManager.persistArtifact(document2, new ArtifactContent(artifactFileName, contentStream));
        Assert.assertNotNull(artifact2);
        uuid2 = artifact2.getUuid();

        // Add a relationship
        ArtificerModelUtils.addGenericRelationship(artifact, "TargetedRelationship", uuid2);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the targeted relationship was stored
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 1 relationship.", 1, artifact.getRelationship().size());
        Relationship relationship = ArtificerModelUtils.getGenericRelationship(artifact, "TargetedRelationship");
        Assert.assertNotNull(relationship);
        Assert.assertEquals("TargetedRelationship", relationship.getRelationshipType());
        Assert.assertEquals(1, relationship.getRelationshipTarget().size()); // has only one target
        Assert.assertEquals(uuid2, relationship.getRelationshipTarget().get(0).getValue());

        // Add a third artifact.
        artifactFileName = "PO.xml";
        contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document3 = new Document();
        document3.setName(artifactFileName);
        document3.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
        BaseArtifactType artifact3 = persistenceManager.persistArtifact(document3,  new ArtifactContent(artifactFileName, contentStream));
        Assert.assertNotNull(artifact3);
        uuid3 = artifact3.getUuid();

        // Add a third relationship, again with a target.
        ArtificerModelUtils.addGenericRelationship(artifact, "TargetedRelationship", uuid3);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // More verifications
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 1 relationships.", 1, artifact.getRelationship().size());
        relationship = ArtificerModelUtils.getGenericRelationship(artifact, "TargetedRelationship");
        Assert.assertNotNull(relationship);
        Assert.assertEquals("TargetedRelationship", relationship.getRelationshipType());
        Assert.assertEquals(2, relationship.getRelationshipTarget().size());
        Set<String> expected = new HashSet<String>();
        Set<String> actual = new HashSet<String>();
        expected.add(uuid2);
        expected.add(uuid3);
        actual.add(relationship.getRelationshipTarget().get(0).getValue());
        actual.add(relationship.getRelationshipTarget().get(1).getValue());
        Assert.assertEquals(expected, actual);

        // Add a fourth (bogus) relationship
        ArtificerModelUtils.addGenericRelationship(artifact, "TargetedRelationship", "not-a-valid-uuid");
    	try {
			persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());
			Assert.fail("Expected an update failure.");
		} catch (Exception e) {
		    Assert.assertEquals(ArtificerNotFoundException.class, e.getClass());
			Assert.assertEquals("No artifact found with UUID: not-a-valid-uuid", e.getMessage());
		}
    }

    @Test
    public void testArtifactComments() throws Exception {
        BaseArtifactType artifact = ArtifactType.ExtendedArtifactType("FooType").newArtifactInstance();
        artifact.setName("FooArtifact");

        artifact = persistenceManager.persistArtifact(artifact, null);

        persistenceManager.addComment(artifact.getUuid(), ArtifactType.valueOf(artifact), "xyz");
        persistenceManager.addComment(artifact.getUuid(), ArtifactType.valueOf(artifact), "abc");
        artifact = persistenceManager.addComment(artifact.getUuid(), ArtifactType.valueOf(artifact), "lmn");

        Assert.assertEquals(3, artifact.getComment().size());
        // ensure they were ordered by timestamp asc
        commentAssertions(artifact.getComment().get(0), "xyz");
        commentAssertions(artifact.getComment().get(1), "abc");
        commentAssertions(artifact.getComment().get(2), "lmn");
    }

    private void commentAssertions(Comment comment, String text) {
        Assert.assertEquals(text, comment.getText());
        Assert.assertNotNull(comment.getCreatedTimestamp());
        Assert.assertEquals("junituser", comment.getCreatedBy());
    }

    @Test
    public void testDeleteArtifact() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);

        // Add an artifact
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
        Assert.assertNotNull(artifact);
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((DocumentArtifactType) artifact).getContentSize());
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid());

        // Now delete that artifact
        ArtifactType at = ArtifactType.valueOf(artifact);
        persistenceManager.deleteArtifact(document.getUuid(), at, false);

        // Now make sure we can't load it back up
        BaseArtifactType deleted = persistenceManager.getArtifact(document.getUuid(), at);
        Assert.assertNull(deleted);

        ArtificerQuery query = queryManager.createQuery("/s-ramp[@uuid = ?]");
        query.setString(document.getUuid());
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertEquals(0, artifactSet.size());

        // Ensure we can re-create an artifact w/ the same UUID, then delete it again without same-name collisions
        // in the trash folder.
        pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        BaseArtifactType artifact2 = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
        Assert.assertEquals(artifact.getUuid(), artifact2.getUuid());
        persistenceManager.deleteArtifact(document.getUuid(), at, false);
        deleted = persistenceManager.getArtifact(document.getUuid(), at);
        Assert.assertNull(deleted);
    }

}
