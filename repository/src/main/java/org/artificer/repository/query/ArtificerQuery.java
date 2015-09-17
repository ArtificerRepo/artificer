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
package org.artificer.repository.query;

import org.artificer.common.ArtificerException;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.repository.QueryManager;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

/**
 * An S-RAMP Query, created by the {@link QueryManager} from an xpath template.  The
 * xpath template is of the same form as a typical JDBC statement (using the ? character
 * for replacements).  Following are same example usages:
 * <br/>
 * <br/>
 * <pre>
 *   QueryManager queryManager = getQueryManager(); // get the query manager
 *   SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@prop1 = ?]");
 *   query.setString("value1");
 *   ArtifactSet results = query.executeQuery();
 * </pre>
 * <br/>
 * <pre>
 *   QueryManager queryManager = getQueryManager(); // get the query manager
 *   SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@name = ? and @length > ?]");
 *   query.setString("value1"); // Set the value of the first ? as a quoted string literal
 *   query.setInteger(20);  // Set the value of the second ? as a number
 *   ArtifactSet results = query.executeQuery();
 * </pre>
 *
 * @author eric.wittmann@redhat.com
 */
public interface ArtificerQuery {

	/**
	 * Sets the value of the next replacement as a quoted string literal.
	 * @param paramValue the replacement parameter value
	 */
	public void setString(String paramValue);

    /**
     * Sets the value of the next replacement as a Date (short Date).
     * @param date
     */
    public void setDate(Date date);

    /**
     * Sets the value of the next replacement as a Calendar (DateTime).
     * @param date
     */
    public void setDateTime(Calendar date);

	/**
	 * Sets the value of the next replacement as a number literal.
	 * @param paramValue the replacement parameter value
	 */
	public void setNumber(int paramValue);

	/**
	 * Sets the value of the next replacement as a number literal.
	 * @param paramValue the replacement parameter value
	 */
	public void setNumber(long paramValue);

	/**
	 * Sets the value of the next replacement as a number literal.
	 * @param paramValue the replacement parameter value
	 */
	public void setNumber(float paramValue);

	/**
	 * Sets the value of the next replacement as a number literal.
	 * @param paramValue the replacement parameter value
	 */
	public void setNumber(double paramValue);

	/**
	 * Sets the value of the next replacement as a number literal.
	 * @param paramValue the replacement parameter value
	 */
	public void setNumber(BigInteger paramValue);

	/**
	 * Once all of the replacements are set, this will execute the query
	 * and return a set of s-ramp artifacts.
	 * @return a set of s-ramp artifacts
	 */
	public PagedResult<ArtifactSummary> executeQuery() throws ArtificerException;

}
