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

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType.Property;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.*;
import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.common.audit.AuditUtils;
import org.overlord.sramp.common.ontology.SrampOntology;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.repository.jcr.modeshape.auth.MockSecurityContext;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.InputStream;
import java.util.*;


/**
 * Tests all things auditing related.
 * @author eric.wittmann@redhat.com
 */
@FixMethodOrder(MethodSorters.DEFAULT)
public class JCRAuditTest extends AbstractAuditingJCRPersistenceTest {

    @BeforeClass
    public static void enableAuditing() {
        System.setProperty(SrampConstants.SRAMP_CONFIG_AUDITING, "true");
    }

    @Test
    public void testCreatedBy() throws Exception {
        BaseArtifactType artifact = createArtifact(null);

        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals("junituser", artifact.getCreatedBy());
    }

    @Test
    public void testLastModifiedBy() throws Exception {
        BaseArtifactType artifact = createArtifact(null);

        // Now update the artifact as a different user.
        MockSecurityContext.currentUser = "junituser2";
        artifact.setDescription("New description of the artifact.");
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());
        MockSecurityContext.currentUser = "junituser";

        artifact = persistenceManager.getArtifact(artifact.getUuid(), ArtifactType.Document());

        Assert.assertEquals("junituser", artifact.getCreatedBy());
        Assert.assertEquals("junituser2", artifact.getLastModifiedBy());
    }

    @Test
    public void testCreateAuditEntry() throws Exception {
        BaseArtifactType artifact = createArtifact(null);

        AuditEntrySet auditEntries = auditManager.getArtifactAuditEntries(artifact.getUuid());
        Assert.assertNotNull(auditEntries);
        Assert.assertEquals(1, auditEntries.size());
        AuditEntry entry = auditEntries.iterator().next();
        Assert.assertNotNull(entry);
        Assert.assertEquals(AuditEntryTypes.ARTIFACT_ADD.toString(), entry.getType());
        Assert.assertEquals("junituser", entry.getWho());

        List<AuditItemType> items = entry.getAuditItem();
        Assert.assertNotNull(entry);
        Assert.assertFalse(items.isEmpty());
        Assert.assertEquals(1, items.size());
        AuditItemType item = items.get(0);
        Assert.assertEquals(AuditItemTypes.PROPERTY_ADDED, item.getType());
        List<Property> properties = item.getProperty();
        Assert.assertNotNull(properties);
        Assert.assertFalse(properties.isEmpty());
        Assert.assertEquals(2, properties.size());
    }

    @Test
    public void testCreateAuditEntryWithCustomProperties() throws Exception {
        BaseArtifactType artifact = createArtifact(null, "hello", "world", "foo", "bar");

        AuditEntrySet auditEntries = auditManager.getArtifactAuditEntries(artifact.getUuid());
        Assert.assertNotNull(auditEntries);
        Assert.assertEquals(1, auditEntries.size());
        AuditEntry entry = auditEntries.iterator().next();
        Assert.assertNotNull(entry);
        Assert.assertEquals(AuditEntryTypes.ARTIFACT_ADD.toString(), entry.getType());
        Assert.assertEquals("junituser", entry.getWho());

        List<AuditItemType> items = entry.getAuditItem();
        Assert.assertNotNull(entry);
        Assert.assertFalse(items.isEmpty());
        Assert.assertEquals(1, items.size());
        AuditItemType item = getAuditItem(items, AuditItemTypes.PROPERTY_ADDED);
        Assert.assertEquals(AuditItemTypes.PROPERTY_ADDED, item.getType());
        List<Property> properties = item.getProperty();
        Assert.assertNotNull(properties);
        Assert.assertFalse(properties.isEmpty());
        Assert.assertEquals(4, properties.size());
    }

    @Test
    public void testCreateAuditEntryWithClassifiers() throws Exception {
        createOntology();
        BaseArtifactType artifact = createArtifact(Collections.singleton("urn:example.org/world#China"));

        AuditEntrySet auditEntries = auditManager.getArtifactAuditEntries(artifact.getUuid());
        Assert.assertNotNull(auditEntries);
        Assert.assertEquals(1, auditEntries.size());
        AuditEntry entry = auditEntries.iterator().next();
        Assert.assertNotNull(entry);
        Assert.assertEquals(AuditEntryTypes.ARTIFACT_ADD.toString(), entry.getType());
        Assert.assertEquals("junituser", entry.getWho());

        List<AuditItemType> items = entry.getAuditItem();
        Assert.assertNotNull(entry);
        Assert.assertFalse(items.isEmpty());
        Assert.assertEquals(2, items.size());

        AuditItemType item = getAuditItem(items, AuditItemTypes.CLASSIFIERS_ADDED);
        Assert.assertEquals(AuditItemTypes.CLASSIFIERS_ADDED, item.getType());
        List<Property> properties = item.getProperty();
        Assert.assertNotNull(properties);
        Assert.assertFalse(properties.isEmpty());
        Assert.assertEquals(1, properties.size());
    }

    @Test
    public void testUpdateAuditEntry() throws Exception {
        BaseArtifactType artifact = createArtifact(null, "hello", "world");

        createArtifact(null);

        // Update the artifact's name, description, and add a custom property
        artifact.setName("S-RAMP Press Release");
        artifact.setDescription("Sample description.");
        SrampModelUtils.setCustomProperty(artifact, "foo", "bar");
        SrampModelUtils.unsetCustomProperty(artifact, "hello");
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());

        int expectedEntries = 2;

        // Now do some assertions.
        AuditEntrySet entries = auditManager.getArtifactAuditEntries(artifact.getUuid());
        Assert.assertNotNull(entries);
        Assert.assertEquals(expectedEntries, entries.size());
        AuditEntry entry = entries.iterator().next();
        Assert.assertNotNull(entry.getUuid());
        Assert.assertEquals("artifact:update", entry.getType());
        Assert.assertNotNull(entry.getWhen());
        Assert.assertEquals("junituser", entry.getWho());
        String updateEntryUuid = entry.getUuid();
        entry = entries.iterator().next();
        Assert.assertNotNull(entry.getUuid());
        Assert.assertEquals("artifact:add", entry.getType());
        Assert.assertNotNull(entry.getWhen());
        Assert.assertEquals("junituser", entry.getWho());

        // Get the full audit entry for the "artifact:add" event
        AuditEntry auditEntry = auditManager.getArtifactAuditEntry(artifact.getUuid(), entry.getUuid());
        Assert.assertEquals("artifact:add", auditEntry.getType());
        Assert.assertNotNull(auditEntry.getWhen());
        Assert.assertEquals("junituser", auditEntry.getWho());
        List<AuditItemType> auditItems = auditEntry.getAuditItem();
        Assert.assertNotNull(auditItems);
        AuditItemType auditItem = AuditUtils.getAuditItem(auditEntry, AuditItemTypes.PROPERTY_ADDED);
        Assert.assertNotNull(auditItem);
        Assert.assertEquals(AuditItemTypes.PROPERTY_ADDED, auditItem.getType());
        List<Property> properties = auditItem.getProperty();
        for (Property property : properties) {
            Assert.assertNotNull(property);
            String name = property.getName();
            String value = property.getValue();
            if (name.equals("name")) {
                Assert.assertEquals("s-ramp-press-release.pdf", value);
            } else if (name.equals("description")) {
                Assert.assertEquals("Sample description.", value);
            } else if (name.equals("hello")) {
                Assert.assertEquals("world", value);
            } else {
                Assert.fail("No assertion for audited property: " + name);
            }
        }
        Assert.assertEquals(3, properties.size());

        // Get the full audit entry for the "artifact:update" entry
        auditEntry = auditManager.getArtifactAuditEntry(artifact.getUuid(), updateEntryUuid);
        Assert.assertEquals("artifact:update", auditEntry.getType());
        Assert.assertNotNull(auditEntry.getWhen());
        Assert.assertEquals("junituser", auditEntry.getWho());
        auditItems = auditEntry.getAuditItem();
        Assert.assertNotNull(auditItems);
        Assert.assertEquals(3, auditItems.size());
        AuditItemType propAddedItem = AuditUtils.getAuditItem(auditEntry, AuditItemTypes.PROPERTY_ADDED);
        AuditItemType propChangedItem = AuditUtils.getAuditItem(auditEntry, AuditItemTypes.PROPERTY_CHANGED);
        AuditItemType propRemovedItem = AuditUtils.getAuditItem(auditEntry, AuditItemTypes.PROPERTY_REMOVED);
        Assert.assertNotNull(propAddedItem);
        Assert.assertNotNull(propChangedItem);
        Assert.assertNotNull(propRemovedItem);
        // Assertions on property-changed.
        Assert.assertEquals(AuditItemTypes.PROPERTY_CHANGED, propChangedItem.getType());
        properties = propChangedItem.getProperty();
        for (Property property : properties) {
            Assert.assertNotNull(property);
            String name = property.getName();
            String value = property.getValue();
            if (name.equals("name")) {
                Assert.assertEquals("S-RAMP Press Release", value);
            } else {
                Assert.fail("No assertion for audited property: " + name);
            }
        }
        Assert.assertEquals(1, properties.size());
        // Assertions on property-added.
        Assert.assertEquals(AuditItemTypes.PROPERTY_ADDED, propAddedItem.getType());
        properties = propAddedItem.getProperty();
        for (Property property : properties) {
            Assert.assertNotNull(property);
            String name = property.getName();
            String value = property.getValue();
            if (name.equals("foo")) {
                Assert.assertEquals("bar", value);
            } else {
                Assert.fail("No assertion for audited property: " + name);
            }
        }
        Assert.assertEquals(1, properties.size());
        // Assertions on property-removed.
        Assert.assertEquals(AuditItemTypes.PROPERTY_REMOVED, propRemovedItem.getType());
        properties = propRemovedItem.getProperty();
        for (Property property : properties) {
            Assert.assertNotNull(property);
            String name = property.getName();
            if (name.equals("hello")) {
            } else {
                Assert.fail("No assertion for audited property: " + name);
            }
        }
        Assert.assertEquals(1, properties.size());

        // Get all audit entries for the user.
        entries = auditManager.getUserAuditEntries("junituser");
        Assert.assertNotNull(entries);
        Assert.assertEquals(3, entries.size());

        persistenceManager.printArtifactGraph(artifact.getUuid(), ArtifactType.Document());
    }

    @Test
    public void testWithDerivedArtifacts() throws Exception {
        BaseArtifactType artifact = createXsdArtifact();

        AuditEntrySet entries = auditManager.getArtifactAuditEntries(artifact.getUuid());
        Assert.assertNotNull(entries);
        Assert.assertEquals(1, entries.size());

        // Get all audit entries for the user.  There should only be 7 because the
        // source document has 6 derived artifacts.
        entries = auditManager.getUserAuditEntries("junituser");
        Assert.assertNotNull(entries);
        Assert.assertEquals(7, entries.size());
    }

    @Test
    public void testCustomAuditEntry() throws Exception {
        DatatypeFactory dtFactory = DatatypeFactory.newInstance();

        BaseArtifactType artifact = createXsdArtifact();

        // Create another audit entry
        XMLGregorianCalendar now = dtFactory.newXMLGregorianCalendar((GregorianCalendar)Calendar.getInstance());
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setType("junit:test1");
        auditEntry.setWhen(now);
        auditEntry.setWho("junituser");
        AuditItemType item = AuditUtils.getOrCreateAuditItem(auditEntry, "junit:item");
        AuditUtils.setAuditItemProperty(item, "foo", "bar");
        AuditUtils.setAuditItemProperty(item, "hello", "world");
        String auditEntryUuid = auditManager.addAuditEntry(artifact.getUuid(), auditEntry).getUuid();

        // Now fetch it back and assert
        AuditEntry re = auditManager.getArtifactAuditEntry(artifact.getUuid(), auditEntryUuid);
        Assert.assertNotNull(re);
        Assert.assertNotNull(re.getUuid());
        Assert.assertEquals("junituser", re.getWho());
        Assert.assertEquals(1, re.getAuditItem().size());
        Assert.assertEquals("junit:item", re.getAuditItem().iterator().next().getType());
        Assert.assertEquals(2, re.getAuditItem().iterator().next().getProperty().size());
    }

    /**
     * @return a new artifact
     * @throws SrampException
     */
    private BaseArtifactType createArtifact(Set<String> classifiers, String ... args) throws Exception {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.setDescription("Sample description.");

        if (classifiers != null) {
            for (String classifier : classifiers) {
                document.getClassifiedBy().add(classifier);
            }
        }

        if (args != null && args.length > 0) {
            for (int i = 0; i < args.length; i+=2) {
                String propName = args[i];
                String propVal = args[i+1];
                SrampModelUtils.setCustomProperty(document, propName, propVal);
            }
        }

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, pdf));
        Assert.assertNotNull(artifact);
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid());
        return artifact;
    }

    /**
     * @return a new artifact
     * @throws SrampException
     */
    private BaseArtifactType createXsdArtifact() throws Exception {
        String artifactFileName = "PO.xsd";
        InputStream content = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        XsdDocument document = new XsdDocument();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, new ArtifactContent(artifactFileName, content));
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        return artifact;
    }

    private String createOntology() throws SrampException {
        SrampOntology ontology = new SrampOntology();
        ontology.setBase("urn:example.org/world");
        ontology.setLabel("World Ontology");
        ontology.setComment("This is my test ontology.");

        SrampOntology.SrampOntologyClass world = createClass(ontology, null, "World", "World", "The entire world");
        SrampOntology.SrampOntologyClass asia = createClass(ontology, world, "Asia", "Asia", null);
        SrampOntology.SrampOntologyClass europe = createClass(ontology, world, "Europe", "Europe", "Two world wars");
        SrampOntology.SrampOntologyClass japan = createClass(ontology, asia, "Japan", "Japan", "Samurai *and* ninja?  Not fair.");
        SrampOntology.SrampOntologyClass china = createClass(ontology, asia, "China", "China", "Gunpowder!");
        SrampOntology.SrampOntologyClass uk = createClass(ontology, europe, "UnitedKingdom", "United Kingdom", "The food could be better");
        SrampOntology.SrampOntologyClass germany = createClass(ontology, europe, "Germany", "Germany", "The fatherland");

        ontology.getRootClasses().add(world);

        world.getChildren().add(asia);
        world.getChildren().add(europe);
        asia.getChildren().add(japan);
        asia.getChildren().add(china);
        europe.getChildren().add(uk);
        europe.getChildren().add(germany);

        String uuid = persistenceManager.persistOntology(ontology).getUuid();
        return uuid;
    }

    /**
     * Creates a test class.
     * @param ontology
     * @param parent
     * @param id
     * @param label
     * @param comment
     */
    private SrampOntology.SrampOntologyClass createClass(SrampOntology ontology, SrampOntology.SrampOntologyClass parent, String id, String label, String comment) {
        SrampOntology.SrampOntologyClass rval = ontology.createClass(id);
        rval.setParent(parent);
        rval.setComment(comment);
        rval.setLabel(label);
        return rval;
    }

    /**
     * @param items
     * @param type
     */
    private AuditItemType getAuditItem(List<AuditItemType> items, String type) {
        if (items == null)
            return null;
        for (AuditItemType auditItemType : items) {
            if (auditItemType.getType().equals(type.toString())) {
                return auditItemType;
            }
        }
        return null;
    }

}
