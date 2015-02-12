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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.artificer.repository.error.InvalidQueryException;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.artificer.common.ArtificerException;
import org.artificer.common.query.xpath.XPathParser;
import org.artificer.common.query.xpath.ast.Query;
import org.artificer.repository.i18n.Messages;

/**
 * A base class for concrete implementations of the {@link ArtificerQuery} interface.  This
 * base class does a lot of the common work, such as managing the replacement of params
 * in the xpath template.  It frees up individual providers to focus on the
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractArtificerQueryImpl implements ArtificerQuery {

	private static final XPathParser sParser = new XPathParser();

	private String xpathTemplate;
	private List<QueryReplacementParam<?>> replacementParams = new ArrayList<QueryReplacementParam<?>>();
	private String orderByProperty;
	private boolean orderAscending;

	/**
	 * Constructor.
	 * @param xpathTemplate
	 * @param orderByProperty
	 * @param orderAscending
	 */
	public AbstractArtificerQueryImpl(String xpathTemplate, String orderByProperty, boolean orderAscending) {
		setXpathTemplate(xpathTemplate);
		setOrderByProperty(orderByProperty);
		setOrderAscending(orderAscending);
	}

	/**
	 * @return the xpathTemplate
	 */
	protected String getXpathTemplate() {
		return xpathTemplate;
	}

	/**
	 * @param xpathTemplate the xpathTemplate to set
	 */
	protected void setXpathTemplate(String xpathTemplate) {
		this.xpathTemplate = xpathTemplate;
	}

	/**
	 * @return the orderByProperty
	 */
	protected String getOrderByProperty() {
		return orderByProperty;
	}

	/**
	 * @param orderByProperty the orderByProperty to set
	 */
	protected void setOrderByProperty(String orderByProperty) {
		this.orderByProperty = orderByProperty;
	}

	/**
	 * @return the orderAscending
	 */
	protected boolean isOrderAscending() {
		return orderAscending;
	}

	/**
	 * @param orderAscending the orderAscending to set
	 */
	protected void setOrderAscending(boolean orderAscending) {
		this.orderAscending = orderAscending;
	}

	@Override
	public void setString(String paramValue) {
		this.replacementParams.add(new StringReplacementParam(paramValue));
	}

	@Override
	public void setDate(Date date) {
        String val = ISODateTimeFormat.date().print(new DateTime(date));
        this.replacementParams.add(new StringReplacementParam(val));
	}

	@Override
	public void setDateTime(Calendar date) {
        String val = ISODateTimeFormat.dateTimeNoMillis().print(new DateTime(date));
        this.replacementParams.add(new StringReplacementParam(val));
	}

	@Override
	public void setNumber(int paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	@Override
	public void setNumber(long paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	@Override
	public void setNumber(float paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	@Override
	public void setNumber(double paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	@Override
	public void setNumber(BigInteger paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	@Override
	public final ArtifactSet executeQuery() throws ArtificerException {
		String xpathTemplate = getXpathTemplate();
		String xpath = formatQuery(xpathTemplate, this.replacementParams);
		Query queryModel = parseXPath(xpath);
		validateQuery(queryModel);
		return executeQuery(queryModel);
	}

	/**
	 * Formats an xpath template into full xpath using the given list of replacement params.
	 * @param xpathTemplate the xpath template (with ?'s)
	 * @param replacementParams replacements for the template's ?'s
	 * @return final xpath used to query the s-ramp repository
	 */
	protected static final String formatQuery(String xpathTemplate, List<QueryReplacementParam<?>> replacementParams)
			throws InvalidQueryException {
		StringBuilder builder = new StringBuilder();
		String [] xpathSegments = xpathTemplate.split("\\?"); //$NON-NLS-1$
		int paramCounter = 0;
		for (String segment : xpathSegments) {
			builder.append(segment);
			boolean isLastSegment = segment == xpathSegments[xpathSegments.length - 1];
			if (!isLastSegment) {
				if (paramCounter >= replacementParams.size())
					throw new InvalidQueryException(Messages.i18n.format("TOO_FEW_QUERY_PARAMS")); //$NON-NLS-1$
				QueryReplacementParam<?> param = replacementParams.get(paramCounter);
				builder.append(param.getFormattedValue());
				paramCounter++;
			}
		}
		if (replacementParams.size() > paramCounter)
			throw new InvalidQueryException(Messages.i18n.format("TOO_MANY_QUERY_PARAMS")); //$NON-NLS-1$

		return builder.toString();
	}

	/**
	 * Parse the given xpath into an AST.
	 * @param xpath an s-ramp xpath query
	 * @return a {@link Query}
	 * @throws InvalidQueryException
	 */
	protected static final Query parseXPath(String xpath) throws InvalidQueryException {
		try {
			return sParser.parseXPath(xpath);
		} catch (Throwable e) {
			throw new InvalidQueryException(Messages.i18n.format("QUERY_PARSE_FAILED"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Perform some static validation of the s-ramp query.
	 * @param queryModel the parsed s-ramp query model
	 * @throws InvalidQueryException
	 */
	protected void validateQuery(Query queryModel) throws InvalidQueryException {
		// TODO static validation of the query goes here
	}

	/**
	 * Executes the s-ramp xpath query, returning a set of artifacts.
	 * @param queryModel the s-ramp query model
	 * @return a set of s-ramp artifacts
	 * @throws org.artificer.common.ArtificerException
	 */
	protected abstract ArtifactSet executeQuery(Query queryModel) throws ArtificerException;
}
