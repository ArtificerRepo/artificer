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
package org.overlord.sramp.ui.client.services.place;

import org.overlord.sramp.ui.client.services.IService;

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;

/**
 * The place service is responsible for doing place related stuff.  For example,
 * it can provide a place token for a given place.
 *
 * @author eric.wittmann@redhat.com
 */
public interface IPlaceService extends IService {

	/**
	 * Gets the place history mapper.
	 */
	public PlaceHistoryMapper getPlaceHistoryMapper();
	
	/**
	 * Generates a place token (suitable for use in hyperlinks, for example).
	 * @param place the place to tokenize
	 * @return a place token
	 */
	public String generatePlaceToken(Place place);
	
}
