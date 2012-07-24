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
package org.overlord.sramp.ui.client.services.i18n;

import java.util.MissingResourceException;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A localizatoin dictionary. The data comes from JSON data downloaded from the server.
 * 
 * @author eric.wittmann@redhat.com
 */
public class LocalizationDictionary {

	private JavaScriptObject dict;

	/**
	 * Constructor.
	 */
	private LocalizationDictionary() {
	}

	/**
	 * Creates a localization dictionary from json data.
	 * 
	 * @param jsonData
	 */
	public static LocalizationDictionary create(String jsonData) {
		LocalizationDictionary dict = new LocalizationDictionary();
		dict.attach(jsonData);
		return dict;
	}

	/**
	 * Evaluate and attach the given
	 * 
	 * @param json
	 */
	private final native void attach(String json) /*-{
		this.@org.overlord.sramp.ui.client.services.i18n.LocalizationDictionary::dict = eval(json);
	}-*/;

	/**
	 * Get the value associated with the given key.
	 * 
	 * @param key to lookup
	 * @return the value
	 * @throws MissingResourceException if the value is not found
	 */
	public native String get(String key) /*-{
		key = String(key);
		var map = this.@org.overlord.sramp.ui.client.services.i18n.LocalizationDictionary::dict;
		var value = map[key];
		if (value == null)
			return null;
		else
			return String(value);
	}-*/;

}
