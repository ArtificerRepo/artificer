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
package org.artificer.server;

import org.artificer.common.ReverseRelationship;
import org.artificer.repository.QueryManager;
import org.artificer.repository.query.ArtifactSet;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.server.core.api.PagedResult;
import org.artificer.server.core.api.QueryService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "QueryService")
@Remote(QueryService.class)
// Required so that artificer-repository-hibernate can control the transactions during EJB calls.
@TransactionManagement(TransactionManagementType.BEAN)
public class QueryServiceImpl extends AbstractServiceImpl implements QueryService {

    @Override
    public List<BaseArtifactType> query(String query) throws Exception {
        return query(query, "name", true);
    }

    @Override
    public List<BaseArtifactType> query(String query, String orderBy, Boolean ascending) throws Exception { // Add on the "/s-ramp/" if it's missing
        String xpath = query;
        if (!xpath.startsWith("/s-ramp")) { //$NON-NLS-1$
            if (query.startsWith("/")) //$NON-NLS-1$
                xpath = "/s-ramp" + query; //$NON-NLS-1$
            else
                xpath = "/s-ramp/" + query; //$NON-NLS-1$
        }

        if (orderBy == null)
            orderBy = "name"; //$NON-NLS-1$
        if (ascending == null)
            ascending = true;

        ArtifactSet artifactSet = executeQuery(xpath, orderBy, ascending);
        try {
            return artifactSet.list();
        } finally {
            artifactSet.close();
        }
    }

    @Override
    public PagedResult<BaseArtifactType> query(String query, Integer startPage, Integer startIndex, Integer count,
            String orderBy, Boolean ascending) throws Exception {
        // Add on the "/s-ramp/" if it's missing
        String xpath = query;
        if (!xpath.startsWith("/s-ramp")) { //$NON-NLS-1$
            if (query.startsWith("/")) //$NON-NLS-1$
                xpath = "/s-ramp" + query; //$NON-NLS-1$
            else
                xpath = "/s-ramp/" + query; //$NON-NLS-1$
        }

        if (orderBy == null)
            orderBy = "name"; //$NON-NLS-1$
        if (ascending == null)
            ascending = true;

        ArtifactSet artifactSet = executeQuery(xpath, orderBy, ascending);

        startIndex = startIndex(startPage, startIndex, count);
        if (count == null)
            count = 100;

        int startIdx = startIndex;
        int endIdx = startIdx + count - 1;
        try {
            List<BaseArtifactType> results = artifactSet.pagedList(startIdx, endIdx);
            return new PagedResult<BaseArtifactType>(results, xpath, artifactSet.size(), startIndex, count,
                    orderBy, ascending);
        } finally {
            artifactSet.close();
        }
    }

    private ArtifactSet executeQuery(String query, String orderBy, Boolean ascending) throws Exception {
        QueryManager queryManager = queryManager();
        ArtificerQuery artificerQuery = queryManager.createQuery(query, orderBy, ascending);
        return artificerQuery.executeQuery();
    }

    @Override
    public StoredQuery createStoredQuery(StoredQuery storedQuery) throws Exception {
        return persistenceManager().persistStoredQuery(storedQuery);
    }

    @Override
    public void updateStoredQuery(String queryName, StoredQuery storedQuery) throws Exception {
        persistenceManager().updateStoredQuery(queryName, storedQuery);
    }

    @Override
    public StoredQuery getStoredQuery(String queryName) throws Exception {
        return persistenceManager().getStoredQuery(queryName);
    }

    @Override
    public List<StoredQuery> getStoredQueries() throws Exception {
        return persistenceManager().getStoredQueries();
    }

    @Override
    public void deleteStoredQuery(String queryName) throws Exception {
        persistenceManager().deleteStoredQuery(queryName);
    }

    @Override
    public List<ReverseRelationship> reverseRelationships(String uuid) throws Exception {
        return queryManager().reverseRelationships(uuid);
    }
}
