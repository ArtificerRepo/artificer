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
import com.google.gwt.user.client.ui.InlineHyperlink;

/**
 * A hyperlink that can be easily created from a {@link Place}.  Requires the {@link IPlaceService}
 * to be available.
 *
 * @author eric.wittmann@redhat.com
 */
public class PlaceHyperlink extends InlineHyperlink {

	private Place targetPlace;
	
	/**
	 * Constructor.
	 */
	public PlaceHyperlink(String text, Place place) {
		super(text, toHistoryToken(place));
		this.targetPlace = place;
		if (place == null)
			getElement().setClassName("disabled");
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
		if (place == null)
			getElement().setClassName("disabled");
		else
			getElement().removeClassName("disabled");
		setTargetHistoryToken(toHistoryToken(place));
		this.targetPlace = place;
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
