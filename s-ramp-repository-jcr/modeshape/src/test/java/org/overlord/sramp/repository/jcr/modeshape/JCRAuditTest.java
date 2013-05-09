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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType.Property;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.common.audit.AuditUtils;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.repository.jcr.modeshape.auth.MockSecurityContext;


/**
 * Tests all things auditing related.
 * @author eric.wittmann@redhat.com
 */
public class JCRAuditTest extends AbstractAuditingJCRPersistenceTest {

    @BeforeClass
    public static void enableAuditing() {
        System.setProperty(SrampConstants.SRAMP_CONFIG_AUDITING, "true");
    }

    @Test
    public void testCreatedBy() throws Exception {
        BaseArtifactType artifact = createArtifact();

        Assert.assertEquals(Document.class, artifact.getClass());
        Assert.assertEquals("junituser", artifact.getCreatedBy());
    }

    @Test
    public void testLastModifiedBy() throws Exception {
        BaseArtifactType artifact = createArtifact();

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
    public void testPropertyChange() throws Exception {
        BaseArtifactType artifact = createArtifact();
        // Create a second one.
        createArtifact();

        // Update the artifact's name, description, and add a custom property
        artifact.setName("S-RAMP Press Release");
        artifact.setDescription("Sample description.");
        SrampModelUtils.setCustomProperty(artifact, "foo", "bar");
        persistenceManager.updateArtifact(artifact, ArtifactType.Document());

        // Allow some time for the async auditor to complete
        Thread.sleep(500);

        // Now do some assertions.
        AuditEntrySet entries = auditManager.getArtifactAuditEntries(artifact.getUuid());
        Assert.assertNotNull(entries);
        Assert.assertEquals(2, entries.size());
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

        // Get the full audit entry for the final "artifact:add" entry
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
            if (name.equals("sramp:name")) {
                Assert.assertEquals("s-ramp-press-release.pdf", value);
            } else if (name.equals("sramp:contentSize")) {
                Assert.assertEquals("18873", value);
            } else if (name.equals("sramp:description")) {
                Assert.assertEquals("Sample description.", value);
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
            if (name.equals("sramp:name")) {
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
            if (name.equals("sramp-properties:foo")) {
                Assert.assertEquals("bar", value);
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
        // Allow some time for the async auditor to complete
        Thread.sleep(500);
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
        // Allow some time for the async auditor to complete
        Thread.sleep(500);

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
    private BaseArtifactType createArtifact() throws SrampException {
        String artifactFileName = "s-ramp-press-release.pdf";
        InputStream pdf = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        Document document = new Document();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.DOCUMENT);
        document.setDescription("Sample description.");

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, pdf);
        Assert.assertNotNull(artifact);
        log.info("persisted s-ramp-press-release.pdf to JCR, returned artifact uuid=" + artifact.getUuid());
        return artifact;
    }

    /**
     * @return a new artifact
     * @throws SrampException
     */
    private BaseArtifactType createXsdArtifact() throws SrampException {
        String artifactFileName = "PO.xsd";
        InputStream content = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
        XsdDocument document = new XsdDocument();
        document.setName(artifactFileName);
        document.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);

        BaseArtifactType artifact = persistenceManager.persistArtifact(document, content);
        Assert.assertNotNull(artifact);
        log.info("persisted PO.xsd to JCR, returned artifact uuid=" + artifact.getUuid());
        return artifact;
    }

}
