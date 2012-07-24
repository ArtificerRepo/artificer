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
package org.overlord.sramp.ui.client.views;

import org.overlord.sramp.ui.client.activities.IDashboardActivity;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Implementation of the dashboard view.
 *
 * @author eric.wittmann@redhat.com
 */
public class DashboardView extends AbstractView<IDashboardActivity> implements IDashboardView {
	
	/**
	 * Constructor.
	 */
	public DashboardView() {
		ILocalizationService i18n = getService(ILocalizationService.class);

		VerticalPanel vpanel = new VerticalPanel();
		vpanel.add(new Label(i18n.translate("dashboard.label")));
		vpanel.add(new Label(i18n.translate("dashboard.greeting")));
		Anchor link = new Anchor(i18n.translate("dashboard.link_to_browse"));
		link.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				getActivity().goTo(new BrowsePlace());
			}
		});
		vpanel.add(link);
		this.initWidget(vpanel);
	}

}
