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
package org.overlord.sramp.ui.server.rsvcs;

import java.util.ArrayList;
import java.util.List;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.ui.server.api.SrampAtomApiClient;
import org.overlord.sramp.ui.server.util.ExceptionUtils;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.beans.PageInfo;
import org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteService;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;
import org.overlord.sramp.ui.shared.types.ArtifactTypeFilter;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * Implementation of the query remote service.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryRemoteService extends RemoteServiceServlet implements IQueryRemoteService {

	private static final long serialVersionUID = QueryRemoteService.class.hashCode();

	/**
	 * Constructor.
	 */
	public QueryRemoteService() {
	}

	/**
	 * @see org.overlord.sramp.ui.shared.rsvcs.IQueryRemoteService#findArtifacts(org.overlord.sramp.ui.shared.beans.PageInfo, org.overlord.sramp.ui.shared.types.ArtifactTypeFilter, java.lang.String)
	 */
	@Override
	public List<ArtifactSummary> findArtifacts(PageInfo page, ArtifactTypeFilter filter, String nameFilter)
			throws RemoteServiceException {
		try {
			int startIndex = page.getPage() * page.getPageSize();
			String query = filter.getQueryBase();
			if (nameFilter != null) {
				String criteria = nameFilter.replace("*", ".*").replace("'", "''");
				query = query + "[fn:matches(@name, '" + criteria + "')]";
			}
			QueryResultSet rset = SrampAtomApiClient.getInstance().query(query, startIndex,
					page.getPageSize(), page.getOrderBy(), page.isAscending());
			List<ArtifactSummary> rval = new ArrayList<ArtifactSummary>();
			for (org.overlord.sramp.client.query.ArtifactSummary entry : rset) {
				ArtifactSummary arty = new ArtifactSummary();
				ArtifactType artifactType = entry.getType();
				arty.setModel(artifactType.getArtifactType().getModel());
				arty.setType(artifactType.getArtifactType().getType());
				arty.setUuid(entry.getUuid());
				arty.setName(entry.getName());
				arty.setDescription(entry.getDescription());
				arty.setCreatedBy(entry.getCreatedBy());
				arty.setCreatedOn(entry.getCreatedTimestamp());
				arty.setUpdatedOn(entry.getLastModifiedTimestamp());
				arty.setDerived(artifactType.getArtifactType().isDerived());
				rval.add(arty);
			}
			return rval;
		} catch (Throwable t) {
			throw ExceptionUtils.createRemoteException(t);
		}
	}

}
