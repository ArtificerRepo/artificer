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
package org.overlord.sramp.ui.client.places;

import java.util.Map;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Place:  /dashboard/browse
 * 
 * @author eric.wittmann@redhat.com
 */
public class BrowsePlace extends Place {
	
	private String typeFilter;
	private int page;
	private int pageSize;

	/**
	 * Constructor.
	 */
	public BrowsePlace() {
		this(-1, -1, null);
	}
	
	/**
	 * Constructor.
	 * @param page
	 * @param pageSize
	 * @param typeFilter
	 */
	public BrowsePlace(int page, int pageSize, String typeFilter) {
		setTypeFilter(typeFilter);
		setPage(page);
		setPageSize(pageSize);
	}

	/**
	 * @return the typeFilter
	 */
	public String getTypeFilter() {
		return typeFilter;
	}

	/**
	 * @param typeFilter the typeFilter to set
	 */
	public void setTypeFilter(String typeFilter) {
		this.typeFilter = typeFilter;
	}

	/**
	 * @return the page
	 */
	public int getPage() {
		return page;
	}

	/**
	 * @return the page
	 */
	public int getPage(int defaultPage) {
		if (page == -1)
			return defaultPage;
		return page;
	}

	/**
	 * @param page the page to set
	 */
	public void setPage(int page) {
		this.page = page;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize(int defaultPageSize) {
		if (pageSize == -1)
			return defaultPageSize;
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	/*
	 * Tokenizer.
	 */
	public static class Tokenizer implements PlaceTokenizer<BrowsePlace> {
		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
		 */
		@Override
		public String getToken(BrowsePlace place) {
			Integer page = null;
			Integer pageSize = null;
			if (place.getPage() != -1)
				page = place.getPage();
			if (place.getPageSize() != -1)
				pageSize = place.getPageSize();
			return PlaceUtils.createPlaceToken(
					"tf", place.getTypeFilter(),
					"p", page,
					"ps", pageSize );
		}

		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
		 */
		@Override
		public BrowsePlace getPlace(String token) {
			Map<String, String> params = PlaceUtils.parsePlaceToken(token);
			String typeFilter = params.get("tf");
			String p = params.get("p");
			String ps = params.get("ps");
			Integer page = -1, pageSize = -1;
			if (p != null)
				page = new Integer(p);
			if (ps != null)
				pageSize = new Integer(ps);
			
			return new BrowsePlace(page, pageSize, typeFilter);
		}
	}
}