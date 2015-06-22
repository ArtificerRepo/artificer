/*
 * Copyright 2011 JBoss Inc
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
package org.artificer.server.atom.services;

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.MediaType;
import org.artificer.common.query.ArtifactSummary;
import org.artificer.repository.query.PagedResult;
import org.artificer.server.QueryServiceImpl;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Set;
import java.util.UUID;


/**
 * Base class for all resources that respond with Atom Feeds.
 */
public abstract class AbstractFeedResource extends AbstractResource {

	private static Logger logger = LoggerFactory.getLogger(AbstractFeedResource.class);

	protected final QueryServiceImpl queryService = new QueryServiceImpl();

	/**
	 * Common method that performs a query for artifacts and returns them in an Atom {@link Feed}.
	 * @param query the x-path formatted s-ramp query
	 * @param startIndex which index within the result set to start with (0 indexed)
	 * @param count the number of items desired
	 * @param orderBy the property to sort the results by
	 * @param ascending the sort direction
	 * @param propNames the set of s-ramp property names - the extra properties that the query should return as part of the {@link Feed}
	 * @return an Atom {@link Feed}
	 * @throws org.artificer.atom.err.ArtificerAtomException
	 */
	protected Feed createArtifactFeed(String query, Integer startPage, Integer startIndex, Integer count, String orderBy,
			Boolean ascending, Set<String> propNames, String baseUrl) throws ArtificerAtomException {
	    if (query == null)
            throw new ArtificerAtomException(Messages.i18n.format("MISSING_QUERY_PARAM"));

		try {
            PagedResult<ArtifactSummary> artifactSet = queryService.query(
                    query, startPage, startIndex, count, orderBy, ascending);
            Feed feed = createFeed(artifactSet, propNames, baseUrl);
			addPaginationLinks(feed, artifactSet, baseUrl);
			return feed;
		} catch (Throwable e) {
			logError(logger, Messages.i18n.format("Error trying to create an Artifact Feed."), e);
			throw new ArtificerAtomException(e);
		}
	}

	/**
	 * Creates the Atom {@link Feed} from the given artifact set (query result set).
	 *
	 * Note: the Atom feed format allows pagination via the following links:
	 *
	 * <pre>
	 *   <link rel="first" href="http://www.example.org/feed"/>
	 *   <link rel="next" href="http://www.example.org/feed?page=4"/>
	 *   <link rel="previous" href="http://www.example.org/feed?page=2"/>
	 *   <link rel="last" href="http://www.example.org/feed?page=147"/>
	 * </pre>
	 *
     * @param pagedResult
	 * @param propNames the additional s-ramp properties to return in the {@link Feed}
	 * @return an Atom {@link Feed}
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
    private Feed createFeed(PagedResult<ArtifactSummary> pagedResult, Set<String> propNames, String baseUrl) throws Exception {
		Feed feed = new Feed();
		feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_PROVIDER_QNAME, "Artificer");
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_ITEMS_PER_PAGE_QNAME, String.valueOf(pagedResult.getPageSize()));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_START_INDEX_QNAME, String.valueOf(pagedResult.getStartIndex()));
        feed.getExtensionAttributes().put(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME, String.valueOf(pagedResult.getTotalSize()));
		feed.setId(new URI("urn:uuid:" + UUID.randomUUID().toString()));
		feed.setTitle("S-RAMP Feed");
		feed.setSubtitle("Ad Hoc query feed");
		feed.setUpdated(new Date());
		feed.getAuthors().add(new Person("anonymous"));

        for (ArtifactSummary artifact : pagedResult.getResults()) {
			Entry entry = ArtificerAtomUtils.wrapArtifactSummary(artifact);
			feed.getEntries().add(entry);
		}

		return feed;
	}

	/**
	 * Add pagination links to the feed.
	 *
	 * @param feed
     * @param pagedResult
	 * @param baseUrl
	 * @throws UnsupportedEncodingException
	 */
	private void addPaginationLinks(Feed feed, PagedResult<ArtifactSummary> pagedResult, String baseUrl)
            throws UnsupportedEncodingException {
        long pageSize = pagedResult.getPageSize();
        int startIndex = pagedResult.getStartIndex();
        String orderBy = pagedResult.getOrderBy();
        boolean ascending = pagedResult.isAscending();
        String hrefPattern = "%1$s?query=%2$s&startIndex=%3$s&pageSize=%4$s&orderBy=%5$s&ascending=%6$s";
		String encodedQuery = URLEncoder.encode(pagedResult.getQuery(), "UTF-8");
		String firstHref = String.format(hrefPattern, baseUrl, encodedQuery, 0, String.valueOf(pageSize),
				String.valueOf(orderBy), String.valueOf(ascending));
		long prevIndex = Math.max(0,  startIndex - pageSize);
		String prevHref = String.format(hrefPattern, baseUrl, encodedQuery, prevIndex, String.valueOf(pageSize),
				String.valueOf(orderBy), String.valueOf(ascending));
		String nextHref = String.format(hrefPattern, baseUrl, encodedQuery, startIndex + pageSize, String.valueOf(pageSize),
				String.valueOf(orderBy), String.valueOf(ascending));

		Link first = new Link("first", firstHref, MediaType.APPLICATION_ATOM_XML_FEED_TYPE);
		Link prev = new Link("prev", prevHref, MediaType.APPLICATION_ATOM_XML_FEED_TYPE);
		Link next = new Link("next", nextHref, MediaType.APPLICATION_ATOM_XML_FEED_TYPE);

		if (startIndex > 0) {
			feed.getLinks().add(first);
			feed.getLinks().add(prev);
		}
		if (startIndex + pageSize < pagedResult.getTotalSize()) {
			feed.getLinks().add(next);
		}

	}

}
