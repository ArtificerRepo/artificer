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

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Implementation of a filter panel where each option in the list of filter
 * options is a {@link Place}.
 *
 * @author eric.wittmann@redhat.com
 */
public class PlaceFilterPanel<P extends Place> extends VerticalPanel {
	
	private InlineLabel label;
	private UnorderedListPanel options;

	/**
	 * Constructor.
	 * @param label
	 */
	public PlaceFilterPanel(String label) {
		if (label == null)
			throw new IllegalArgumentException("Please provide a filter label.");
		this.label = new InlineLabel(label);
		this.label.setStyleName("label");
		this.options = new UnorderedListPanel();
		add(this.label);
		add(this.options);
		getElement().setClassName("filterPanel");
		this.options.getElement().setClassName("filterOptions");
	}

	/**
	 * Adds a single option to the list of options.
	 * @param label the label on the filter option
	 * @param place the place to go when the filter is clicked
	 */
	public void addFilterOption(String label, P place) {
		PlaceHyperlink link = new PlaceHyperlink(label, place);
		this.options.add(link);
	}
	
	/**
	 * Called to set the current filter setting.  This will change the
	 * look and feel of the selected option and disable it.
	 * @param place
	 */
	@SuppressWarnings("unchecked")
	public void setCurrentPlace(P place) {
		for (int idx = 0; idx < this.options.getWidgetCount(); idx++) {
			PlaceHyperlink link = (PlaceHyperlink) this.options.getWidget(idx);
			P targetPlace = (P) link.getTargetPlace();
			if (targetPlace != null && matches(place, targetPlace)) {
				link.setEnabled(false);
				link.getElement().getParentElement().addClassName("selected");
				break;
			}
		}
	}

	/**
	 * Returns true if the given current place matches the given target place.
	 * @param currentPlace
	 * @param targetPlace
	 */
	protected boolean matches(P currentPlace, P targetPlace) {
		return targetPlace.equals(currentPlace);
	}
	
}
