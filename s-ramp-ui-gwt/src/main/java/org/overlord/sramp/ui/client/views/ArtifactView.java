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

import org.overlord.sramp.ui.client.activities.IArtifactActivity;
import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.widgets.PleaseWait;
import org.overlord.sramp.ui.client.widgets.SimpleFormLayoutPanel;
import org.overlord.sramp.ui.shared.beans.ArtifactDetails;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Artifact view concrete implementation.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactView extends AbstractView<IArtifactActivity> implements IArtifactView {
	
	private ArtifactPlace currentPlace;
	private FlowPanel main;

	/**
	 * Constructor.
	 */
	public ArtifactView() {
		main = new FlowPanel();
		main.setStyleName("artifactView");
		this.initWidget(main);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IArtifactView#onArtifactLoading(org.overlord.sramp.ui.client.places.ArtifactPlace)
	 */
	@Override
	public void onArtifactLoading(ArtifactPlace currentPlace) {
		this.currentPlace = currentPlace;
		main.clear();
		main.add(new PleaseWait(i18n().translate("views.artifact.loading-message")));
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IArtifactView#onArtifactLoaded(org.overlord.sramp.ui.shared.beans.ArtifactDetails)
	 */
	@Override
	public void onArtifactLoaded(ArtifactDetails artifact) {
		main.clear();

		VerticalPanel content = new VerticalPanel();
		content.setStyleName("artifactView-content");
		content.setWidth("100%");
		main.add(content);

		DisclosurePanel details = new DisclosurePanel("Artifact Details");
		details.setStyleName("dpanel");
		details.setOpen(true);
		details.add(createDetailsForm(artifact));
		
		DisclosurePanel description = new DisclosurePanel("Description");
		description.setStyleName("dpanel");
		description.setOpen(true);
		description.add(createDescriptionForm(artifact));
		
		content.add(details);
		content.add(description);
		content.setCellWidth(details, "100%");
		content.setCellWidth(description, "100%");
	}

	/**
	 * Creates a form that shows the description of the artifact.
	 * @param artifact
	 */
	private Widget createDescriptionForm(ArtifactDetails artifact) {
		FlowPanel wrapper = new FlowPanel();
		wrapper.setStyleName("dpanel-content");

		String artifactDesc = artifact.getDescription();
		if (artifactDesc == null || artifactDesc.trim().length() == 0)
			artifactDesc = "No description available.";
		Label descriptionWidget = new Label(artifactDesc);
		
		wrapper.add(descriptionWidget);
		return wrapper;
	}

	/**
	 * Creates the details form that shows all of the detailed meta information about the artifact.
	 * @param artifact
	 */
	private Widget createDetailsForm(ArtifactDetails artifact) {
		String createdOn = i18n().formatDateTime(artifact.getCreatedOn());
		String updatedOn = i18n().formatDateTime(artifact.getUpdatedOn());
		
		FlowPanel wrapper = new FlowPanel();
		wrapper.setStyleName("dpanel-content");
		
		SimpleFormLayoutPanel formLayoutPanel = new SimpleFormLayoutPanel();
		formLayoutPanel.add("UUID", new InlineLabel(artifact.getUuid()));
		formLayoutPanel.add("Name", new InlineLabel(artifact.getName()));
		formLayoutPanel.add("Created By", new InlineLabel(artifact.getCreatedBy()));
		formLayoutPanel.add("Created On", new InlineLabel(createdOn));
		formLayoutPanel.add("Updated By", new InlineLabel(artifact.getUpdatedBy()));
		formLayoutPanel.add("Updated On", new InlineLabel(updatedOn));
		
		wrapper.add(formLayoutPanel);
		return wrapper;
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IArtifactView#onArtifactLoadError(org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException)
	 */
	@Override
	public void onArtifactLoadError(RemoteServiceException error) {
		main.clear();
		main.add(new Label(i18n().translate("views.artifact.load-error.label", this.currentPlace.getUuid())));
		growl().growl(
				i18n().translate("views.artifact.load-error.title"),
				i18n().translate("views.artifact.load-error.message"),
				error);
	}

}
