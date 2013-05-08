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
package org.overlord.sramp.server.atom.services;

import static org.overlord.sramp.common.test.resteasy.TestPortProvider.generateURL;

import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.commons.io.IOUtils;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType.Property;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.common.audit.AuditUtils;

/**
 * Unit test for the auditing rest api.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditResourceTest extends AbstractResourceTest {

	@Test
	public void testListAndGet() throws Exception {
	    Document pdf = addPdf();
        // Add another document
        addPdf();
		// Wait for the audit entries to be persisted.
		Thread.sleep(250);

		// List all the audit entries
        ClientRequest request = new ClientRequest(generateURL("/s-ramp/audit/artifact/" + pdf.getUuid()));
		Feed auditEntryFeed = request.get(Feed.class).getEntity();
		Assert.assertNotNull(auditEntryFeed);
		List<Entry> entries = auditEntryFeed.getEntries();
		Assert.assertEquals(1, entries.size());
		String auditEntryUuid = null;
		for (Entry entry : entries) {
		    auditEntryUuid = entry.getId().toString();
        }

		// GET the audit entry (the last one in the list - the artifact:add)
		request = new ClientRequest(generateURL("/s-ramp/audit/artifact/" + pdf.getUuid() + "/" + auditEntryUuid));
		Entry entry = request.get(Entry.class).getEntity();
		AuditEntry auditEntry = SrampAtomUtils.unwrap(entry, AuditEntry.class);
		Assert.assertNotNull(auditEntry);
        Assert.assertEquals("junituser", auditEntry.getWho());
        Assert.assertEquals(AuditEntryTypes.ARTIFACT_ADD, auditEntry.getType());
        List<AuditItemType> auditItems = auditEntry.getAuditItem();
        Assert.assertEquals(1, auditItems.size());
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
                Assert.assertEquals("sample.pdf", value);
            } else if (name.equals("sramp:contentSize")) {
                Assert.assertEquals("218882", value);
            } else if (name.equals("sramp:contentType")) {
                Assert.assertEquals("application/pdf", value);
            } else {
                Assert.fail("No assertion for audited property: " + name);
            }
        }

        // List all the audit entries by user
        request = new ClientRequest(generateURL("/s-ramp/audit/user/junituser"));
        auditEntryFeed = request.get(Feed.class).getEntity();
        Assert.assertNotNull(auditEntryFeed);
        entries = auditEntryFeed.getEntries();
        Assert.assertEquals(2, entries.size());
	}

    @Test
    public void testCreate() throws Exception {
        Document pdf = addPdf();
        // Wait for the audit entries to be persisted.
        Thread.sleep(250);

        DatatypeFactory dtFactory = DatatypeFactory.newInstance();

        // Create another audit entry
        ClientRequest request = new ClientRequest(generateURL("/s-ramp/audit/artifact/" + pdf.getUuid()));
        XMLGregorianCalendar now = dtFactory.newXMLGregorianCalendar((GregorianCalendar)Calendar.getInstance());
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setType("junit:test1");
        auditEntry.setWhen(now);
        auditEntry.setWho("junituser");
        AuditItemType item = AuditUtils.getOrCreateAuditItem(auditEntry, "junit:item");
        AuditUtils.setAuditItemProperty(item, "foo", "bar");
        AuditUtils.setAuditItemProperty(item, "hello", "world");

        request.body(MediaType.APPLICATION_AUDIT_ENTRY_XML_TYPE, auditEntry);
        ClientResponse<Entry> response = request.post(Entry.class);
        Entry entry = response.getEntity();
        AuditEntry re = SrampAtomUtils.unwrap(entry, AuditEntry.class);
        Assert.assertNotNull(re);
        Assert.assertNotNull(re.getUuid());
        Assert.assertEquals("junituser", re.getWho());
        Assert.assertEquals(1, re.getAuditItem().size());
        Assert.assertEquals("junit:item", re.getAuditItem().iterator().next().getType());
        Assert.assertEquals(2, re.getAuditItem().iterator().next().getProperty().size());

        // List all the audit entries
        request = new ClientRequest(generateURL("/s-ramp/audit/artifact/" + pdf.getUuid()));
        Feed auditEntryFeed = request.get(Feed.class).getEntity();
        Assert.assertNotNull(auditEntryFeed);
        List<Entry> entries = auditEntryFeed.getEntries();
        Assert.assertEquals(2, entries.size());

        // Get just the custom entry we created
        request = new ClientRequest(generateURL("/s-ramp/audit/artifact/" + pdf.getUuid() + "/" + re.getUuid()));
        response = request.get(Entry.class);
        entry = response.getEntity();
        re = SrampAtomUtils.unwrap(entry, AuditEntry.class);
        Assert.assertNotNull(re);
        Assert.assertNotNull(re.getUuid());
        Assert.assertEquals("junituser", re.getWho());
        Assert.assertEquals(1, re.getAuditItem().size());
        Assert.assertEquals("junit:item", re.getAuditItem().iterator().next().getType());
        Assert.assertEquals(2, re.getAuditItem().iterator().next().getProperty().size());
    }

    /**
     * Adds a PDF document to the repository.
     * @throws Exception
     */
    private Document addPdf() throws Exception {
        // Add the PDF to the repository
        String artifactFileName = "sample.pdf";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        //String uuid = null;
        try {
            ClientRequest request = new ClientRequest(generateURL("/s-ramp/core/Document"));
            request.header("Slug", artifactFileName);
            request.body("application/pdf", contentStream);

            ClientResponse<Entry> response = request.post(Entry.class);

            Entry entry = response.getEntity();
            Assert.assertEquals(artifactFileName, entry.getTitle());
            BaseArtifactType arty = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(arty instanceof Document);
            return (Document) arty;
        } finally {
            IOUtils.closeQuietly(contentStream);
        }
    }

}
