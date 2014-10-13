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
package org.overlord.sramp.server.atom.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.repository.AuditManager;
import org.overlord.sramp.repository.AuditManagerFactory;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.server.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JAX-RS resource that handles endpoints related to auditing.  This includes:
 *
 * <ul>
 *   <li>Add an audit entry for an artifact</li>
 *   <li>Get all audit entries for an artifact</li>
 *   <li>Get all audit entries for a user</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp")
public class AuditResource extends AbstractResource {

    private static Logger logger = LoggerFactory.getLogger(AuditResource.class);
//    private final Sramp sramp = new Sramp();

	/**
	 * Constructor.
	 */
	public AuditResource() {
	}

    /**
     * S-RAMP atom POST to add an audit entry to the audit trail of an artifact.
     */
    @POST
    @Path("audit/artifact/{artifactUuid}")
    @Consumes(MediaType.APPLICATION_AUDIT_ENTRY_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(@PathParam("artifactUuid") String artifactUuid, AuditEntry auditEntry) throws SrampAtomException {
        try {
            AuditManager auditManager = AuditManagerFactory.newInstance();
            AuditEntry rval = auditManager.addAuditEntry(artifactUuid, auditEntry);
            return auditEntryToAtomEntry(rval);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_CREATING_AUDIT_ENTRY", artifactUuid), e); //$NON-NLS-1$
            throw new SrampAtomException(e);
        }
    }

    /**
     * Called to get the details of a single audit entry.
     * @throws SrampAtomException
     */
    @GET
    @Path("audit/artifact/{artifactUuid}/{auditEntryUuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry get(@PathParam("artifactUuid") String artifactUuid,
            @PathParam("auditEntryUuid") String auditEntryUuid) throws SrampAtomException {
        try {
            AuditManager auditManager = AuditManagerFactory.newInstance();
            AuditEntry auditEntry = auditManager.getArtifactAuditEntry(artifactUuid, auditEntryUuid);
            return auditEntryToAtomEntry(auditEntry);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_AUDIT_ENTRY", artifactUuid, auditEntryUuid), e); //$NON-NLS-1$
            throw new SrampAtomException(e);
        }
    }

    /**
     * Called to get a Feed of all audit entries for an artifact.
     * @throws SrampAtomException
     */
    @GET
    @Path("audit/artifact/{artifactUuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed listForArtifact(
            @PathParam("artifactUuid") String artifactUuid,
            @QueryParam("startPage") Integer startPage,
            @QueryParam("startIndex") Integer startIndex,
            @QueryParam("count") Integer count) throws SrampAtomException {
        if (startIndex == null && startPage != null) {
            int c = count != null ? count.intValue() : 100;
            startIndex = (startPage.intValue() - 1) * c;
        }
        if (startIndex == null)
            startIndex = 0;
        if (count == null)
            count = 100;
        int startIdx = startIndex;
        int endIdx = startIdx + count - 1;
        try {
            AuditManager auditManager = AuditManagerFactory.newInstance();

            // Get all audit entries by artifact uuid
            AuditEntrySet entries = auditManager.getArtifactAuditEntries(artifactUuid);
            return createAuditFeed(entries, startIdx, endIdx);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_AUDIT_ENTRIES", artifactUuid), e); //$NON-NLS-1$
            throw new SrampAtomException(e);
        }
    }

    /**
     * Called to get a Feed of all audit entries for an artifact.
     * @throws SrampAtomException
     */
    @GET
    @Path("audit/user/{username}")
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed listForUser(
            @PathParam("username") String username,
            @QueryParam("startPage") Integer startPage,
            @QueryParam("startIndex") Integer startIndex,
            @QueryParam("count") Integer count) throws SrampAtomException {
        if (startIndex == null && startPage != null) {
            int c = count != null ? count.intValue() : 100;
            startIndex = (startPage.intValue() - 1) * c;
        }
        if (startIndex == null)
            startIndex = 0;
        if (count == null)
            count = 100;
        int startIdx = startIndex;
        int endIdx = startIdx + count - 1;
        try {
            AuditManager auditManager = AuditManagerFactory.newInstance();

            // Get all audit entries by artifact uuid
            AuditEntrySet entries = auditManager.getUserAuditEntries(username);
            return createAuditFeed(entries, startIdx, endIdx);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_AUDIT_ENTRIES_2", username), e); //$NON-NLS-1$
            throw new SrampAtomException(e);
        }
    }

    /**
     * Creates a {@link Feed} of audit entries.
     * @param auditEntrySet
     * @param fromRow
     * @param toRow
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Feed createAuditFeed(AuditEntrySet auditEntrySet, int fromRow, int toRow) throws Exception {
        Feed feed = new Feed();
        feed.getExtensionAttributes().put(SrampConstants.SRAMP_PROVIDER_QNAME, "JBoss Overlord"); //$NON-NLS-1$
        feed.getExtensionAttributes().put(SrampConstants.SRAMP_ITEMS_PER_PAGE_QNAME, String.valueOf((toRow - fromRow) + 1));
        feed.getExtensionAttributes().put(SrampConstants.SRAMP_START_INDEX_QNAME, String.valueOf(fromRow));
        feed.getExtensionAttributes().put(SrampConstants.SRAMP_TOTAL_RESULTS_QNAME, String.valueOf(auditEntrySet.size()));
        feed.setId(new URI("urn:uuid:" + UUID.randomUUID().toString())); //$NON-NLS-1$
        feed.setTitle("S-RAMP Audit Feed"); //$NON-NLS-1$
        feed.setSubtitle("All Audit Entries for Artifact"); //$NON-NLS-1$
        feed.setUpdated(new Date());

        Iterator<AuditEntry> iterator = auditEntrySet.iterator();

        // Skip any initial rows
        for (int i = 0; i < fromRow; i++) {
            if (!iterator.hasNext())
                break;
            iterator.next();
        }

        // Now get only the rows we're interested in.
        for (int i = fromRow; i <= toRow; i++) {
            if (!iterator.hasNext())
                break;
            AuditEntry auditEntry = iterator.next();
            Entry entry = new Entry();
            entry.setId(new URI(auditEntry.getUuid()));
            entry.setPublished(auditEntry.getWhen().toGregorianCalendar().getTime());
            entry.setUpdated(auditEntry.getWhen().toGregorianCalendar().getTime());
            entry.getAuthors().add(new Person(auditEntry.getWho()));
            entry.setTitle(auditEntry.getType());
            entry.setSummary(""); //$NON-NLS-1$

            feed.getEntries().add(entry);
        }

        return feed;
    }

    /**
     * Turns an audit entry into an Atom entry.
     * @param auditEntry
     * @throws URISyntaxException
     */
    private Entry auditEntryToAtomEntry(AuditEntry auditEntry) throws URISyntaxException {
        Entry entry = new Entry();
        entry.setId(new URI("urn:uuid:" + auditEntry.getUuid())); //$NON-NLS-1$
        entry.setPublished(auditEntry.getWhen().toGregorianCalendar().getTime());
        entry.setUpdated(auditEntry.getWhen().toGregorianCalendar().getTime());
        entry.getAuthors().add(new Person(auditEntry.getWho()));
        entry.setTitle(auditEntry.getType());
        entry.setSummary(""); //$NON-NLS-1$
        entry.setAnyOtherJAXBObject(auditEntry);
        return entry;
    }

}
