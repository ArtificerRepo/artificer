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
package org.overlord.sramp.repository.query;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.overlord.sramp.query.xpath.XPathParser;
import org.overlord.sramp.query.xpath.ast.Query;

/**
 * A base class for concrete implementations of the {@link SrampQuery} interface.  This
 * base class does a lot of the common work, such as managing the replacement of params
 * in the xpath template.  It frees up individual providers to focus on the 
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractSrampQueryImpl implements SrampQuery {

	private static final XPathParser sParser = new XPathParser();
	
	private String xpathTemplate;
	private List<QueryReplacementParam<?>> replacementParams = new ArrayList<QueryReplacementParam<?>>();
	
	/**
	 * Constructor.
	 * @param xpathTemplate
	 */
	public AbstractSrampQueryImpl(String xpathTemplate) {
		setXpathTemplate(xpathTemplate);
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
	 * @see org.overlord.sramp.repository.query.SrampQuery#setString(java.lang.String)
	 */
	@Override
	public void setString(String paramValue) {
		this.replacementParams.add(new StringReplacementParam(paramValue));
	}

	/**
	 * @see org.overlord.sramp.repository.query.SrampQuery#setNumber(int)
	 */
	@Override
	public void setNumber(int paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	/**
	 * @see org.overlord.sramp.repository.query.SrampQuery#setNumber(long)
	 */
	@Override
	public void setNumber(long paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	/**
	 * @see org.overlord.sramp.repository.query.SrampQuery#setNumber(float)
	 */
	@Override
	public void setNumber(float paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	/**
	 * @see org.overlord.sramp.repository.query.SrampQuery#setNumber(double)
	 */
	@Override
	public void setNumber(double paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	/**
	 * @see org.overlord.sramp.repository.query.SrampQuery#setNumber(java.math.BigInteger)
	 */
	@Override
	public void setNumber(BigInteger paramValue) {
		this.replacementParams.add(new NumberReplacementParam(paramValue));
	}

	/**
	 * @see org.overlord.sramp.repository.query.SrampQuery#executeQuery()
	 */
	@Override
	public final ArtifactSet executeQuery() throws InvalidQueryException, QueryExecutionException {
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
		String [] xpathSegments = xpathTemplate.split("\\?");
		int paramCounter = 0;
		for (String segment : xpathSegments) {
			builder.append(segment);
			boolean isLastSegment = segment == xpathSegments[xpathSegments.length - 1];
			if (!isLastSegment) {
				if (paramCounter >= replacementParams.size())
					throw new InvalidQueryException("Not enough query replacement parameters provided.");
				QueryReplacementParam<?> param = replacementParams.get(paramCounter);
				builder.append(param.getFormattedValue());
				paramCounter++;
			}
		}
		if (replacementParams.size() > paramCounter)
			throw new InvalidQueryException("Too many query replacement parameters provided.");
			
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
		} catch (Exception e) {
			throw new InvalidQueryException("Query failed to parse.", e);
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
	 */
	protected abstract ArtifactSet executeQuery(Query queryModel);
}
