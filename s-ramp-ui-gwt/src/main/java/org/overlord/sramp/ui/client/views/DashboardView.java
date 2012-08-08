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
import org.overlord.sramp.ui.client.util.JsonMap;
import org.overlord.sramp.ui.client.widgets.ArtifactUploadForm;
import org.overlord.sramp.ui.client.widgets.PlaceHyperlink;
import org.overlord.sramp.ui.client.widgets.PleaseWait;
import org.overlord.sramp.ui.client.widgets.TitlePanel;
import org.overlord.sramp.ui.client.widgets.UnorderedListPanel;
import org.overlord.sramp.ui.client.widgets.dialogs.DialogBox;
import org.overlord.sramp.ui.client.widgets.dialogs.ErrorDialog;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
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
		final ArtifactUploadDialogBox dialog = new ArtifactUploadDialogBox();
		
		form.addSubmitHandler(new SubmitHandler() {
			@Override
			public void onSubmit(SubmitEvent event) {
				dialog.onUploadStart(form.getFilename());
			}
		});
		form.addSubmitCompleteHandler(new SubmitCompleteHandler() {
			public void onSubmitComplete(SubmitCompleteEvent event) {
				String jsonData = event.getResults();
				// TODO handle error conditions in some way
				int startIdx = jsonData.indexOf('(');
				int endIdx = jsonData.lastIndexOf(')') + 1;
				if (jsonData.endsWith(")"))
					jsonData = jsonData.substring(startIdx);
				else
					jsonData = jsonData.substring(startIdx, endIdx);
				form.reset();

				JsonMap jsonMap = JsonMap.fromJSON(jsonData);
				String uuid = jsonMap.get("uuid");
				String error = jsonMap.get("error");
				if (uuid != null) {
					dialog.onUploadComplete(uuid);
				} else if (error != null) {
					dialog.hide();
					ErrorDialog errorDialog = new ErrorDialog("Error Uploading Artifact", error);
					errorDialog.center();
					errorDialog.show();
				}
			}
		});

		return form;
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

	/**
	 * A dialog box that is displayed during and after an artifact is uploaded to the repository
	 * from the dashboard.
	 * @author eric.wittmann@redhat.com
	 */
	private class ArtifactUploadDialogBox extends DialogBox {
		
		private VerticalPanel content;
		
		/**
		 * Constructor.
		 */
		public ArtifactUploadDialogBox() {
			super(i18n().translate("dashboard.upload-dialog.title-1"));
			content = new VerticalPanel();
			content.setStyleName("artifactUploadDialogContent");
			setWidget(content);
			getElement().addClassName("artifactUploadDialog");
		}

		/**
		 * Called when the artifact upload begins.
		 * @param fileName
		 */
		public void onUploadStart(String fileName) {
			content.clear();
			content.add(new PleaseWait(i18n().translate("dashboard.upload-dialog.please-wait", fileName)));
			center();
			show();
		}
		
		/**
		 * Called after the artifact has been successfully uploaded.
		 * @param uuid
		 */
		public void onUploadComplete(String uuid) {
			setText(i18n().translate("dashboard.upload-dialog.title-2"));
			content.clear();
			InlineLabel msg = new InlineLabel(i18n().translate("dashboard.upload-dialog.success.message", uuid));
			msg.setStyleName("message");
			content.add(msg);
			
			FlowPanel linkWrapper = new FlowPanel();
			linkWrapper.setStyleName("linkWrapper");
			PlaceHyperlink link = new PlaceHyperlink(i18n().translate("dashboard.upload-dialog.success.link-label"));
			link.setTargetPlace(new ArtifactPlace(uuid));
			link.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
			linkWrapper.add(link);
			content.add(linkWrapper);
			
	    	HorizontalPanel buttonPanel = new HorizontalPanel();
	    	buttonPanel.setStyleName("buttonPanel");
	    	buttonPanel.addStyleName("artifactUploadButtonPanel");
	    	Button closeButton = new Button(i18n().translate("dialogs.close"));
	    	closeButton.setStyleName("closeButton");
	    	closeButton.addStyleName("button");
	    	closeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
				}
			});
	    	buttonPanel.add(closeButton);
	    	content.add(buttonPanel);
	    	
	    	center();
		}
		
	}
}
