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
package org.artificer.ui.server.services;

import org.apache.commons.lang3.StringUtils;
import org.artificer.client.ArtificerClientException;
import org.artificer.client.ArtificerClientQuery;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.ui.client.shared.beans.ArtifactFilterBean;
import org.artificer.ui.client.shared.beans.ArtifactResultSetBean;
import org.artificer.ui.client.shared.beans.ArtifactSearchBean;
import org.artificer.ui.client.shared.beans.ArtifactSummaryBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.client.shared.services.IArtifactSearchService;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;

import javax.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Concrete implementation of the artifact search service.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class ArtifactSearchService implements IArtifactSearchService {

    /**
     * Constructor.
     */
    public ArtifactSearchService() {
    }

    @Override
    public ArtifactResultSetBean search(ArtifactSearchBean searchBean) throws ArtificerUiException {
        int pageSize = 20;
        try {
            ArtifactResultSetBean rval = new ArtifactResultSetBean();

            int req_startIndex = (searchBean.getPage() - 1) * pageSize;
            ArtificerClientQuery query = ArtificerApiClientAccessor.getClient().buildQuery(searchBean.getQueryText());
            ArtificerClientQuery sq = query.startIndex(req_startIndex).orderBy(searchBean.getSortColumnId());
            if (searchBean.isSortAscending()) {
                sq.ascending();
            } else {
                sq.descending();
            }
            QueryResultSet resultSet = sq.count(pageSize + 1).query();
            ArrayList<ArtifactSummaryBean> artifacts = new ArrayList<ArtifactSummaryBean>();
            for (ArtifactSummary artifactSummary : resultSet) {
                ArtifactSummaryBean bean = new ArtifactSummaryBean();
                ArtifactType artifactType = artifactSummary.getArtifactType();
                bean.setModel(artifactType.getArtifactType().getModel());
                bean.setType(artifactType.getType());
                bean.setRawType(artifactType.getArtifactType().getType());
                bean.setUuid(artifactSummary.getUuid());
                bean.setName(artifactSummary.getName());
                bean.setDescription(artifactSummary.getDescription());
                bean.setCreatedBy(artifactSummary.getCreatedBy());
                bean.setCreatedOn(artifactSummary.getCreatedTimestamp().getTime());
                bean.setUpdatedOn(artifactSummary.getLastModifiedTimestamp().getTime());
                bean.setDerived(artifactType.isDerived());
                artifacts.add(bean);
            }
            boolean hasMorePages = false;
            if (artifacts.size() > pageSize) {
                artifacts.remove(artifacts.get(artifacts.size()-1));
                hasMorePages = true;
            }
            // Does the server support opensearch style attributes?  If so,
            // use that information.  Else figure it out from the request params.
            if (resultSet.getTotalResults() != -1) {
                rval.setItemsPerPage(pageSize);
                rval.setStartIndex(resultSet.getStartIndex());
                rval.setTotalResults(resultSet.getTotalResults());
            } else {
                rval.setItemsPerPage(pageSize);
                rval.setTotalResults(hasMorePages ? pageSize + 1 : artifacts.size());
                rval.setStartIndex(req_startIndex);
            }

            rval.setArtifacts(artifacts);
            return rval;
        } catch (ArtificerClientException e) {
            throw new ArtificerUiException(e.getMessage());
        } catch (ArtificerServerException e) {
            throw new ArtificerUiException(e.getMessage());
        }
    }

    @Override
    public String query(ArtifactFilterBean filters) throws ArtificerUiException {

        StringBuilder queryBuilder = new StringBuilder();
        // Initial query
        queryBuilder.append("/s-ramp");
        // Artifact type
        if (filters.getArtifactType() != null && filters.getArtifactType().trim().length() > 0) {
            ArtifactType type = ArtifactType.valueOf(filters.getArtifactType());
            queryBuilder.append("/").append(type.getModel()).append("/").append(type.getType());
        }
        List<String> criteria = new ArrayList<String>();

        if (filters.getKeywords() != null && filters.getKeywords().trim().length() > 0) {
            criteria.add("xp2:matches(., '" + filters.getKeywords() + "')");
        }
        if (filters.getUuid() != null && filters.getUuid().trim().length() > 0) {
            criteria.add("@uuid = '" + filters.getUuid() + "'");
        }
        if (filters.getName() != null && filters.getName().trim().length() > 0) {
            criteria.add("@name = '" + filters.getName() + "'");
        }
        // Created on
        if (filters.getDateCreatedFrom() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateCreatedFrom());
            zeroOutTime(cal);
            criteria.add("@createdTimestamp >= " + cal.getTimeInMillis());
        }
        if (filters.getDateCreatedTo() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateCreatedTo());
            zeroOutTime(cal);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            criteria.add("@createdTimestamp < " + cal.getTimeInMillis());
        }
        // Last Modified on
        if (filters.getDateModifiedFrom() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateModifiedFrom());
            zeroOutTime(cal);
            criteria.add("@lastModifiedTimestamp >= " + cal.getTimeInMillis());
        }
        if (filters.getDateModifiedTo() != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateModifiedTo());
            zeroOutTime(cal);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            criteria.add("@lastModifiedTimestamp < " + cal.getTimeInMillis());
        }
        // Created By
        if (filters.getCreatedBy() != null && filters.getCreatedBy().trim().length() > 0) {
            criteria.add("@createdBy = '" + filters.getCreatedBy() + "'");
        }
        // Last Modified By
        if (filters.getLastModifiedBy() != null && filters.getLastModifiedBy().trim().length() > 0) {
            criteria.add("@lastModifiedBy = '" + filters.getLastModifiedBy() + "'");
        }
        // Origin
        switch (filters.getOrigin()) {
            case PRIMARY_ORIGINAL:
                criteria.add("@derived = 'false' and @expandedFromArchive = 'false'");
                break;
            case PRIMARY_EXPANDED:
                criteria.add("@derived = 'false' and @expandedFromArchive = 'true'");
                break;
            case DERIVED:
                criteria.add("@derived = 'true'");
                break;
        }
        // Classifiers
        if (hasClassifiers(filters)) {
            Set<String> ontologyBases = filters.getClassifiers().keySet();
            StringBuilder classifierCriteria = new StringBuilder();
            classifierCriteria.append("s-ramp:classifiedByAllOf(.");
            for (String base : ontologyBases) {
                Set<String> ids = filters.getClassifiers().get(base);
                for (String id : ids) {
                    String classifierUri = base + "#" + id;
                    classifierCriteria.append(",'" + classifierUri + "'");
                }
            }
            classifierCriteria.append(")");
            criteria.add(classifierCriteria.toString());
        }
        // Custom properties
        if (!filters.getCustomProperties().isEmpty()) {
            for (Entry<String, String> entry : filters.getCustomProperties().entrySet()) {
                String propName = entry.getKey();
                String propVal = entry.getValue();
                // Note: this looks dangerous (injection) but know that
                // S-RAMP queries are read-only.  Also, the UI allows
                // the user to input any query they want anyway (via the
                // query text box)...
                if (propVal == null || propVal.trim().length() == 0) {
                    criteria.add("@" + propName);
                } else {
                    criteria.add("@" + propName + " = '" + propVal + "'");
                }
            }
        }

        // Now create the query predicate from the generated criteria
        if (criteria.size() > 0) {
            queryBuilder.append("[");
            queryBuilder.append(StringUtils.join(criteria, " and "));
            queryBuilder.append("]");
        }
        
        return queryBuilder.toString();
    }

    /**
     * Returns true if the filters has *at least* one classifier configured.
     * @param filters
     */
    protected boolean hasClassifiers(ArtifactFilterBean filters) {
        Map<String, Set<String>> classifiers = filters.getClassifiers();
        if (classifiers == null)
            return false;
        for (String key : classifiers.keySet()) {
            Set<String> oclasses = classifiers.get(key);
            if (oclasses != null && !oclasses.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Set the time components of the given {@link Calendar} to 0's.
     * @param cal
     */
    protected void zeroOutTime(Calendar cal) {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }


}
