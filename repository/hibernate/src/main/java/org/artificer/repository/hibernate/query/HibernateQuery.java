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
package org.artificer.repository.hibernate.query;

import org.artificer.common.ArtificerException;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.RepositoryProviderFactory;
import org.artificer.repository.error.QueryExecutionException;
import org.artificer.repository.hibernate.HibernatePersistenceManager;
import org.artificer.repository.hibernate.HibernateUtil;
import org.artificer.repository.hibernate.data.HibernateArtifactSet;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.i18n.Messages;
import org.artificer.repository.query.AbstractArtificerQueryImpl;
import org.artificer.repository.query.ArtifactSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brett Meyer
 */
public class HibernateQuery extends AbstractArtificerQueryImpl {

	private static final Logger LOG = LoggerFactory.getLogger(HibernatePersistenceManager.class);

	private static Map<String, String> sOrderByMappings = new HashMap<String, String>();
	static {
		sOrderByMappings.put("createdBy", "createdBy.username");
		sOrderByMappings.put("version", "version");
		sOrderByMappings.put("uuid", "uuid");
		sOrderByMappings.put("createdTimestamp", "createdBy.lastActionTime");
		sOrderByMappings.put("lastModifiedTimestamp", "modifiedBy.lastActionTime");
		sOrderByMappings.put("lastModifiedBy", "modifiedBy.username");
		sOrderByMappings.put("name", "name");
	}

	/**
	 * Constructor.
	 * @param xpathTemplate
	 * @param orderByProperty
	 * @param orderAscending
	 */
	public HibernateQuery(String xpathTemplate, String orderByProperty, boolean orderAscending) {
		super(xpathTemplate, orderByProperty, orderAscending);
	}

	@Override
	protected ArtifactSet executeQuery(final Query queryModel) throws ArtificerException {
		try {
            final String orderBy;
            if (getOrderByProperty() != null) {
                String propName = sOrderByMappings.get(getOrderByProperty());
                orderBy = propName != null ? propName : null;
            } else {
                orderBy = null;
            }

            return new HibernateUtil.HibernateTask<HibernateArtifactSet>() {
                @Override
                protected HibernateArtifactSet doExecute(EntityManager entityManager) throws Exception {
                    ArtificerToHibernateQueryVisitor visitor = new ArtificerToHibernateQueryVisitor(entityManager,
                            (ClassificationHelper) RepositoryProviderFactory.persistenceManager());
                    queryModel.accept(visitor);
                    if (orderBy != null) {
                        visitor.setOrder(orderBy);
                        visitor.setOrderAscending(isOrderAscending());
                    }

                    long startTime = System.currentTimeMillis();
                    List<ArtificerArtifact> results = visitor.query();
                    long endTime = System.currentTimeMillis();

                    LOG.debug(Messages.i18n.format("QUERY_EXECUTED_IN", endTime - startTime));

                    return new HibernateArtifactSet(results);
                }
            }.execute();

		} catch (ArtificerException e) {
            throw e;
		} catch (Throwable t) {
			throw new QueryExecutionException(t);
		}
	}

}
