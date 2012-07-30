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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.http.client.URL;

/**
 * Some static utilities useful when dealing with places.
 * 
 * @author eric.wittmann@redhat.com
 */
public class PlaceUtils {

	/**
	 * Constructor.
	 */
	private PlaceUtils() {
	}

	/**
	 * Parses the place token into a map of query parameters. The expected format of any place token is a
	 * standard URL query-parameter formatted string.
	 * 
	 * @param placeToken the place token
	 * @return a map of query params
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, String> parsePlaceToken(String placeToken) {
		if (placeToken == null || placeToken.trim().length() == 0)
			return Collections.EMPTY_MAP;

		Map<String, String> queryParams = new HashMap<String, String>();
		String[] paramSplits = placeToken.split("&");
		if (paramSplits.length > 0) {
			for (String paramSplit : paramSplits) {
				String[] split2 = paramSplit.split("=");
				if (split2.length == 2) {
					String key = split2[0];
					String encodedValue = split2[1];
					String value = URL.decodeQueryString(encodedValue);
					queryParams.put(key, value);
				}
			}
		}
		return queryParams;
	}

	/**
	 * Creates a place token in the standard query-param string format. The query parameters should be passed
	 * in with a key and a value for each parameter. If one of the parameters should be present without a
	 * value, pass an empty string for its associated value.  If the value of a param is null, it will be
	 * excluded from the token.
	 * 
	 * @param params param/value pairs
	 * @return place token string
	 */
	public static String createPlaceToken(Object ... params) {
		StringBuilder builder = new StringBuilder();
    	boolean first = true;
		for (int i = 0; i < params.length; i += 2) {
			String key = (String) params[i];
			Object val = params[i + 1];
			String encodedValue = "";
			if (val != null) {
	    		if (first) {
	    			first = false;
	    		} else {
		        	builder.append("&");
	    		}
				encodedValue = URL.encodeQueryString(val.toString());
				builder.append(key).append("=").append(encodedValue);
			}
		}
		return builder.toString();
	}

}
