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
import com.google.gwt.user.client.ui.Widget;

/**
 * A widget that is used to show a list of breadcrumbs at the top of every page in the UI.
 *
 * @author eric.wittmann@redhat.com
 */
public class BreadcrumbPanel extends UnorderedListPanel {

	/**
	 * Constructor.
	 */
	public BreadcrumbPanel() {
		getElement().setId("breadcrumbs");
		setVisible(false);
	}

	/**
	 * Adds a single crumb to the trail of breadcrumbs.
	 * @param label the label for the crumb
	 * @param place the place to go when the user clicks the breadcrumb
	 */
	public void addCrumb(String label, Place place) {
		Widget w = null;
		boolean first = this.getWidgetCount() == 0;
		if (place == null) {
			w = new InlineLabel(label);
		} else {
			w = new PlaceHyperlink(label, place);
		}
		if (first)
			w.getElement().setClassName("first");
		add(w);
		setVisible(true);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.widgets.UnorderedListPanel#clear()
	 */
	@Override
	public void clear() {
		super.clear();
		setVisible(false);
	}
	
}
