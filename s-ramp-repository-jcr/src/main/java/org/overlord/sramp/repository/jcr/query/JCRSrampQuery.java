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
package org.overlord.sramp.repository.jcr.query;

import java.util.HashMap;
import java.util.Map;

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.query.xpath.ast.Query;
import org.overlord.sramp.common.query.xpath.visitors.XPathSerializationVisitor;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.jcr.ClassificationHelper;
import org.overlord.sramp.repository.jcr.JCRConstants;
import org.overlord.sramp.repository.jcr.JCRPersistence;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.overlord.sramp.repository.query.AbstractSrampQueryImpl;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.QueryExecutionException;
import org.overlord.sramp.repository.query.SrampQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JCR implementation of an s-ramp query ({@link SrampQuery}).
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRSrampQuery extends AbstractSrampQueryImpl {

	private static Logger log = LoggerFactory.getLogger(JCRPersistence.class);

	private static Map<String, String> sOrderByMappings = new HashMap<String, String>();
	static {
		sOrderByMappings.put("createdBy", "jcr:createdBy"); //$NON-NLS-1$ //$NON-NLS-2$
		sOrderByMappings.put("version", "version"); //$NON-NLS-1$ //$NON-NLS-2$
		sOrderByMappings.put("uuid", "sramp:uuid"); //$NON-NLS-1$ //$NON-NLS-2$
		sOrderByMappings.put("createdTimestamp", "jcr:created"); //$NON-NLS-1$ //$NON-NLS-2$
		sOrderByMappings.put("lastModifiedTimestamp", "jcr:lastModified"); //$NON-NLS-1$ //$NON-NLS-2$
		sOrderByMappings.put("lastModifiedBy", "jcr:lastModifiedBy"); //$NON-NLS-1$ //$NON-NLS-2$
		sOrderByMappings.put("name", "sramp:name"); //$NON-NLS-1$ //$NON-NLS-2$
	}

    private Session session;

	/**
	 * Constructor.
	 * @param xpathTemplate
	 * @param orderByProperty
	 * @param orderAscending
	 */
	public JCRSrampQuery(String xpathTemplate, String orderByProperty, boolean orderAscending) {
		super(xpathTemplate, orderByProperty, orderAscending);
	}

	/**
	 * @see org.overlord.sramp.common.repository.query.AbstractSrampQueryImpl#executeQuery(org.overlord.sramp.common.query.xpath.ast.Query)
	 */
	@Override
	protected ArtifactSet executeQuery(Query queryModel) throws SrampException {
		Session session = null;
        boolean logoutOnClose = true;
		try {
		    if (this.session != null) {
		        session = this.session;
		        logoutOnClose = false;
		    } else {
		        session = JCRRepositoryFactory.getSession();
                logoutOnClose = true;
		    }
			javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
			String jcrSql2Query = createSql2Query(queryModel);
			if (log.isDebugEnabled()) {
				XPathSerializationVisitor visitor = new XPathSerializationVisitor();
				queryModel.accept(visitor);
				String originalQuery = visitor.getXPath();
				System.out.println(Messages.i18n.format("JCR_QUERY_FROM", jcrSql2Query, originalQuery)); //$NON-NLS-1$
			}
			javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(jcrSql2Query, JCRConstants.JCR_SQL2);
			long startTime = System.currentTimeMillis();
			QueryResult jcrQueryResult = jcrQuery.execute();
			NodeIterator jcrNodes = jcrQueryResult.getNodes();
			long endTime = System.currentTimeMillis();

			log.debug(Messages.i18n.format("QUERY_EXECUTED", jcrSql2Query)); //$NON-NLS-1$
			log.debug(Messages.i18n.format("QUERY_EXECUTED_IN", endTime - startTime)); //$NON-NLS-1$

			return new JCRArtifactSet(session, jcrNodes, logoutOnClose);
		} catch (SrampException e) {
            // Only logout of the session on a throw.  Otherwise, the JCRArtifactSet will be
            // responsible for closing the session.
		    if (logoutOnClose)
		        JCRRepositoryFactory.logoutQuietly(session);
		    throw e;
		} catch (Throwable t) {
			// Only logout of the session on a throw.  Otherwise, the JCRArtifactSet will be
			// responsible for closing the session.
            if (logoutOnClose)
                JCRRepositoryFactory.logoutQuietly(session);
			throw new QueryExecutionException(t);
		}
	}

	/**
	 * Visits the S-RAMP query AST/model and produces a functionally equivalent JCR SQL-2 query.
	 * @param queryModel the s-ramp query
	 * @throws SrampException
	 */
	private String createSql2Query(Query queryModel) throws SrampException {
		String jcrOrderBy = null;
		if (getOrderByProperty() != null) {
			String jcrPropName = sOrderByMappings.get(getOrderByProperty());
			if (jcrPropName != null) {
				jcrOrderBy = jcrPropName;
			}
		}
		SrampToJcrSql2QueryVisitor visitor = new SrampToJcrSql2QueryVisitor((ClassificationHelper) PersistenceFactory.newInstance());
		queryModel.accept(visitor);
		String sql2Query = visitor.getSql2Query();
		String alias = visitor.getSelectAlias();
		if (jcrOrderBy != null) {
			sql2Query += " ORDER BY " + alias + ".[" + jcrOrderBy + "] " + (isOrderAscending() ? "ASC" : "DESC"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		}
		return sql2Query;
	}

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        this.session = session;
    }

}
