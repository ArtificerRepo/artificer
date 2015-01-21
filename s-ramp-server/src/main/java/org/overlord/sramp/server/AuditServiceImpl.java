/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.server;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.overlord.sramp.repository.AuditManager;
import org.overlord.sramp.repository.audit.AuditEntrySet;
import org.overlord.sramp.server.core.api.AuditService;
import org.overlord.sramp.server.core.api.PagedResult;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "AuditService")
@Remote(AuditService.class)
public class AuditServiceImpl extends AbstractServiceImpl implements AuditService {

    @Override
    public AuditEntry create(String artifactUuid, AuditEntry auditEntry) throws Exception {
        AuditManager auditManager = auditManager();
        return auditManager.addAuditEntry(artifactUuid, auditEntry);
    }

    @Override
    public AuditEntry get(String artifactUuid, String auditEntryUuid) throws Exception {
        AuditManager auditManager = auditManager();
        return auditManager.getArtifactAuditEntry(artifactUuid, auditEntryUuid);
    }

    @Override
    public List<AuditEntry> queryByArtifact(String artifactUuid) throws Exception {
        AuditManager auditManager = auditManager();
        AuditEntrySet results = auditManager.getArtifactAuditEntries(artifactUuid);
        return results.list();
    }

    @Override
    public PagedResult<AuditEntry> queryByArtifact(String artifactUuid, Integer startPage, Integer startIndex, Integer count)
            throws Exception {
        AuditManager auditManager = auditManager();
        AuditEntrySet results = auditManager.getArtifactAuditEntries(artifactUuid);
        return doPaging(results, startPage, startIndex, count);
    }

    @Override
    public List<AuditEntry> queryByUser(String username) throws Exception {
        AuditManager auditManager = auditManager();
        AuditEntrySet results = auditManager.getUserAuditEntries(username);
        return results.list();
    }

    @Override
    public PagedResult<AuditEntry> queryByUser(String username, Integer startPage, Integer startIndex, Integer count)
            throws Exception {
        AuditManager auditManager = auditManager();
        AuditEntrySet results = auditManager.getUserAuditEntries(username);
        return doPaging(results, startPage, startIndex, count);
    }

    private PagedResult<AuditEntry> doPaging(AuditEntrySet results, Integer startPage, Integer startIndex, Integer count)
            throws Exception {
        startIndex = startIndex(startPage, startIndex, count);
        if (count == null)
            count = 100;
        int startIdx = startIndex;
        int endIdx = startIdx + count - 1;
        try {
            List<AuditEntry> entries = results.pagedList(startIdx, endIdx);
            return new PagedResult<AuditEntry>(entries, "", results.size(), startIndex, "", true);
        } finally {
            results.close();
        }
    }
}
