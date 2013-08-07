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
package org.overlord.sramp.client;

import static org.overlord.sramp.common.test.resteasy.TestPortProvider.generateURL;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.audit.AuditEntrySummary;
import org.overlord.sramp.client.audit.AuditResultSet;

/**
 * Unit test for the
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditingClientTest extends AbstractAuditingClientTest {

    @Test
    public void testAuditing() throws Exception {
        DatatypeFactory dtFactory = DatatypeFactory.newInstance();
        BaseArtifactType doc = addXmlDoc();
        // Add a second artifact.
        addXmlDoc();
        String artifactUuid = doc.getUuid();
        // Wait for a bit to let the async audit events persist
        Thread.sleep(1000);

        // Checking auditing - should be 1 event (artifact:add)
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp")); //$NON-NLS-1$
        AuditResultSet resultSet = client.getAuditTrailForArtifact(artifactUuid);
        Assert.assertNotNull(resultSet);
        Assert.assertEquals(1, resultSet.getTotalResults());
        AuditEntrySummary summary = resultSet.get(0);
        Assert.assertNotNull(summary);
        Assert.assertEquals("junituser", summary.getWho()); //$NON-NLS-1$
        Assert.assertEquals("artifact:add", summary.getType()); //$NON-NLS-1$

        // Add a custom entry
        AuditEntry auditEntry = new AuditEntry();
        XMLGregorianCalendar now = dtFactory.newXMLGregorianCalendar((GregorianCalendar)Calendar.getInstance());
        auditEntry.setType("junit:test1"); //$NON-NLS-1$
        auditEntry.setWhen(now);
        auditEntry.setWho("junituser"); //$NON-NLS-1$
        AuditEntry newEntry = client.addAuditEntry(artifactUuid, auditEntry );
        Assert.assertNotNull(newEntry);
        Assert.assertEquals("junituser", newEntry.getWho()); //$NON-NLS-1$
        Assert.assertEquals("junit:test1", newEntry.getType()); //$NON-NLS-1$

        // Get the audit trail again
        resultSet = client.getAuditTrailForArtifact(artifactUuid);
        Assert.assertNotNull(resultSet);
        Assert.assertEquals(2, resultSet.getTotalResults());
        summary = resultSet.get(0);
        Assert.assertNotNull(summary);
        Assert.assertEquals("junituser", summary.getWho()); //$NON-NLS-1$
        Assert.assertEquals("junit:test1", summary.getType()); //$NON-NLS-1$
        summary = resultSet.get(1);
        Assert.assertNotNull(summary);
        Assert.assertEquals("junituser", summary.getWho()); //$NON-NLS-1$
        Assert.assertEquals("artifact:add", summary.getType()); //$NON-NLS-1$

        // Get the full audit entry
        auditEntry = client.getAuditEntry(artifactUuid, summary.getUuid());
        Assert.assertNotNull(newEntry);
        Assert.assertEquals("junituser", auditEntry.getWho()); //$NON-NLS-1$
        Assert.assertEquals("artifact:add", auditEntry.getType()); //$NON-NLS-1$

        // Get all audit entries by user
        resultSet = client.getAuditTrailForUser("junituser"); //$NON-NLS-1$
        Assert.assertNotNull(resultSet);
        Assert.assertEquals(3, resultSet.getTotalResults());
    }
}
