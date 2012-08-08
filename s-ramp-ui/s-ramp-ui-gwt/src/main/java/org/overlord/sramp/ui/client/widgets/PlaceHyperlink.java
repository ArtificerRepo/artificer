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
package org.overlord.sramp.ui.client.widgets;

import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.place.IPlaceService;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.impl.HyperlinkImpl;

/**
 * A hyperlink that can be easily created from a {@link Place}.  Requires the {@link IPlaceService}
 * to be available.
 *
 * @author eric.wittmann@redhat.com
 */
public class PlaceHyperlink extends Anchor {
	
	private static HyperlinkImpl impl = new HyperlinkImpl();

	private Place targetPlace;
	private boolean enabled = true;

	/**
	 * Constructor.
	 * @param text
	 */
	public PlaceHyperlink(String text) {
		this(text, null);
	}

	/**
	 * Constructor.
	 * @param text
	 * @param place
	 */
	public PlaceHyperlink(String text, Place place) {
		super(text);
		this.targetPlace = place;
		getElement().setClassName("placeLink");
		setTargetPlace(place);
	}

	/**
	 * @return the place
	 */
	public Place getTargetPlace() {
		return targetPlace;
	}

	/**
	 * @param place the place to set
	 */
	public void setTargetPlace(Place place) {
		this.setHref("#" + toHistoryToken(place));
		this.targetPlace = place;
		setEnabled(place != null);
	}

	/**
	 * Call to enable or disable the link.
	 * @param enabled
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (enabled) {
			getElement().removeClassName("disabled");
		} else {
			getElement().addClassName("disabled");
		}
	}
	
	/**
	 * Returns true if this link is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.Hyperlink#onBrowserEvent(com.google.gwt.user.client.Event)
	 */
	@Override
	public void onBrowserEvent(Event event) {
		if (!isEnabled()) {
			DOM.eventPreventDefault(event);
		} else {
			super.onBrowserEvent(event);
			if (DOM.eventGetType(event) == Event.ONCLICK && impl.handleAsClick(event)) {
				History.newItem(toHistoryToken(getTargetPlace()));
				DOM.eventPreventDefault(event);
			}
		}
	}

	/**
	 * Creates a history token for the given {@link Place}. 
	 * @param place the {@link Place} to tokenize
	 * @return the place token
	 */
	private static String toHistoryToken(Place place) {
		if (place == null)
			return "";
		IPlaceService placeService = Services.getServices().getService(IPlaceService.class);
		return placeService.generatePlaceToken(place);
	}
	
}
