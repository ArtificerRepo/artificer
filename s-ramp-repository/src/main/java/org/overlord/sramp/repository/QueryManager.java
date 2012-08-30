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
package org.overlord.sramp.repository;

import org.overlord.sramp.repository.query.SrampQuery;

/**
 * Service used to query the repository using the s-ramp query api.
 *
 * @author eric.wittmann@redhat.com
 */
public interface QueryManager {

	/**
	 * Create an s-ramp query from the given xpath template.  This template can
	 * have replacements using the familiar ? syntax used by JDBC.  For example,
	 * the xpath might be something like:</br>
	 * <br/>
	 * <code>/s-ramp/xsd/XsdDocument[@prop1 = ?]</code>
	 * <br/>
	 * <br/>
	 * This method will create the {@link SrampQuery} object, which can then be 
	 * used to bind real values to the template and finally execute the query. An
	 * example illustrates the idea:<br/>
	 * <br/>
	 * <pre>
	 *   QueryManager queryManager = getQueryManager(); // get the query manager
	 *   SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop1 = ?]");
	 *   query.setString("value1");
	 *   ArtifactSet results = query.executeQuery();
	 * </pre>
	 * @param xpathTemplate the templatized xpath
	 * @param orderByProperty property name to use when sorting
	 * @param orderAscending whether to sort ascending
	 * @return a new {@link SrampQuery} object
	 * @throws RepositoryException
	 */
	public SrampQuery createQuery(String xpathTemplate, String orderByProperty, boolean orderAscending) throws RepositoryException;

	/**
	 * Create an s-ramp query from the given xpath template.  No order-by hints are given,
	 * so the s-ramp repository is free to return the artifacts in any arbitrary order.
	 * @param xpathTemplate the templatized xpath
	 * @return a new {@link SrampQuery} object
	 * @throws RepositoryException
	 */
	public SrampQuery createQuery(String xpathTemplate) throws RepositoryException;

}
