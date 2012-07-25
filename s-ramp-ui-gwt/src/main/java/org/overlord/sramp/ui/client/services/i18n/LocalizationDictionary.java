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

import java.util.Map;

/**
 * A localizatoin dictionary.
 * 
 * @author eric.wittmann@redhat.com
 */
public class LocalizationDictionary {

	private Map<String, String> dict;

	/**
	 * Constructor.
	 * @param messageData 
	 */
	private LocalizationDictionary(Map<String, String> messageData) {
		this.dict = messageData;
	}

	/**
	 * Creates a localization dictionary from message data.
	 * 
	 * @param messageData
	 */
	public static LocalizationDictionary create(Map<String, String> messageData) {
		LocalizationDictionary dict = new LocalizationDictionary(messageData);
		return dict;
	}

	/**
	 * Get the value associated with the given key.
	 * 
	 * @param key to lookup
	 * @return the value
	 */
	public String get(String key) {
		String val = this.dict.get(key);
		if (val == null)
			val = "**" + key + "**";
		return val;
	}

}
