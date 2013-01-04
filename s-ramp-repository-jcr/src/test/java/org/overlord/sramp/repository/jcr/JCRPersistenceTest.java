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
package org.overlord.sramp.repository.jcr;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;
import org.overlord.sramp.ArtifactNotFoundException;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.SrampModelUtils;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Document;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.Relationship;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;


/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class JCRPersistenceTest extends AbstractJCRPersistenceTest {

    @Test
    public void testSave_PDF() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);

        Assert.assertNotNull(artifact);
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.Document());
        }
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((Document) artifact).getContentSize());
    }

    @Test
    public void testSavePO_XSD() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XsdDocument());
        }

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());
    }

    @Test
    public void testSavePO_XML() throws Exception {
        String artifactFileName = "PO.xml";
        InputStream POXml = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);

        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXml);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xml to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XmlDocument());
        }
        Assert.assertEquals(XmlDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(825l), ((XmlDocument) artifact).getContentSize());
    }

    @Test
    public void testGetArtifact_XSD() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());

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
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());
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
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact content
        InputStream otherXsd = this.getClass().getResourceAsStream("/sample-files/xsd/XMLSchema.xsd");
        persistenceManager.updateArtifactContent(artifact.getUuid(), ArtifactType.XsdDocument(), otherXsd);

        // Now verify the content was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertEquals(new Long(87677), ((XsdDocument) artifact).getContentSize());
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
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty());
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
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        String p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        String p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        Set<String> ps = new HashSet<String>();
        ps.add(p1);
        ps.add(p2);
        Assert.assertTrue("Prop1 missing from properties.", ps.contains("prop1=propval1"));
        Assert.assertTrue("Prop2 missing from properties.", ps.contains("prop2=propval2"));
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
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        ps.clear();
        ps.add(p1);
        ps.add(p2);
        Assert.assertFalse("Prop1 wasn't updated (old value detected).", ps.contains("prop1=propval1"));
        Assert.assertTrue("Prop1 wasn't updated (new value not found).", ps.contains("prop1=propval1-updated"));
        Assert.assertFalse("Prop2 existed unexpectedly.", ps.contains("prop2=propval2"));
        Assert.assertTrue("Prop3 missing from properties.", ps.contains("prop3=propval3"));
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
		BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);
        Assert.assertNotNull(artifact);
        log.info("persisted PDF to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((Document) artifact).getContentSize());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        Assert.assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty());
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
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        String p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        String p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        Set<String> ps = new HashSet<String>();
        ps.add(p1);
        ps.add(p2);
        Assert.assertTrue("Prop1 missing from properties.", ps.contains("prop1=propval1"));
        Assert.assertTrue("Prop2 missing from properties.", ps.contains("prop2=propval2"));
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
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2);
        p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue();
        p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue();
        ps.clear();
        ps.add(p1);
        ps.add(p2);
        Assert.assertFalse("Prop1 wasn't updated (old value detected).", ps.contains("prop1=propval1"));
        Assert.assertTrue("Prop1 wasn't updated (new value not found).", ps.contains("prop1=propval1-updated"));
        Assert.assertFalse("Prop2 existed unexpectedly.", ps.contains("prop2=propval2"));
        Assert.assertTrue("Prop3 missing from properties.", ps.contains("prop3=propval3"));
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
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, contentStream);
        Assert.assertNotNull(artifact);
        uuid1 = artifact.getUuid();
        contentStream.close();

        // Now update the artifact's generic relationships
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertTrue("Expected 0 relationships.", artifact.getRelationship().isEmpty());
        SrampModelUtils.addGenericRelationship(artifact, "NoTargetRelationship", null);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the relationship was stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 1 relationship.", 1, artifact.getRelationship().size());
        Assert.assertEquals("NoTargetRelationship", artifact.getRelationship().get(0).getRelationshipType());
        Assert.assertEquals(Collections.EMPTY_LIST, artifact.getRelationship().get(0).getRelationshipTarget());

        // Add a second artifact.
        artifactFileName = "XMLSchema.xsd";
        contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        Document document2 = new Document();
        document2.setName(artifactFileName);
        document2.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact2 = persistenceManager.persistArtifact(document2, contentStream);
        Assert.assertNotNull(artifact2);
        uuid2 = artifact2.getUuid();

        // Add a second relationship, this time with a target.
        SrampModelUtils.addGenericRelationship(artifact, "TargetedRelationship", uuid2);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the targeted relationship was stored
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 2 relationships.", 2, artifact.getRelationship().size());
        Relationship relationship = SrampModelUtils.getGenericRelationship(artifact, "NoTargetRelationship");
        Assert.assertNotNull(relationship);
        Assert.assertEquals("NoTargetRelationship", relationship.getRelationshipType());
        Assert.assertEquals(Collections.EMPTY_LIST, relationship.getRelationshipTarget());
        relationship = SrampModelUtils.getGenericRelationship(artifact, "TargetedRelationship");
        Assert.assertNotNull(relationship);
        Assert.assertEquals("TargetedRelationship", relationship.getRelationshipType());
        Assert.assertEquals(1, relationship.getRelationshipTarget().size()); // has only one target
        Assert.assertEquals(uuid2, relationship.getRelationshipTarget().get(0).getValue());

        // Add a third artifact.
        artifactFileName = "PO.xml";
        contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document3 = new Document();
        document3.setName(artifactFileName);
        document3.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
        BaseArtifactType artifact3 = persistenceManager.persistArtifact(document3,  contentStream);
        Assert.assertNotNull(artifact3);
        uuid3 = artifact3.getUuid();

        // Add a third relationship, again with a target.
        SrampModelUtils.addGenericRelationship(artifact, "TargetedRelationship", uuid3);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // More verifications
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 2 relationships.", 2, artifact.getRelationship().size());
        relationship = SrampModelUtils.getGenericRelationship(artifact, "NoTargetRelationship");
        Assert.assertNotNull(relationship);
        Assert.assertEquals("NoTargetRelationship", relationship.getRelationshipType());
        Assert.assertEquals(Collections.EMPTY_LIST, relationship.getRelationshipTarget());
        relationship = SrampModelUtils.getGenericRelationship(artifact, "TargetedRelationship");
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
        SrampModelUtils.addGenericRelationship(artifact, "TargetedRelationship", "not-a-valid-uuid");
    	try {
			persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());
			Assert.fail("Expected an update failure.");
		} catch (Exception e) {
		    Assert.assertEquals(ArtifactNotFoundException.class, e.getClass());
			Assert.assertEquals("No artifact found with UUID: not-a-valid-uuid", e.getMessage());
		}
    }


}
