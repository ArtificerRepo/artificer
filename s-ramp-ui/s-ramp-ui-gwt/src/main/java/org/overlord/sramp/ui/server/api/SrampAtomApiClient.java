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
package org.overlord.sramp.ui.server.api;

import org.apache.commons.configuration.Configuration;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAtomApiClient extends org.overlord.sramp.client.SrampAtomApiClient {

	private static SrampAtomApiClient sInstance;

	/**
	 * @param config
	 */
	public static void init(Configuration config) {
		sInstance = new SrampAtomApiClient(config);
	}
	
	/**
	 * Singleton accessor.
	 */
	public static SrampAtomApiClient getInstance() {
		if (sInstance == null)
			throw new RuntimeException("SRAMPAtomApiClient not yet initialized!");
		else
			return sInstance;
	}

	/**
	 * Private singleton constructor.
	 * @param config 
	 */
	private SrampAtomApiClient(Configuration config) {
		super((String) config.getProperty("s-ramp.atom-api.endpoint"));
	}

}
