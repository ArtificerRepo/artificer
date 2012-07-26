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

	/**
	 * Constructor.
	 */
	public BrowsePlace() {
		this(null);
	}
	
	/**
	 * Constructor.
	 * @param typeFilter
	 */
	public BrowsePlace(String typeFilter) {
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
			return PlaceUtils.createPlaceToken("tf", place.getTypeFilter());
		}

		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
		 */
		@Override
		public BrowsePlace getPlace(String token) {
			Map<String, String> params = PlaceUtils.parsePlaceToken(token);
			String tf = params.get("tf");
			return new BrowsePlace(tf);
		}
	}
}