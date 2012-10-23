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
package org.overlord.sramp.client.query;

import java.util.Iterator;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

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
	 * Gets an item at the given index.
	 * @param index
	 */
	public ArtifactSummary get(int index) {
		if (index >= currentFeed.getEntries().size()) {
			return null;
		} else {
			return new ArtifactSummary(currentFeed.getEntries().get(index));
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
			return new ArtifactSummary(delegate.next());
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
}
