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
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.SrampClientQuery;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactOriginEnum;
import org.overlord.sramp.ui.client.shared.beans.ArtifactResultSetBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSearchBean;
import org.overlord.sramp.ui.client.shared.beans.ArtifactSummaryBean;
import org.overlord.sramp.ui.client.shared.exceptions.SrampUiException;
import org.overlord.sramp.ui.client.shared.services.IArtifactSearchService;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;

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

    /**
     * @see org.overlord.sramp.ui.client.shared.services.IArtifactSearchService#search(org.overlord.sramp.ui.client.shared.beans.ArtifactFilterBean, java.lang.String, int, java.lang.String, boolean)
     */
    @Override
    public ArtifactResultSetBean search(ArtifactSearchBean searchBean) throws SrampUiException {
        int pageSize = 20;
        try {
            ArtifactResultSetBean rval = new ArtifactResultSetBean();

            int req_startIndex = (searchBean.getPage() - 1) * pageSize;
            SrampClientQuery query = null;
            if (searchBean.getSearchText() != null && searchBean.getSearchText().startsWith("/")) { //$NON-NLS-1$
                query = SrampApiClientAccessor.getClient().buildQuery(searchBean.getSearchText());
            } else {
                query = createQuery(searchBean.getFilters(), searchBean.getSearchText());
            }
            SrampClientQuery sq = query.startIndex(req_startIndex).orderBy(searchBean.getSortColumnId());
            if (searchBean.isSortAscending()) {
                sq.ascending();
            } else {
                sq.descending();
            }
            QueryResultSet resultSet = sq.count(pageSize + 1).query();
            ArrayList<ArtifactSummaryBean> artifacts = new ArrayList<ArtifactSummaryBean>();
            for (ArtifactSummary artifactSummary : resultSet) {
                ArtifactSummaryBean bean = new ArtifactSummaryBean();
                ArtifactType artifactType = artifactSummary.getType();
                bean.setModel(artifactType.getArtifactType().getModel());
                bean.setType(artifactType.getType());
                bean.setRawType(artifactType.getArtifactType().getType());
                bean.setUuid(artifactSummary.getUuid());
                bean.setName(artifactSummary.getName());
                bean.setDescription(artifactSummary.getDescription());
                bean.setCreatedBy(artifactSummary.getCreatedBy());
                bean.setCreatedOn(artifactSummary.getCreatedTimestamp());
                bean.setUpdatedOn(artifactSummary.getLastModifiedTimestamp());
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
        queryBuilder.append("/s-ramp"); //$NON-NLS-1$
        // Artifact type
        if (filters.getArtifactType() != null && filters.getArtifactType().trim().length() > 0) {
            ArtifactType type = ArtifactType.valueOf(filters.getArtifactType());
            queryBuilder.append("/").append(type.getModel()).append("/").append(type.getType()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        List<String> criteria = new ArrayList<String>();
        List<Object> params = new ArrayList<Object>();

        // Search Text
        if (searchText != null && searchText.trim().length() > 0) {
            criteria.add("fn:matches(@name, ?)"); //$NON-NLS-1$
            params.add(searchText.replace("*", ".*")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        // Created on
        if (filters.getDateCreatedFrom() != null) {
            criteria.add("@createdTimestamp >= ?"); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateCreatedFrom());
            zeroOutTime(cal);
            params.add(cal);
        }
        if (filters.getDateCreatedTo() != null) {
            criteria.add("@createdTimestamp < ?"); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateCreatedTo());
            zeroOutTime(cal);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            params.add(cal);
        }
        // Last Modified on
        if (filters.getDateModifiedFrom() != null) {
            criteria.add("@lastModifiedTimestamp >= ?"); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateModifiedFrom());
            zeroOutTime(cal);
            params.add(cal);
        }
        if (filters.getDateModifiedTo() != null) {
            criteria.add("@lastModifiedTimestamp < ?"); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            cal.setTime(filters.getDateModifiedTo());
            zeroOutTime(cal);
            cal.add(Calendar.DAY_OF_YEAR, 1);
            params.add(cal);
        }
        // Created By
        if (filters.getCreatedBy() != null && filters.getCreatedBy().trim().length() > 0) {
            criteria.add("@createdBy = ?"); //$NON-NLS-1$
            params.add(filters.getCreatedBy());
        }
        // Last Modified By
        if (filters.getLastModifiedBy() != null && filters.getLastModifiedBy().trim().length() > 0) {
            criteria.add("@lastModifiedBy = ?"); //$NON-NLS-1$
            params.add(filters.getLastModifiedBy());
        }
        // Origin
        if (filters.getOrigin() == ArtifactOriginEnum.primary) {
            criteria.add("@derived = ?"); //$NON-NLS-1$
            params.add("false"); //$NON-NLS-1$
        } else if (filters.getOrigin() == ArtifactOriginEnum.derived) {
            criteria.add("@derived = ?"); //$NON-NLS-1$
            params.add("true"); //$NON-NLS-1$
        }
        // Classifiers
        if (hasClassifiers(filters)) {
            Set<String> ontologyBases = filters.getClassifiers().keySet();
            StringBuilder classifierCriteria = new StringBuilder();
            classifierCriteria.append("s-ramp:classifiedByAllOf(."); //$NON-NLS-1$
            for (String base : ontologyBases) {
                Set<String> ids = filters.getClassifiers().get(base);
                for (String id : ids) {
                    String classifierUri = base + "#" + id; //$NON-NLS-1$
                    classifierCriteria.append(",?"); //$NON-NLS-1$
                    params.add(classifierUri);
                }
            }
            classifierCriteria.append(")"); //$NON-NLS-1$
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
                    criteria.add("@" + propName); //$NON-NLS-1$
                } else {
                    criteria.add("@" + propName + " = ?"); //$NON-NLS-1$ //$NON-NLS-2$
                    params.add(propVal);
                }
            }
        }

        // Now create the query predicate from the generated criteria
        if (criteria.size() > 0) {
            queryBuilder.append("["); //$NON-NLS-1$
            queryBuilder.append(StringUtils.join(criteria, " and ")); //$NON-NLS-1$
            queryBuilder.append("]"); //$NON-NLS-1$
        }

        // Create the query, and parameterize it
        SrampAtomApiClient client = SrampApiClientAccessor.getClient();
        SrampClientQuery query = client.buildQuery(queryBuilder.toString());
        for (Object param : params) {
            if (param instanceof String) {
                query.parameter((String) param);
            }
            if (param instanceof Calendar) {
                query.parameter((Calendar) param);
            }
        }
        return query;
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
