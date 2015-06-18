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
package org.artificer.client.query;

import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.query.ArtifactSummary;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

import java.util.Calendar;
import java.util.Iterator;

/**
 * An instance of this class is returned by the Atom API client when consumers
 * call the query methods.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryResultSet implements Iterable<ArtifactSummary> {

	private Feed currentFeed;

	/**
	 * Constructor.
	 * @param feed
	 */
	public QueryResultSet(Feed feed) {
		this.currentFeed = feed;
	}

	/**
	 * Returns the number of artifacts that matched the query.
	 */
	public long size() {
		return this.currentFeed.getEntries().size();
	}

	/**
	 * Returns the total results matched by the query.
	 *
	 * Returns -1 if this feature is not supported by the server.
	 */
	public long getTotalResults() {
        Object totalResultsAttr = this.currentFeed.getExtensionAttributes().get(ArtificerConstants.SRAMP_TOTAL_RESULTS_QNAME);
        if (totalResultsAttr != null) {
            return Long.parseLong((String) totalResultsAttr);
        } else {
            return -1;
        }
	}

    /**
     * Returns the number of items per page in the result set, even if the
     * total number of items returned is less.  This represents the number
     * of results *requested*, not returned.  So if the result set represents
     * the final page, then the total number of items in the collection might
     * be less than this number.  This represents the maximum number that
     * could be contained in the collection, however.
     *
     * Returns -1 if this feature is not supported by the server.
     */
	public int getItemsPerPage() {
        Object itemsPerPageAttr = this.currentFeed.getExtensionAttributes().get(ArtificerConstants.SRAMP_ITEMS_PER_PAGE_QNAME);
        if (itemsPerPageAttr != null) {
            return Integer.parseInt((String) itemsPerPageAttr);
        } else {
            return -1;
        }
	}

    /**
     * Returns the starting index for the result set.  This is the index
     * within the overall result set of the first item in the collection.
     *
     * Returns -1 if this feature is not supported by the server.
     */
	public int getStartIndex() {
        Object startIndexAttr = this.currentFeed.getExtensionAttributes().get(ArtificerConstants.SRAMP_START_INDEX_QNAME);
        if (startIndexAttr != null) {
            return Integer.parseInt((String) startIndexAttr);
        } else {
            return -1;
        }
	}

	/**
	 * Gets an item at the given index.
	 * @param index
	 */
	public ArtifactSummary get(int index) {
		if (index >= currentFeed.getEntries().size()) {
			return null;
		} else {
			return summary(currentFeed.getEntries().get(index));
		}
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<ArtifactSummary> iterator() {
		return new DelegatingIterator(currentFeed.getEntries().iterator());
	}


	/**
	 * Delegates iteration to an underlying impl, simply converts the objects
	 * into the correct type.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	private static class DelegatingIterator implements Iterator<ArtifactSummary> {

		private Iterator<Entry> delegate;

		/**
		 * Constructor.
		 * @param delegate
		 */
		public DelegatingIterator(Iterator<Entry> delegate) {
			this.delegate = delegate;
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return delegate.hasNext();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public ArtifactSummary next() {
			return summary(delegate.next());
        }

        /**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

    private static ArtifactSummary summary(Entry entry) {
        ArtifactSummary summary = new ArtifactSummary();
        summary.setUuid(entry.getId().toString().replace("urn:uuid:", ""));
        summary.setName(entry.getTitle());
        summary.setDescription(entry.getSummary());
        ArtifactType artifactType = ArtificerAtomUtils.getArtifactType(entry);
        summary.setModel(artifactType.getModel());
        summary.setType(artifactType.getType());
        summary.setDerived(artifactType.isDerived());
        summary.setExtensionAttributes(entry.getExtensionAttributes());
        Calendar published = Calendar.getInstance();
        published.setTime(entry.getPublished());
        summary.setCreatedTimestamp(published);
        summary.setCreatedBy(entry.getAuthors().get(0).getName());
        Calendar updated = Calendar.getInstance();
        published.setTime(entry.getUpdated());
        summary.setLastModifiedTimestamp(updated);
        return summary;
    }
}
