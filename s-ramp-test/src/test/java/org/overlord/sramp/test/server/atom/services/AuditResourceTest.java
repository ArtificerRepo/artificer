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
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.common.audit.AuditUtils;

/**
 * Unit test for the auditing rest api.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditResourceTest extends AbstractAuditingResourceTest {

    @Test
	public void testListAndGet() throws Exception {
	    ClientRequest request = clientRequest("/s-ramp/audit/user/" + getUsername()); //$NON-NLS-1$
        ClientResponse<Feed> feedResponse = request.get(Feed.class);
        Feed auditEntryFeed = feedResponse.getEntity();
        Object totalResultsAttr = auditEntryFeed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int currentTotal = Integer.parseInt(String.valueOf(totalResultsAttr));
	    
	    Document pdf = addPdf();
        // Add another document
        addPdf();

		// List all the audit entries
        request = clientRequest("/s-ramp/audit/artifact/" + pdf.getUuid()); //$NON-NLS-1$
		auditEntryFeed = request.get(Feed.class).getEntity();
		Assert.assertNotNull(auditEntryFeed);
		List<Entry> entries = auditEntryFeed.getEntries();
		Assert.assertEquals(1, entries.size());
		String auditEntryUuid = null;
		for (Entry entry : entries) {
		    auditEntryUuid = entry.getId().toString();
        }

		// GET the audit entry (the last one in the list - the artifact:add)
		request = clientRequest("/s-ramp/audit/artifact/" + pdf.getUuid() + "/" + auditEntryUuid); //$NON-NLS-1$ //$NON-NLS-2$
		Entry entry = request.get(Entry.class).getEntity();
		AuditEntry auditEntry = SrampAtomUtils.unwrap(entry, AuditEntry.class);
		Assert.assertNotNull(auditEntry);
        Assert.assertEquals(getUsername(), auditEntry.getWho()); //$NON-NLS-1$
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
            if (name.equals("name")) { //$NON-NLS-1$
                Assert.assertEquals("sample.pdf", value); //$NON-NLS-1$
            } else {
                Assert.fail("No assertion for audited property: " + name); //$NON-NLS-1$
            }
        }

        // List all the audit entries by user
        request = clientRequest("/s-ramp/audit/user/" + getUsername()); //$NON-NLS-1$
        auditEntryFeed = request.get(Feed.class).getEntity();
        Assert.assertNotNull(auditEntryFeed);
        totalResultsAttr = auditEntryFeed.getExtensionAttributes().get(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME);
        int total = Integer.parseInt(String.valueOf(totalResultsAttr));
        Assert.assertEquals(2, total - currentTotal);
	}

    @Test
    public void testCreate() throws Exception {
        Document pdf = addPdf();

        DatatypeFactory dtFactory = DatatypeFactory.newInstance();

        // Create another audit entry
        ClientRequest request = clientRequest("/s-ramp/audit/artifact/" + pdf.getUuid()); //$NON-NLS-1$
        XMLGregorianCalendar now = dtFactory.newXMLGregorianCalendar((GregorianCalendar)Calendar.getInstance());
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setType("junit:test1"); //$NON-NLS-1$
        auditEntry.setWhen(now);
        auditEntry.setWho(getUsername()); //$NON-NLS-1$
        AuditItemType item = AuditUtils.getOrCreateAuditItem(auditEntry, "junit:item"); //$NON-NLS-1$
        AuditUtils.setAuditItemProperty(item, "foo", "bar"); //$NON-NLS-1$ //$NON-NLS-2$
        AuditUtils.setAuditItemProperty(item, "hello", "world"); //$NON-NLS-1$ //$NON-NLS-2$

        request.body(MediaType.APPLICATION_AUDIT_ENTRY_XML_TYPE, auditEntry);
        ClientResponse<Entry> response = request.post(Entry.class);
        Entry entry = response.getEntity();
        AuditEntry re = SrampAtomUtils.unwrap(entry, AuditEntry.class);
        Assert.assertNotNull(re);
        Assert.assertNotNull(re.getUuid());
        Assert.assertEquals(getUsername(), re.getWho()); //$NON-NLS-1$
        Assert.assertEquals(1, re.getAuditItem().size());
        Assert.assertEquals("junit:item", re.getAuditItem().iterator().next().getType()); //$NON-NLS-1$
        Assert.assertEquals(2, re.getAuditItem().iterator().next().getProperty().size());

        // List all the audit entries
        request = clientRequest("/s-ramp/audit/artifact/" + pdf.getUuid()); //$NON-NLS-1$
        Feed auditEntryFeed = request.get(Feed.class).getEntity();
        Assert.assertNotNull(auditEntryFeed);
        List<Entry> entries = auditEntryFeed.getEntries();
        Assert.assertEquals(2, entries.size());

        // Get just the custom entry we created
        request = clientRequest("/s-ramp/audit/artifact/" + pdf.getUuid() + "/" + re.getUuid()); //$NON-NLS-1$ //$NON-NLS-2$
        response = request.get(Entry.class);
        entry = response.getEntity();
        re = SrampAtomUtils.unwrap(entry, AuditEntry.class);
        Assert.assertNotNull(re);
        Assert.assertNotNull(re.getUuid());
        Assert.assertEquals(getUsername(), re.getWho()); //$NON-NLS-1$
        Assert.assertEquals(1, re.getAuditItem().size());
        Assert.assertEquals("junit:item", re.getAuditItem().iterator().next().getType()); //$NON-NLS-1$
        Assert.assertEquals(2, re.getAuditItem().iterator().next().getProperty().size());
    }

    /**
     * Adds a PDF document to the repository.
     * @throws Exception
     */
    private Document addPdf() throws Exception {
        // Add the PDF to the repository
        String artifactFileName = "sample.pdf"; //$NON-NLS-1$
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        //String uuid = null;
        try {
            ClientRequest request = clientRequest("/s-ramp/core/Document"); //$NON-NLS-1$
            request.header("Slug", artifactFileName); //$NON-NLS-1$
            request.body("application/pdf", contentStream); //$NON-NLS-1$

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
