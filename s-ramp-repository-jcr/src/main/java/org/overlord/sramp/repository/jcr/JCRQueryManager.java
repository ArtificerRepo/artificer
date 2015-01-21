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
package org.overlord.sramp.repository.jcr;

import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.jcr.query.JCRSrampQuery;
import org.overlord.sramp.repository.query.SrampQuery;

/**
 * An implementation of the {@link QueryManager} using JCR.  Works along with the
 * JCR PersistenceManager implementation ({@link JCRPersistence}).
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRQueryManager extends JCRAbstractManager implements QueryManager {

	/**
	 * Default constructor.
	 */
	public JCRQueryManager() {
	}

	/**
	 * @see org.overlord.sramp.common.repository.QueryManager#createQuery(java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public SrampQuery createQuery(String xpathTemplate, String orderByProperty, boolean orderAscending) {
		return new JCRSrampQuery(xpathTemplate, orderByProperty, orderAscending);
	}

	/**
	 * @see org.overlord.sramp.common.repository.QueryManager#createQuery(java.lang.String)
	 */
	@Override
	public SrampQuery createQuery(String xpathTemplate) {
		return createQuery(xpathTemplate, null, false);
	}

}
