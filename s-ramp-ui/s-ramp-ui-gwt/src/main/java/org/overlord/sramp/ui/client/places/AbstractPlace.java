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

import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;

import com.google.gwt.place.shared.Place;

/**
 * Base class for all places in the s-ramp ui.
 * 
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractPlace extends Place {
	
	/**
	 * Constructor.
	 */
	public AbstractPlace() {
	}

	/**
	 * Gets the key into the i18n messages.properties.  This returns a key that will be used
	 * to lookup a localized title for this place.  It will look up the localized title using
	 * the standard {@link ILocalizationService}.  Subclasses can optionally override this
	 * to provide a different key.
	 */
	public String getTitleKey() {
		String[] ksplit = getClass().toString().split("\\.");
		String k = ksplit[ksplit.length - 1];
		return "places." + k.toLowerCase() + ".title";
	}

	/**
	 * Gets the parameters that should be passed to the {@link ILocalizationService} when 
	 * resolving the localized title for this place.  Subclasses can override this to provide
	 * the params passed.
	 */
	public Object [] getTitleParams() {
		return new Object[0];
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public abstract boolean equals(Object obj);

}
