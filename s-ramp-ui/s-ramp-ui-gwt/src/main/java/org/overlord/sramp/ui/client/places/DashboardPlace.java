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

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * Place:  /dashboard
 * 
 * This is also the default place.
 * 
 * @author eric.wittmann@redhat.com
 */
public class DashboardPlace extends Place {

	/**
	 * Constructor.
	 */
	public DashboardPlace() {
	}

	/*
	 * Tokenizer.
	 */
	public static class Tokenizer implements PlaceTokenizer<DashboardPlace> {
		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getToken(com.google.gwt.place.shared.Place)
		 */
		@Override
		public String getToken(DashboardPlace place) {
			return "";
		}

		/**
		 * @see com.google.gwt.place.shared.PlaceTokenizer#getPlace(java.lang.String)
		 */
		@Override
		public DashboardPlace getPlace(String token) {
			return new DashboardPlace();
		}
	}
}