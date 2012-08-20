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

import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;

/**
 * A simple form panel that shows information about an artifact
 * summary (for example, when an artifact is selected from the artifact list in
 * the browse view).
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactSummaryPanel extends FlowPanel {

	private InlineLabel noArtifactMessage;
	
	private SimpleFormLayoutPanel artifactForm;
	private TextBox uuidField;
	private TextBox nameField;
	private TextBox createdByField;
	private TextBox createdOnField;
	private TextBox updatedOnField;
	private InlineLabel descriptionField;
	private PlaceHyperlink detailsLink;

	/**
	 * Constructor.
	 */
	public ArtifactSummaryPanel() {
		ILocalizationService i18n = Services.getServices().getService(ILocalizationService.class);
		
		noArtifactMessage = new InlineLabel(i18n.translate("views.browse.summary-panel.no-artifact-message"));
		noArtifactMessage.setStyleName("message");
		
		uuidField = new TextBox();
		uuidField.setReadOnly(true);
		nameField = new TextBox();
		nameField.setReadOnly(true);
		createdByField = new TextBox();
		createdByField.setReadOnly(true);
		createdOnField = new TextBox();
		createdOnField.setReadOnly(true);
		updatedOnField = new TextBox();
		updatedOnField.setReadOnly(true);
		descriptionField = new InlineLabel();
		detailsLink = new PlaceHyperlink(i18n.translate("views.browse.summary-panel.details-link"));
		
		artifactForm = new SimpleFormLayoutPanel();

		artifactForm.add(i18n.translate("views.browse.summary-panel.uuid-label"), uuidField);
		artifactForm.add(i18n.translate("views.browse.summary-panel.name-label"), nameField);
		artifactForm.add(i18n.translate("views.browse.summary-panel.created-by-label"), createdByField);
		artifactForm.add(i18n.translate("views.browse.summary-panel.created-on-label"), createdOnField);
		artifactForm.add(i18n.translate("views.browse.summary-panel.updated-on-label"), updatedOnField);
		artifactForm.addTwoCol(null, descriptionField);
		FlowPanel detailsLinkWrapper = new FlowPanel();
		detailsLinkWrapper.setStyleName("placeLinkWrapper");
		detailsLinkWrapper.add(detailsLink);
		artifactForm.addTwoCol(null, detailsLinkWrapper);
		
		this.add(noArtifactMessage);
		this.add(artifactForm);
		
		getElement().addClassName("artifactSummaryPanel");
		reset();
	}
	
	/**
	 * When called, the form fields are populated with data from the given artifact.
	 * @param artifact
	 */
	public void setValue(ArtifactSummary artifact) {
		String createdOn = Services.getServices().getService(ILocalizationService.class).formatDateTime(artifact.getCreatedOn());
		String updatedOn = Services.getServices().getService(ILocalizationService.class).formatDateTime(artifact.getUpdatedOn());
		
		uuidField.setValue(artifact.getUuid());
		nameField.setValue(artifact.getName());
		createdOnField.setValue(createdOn);
		createdByField.setValue(artifact.getCreatedBy());
		updatedOnField.setValue(updatedOn);
		descriptionField.setText(artifact.getDescription());

		ArtifactPlace artifactDetailsPlace = new ArtifactPlace(artifact.getModel(), artifact.getType(), artifact.getUuid());
		detailsLink.setTargetPlace(artifactDetailsPlace);

		this.noArtifactMessage.setVisible(false);
		this.artifactForm.setVisible(true);
	}

	/**
	 * Called when the artifact info should no longer be displayed.  This is typically
	 * called if an artifact is deselected from a list, for example.
	 */
	public void reset() {
		this.noArtifactMessage.setVisible(true);
		this.artifactForm.setVisible(false);
		
		uuidField.setValue("");
		nameField.setValue("");
		createdOnField.setValue("");
		createdByField.setValue("");
		updatedOnField.setValue("");
		descriptionField.setText("");
	}

}
