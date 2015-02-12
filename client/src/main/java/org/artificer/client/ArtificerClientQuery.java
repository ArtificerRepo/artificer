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
package org.artificer.client;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.artificer.client.query.QueryResultSet;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.client.i18n.Messages;


/**
 * An S-RAMP Query, created by the {@link ArtificerAtomApiClient} from an xpath template.  The
 * xpath template is of the same form as a typical JDBC statement (using the ? character
 * for replacements).  Following are same example usages:
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerClientQuery {

    private ArtificerAtomApiClient client;
    private String queryTemplate;
    private List<String> replacementParams = new ArrayList<String>();
    private int startIndex = 0;
    private int count = 20;
    private String orderBy = "name"; //$NON-NLS-1$
    private boolean ascending = true;
    private Set<String> propertyNames = new HashSet<String>();

    /**
     * Constructor.
     * @param client
     * @param queryTemplate
     */
    protected ArtificerClientQuery(ArtificerAtomApiClient client, String queryTemplate) {
        this.client = client;
        this.queryTemplate = queryTemplate;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.
     * @param param
     */
    public ArtificerClientQuery parameter(String param) {
        replacementParams.add("'" + param.replace("'", "''") + "'"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        return this;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.
     * @param param
     */
    public ArtificerClientQuery parameter(int param) {
        replacementParams.add(String.valueOf(param));
        return this;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.
     * @param param
     */
    public ArtificerClientQuery parameter(long param) {
        replacementParams.add(String.valueOf(param));
        return this;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.
     * @param param
     */
    public ArtificerClientQuery parameter(double param) {
        replacementParams.add(String.valueOf(param));
        return this;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.  Note: this will add a date to the query.  In
     * order to be more precise and send a full DateTime, use the Calendar
     * form of this method:  <code>parameter(Calendar param)</code>
     * @param param
     */
    public ArtificerClientQuery parameter(Date param) {
        String val = ISODateTimeFormat.date().print(new DateTime(param));
        replacementParams.add("'" + val + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        return this;
    }

    /**
     * Sets a parameter on the query.  This should match up to a ? in the
     * query template provided.  note: this will add a DateTime to the query.
     * @param param
     */
    public ArtificerClientQuery parameter(Calendar param) {
        String val = ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(param));
        replacementParams.add("'" + val + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        return this;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.
     * @param param
     */
    public ArtificerClientQuery parameter(float param) {
        replacementParams.add(String.valueOf(param));
        return this;
    }

    /**
     * Sets a parameter on the query - this should match up to a ? in the
     * query template provided.
     * @param param
     */
    public ArtificerClientQuery parameter(Number param) {
        replacementParams.add(param.toString());
        return this;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public ArtificerClientQuery startIndex(int startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * @param count the count to set
     */
    public ArtificerClientQuery count(int count) {
        this.count = count;
        return this;
    }

    /**
     * @param orderBy the orderBy to set
     */
    public ArtificerClientQuery orderBy(String orderBy) {
        this.orderBy = orderBy;
        return this;
    }

    /**
     * Sets ascending to true.
     */
    public ArtificerClientQuery ascending() {
        this.ascending = true;
        return this;
    }

    /**
     * Sets ascending to false.
     */
    public ArtificerClientQuery descending() {
        this.ascending = false;
        return this;
    }

    /**
     * @param propertyName property name to include in the result
     */
    public ArtificerClientQuery propertyName(String propertyName) {
        this.propertyNames.add(propertyName);
        return this;
    }

    /**
     * Formats the query given the replacement params, then issues the query
     * to the S-RAMP repository and returns the result.
     * @throws org.artificer.atom.err.ArtificerAtomException
     * @throws ArtificerClientException
     */
    public QueryResultSet query() throws ArtificerClientException, ArtificerAtomException {
        String query = formatQuery();
        return client.query(query, startIndex, count, orderBy, ascending, propertyNames);
    }

    /**
     * Formats the query by replacing all ? characters with their replacement
     * parameters.
     * @throws ArtificerClientException
     */
    private String formatQuery() throws ArtificerClientException {
        StringBuilder builder = new StringBuilder();
        String [] xpathSegments = queryTemplate.split("\\?"); //$NON-NLS-1$
        int paramCounter = 0;
        for (String segment : xpathSegments) {
            builder.append(segment);
            boolean isLastSegment = segment == xpathSegments[xpathSegments.length - 1];
            if (!isLastSegment) {
                if (paramCounter >= replacementParams.size())
                    throw new ArtificerClientException(Messages.i18n.format("TOO_FEW_QUERY_PARAMS")); //$NON-NLS-1$
                String param = replacementParams.get(paramCounter);
                builder.append(param);
                paramCounter++;
            }
        }
        if (replacementParams.size() > paramCounter)
            throw new ArtificerClientException(Messages.i18n.format("TOO_MANY_QUERY_PARAMS")); //$NON-NLS-1$

        return builder.toString();
    }

}
