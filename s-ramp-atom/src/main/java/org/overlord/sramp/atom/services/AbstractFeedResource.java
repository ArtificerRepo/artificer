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
package org.overlord.sramp.atom.services;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.visitors.ArtifactToSummaryAtomEntryVisitor;
import org.overlord.sramp.repository.QueryManager;
import org.overlord.sramp.repository.QueryManagerFactory;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;
import org.overlord.sramp.visitors.ArtifactVisitorHelper;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;


/**
 * Base class for all resources that respond with Atom Feeds.
 */
public abstract class AbstractFeedResource {

	/**
	 * Constructor.
	 */
	protected AbstractFeedResource() {
	}

    /**
     * Common method that performs a query for artifacts and returns them in an Atom {@link Feed}.
     * @param query the x-path formatted s-ramp query
     * @param page which page in the results should be returned
     * @param pageSize the size of each page of results
     * @param orderBy the property to sort the results by
     * @param ascending the sort direction
     * @param propNames the set of s-ramp property names - the extra properties that the query should return as part of the {@link Feed}
     * @return an Atom {@link Feed}
     * @throws SrampAtomException
     */
	protected Feed createArtifactFeed(String query, Integer page, Integer pageSize, String orderBy,
			Boolean ascending, Set<String> propNames) throws SrampAtomException {
		if (page == null)
			page = 0;
		if (pageSize == null)
			pageSize = 20;
		if (orderBy == null)
			orderBy = "name";
		if (ascending == null)
			ascending = true;

		ArtifactSet artifactSet = null;
		try {
			QueryManager queryManager = QueryManagerFactory.newInstance();
			SrampQuery srampQuery = queryManager.createQuery(query, orderBy, ascending);
			artifactSet = srampQuery.executeQuery();
			int startIdx = page * pageSize;
			int endIdx = startIdx + pageSize - 1;
			Feed feed = createFeed(artifactSet, startIdx, endIdx, propNames);
			addPaginationLinks(feed, artifactSet, query, page, pageSize, orderBy, ascending);
			return feed;
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		} finally {
			if (artifactSet != null)
				artifactSet.close();
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
	 * @param artifactSet the set of artifacts that matched the query
	 * @param fromRow return rows starting at this index (inclusive)
	 * @param toRow return rows ending at this index (inclusive)
	 * @param propNames the additional s-ramp properties to return in the {@link Feed}
	 * @return an Atom {@link Feed}
	 * @throws Exception
	 */
	private Feed createFeed(ArtifactSet artifactSet, int fromRow, int toRow, Set<String> propNames) throws Exception {
		Feed feed = new Feed();
		feed.setId(new URI(UUID.randomUUID().toString()));
		feed.setTitle("S-RAMP Feed");
		feed.setSubtitle("Ad Hoc query feed");
		feed.setUpdated(new Date());
		feed.getAuthors().add(new Person("anonymous"));

		Iterator<BaseArtifactType> iterator = artifactSet.iterator();

		// Skip any initial rows
		for (int i = 0; i < fromRow; i++) {
			if (!iterator.hasNext())
				break;
			iterator.next();
		}

		// Now get only the rows we're interested in.
        ArtifactToSummaryAtomEntryVisitor visitor = new ArtifactToSummaryAtomEntryVisitor(propNames);
		for (int i = fromRow; i <= toRow; i++) {
			if (!iterator.hasNext())
				break;
            BaseArtifactType artifact = iterator.next();
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            Entry entry = visitor.getAtomEntry();
			feed.getEntries().add(entry);
			visitor.reset();
		}

		return feed;
	}

	/**
	 * Add pagination links to the feed.
	 *
	 * TODO use real URLs rather than hard-coded localhost:8080 values
	 *
	 * @param feed
	 * @param query
	 * @param artifactSet
	 * @param page
	 * @param pageSize
	 * @param orderBy
	 * @param ascending
	 * @throws UnsupportedEncodingException
	 */
	private void addPaginationLinks(Feed feed, ArtifactSet artifactSet, String query, int page, int pageSize,
			String orderBy, boolean ascending) throws UnsupportedEncodingException {
		String endpoint = "http://localhost:8080/s-ramp-atom/s-ramp";

		String hrefPattern = "%1$s?query=%2$s&page=%3$s&pageSize=%4$s&orderBy=%5$s&ascending=%6$s";
		String encodedQuery = URLEncoder.encode(query, "UTF-8");
		String firstHref = String.format(hrefPattern, endpoint, encodedQuery, 0, String.valueOf(pageSize),
				String.valueOf(orderBy), String.valueOf(ascending));
		String prevHref = String.format(hrefPattern, endpoint, encodedQuery, page - 1, String.valueOf(pageSize),
				String.valueOf(orderBy), String.valueOf(ascending));
		String nextHref = String.format(hrefPattern, endpoint, encodedQuery, page + 1, String.valueOf(pageSize),
				String.valueOf(orderBy), String.valueOf(ascending));

		Link first = new Link("first", firstHref, MediaType.APPLICATION_ATOM_XML_FEED_TYPE);
		Link prev = new Link("prev", prevHref, MediaType.APPLICATION_ATOM_XML_FEED_TYPE);
		Link next = new Link("next", nextHref, MediaType.APPLICATION_ATOM_XML_FEED_TYPE);

		if (page > 0) {
			feed.getLinks().add(first);
			feed.getLinks().add(prev);
		}
		if (artifactSet.iterator().hasNext()) {
			feed.getLinks().add(next);
		}

	}

}
