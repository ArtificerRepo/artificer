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
package org.overlord.sramp.ui.client;

import java.util.HashMap;
import java.util.Map;

import org.overlord.sramp.ui.client.places.PlaceList;

import com.google.gwt.place.impl.AbstractPlaceHistoryMapper;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.PlaceTokenizer;

/**
 * A custom place history mapper. This is necessary because I want the format of the place token to be:
 * 
 * http://host:port/srampui/u#/dashboard/browse?filter=xsd&order=name:d
 * 
 * The token in the above example is: /dashboard/browse?filter=xsd&order=name:d
 * 
 * The first part of the token is "/dashboard/browse" and the second part is "filter=xsd&order=name:d". This
 * cannot be accomplished using the standard GWT place history mapper tokenizer support because the
 * {@link AbstractPlaceHistoryMapper} does not have a way to customize the character separator between the two
 * parts. It is hard-coded to be a colon. Very sad for me.
 * 
 * This implementation uses the full listing of valid {@link Place}s from the {@link PlaceList} class.  This
 * could be improved by hooking into the rebind phase of the GWT compilation.
 * 
 * @author eric.wittmann@redhat.com
 */
public class PlaceHistoryMapperImpl implements PlaceHistoryMapper {

	private Map<String, PlaceTokenizer<Place>> tokenizersByPrefix = new HashMap<String, PlaceTokenizer<Place>>();
	private Map<Class<?>, PlaceTokenizer<Place>> tokenizersByPlace = new HashMap<Class<?>, PlaceTokenizer<Place>>();
	private Map<Class<?>, String> prefixesByPlace = new HashMap<Class<?>, String>();

	/**
	 * C'tor.
	 */
	@SuppressWarnings("unchecked")
	public PlaceHistoryMapperImpl() {
		for (Object[] placeSpec : PlaceList.PLACES) {
			String prefix = (String) placeSpec[0];
			PlaceTokenizer<Place> tokenizer = (PlaceTokenizer<Place>) placeSpec[1];
			Class<?> placeClass = (Class<?>) placeSpec[2];
			tokenizersByPrefix.put(prefix, tokenizer);
			tokenizersByPlace.put(placeClass, tokenizer);
			prefixesByPlace.put(placeClass, prefix);
		}
	}

	/**
	 * @see com.google.gwt.place.shared.PlaceHistoryMapper#getPlace(java.lang.String)
	 */
	@Override
	public Place getPlace(String token) {
		int separatorAt = token.indexOf('?');
		String prefix;
		String rest;
		if (separatorAt >= 0) {
			prefix = token.substring(0, separatorAt);
			rest = token.substring(separatorAt + 1);
		} else {
			prefix = token;
			rest = null;
		}
		PlaceTokenizer<?> tokenizer = tokenizersByPrefix.get(prefix);
		if (tokenizer != null) {
			return tokenizer.getPlace(rest);
		}
		return null;
	}

	/**
	 * @see com.google.gwt.place.shared.PlaceHistoryMapper#getToken(com.google.gwt.place.shared.Place)
	 */
	@Override
	public String getToken(Place place) {
		PlaceTokenizer<Place> placeTokenizer = tokenizersByPlace.get(place.getClass());
		String token = prefixesByPlace.get(place.getClass());
		String rest = placeTokenizer.getToken(place);
		if (rest != null && rest.trim().length() > 0)
			token += "?" + rest; 
		return token;
	}

}
