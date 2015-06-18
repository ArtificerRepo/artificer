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
import org.artificer.common.query.ArtifactSummary;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.repository.ClassificationHelper;
import org.artificer.repository.RepositoryProviderFactory;
import org.artificer.repository.error.QueryExecutionException;
import org.artificer.repository.hibernate.HibernatePersistenceManager;
import org.artificer.repository.hibernate.HibernateUtil;
import org.artificer.repository.hibernate.i18n.Messages;
import org.artificer.repository.query.AbstractArtificerQueryImpl;
import org.artificer.repository.query.ArtificerQueryArgs;
import org.artificer.repository.query.PagedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Brett Meyer
 */
public class HibernateQuery extends AbstractArtificerQueryImpl {

	private static final Logger LOG = LoggerFactory.getLogger(HibernatePersistenceManager.class);

	/**
	 * Constructor.
	 * @param xpathTemplate
	 * @param args
	 */
	public HibernateQuery(String xpathTemplate, ArtificerQueryArgs args) {
		super(xpathTemplate, args);
	}

    public HibernateQuery(String xpathTemplate) {
        super(xpathTemplate, new ArtificerQueryArgs());
    }

	@Override
	protected PagedResult<ArtifactSummary> executeQuery(final Query queryModel) throws ArtificerException {
		try {
            return new HibernateUtil.HibernateTask<PagedResult<ArtifactSummary>>() {
                @Override
                protected PagedResult<ArtifactSummary> doExecute(EntityManager entityManager) throws Exception {
                    ArtificerToHibernateQueryVisitor visitor = new ArtificerToHibernateQueryVisitor(entityManager,
                            (ClassificationHelper) RepositoryProviderFactory.persistenceManager());
                    queryModel.accept(visitor);

                    long startTime = System.currentTimeMillis();
                    List<ArtifactSummary> artifacts = visitor.query(args);
                    long totalSize = visitor.getTotalSize();
                    long endTime = System.currentTimeMillis();

                    LOG.debug(Messages.i18n.format("QUERY_EXECUTED_IN", endTime - startTime));

                    return new PagedResult<>(artifacts, xpathTemplate, totalSize, args);
                }
            }.execute();

		} catch (ArtificerException e) {
            throw e;
		} catch (Throwable t) {
			throw new QueryExecutionException(t);
		}
	}

}
