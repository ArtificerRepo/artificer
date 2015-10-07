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

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.artificer.common.query.ArtifactSummary;
import org.artificer.common.query.ReverseRelationship;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.repository.query.ArtificerQueryArgs;
import org.artificer.repository.query.PagedResult;
import org.artificer.server.core.api.QueryService;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "QueryService")
@Remote(QueryService.class)
// Required so that artificer-repository-hibernate can control the transactions during EJB calls.
@TransactionManagement(TransactionManagementType.BEAN)
public class QueryServiceImpl extends AbstractServiceImpl implements QueryService {

    @Override
    public PagedResult<ArtifactSummary> query(String query) throws Exception {
        return query(query, "name", true);
    }

    @Override
    public PagedResult<ArtifactSummary> query(String query, String orderBy, Boolean ascending) throws Exception { // Add on the "/s-ramp/" if it's missing
		String xpath = formatXpath(query);

        ArtificerQueryArgs args = new ArtificerQueryArgs(orderBy, ascending);
        ArtificerQuery artificerQuery = queryManager().createQuery(xpath, args);
        return artificerQuery.executeQuery();
    }

    @Override
    public PagedResult<ArtifactSummary> query(String query, Integer startPage, Integer startIndex, Integer count,
            String orderBy, Boolean ascending) throws Exception {
        String xpath = formatXpath(query);

        ArtificerQueryArgs args = new ArtificerQueryArgs(orderBy, ascending, startPage, startIndex, count);
        ArtificerQuery artificerQuery = queryManager().createQuery(xpath, args);
        return artificerQuery.executeQuery();
    }

	private String formatXpath(String query) {
		String xpath = query;
		if (xpath.equals("/s-ramp/")) {
			// If query is "/s-ramp/" exactly, trim off the trailing "/" -- screws with the parser.
			xpath = "/s-ramp";
		} else if (!xpath.startsWith("/s-ramp")) {
			// Add on the "/s-ramp/" if it's missing
			if (query.startsWith("/"))
				xpath = "/s-ramp" + query;
			else
				xpath = "/s-ramp/" + query;
		}
		return xpath;
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

    @Override
    public List<String> getTypes() throws Exception {
        return queryManager().getTypes();
    }
}
