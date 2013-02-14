/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.ui.server.services;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.jboss.errai.bus.server.annotations.Service;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactOriginEnum;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactSearchService;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;

/**
 * Concrete implementation of the artifact search service.
 *
 * @author eric.wittmann@redhat.com
 */
@Service
public class ArtifactSearchService implements IArtifactSearchService {

    @Inject
    private SrampApiClientAccessor clientAccessor;

    /**
     * Constructor.
     */
    public ArtifactSearchService() {
    }

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactSearchService#search(org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean, java.lang.String)
     */
    @Override
    public List<ArtifactSummaryBean> search(ArtifactFilterBean filters, String searchText) throws SrampUiException {
        try {
            SrampClientQuery query = null;
            if (searchText != null && searchText.startsWith("/")) {
                query = clientAccessor.getClient().buildQuery(searchText);
            } else {
                query = createQuery(filters, searchText);
            }
            QueryResultSet resultSet = query.orderBy("name").ascending().count(25).query();
            ArrayList<ArtifactSummaryBean> rval = new ArrayList<ArtifactSummaryBean>();
            for (ArtifactSummary artifactSummary : resultSet) {
                ArtifactSummaryBean bean = new ArtifactSummaryBean();
                ArtifactType artifactType = artifactSummary.getType();
                bean.setModel(artifactType.getArtifactType().getModel());
                bean.setType(artifactType.getArtifactType().getType());
                bean.setUuid(artifactSummary.getUuid());
                bean.setName(artifactSummary.getName());
                bean.setDescription(artifactSummary.getDescription());
                bean.setCreatedBy(artifactSummary.getCreatedBy());
                bean.setCreatedOn(artifactSummary.getCreatedTimestamp());
                bean.setUpdatedOn(artifactSummary.getLastModifiedTimestamp());
                bean.setDerived(artifactType.getArtifactType().isDerived());
                rval.add(bean);
            }
            return rval;
        } catch (SrampClientException e) {
            throw new SrampUiException(e.getMessage());
        } catch (SrampAtomException e) {
            throw new SrampUiException(e.getMessage());
        }
    }

    /**
     * Creates a query given the selected filters and search text.
     */
    protected SrampClientQuery createQuery(ArtifactFilterBean filters, String searchText) {
        StringBuilder queryBuilder = new StringBuilder();
        // Initial query
        queryBuilder.append("/s-ramp");
        // Artifact type
        if (filters.getArtifactType() != null && filters.getArtifactType().trim().length() > 0) {
            ArtifactType type = ArtifactType.valueOf(filters.getArtifactType());
            queryBuilder.append("/").append(type.getModel()).append("/").append(type.getType());
        }
        List<String> criteria = new ArrayList<String>();
        List<String> params = new ArrayList<String>();

        // Created on
        // Last Modified on
        // Created By
        // Last Modified By
        // Origin
        if (filters.getOrigin() == ArtifactOriginEnum.primary) {
            criteria.add("@derived = 'false'");
        } else if (filters.getOrigin() == ArtifactOriginEnum.derived) {
            criteria.add("@derived = 'true'");
        }

        // Now create the query predicate from the generated criteria
        if (criteria.size() > 0) {
            queryBuilder.append("[");
            queryBuilder.append(StringUtils.join(criteria, " and "));
            queryBuilder.append("]");
        }

        // Create the query, and parameterize it
        SrampAtomApiClient client = clientAccessor.getClient();
        SrampClientQuery query = client.buildQuery(queryBuilder.toString());
        for (String param : params) {
            query.parameter(param);
        }
        return query;
    }


}
