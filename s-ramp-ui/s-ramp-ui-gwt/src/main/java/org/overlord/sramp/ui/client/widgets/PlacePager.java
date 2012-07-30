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
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * A simple pager widget that uses Places to navigate from page to page.
 *
 * @author eric.wittmann@redhat.com
 */
public class PlacePager extends HorizontalPanel {
	
	private PlaceHyperlink prevPage;
	private Label label;
	private PlaceHyperlink nextPage;

	/**
	 * Constructor.
	 */
	public PlacePager() {
		prevPage = new PlaceHyperlink("<");
		label = new InlineLabel();
		nextPage = new PlaceHyperlink(">");
		
		prevPage.getElement().setClassName("prev");
		nextPage.getElement().setClassName("next");
		
		getElement().setClassName("pager");

		super.add(prevPage);
		super.add(label);
		super.add(nextPage);
	}
	
	/**
	 * Called to initialize the places used by the pager to navigate from page to page.
	 * @param prevPlace
	 * @param nextPlace
	 * @param label
	 */
	public void init(Place prevPlace, Place nextPlace, String label) {
		if (prevPlace != null) {
			prevPage.setTargetPlace(prevPlace);
			prevPage.setVisible(true);
		} else {
			prevPage.setTargetPlace(null);
			prevPage.setVisible(false);
		}

		this.label.setText(label);
		if (nextPlace != null) {
			nextPage.setTargetPlace(nextPlace);
			nextPage.setVisible(true);
		} else {
			nextPage.setTargetPlace(null);
			nextPage.setVisible(false);
		}
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.HorizontalPanel#add(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void add(Widget w) {
		throw new IllegalArgumentException("Widgets cannot be added to a pager.");
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.HorizontalPanel#remove(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public boolean remove(Widget w) {
		throw new IllegalArgumentException("Widgets cannot be removed from a pager.");
	}
	
}
