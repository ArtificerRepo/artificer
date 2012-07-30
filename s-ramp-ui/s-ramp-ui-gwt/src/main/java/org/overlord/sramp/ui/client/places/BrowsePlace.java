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

import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Place:  /dashboard/browse
 * 
 * @author eric.wittmann@redhat.com
 */
public class BrowsePlace extends AbstractPagedPlace {
	
	private String typeFilter;

	/**
	 * Constructor.
	 */
	public BrowsePlace() {
		this(null, null, null);
	}
	
	/**
	 * Constructor.
	 * @param page
	 * @param pageSize
	 * @param typeFilter
	 */
	public BrowsePlace(Integer page, Integer pageSize, String typeFilter) {
		super(page, pageSize);
		setTypeFilter(typeFilter);
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

	/*
	 * Tokenizer.
	 */
	public static class Tokenizer implements PlaceTokenizer<BrowsePlace> {
		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
		 */
		@Override
		public String getToken(BrowsePlace place) {
			return PlaceUtils.createPlaceToken(
					"tf", place.getTypeFilter(),
					"p", place.getPage(),
					"ps", place.getPageSize() );
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
			Integer page = null, pageSize = null;
			if (p != null)
				page = new Integer(p);
			if (ps != null)
				pageSize = new Integer(ps);
			
			return new BrowsePlace(page, pageSize, typeFilter);
		}
	}
}