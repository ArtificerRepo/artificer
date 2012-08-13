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
import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.services.growl.GrowlType;
import org.overlord.sramp.ui.client.util.JsonMap;
import org.overlord.sramp.ui.client.widgets.ArtifactUploadForm;
import org.overlord.sramp.ui.client.widgets.PlaceHyperlink;
import org.overlord.sramp.ui.client.widgets.TitlePanel;
import org.overlord.sramp.ui.client.widgets.UnorderedListPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

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
		List<Widget> activityLinks = createActivityLinks();
		for (Widget link : activityLinks)
			ulPanel.add(link);
		activitiesPanel.setWidget(ulPanel);
		
		// Create the Upload Artifact panel
		TitlePanel uploadPanel = new TitlePanel(i18n().translate("dashboard.upload-panel.title"));
		uploadPanel.getElement().setId("dash-uploadPanel");
		uploadPanel.setWidget(createUploadForm());
		
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
	 * Creates the artifact upload form.
	 */
	private Widget createUploadForm() {
		// Create a FormPanel and point it at a service.
		String url = GWT.getModuleBaseURL() + "services/artifactUpload";
		final ArtifactUploadForm form = new ArtifactUploadForm(url);
		final int[] growlId = new int[1];
		
		form.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				int id = growl().growl(
						i18n().translate("dashboard.upload-dialog.title-1"), 
						i18n().translate("dashboard.upload-dialog.please-wait", form.getFilename()), 
						GrowlType.progress);
				growlId[0] = id;
			}
		});
		form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String jsonData = event.getResults();
				int startIdx = jsonData.indexOf('(');
				int endIdx = jsonData.lastIndexOf(')') + 1;
				if (jsonData.endsWith(")"))
					jsonData = jsonData.substring(startIdx);
				else
					jsonData = jsonData.substring(startIdx, endIdx);

				JsonMap jsonMap = JsonMap.fromJSON(jsonData);
				String uuid = jsonMap.get("uuid");
				String error = jsonMap.get("error");
				if (uuid != null) {
					growl().onProgressComplete(
							growlId[0], 
							i18n().translate("dashboard.upload-dialog.title-2"), 
							createUploadSuccessWidget(uuid, form.getFilename()));
				} else if (error != null) {
					growl().onProgressError(
							growlId[0], 
							i18n().translate("dashboard.upload-dialog.error.title"),
							error);
				}
				
				form.reset();
			}
		});

		return form;
	}

	/**
	 * Creates the widget (message) shown to the user when an artifact successfully uploads.
	 * @param uuid the UUID of the new artifact
	 * @param artifactName the filename of the artifact
	 */
	protected Widget createUploadSuccessWidget(String uuid, String artifactName) {
		FlowPanel wrapper = new FlowPanel();
		wrapper.add(new InlineLabel(i18n().translate("dashboard.upload-dialog.success.message", uuid, artifactName)));
		wrapper.add(new InlineLabel(" "));
		Place artifactPlace = new ArtifactPlace(uuid);
		PlaceHyperlink link = new PlaceHyperlink(i18n().translate("dashboard.upload-dialog.success.link-label"), artifactPlace);
		wrapper.add(link);
		return wrapper;
	}

	/**
	 * Create all of the activity links.
	 */
	private List<Widget> createActivityLinks() {
		List<Widget> links = new ArrayList<Widget>();
		PlaceHyperlink browseLink = new PlaceHyperlink(i18n().translate("dashboard.activities-panel.activities.browse.label"), new BrowsePlace());
		links.add(browseLink);
		PlaceHyperlink ontologyLink = new PlaceHyperlink(i18n().translate("dashboard.activities-panel.activities.ontologies.label"), null);
		links.add(ontologyLink);
		PlaceHyperlink savedQueriesLink = new PlaceHyperlink(i18n().translate("dashboard.activities-panel.activities.saved-queries.label"), null);
		links.add(savedQueriesLink);
		return links;
	}
}
