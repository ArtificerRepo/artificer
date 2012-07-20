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

import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.query.QueryResult;

import org.modeshape.jcr.JcrRepository.QueryLanguage;
import org.overlord.sramp.query.xpath.ast.Query;
import org.overlord.sramp.repository.jcr.JCRRepository;
import org.overlord.sramp.repository.query.AbstractSrampQueryImpl;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.QueryExecutionException;
import org.overlord.sramp.repository.query.SrampQuery;

/**
 * A JCR implementation of an s-ramp query ({@link SrampQuery}).
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRSrampQuery extends AbstractSrampQueryImpl {

	/**
	 * Constructor.
	 * @param xpathTemplate
	 */
	public JCRSrampQuery(String xpathTemplate) {
		super(xpathTemplate);
	}

	/**
	 * @see org.overlord.sramp.repository.query.AbstractSrampQueryImpl#executeQuery(org.overlord.sramp.query.xpath.ast.Query)
	 */
	@Override
	protected ArtifactSet executeQuery(Query queryModel) throws QueryExecutionException {
        Session session = null;
        try {
            session = JCRRepository.getSession();
            javax.jcr.query.QueryManager jcrQueryManager = session.getWorkspace().getQueryManager();
            String jcrSql2Query = createSql2Query(queryModel);
            javax.jcr.query.Query jcrQuery = jcrQueryManager.createQuery(jcrSql2Query, QueryLanguage.JCR_SQL2);
            QueryResult jcrQueryResult = jcrQuery.execute();
            NodeIterator jcrNodes = jcrQueryResult.getNodes();
            return new JCRArtifactSet(session, jcrNodes);
        } catch (Throwable t) {
            session.logout();
        	throw new QueryExecutionException(t);
        }
	}

	/**
	 * Visits the S-RAMP query AST/model and produces a functionally equivalent JCR SQL-2 query.
	 * @param queryModel the s-ramp query
	 */
	private String createSql2Query(Query queryModel) {
		SrampToJcrSql2QueryVisitor visitor = new SrampToJcrSql2QueryVisitor();
		queryModel.accept(visitor);
		return visitor.getSql2Query();
	}
	
}
