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
package org.overlord.sramp.ui.client.util;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A javascript map object.  This class is typically used to wrap some map data
 * returned as JSON from the server.
 *
 * @author eric.wittmann@redhat.com
 */
public class JsonMap extends JavaScriptObject {

	/**
	 * Constructor.
	 */
	protected JsonMap() {
	}

	/**
	 * Gets a value from the map.
	 * @param key
	 */
	public final native String get(String key) /*-{
		if (this[key])
			return this[key]; 
		else
			return null;
	}-*/;

	/**
	 * Convert a string of json data into a useful map.
	 * @param jsonData
	 */
	public static final native JsonMap fromJSON(String jsonData) /*-{ return eval(jsonData); }-*/;
	
}
