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
package org.artificer.server.atom.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.artificer.server.AuditServiceImpl;
import org.artificer.server.core.api.PagedResult;
import org.artificer.server.i18n.Messages;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.artificer.atom.MediaType;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.common.ArtificerConstants;
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

    private final AuditServiceImpl auditService = new AuditServiceImpl();

    /**
     * S-RAMP atom POST to add an audit entry to the audit trail of an artifact.
     */
    @POST
    @Path("audit/artifact/{artifactUuid}")
    @Consumes(MediaType.APPLICATION_AUDIT_ENTRY_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(@PathParam("artifactUuid") String artifactUuid, AuditEntry auditEntry) throws ArtificerAtomException {
        try {
            AuditEntry rval = auditService.create(artifactUuid, auditEntry);
            return auditEntryToAtomEntry(rval);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_CREATING_AUDIT_ENTRY", artifactUuid), e); //$NON-NLS-1$
            throw new ArtificerAtomException(e);
        }
    }

    /**
     * Called to get the details of a single audit entry.
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @GET
    @Path("audit/artifact/{artifactUuid}/{auditEntryUuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry get(@PathParam("artifactUuid") String artifactUuid,
            @PathParam("auditEntryUuid") String auditEntryUuid) throws ArtificerAtomException {
        try {
            AuditEntry auditEntry = auditService.get(artifactUuid, auditEntryUuid);
            return auditEntryToAtomEntry(auditEntry);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_AUDIT_ENTRY", artifactUuid, auditEntryUuid), e); //$NON-NLS-1$
            throw new ArtificerAtomException(e);
        }
    }

    /**
     * Called to get a Feed of all audit entries for an artifact.
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @GET
    @Path("audit/artifact/{artifactUuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed listForArtifact(
            @PathParam("artifactUuid") String artifactUuid,
            @QueryParam("startPage") Integer startPage,
            @QueryParam("startIndex") Integer startIndex,
            @QueryParam("count") Integer count) throws ArtificerAtomException {

        try {
            // Get all audit entries by artifact uuid
            PagedResult<AuditEntry> entries = auditService.queryByArtifact(artifactUuid, startPage, startIndex, count);
            return createAuditFeed(entries);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_AUDIT_ENTRIES", artifactUuid), e); //$NON-NLS-1$
            throw new ArtificerAtomException(e);
        }
    }

    /**
     * Called to get a Feed of all audit entries for an artifact.
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @GET
    @Path("audit/user/{username}")
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed listForUser(
            @PathParam("username") String username,
            @QueryParam("startPage") Integer startPage,
            @QueryParam("startIndex") Integer startIndex,
            @QueryParam("count") Integer count) throws ArtificerAtomException {
        try {
            // Get all audit entries by user
            PagedResult<AuditEntry> entries = auditService.queryByUser(username, startPage, startIndex, count);
            return createAuditFeed(entries);
        } catch (Throwable e) {
            logError(logger, Messages.i18n.format("ERROR_GETTING_AUDIT_ENTRIES_2", username), e); //$NON-NLS-1$
            throw new ArtificerAtomException(e);
        }
    }

    /**
     * Creates a {@link Feed} of audit entries.
     * @param auditEntries
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private Feed createAuditFeed(PagedResult<AuditEntry> auditEntries) throws Exception {
        Feed feed = new Feed();
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_PROVIDER_QNAME, "JBoss Overlord"); //$NON-NLS-1$
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_ITEMS_PER_PAGE_QNAME, String.valueOf(auditEntries.getPageSize()));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_START_INDEX_QNAME, String.valueOf(auditEntries.getStartIndex()));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME, String.valueOf(auditEntries.getTotalSize()));
        feed.setId(new URI("urn:uuid:" + UUID.randomUUID().toString())); //$NON-NLS-1$
        feed.setTitle("S-RAMP Audit Feed"); //$NON-NLS-1$
        feed.setSubtitle("All Audit Entries for Artifact"); //$NON-NLS-1$
        feed.setUpdated(new Date());

        for (AuditEntry auditEntry : auditEntries.getResults()) {
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
