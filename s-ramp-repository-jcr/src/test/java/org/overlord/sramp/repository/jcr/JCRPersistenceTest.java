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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Document;
import org.s_ramp.xmlns._2010.s_ramp.Property;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import test.org.overlord.sramp.repository.jcr.JCRRepositoryCleaner;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 */
public class JCRPersistenceTest {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static PersistenceManager persistenceManager = null;

    @BeforeClass
    public static void setup() {
        persistenceManager = PersistenceFactory.newInstance();
    }

    @Before
    public void prepForTest() {
        new JCRRepositoryCleaner().clean();
    }

    @Test
    public void testSave_PDF() throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);

        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.Document, pdf);

        Assert.assertNotNull(artifact);
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.Document);

        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals(new Long(18873l), ((Document) artifact).getContentSize());
    }

    @Test
    public void testSavePO_XSD() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);

        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        //persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XsdDocument);

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());
    }

    @Test
    public void testSavePO_XML() throws Exception {
        String artifactFileName = "PO.xml";
        InputStream POXml = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);

        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XmlDocument, POXml);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xml to JCR, returned artifact uuid=" + artifact.getUuid());

        //print out the derived node
        persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.XmlDocument);

        Assert.assertEquals(XmlDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(825l), ((XmlDocument) artifact).getContentSize());
    }

    @Test
    public void testGetArtifact_XSD() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);

        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);

        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());

        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());

        BaseArtifactType artifact2 = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
        Assert.assertEquals(artifact.getUuid(), artifact2.getUuid());
        Assert.assertEquals(artifact.getCreatedBy(), artifact2.getCreatedBy());
        Assert.assertEquals(artifact.getDescription(), artifact2.getDescription());
        Assert.assertEquals(artifact.getLastModifiedBy(), artifact2.getLastModifiedBy());
        Assert.assertEquals(artifact.getName(), artifact2.getName());
        Assert.assertEquals(artifact.getVersion(), artifact2.getVersion());
        Assert.assertEquals(artifact.getLastModifiedTimestamp(), artifact2.getLastModifiedTimestamp());
    }

    /**
     * Tests the getArtifacts method on the persistence manager.
     * @throws Exception
     */
    @Test
    public void testGetArtifacts() throws Exception {
    	// First, add some artifacts.
        String artifactFileName = "PO.xsd";
        InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName + "-1", ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact);
        String uuid1 = artifact.getUuid();
		log.info("persisted PO.xsd to JCR, returned artifact uuid=" + uuid1);

        POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        artifact = persistenceManager.persistArtifact(artifactFileName + "-2", ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact);
        String uuid2 = artifact.getUuid();
		log.info("persisted PO.xsd (again) to JCR, returned artifact uuid=" + uuid2);

		List<BaseArtifactType> artifacts = persistenceManager.getArtifacts(ArtifactType.XsdDocument);
		Assert.assertNotNull(artifacts);
		Assert.assertTrue("Wrong number of artifacts returned (should have at least 2).", artifacts.size() >= 2);
		boolean foundUuid1 = false;
		boolean foundUuid2 = false;
		for (BaseArtifactType arty : artifacts) {
			if (arty.getUuid().equals(uuid1))
				foundUuid1 = true;
			if (arty.getUuid().equals(uuid2))
				foundUuid2 = true;
		}
		Assert.assertTrue("Failed to find UUID1.", foundUuid1);
		Assert.assertTrue("Failed to find UUID2.", foundUuid2);
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
        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
        artifact.setName("My PO");
        artifact.setDescription("A new description of the PO.xsd artifact.");
        artifact.setVersion("2.0.13");
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument);

        // Now verify the meta-data was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
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
        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());
        Assert.assertEquals(artifactFileName, artifact.getName());

        // Now update the artifact content
        InputStream otherXsd = this.getClass().getResourceAsStream("/sample-files/xsd/XMLSchema.xsd");
        persistenceManager.updateArtifactContent(artifact.getUuid(), ArtifactType.XsdDocument, otherXsd);

        // Now verify the content was updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
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
        BaseArtifactType artifact = persistenceManager.persistArtifact(artifactFileName, ArtifactType.XsdDocument, POXsd);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        Assert.assertEquals(XsdDocument.class, artifact.getClass());
        Assert.assertEquals(new Long(2376l), ((XsdDocument) artifact).getContentSize());

        // Now update the artifact
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
        Assert.assertTrue("Expected 0 properties.", artifact.getProperty().isEmpty());
        Property prop1 = new Property();
        prop1.setPropertyName("prop1");
        prop1.setPropertyValue("propval1");
		artifact.getProperty().add(prop1);
        Property prop2 = new Property();
        prop2.setPropertyName("prop2");
        prop2.setPropertyValue("propval2");
        artifact.getProperty().add(prop2);
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument);

        // Now verify that the properties were stored
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
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
        persistenceManager.updateArtifact(artifact, ArtifactType.XsdDocument);

        // Now verify that the properties were updated
        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.XsdDocument);
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

}
