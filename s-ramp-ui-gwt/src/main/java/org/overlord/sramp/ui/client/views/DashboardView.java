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

import java.util.ArrayList;
import java.util.List;

import org.overlord.sramp.ui.client.activities.IDashboardActivity;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.widgets.PlaceHyperlink;
import org.overlord.sramp.ui.client.widgets.TitlePanel;
import org.overlord.sramp.ui.client.widgets.UnorderedListPanel;

import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Hyperlink;
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
		// Create the dashboard
		HorizontalPanel dashboardPanel = new HorizontalPanel();
		dashboardPanel.getElement().setId("dashboard");
		dashboardPanel.getElement().setAttribute("style", "width: 100%");
		
		// Create two columns
		VerticalPanel leftColumn = new VerticalPanel();
		leftColumn.getElement().setClassName("dashColumn");
		leftColumn.getElement().addClassName("left");
		VerticalPanel rightColumn = new VerticalPanel();
		rightColumn.getElement().setClassName("dashColumn");
		rightColumn.getElement().addClassName("right");
		dashboardPanel.add(leftColumn);
		dashboardPanel.add(rightColumn);
		
		// Create the Activities panel
		TitlePanel activitiesPanel = new TitlePanel(i18n().translate("dashboard.activities-panel.title"));
		activitiesPanel.getElement().setId("dash-activitiesPanel");
		UnorderedListPanel ulPanel = new UnorderedListPanel();
		List<Hyperlink> activityLinks = createActivityLinks();
		for (Hyperlink link : activityLinks)
			ulPanel.add(link);
		activitiesPanel.setWidget(ulPanel);
		
		// Create the Upload Artifact panel
		TitlePanel uploadPanel = new TitlePanel(i18n().translate("dashboard.upload-panel.title"));
		uploadPanel.getElement().setId("dash-uploadPanel");
		uploadPanel.setWidget(new Label("TBD"));
		
		// Create the Help panel
		TitlePanel helpPanel = new TitlePanel(i18n().translate("dashboard.help-panel.title"));
		helpPanel.getElement().setId("dash-helpPanel");
		HTMLPanel helpText = new HTMLPanel(i18n().translate("dashboard.help-panel.help-text"));
		helpPanel.setWidget(helpText);
		
		// Now size the columns properly
		dashboardPanel.setCellWidth(leftColumn, "50%");
		dashboardPanel.setCellWidth(rightColumn, "50%");

		// Add the panels to the dashboard
		leftColumn.add(activitiesPanel);
		leftColumn.add(uploadPanel);
		rightColumn.add(helpPanel);

		this.initWidget(dashboardPanel);
	}

	/**
	 * Create all of the activity links.
	 */
	private List<Hyperlink> createActivityLinks() {
		List<Hyperlink> links = new ArrayList<Hyperlink>();
		Hyperlink browseLink = new PlaceHyperlink(i18n().translate("dashboard.activities-panel.activities.browse.label"), new BrowsePlace());
		links.add(browseLink);
		Hyperlink ontologyLink = new PlaceHyperlink(i18n().translate("dashboard.activities-panel.activities.ontologies.label"), null);
		links.add(ontologyLink);
		Hyperlink savedQueriesLink = new PlaceHyperlink(i18n().translate("dashboard.activities-panel.activities.saved-queries.label"), null);
		links.add(savedQueriesLink);
		return links;
	}

}
