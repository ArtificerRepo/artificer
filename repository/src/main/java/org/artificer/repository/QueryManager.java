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
package org.artificer.repository;

import org.artificer.common.ArtificerException;
import org.artificer.common.ReverseRelationship;
import org.artificer.repository.query.ArtificerQuery;
import org.artificer.repository.query.ArtificerQueryArgs;

import java.util.List;

/**
 * Service used to query the repository using the s-ramp query api.
 *
 * @author eric.wittmann@redhat.com
 */
public interface QueryManager extends AbstractManager {

	/**
	 * Create an s-ramp query from the given xpath template.  This template can
	 * have replacements using the familiar ? syntax used by JDBC.  For example,
	 * the xpath might be something like:</br>
	 * <br/>
	 * <code>/s-ramp/xsd/XsdDocument[@prop1 = ?]</code>
	 * <br/>
	 * <br/>
	 * This method will create the {@link org.artificer.repository.query.ArtificerQuery} object, which can then be
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
	 * @param args
	 * @return a new {@link org.artificer.repository.query.ArtificerQuery} object
	 * @throws org.artificer.common.ArtificerException
	 */
	public ArtificerQuery createQuery(String xpathTemplate, ArtificerQueryArgs args) throws ArtificerException;

    public ArtificerQuery createQuery(String xpathTemplate) throws ArtificerException;

    /**
     * Return all artifacts and relationships that target the given artifact UUID.
     * @param uuid
     * @returnList<ReverseRelationship>
     * @throws ArtificerException
     */
    public List<ReverseRelationship> reverseRelationships(String uuid) throws ArtificerException;

}
