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
package org.artificer.repository;

import org.artificer.repository.audit.AuditEntrySet;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.artificer.common.ArtificerException;


/**
 * Service used to store and retrieve audit information.
 *
 * @author eric.wittmann@redhat.com
 */
public interface AuditManager extends AbstractManager {

    /**
     * Adds an audit entry for an artifact by UUID.
     * @param artifactUuid
     * @param entry
     * @throws org.artificer.common.ArtificerException
     */
    public AuditEntry addAuditEntry(String artifactUuid, AuditEntry entry) throws ArtificerException;

    /**
     * Gets the full audit entry given the UUID of both the artifact in question and the
     * audit entry.
     * @param artifactUuid
     * @param auditEntryUuid
     */
    public AuditEntry getArtifactAuditEntry(String artifactUuid, String auditEntryUuid) throws ArtificerException;

    /**
     * Gets all of the audit entries for a particular artifact.  This is mostly what
     * consumers will be interested in - the audit history for a given artifact.  When
     * the caller is done with the {@link org.artificer.repository.audit.AuditEntrySet}, it must call close to free up
     * any resources.
     *
     * @param artifactUuid
     * @throws org.artificer.common.ArtificerException
     */
    public AuditEntrySet getArtifactAuditEntries(String artifactUuid) throws ArtificerException;

    /**
     * Gets
     * @param username
     * @return
     * @throws org.artificer.common.ArtificerException
     */
    public AuditEntrySet getUserAuditEntries(String username) throws ArtificerException;
}
