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
package org.overlord.sramp.repository.jcr.modeshape;

import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactAlreadyExistsException;
import org.overlord.sramp.common.ArtifactNotFoundException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;


/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class JCRPersistenceTest extends AbstractNoAuditingJCRPersistenceTest {

    @Test
    public void testPersistArtifact_PDF() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf"; //$NON-NLS-1$
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);

        Assert.assertNotNull(artifact);
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.Document());
        }
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((DocumentArtifactType) artifact).getContentSize());
        Assert.assertEquals("4ee67f4c9f12ebe58c0c6d55d20d9dab91d8ab39", ((DocumentArtifactType) artifact).getContentHash()); //$NON-NLS-1$
    }

    @Test
    public void testPersistDuplicateArtifact() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf"; //$NON-NLS-1$
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.setUuid("12345"); // amazing - that's the same UUID as my luggage! //$NON-NLS-1$
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);
        Assert.assertNotNull(artifact);

        // Now try to persist another artifact of the same type with the same UUID.
        pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        document = new Document();
        document.setName(artifactFileName + "-2"); //$NON-NLS-1$
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.setUuid("12345"); // amazing - that's the same UUID as my luggage! //$NON-NLS-1$
        try {
            persistenceManager.persistArtifact(document, pdf);
            Assert.fail("Expected an ArtifactAlreadyExistsException."); //$NON-NLS-1$
        } catch (ArtifactAlreadyExistsException e) {
            // Expected this!
            Assert.assertEquals("Artifact with UUID 12345 already exists.", e.getMessage()); //$NON-NLS-1$
        }

        // Now try to persist another artifact with a *different* type but the same UUID.
        pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        ExtendedArtifactType extendedArtifact = new ExtendedArtifactType();
        extendedArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        extendedArtifact.setExtendedType("FooArtifactType"); //$NON-NLS-1$
        extendedArtifact.setName("MyExtendedArtifact"); //$NON-NLS-1$
        extendedArtifact.setUuid("12345"); //$NON-NLS-1$
        try {
            persistenceManager.persistArtifact(document, pdf);
            Assert.fail("Expected an ArtifactAlreadyExistsException."); //$NON-NLS-1$
        } catch (ArtifactAlreadyExistsException e) {
            // Expected this!
            Assert.assertEquals("Artifact with UUID 12345 already exists.", e.getMessage()); //$NON-NLS-1$
        }
    }

    @Test
    public void testPersistArtifactPO_XSD() throws Exception {
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        XsdDocument document = new XsdDocument();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XsdDocument());
        }

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
    }

    @Test
    public void testPersistArtifactPO_XML() throws Exception {
        String artifactFileName = "PO.xml"; //$NON-NLS-1$
        InputStream POXml = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$

        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXml);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xml to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XmlDocument());
        }
        Assert.assertEquals(XmlDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 825L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
    }

    @Test
    public void testPersistArtifact_ExtendedArtifactType() throws Exception {
        ExtendedArtifactType extendedArtifact = new ExtendedArtifactType();
        extendedArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        extendedArtifact.setExtendedType("FooArtifactType"); //$NON-NLS-1$
        extendedArtifact.setName("MyExtendedArtifact"); //$NON-NLS-1$
        extendedArtifact.setDescription("This is a simple description for testing."); //$NON-NLS-1$

        BaseArtifactType artifact = persistenceManager.persistArtifact(extendedArtifact, null);
        Assert.assertNotNull(artifact);
        log.info("persisted extended artifact to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$

        //print out the derived node
        if (log.isDebugEnabled()) {
            persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XmlDocument());
        }
        Assert.assertEquals(ExtendedArtifactType.class, artifact.getClass());

        String name = ((ExtendedArtifactType) artifact).getName();
        String description = ((ExtendedArtifactType) artifact).getDescription();

        Assert.assertEquals("MyExtendedArtifact", name); //$NON-NLS-1$
        Assert.assertEquals("This is a simple description for testing.", description); //$NON-NLS-1$
    }

    @Test
    public void testGetArtifact_XSD() throws Exception {
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings

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
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        artifact.setName("My PO"); //$NON-NLS-1$
        artifact.setDescription("A new description of the PO.xsd artifact."); //$NON-NLS-1$
        artifact.setVersion("2.0.13"); //$NON-NLS-1$
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify the meta-data was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertEquals("My PO", artifact.getName()); //$NON-NLS-1$
        Assert.assertEquals("A new description of the PO.xsd artifact.", artifact.getDescription()); //$NON-NLS-1$
        Assert.assertEquals("2.0.13", artifact.getVersion()); //$NON-NLS-1$
    }

    /**
     * Tests that we can update the content of an s-ramp artifact.
     * @throws Exception
     */
    @Test
    public void testUpdateContent() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact content
        InputStream otherXsd = this.getClass().getResourceAsStream("/sample-files/xsd/XMLSchema.xsd"); //$NON-NLS-1$
        persistenceManager.updateArtifactContent(artifact.getUuid(), ArtifactType.XsdDocument(), otherXsd);

        // Now verify the content was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 87677L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings
    }

    /**
     * Tests that we can manage s-ramp properties.
     * @throws Exception
     */
    @Test
    public void testUpdateProperties() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        long size = ((DocumentArtifactType) artifact).getContentSize();
        Assert.assertTrue(size >= 2376L); // Not doing an equals here due to the vagaries of Windows vs *nix line endings

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty()); //$NON-NLS-1$
        Property prop1 = new Property();
        prop1.setPropertyName("prop1"); //$NON-NLS-1$
        prop1.setPropertyValue("propval1"); //$NON-NLS-1$
		artifact.getProperty().add(prop1);
        Property prop2 = new Property();
        prop2.setPropertyName("prop2"); //$NON-NLS-1$
        prop2.setPropertyValue("propval2"); //$NON-NLS-1$
        artifact.getProperty().add(prop2);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the properties were stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2); //$NON-NLS-1$
        String p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue(); //$NON-NLS-1$
        String p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue(); //$NON-NLS-1$
        Set<String> ps = new HashSet<String>();
        ps.add(p1);
        ps.add(p2);
        Assert.assertTrue("Prop1 missing from properties.", ps.contains("prop1=propval1")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertTrue("Prop2 missing from properties.", ps.contains("prop2=propval2")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertFalse("Prop3 somehow existed!.", ps.contains("prop3=propval3")); //$NON-NLS-1$ //$NON-NLS-2$

        // Now remove one property, add another one, and change the value of one
        artifact.getProperty().clear();
        prop1 = new Property();
        prop1.setPropertyName("prop1"); //$NON-NLS-1$
        prop1.setPropertyValue("propval1-updated"); //$NON-NLS-1$
		artifact.getProperty().add(prop1);
        Property prop3 = new Property();
        prop3.setPropertyName("prop3"); //$NON-NLS-1$
        prop3.setPropertyValue("propval3"); //$NON-NLS-1$
        artifact.getProperty().add(prop3);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the properties were updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2); //$NON-NLS-1$
        p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue(); //$NON-NLS-1$
        p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue(); //$NON-NLS-1$
        ps.clear();
        ps.add(p1);
        ps.add(p2);
        Assert.assertFalse("Prop1 wasn't updated (old value detected).", ps.contains("prop1=propval1")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertTrue("Prop1 wasn't updated (new value not found).", ps.contains("prop1=propval1-updated")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertFalse("Prop2 existed unexpectedly.", ps.contains("prop2=propval2")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertTrue("Prop3 missing from properties.", ps.contains("prop3=propval3")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    /**
     * Tests that we can manage s-ramp properties on a /core/Document.
     * @throws Exception
     */
    @Test
    public void testUpdateProperties_Document() throws Exception {
    	// First, add an artifact to the repo
        String artifactFileName = "s-ramp-press-release.pdf"; //$NON-NLS-1$
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$

        Document document = new Document();
        document.setName(artifactFileName);
        document.setContentType("application/pdf"); //$NON-NLS-1$
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
		BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);
        Assert.assertNotNull(artifact);
        log.info("persisted PDF to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((DocumentArtifactType) artifact).getContentSize());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        Assert.assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty()); //$NON-NLS-1$
        Property prop1 = new Property();
        prop1.setPropertyName("prop1"); //$NON-NLS-1$
        prop1.setPropertyValue("propval1"); //$NON-NLS-1$
		artifact.getProperty().add(prop1);
        Property prop2 = new Property();
        prop2.setPropertyName("prop2"); //$NON-NLS-1$
        prop2.setPropertyValue("propval2"); //$NON-NLS-1$
        artifact.getProperty().add(prop2);
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());

        // Now verify that the properties were stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2); //$NON-NLS-1$
        String p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue(); //$NON-NLS-1$
        String p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue(); //$NON-NLS-1$
        Set<String> ps = new HashSet<String>();
        ps.add(p1);
        ps.add(p2);
        Assert.assertTrue("Prop1 missing from properties.", ps.contains("prop1=propval1")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertTrue("Prop2 missing from properties.", ps.contains("prop2=propval2")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertFalse("Prop3 somehow existed!.", ps.contains("prop3=propval3")); //$NON-NLS-1$ //$NON-NLS-2$

        // Now remove one property, add another one, and change the value of one
        artifact.getProperty().clear();
        prop1 = new Property();
        prop1.setPropertyName("prop1"); //$NON-NLS-1$
        prop1.setPropertyValue("propval1-updated"); //$NON-NLS-1$
		artifact.getProperty().add(prop1);
        Property prop3 = new Property();
        prop3.setPropertyName("prop3"); //$NON-NLS-1$
        prop3.setPropertyValue("propval3"); //$NON-NLS-1$
        artifact.getProperty().add(prop3);
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());

        // Now verify that the properties were updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());
        Assert.assertTrue("Expected 2 properties.", artifact.getProperty().size() == 2); //$NON-NLS-1$
        p1 = artifact.getProperty().get(0).getPropertyName() + "=" + artifact.getProperty().get(0).getPropertyValue(); //$NON-NLS-1$
        p2 = artifact.getProperty().get(1).getPropertyName() + "=" + artifact.getProperty().get(1).getPropertyValue(); //$NON-NLS-1$
        ps.clear();
        ps.add(p1);
        ps.add(p2);
        Assert.assertFalse("Prop1 wasn't updated (old value detected).", ps.contains("prop1=propval1")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertTrue("Prop1 wasn't updated (new value not found).", ps.contains("prop1=propval1-updated")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertFalse("Prop2 existed unexpectedly.", ps.contains("prop2=propval2")); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertTrue("Prop3 missing from properties.", ps.contains("prop3=propval3")); //$NON-NLS-1$ //$NON-NLS-2$
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
        String artifactFileName = "PO.xsd"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, contentStream);
        Assert.assertNotNull(artifact);
        uuid1 = artifact.getUuid();
        contentStream.close();

        // Now update the artifact's generic relationships
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertTrue("Expected 0 relationships.", artifact.getRelationship().isEmpty()); //$NON-NLS-1$
        SrampModelUtils.addGenericRelationship(artifact, "NoTargetRelationship", null); //$NON-NLS-1$
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the relationship was stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 1 relationship.", 1, artifact.getRelationship().size()); //$NON-NLS-1$
        Assert.assertEquals("NoTargetRelationship", artifact.getRelationship().get(0).getRelationshipType()); //$NON-NLS-1$
        Assert.assertEquals(Collections.EMPTY_LIST, artifact.getRelationship().get(0).getRelationshipTarget());

        // Add a second artifact.
        artifactFileName = "XMLSchema.xsd"; //$NON-NLS-1$
        contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName); //$NON-NLS-1$
        Document document2 = new Document();
        document2.setName(artifactFileName);
        document2.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
        BaseArtifactType artifact2 = persistenceManager.persistArtifact(document2, contentStream);
        Assert.assertNotNull(artifact2);
        uuid2 = artifact2.getUuid();

        // Add a second relationship, this time with a target.
        SrampModelUtils.addGenericRelationship(artifact, "TargetedRelationship", uuid2); //$NON-NLS-1$
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // Now verify that the targeted relationship was stored
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 2 relationships.", 2, artifact.getRelationship().size()); //$NON-NLS-1$
        Relationship relationship = SrampModelUtils.getGenericRelationship(artifact, "NoTargetRelationship"); //$NON-NLS-1$
        Assert.assertNotNull(relationship);
        Assert.assertEquals("NoTargetRelationship", relationship.getRelationshipType()); //$NON-NLS-1$
        Assert.assertEquals(Collections.EMPTY_LIST, relationship.getRelationshipTarget());
        relationship = SrampModelUtils.getGenericRelationship(artifact, "TargetedRelationship"); //$NON-NLS-1$
        Assert.assertNotNull(relationship);
        Assert.assertEquals("TargetedRelationship", relationship.getRelationshipType()); //$NON-NLS-1$
        Assert.assertEquals(1, relationship.getRelationshipTarget().size()); // has only one target
        Assert.assertEquals(uuid2, relationship.getRelationshipTarget().get(0).getValue());

        // Add a third artifact.
        artifactFileName = "PO.xml"; //$NON-NLS-1$
        contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        Document document3 = new Document();
        document3.setName(artifactFileName);
        document3.setArtifactType(BaseArtifactEnum.XML_DOCUMENT);
        BaseArtifactType artifact3 = persistenceManager.persistArtifact(document3,  contentStream);
        Assert.assertNotNull(artifact3);
        uuid3 = artifact3.getUuid();

        // Add a third relationship, again with a target.
        SrampModelUtils.addGenericRelationship(artifact, "TargetedRelationship", uuid3); //$NON-NLS-1$
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());

        // More verifications
        artifact = persistenceManager.getArtifact(uuid1, ArtifactType.XsdDocument());
        Assert.assertEquals("Expected 2 relationships.", 2, artifact.getRelationship().size()); //$NON-NLS-1$
        relationship = SrampModelUtils.getGenericRelationship(artifact, "NoTargetRelationship"); //$NON-NLS-1$
        Assert.assertNotNull(relationship);
        Assert.assertEquals("NoTargetRelationship", relationship.getRelationshipType()); //$NON-NLS-1$
        Assert.assertEquals(Collections.EMPTY_LIST, relationship.getRelationshipTarget());
        relationship = SrampModelUtils.getGenericRelationship(artifact, "TargetedRelationship"); //$NON-NLS-1$
        Assert.assertNotNull(relationship);
        Assert.assertEquals("TargetedRelationship", relationship.getRelationshipType()); //$NON-NLS-1$
        Assert.assertEquals(2, relationship.getRelationshipTarget().size());
        Set<String> expected = new HashSet<String>();
        Set<String> actual = new HashSet<String>();
        expected.add(uuid2);
        expected.add(uuid3);
        actual.add(relationship.getRelationshipTarget().get(0).getValue());
        actual.add(relationship.getRelationshipTarget().get(1).getValue());
        Assert.assertEquals(expected, actual);

        // Add a fourth (bogus) relationship
        SrampModelUtils.addGenericRelationship(artifact, "TargetedRelationship", "not-a-valid-uuid"); //$NON-NLS-1$ //$NON-NLS-2$
    	try {
			persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument());
			Assert.fail("Expected an update failure."); //$NON-NLS-1$
		} catch (Exception e) {
		    Assert.assertEquals(ArtifactNotFoundException.class, e.getClass());
			Assert.assertEquals("No artifact found with UUID: not-a-valid-uuid", e.getMessage()); //$NON-NLS-1$
		}
    }

    @Test
    public void testDeleteArtifact() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf"; //$NON-NLS-1$
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);

        // Add an artifact
        BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);
        Assert.assertNotNull(artifact);
        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((DocumentArtifactType) artifact).getContentSize());
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid()); //$NON-NLS-1$

        // Now delete that artifact
        ArtifactType at = ArtifactType.valueOf(artifact);
        persistenceManager.deleteArtifact(document.getUuid(), at);

        // Now make sure we can't load it back up
        BaseArtifactType deleted = persistenceManager.getArtifact(document.getUuid(), at);
        Assert.assertNull(deleted);

        SrampQuery query = queryManager.createQuery("/s-ramp[@uuid = ?]"); //$NON-NLS-1$
        query.setString(document.getUuid());
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertEquals(0, artifactSet.size());
        
        // Ensure we can re-create an artifact w/ the same UUID, then delete it again without same-name collisions
        // in the trash folder.
        pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        BaseArtifactType artifact2 = persistenceManager.persistArtifact(document, pdf);
        Assert.assertEquals(artifact.getUuid(), artifact2.getUuid());
        persistenceManager.deleteArtifact(document.getUuid(), at);
        deleted = persistenceManager.getArtifact(document.getUuid(), at);
        Assert.assertNull(deleted);
    }


}
