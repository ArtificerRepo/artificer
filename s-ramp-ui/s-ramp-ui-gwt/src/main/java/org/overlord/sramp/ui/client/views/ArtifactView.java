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

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.overlord.sramp.ui.client.activities.IArtifactActivity;
import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.widgets.PleaseWait;
import org.overlord.sramp.ui.client.widgets.SimpleFormLayoutPanel;
import org.overlord.sramp.ui.client.widgets.UnorderedListPanel;
import org.overlord.sramp.ui.shared.beans.ArtifactDetails;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
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

		HorizontalPanel twoColContent = new HorizontalPanel();
		twoColContent.setStyleName("artifactView-content");
		twoColContent.setWidth("100%");
		main.add(twoColContent);

		VerticalPanel leftCol = new VerticalPanel();
		leftCol.setStyleName("leftCol");
		VerticalPanel rightCol = new VerticalPanel();
		rightCol.setStyleName("rightCol");
		leftCol.setWidth("100%");
		rightCol.setWidth("100%");
		twoColContent.add(leftCol);
		twoColContent.add(rightCol);
		twoColContent.setCellWidth(leftCol, "70%");
		twoColContent.setCellWidth(rightCol, "30%");
		twoColContent.setCellHorizontalAlignment(rightCol, HorizontalPanel.ALIGN_RIGHT);

		// Artifact details
		DisclosurePanel details = new DisclosurePanel(i18n().translate("views.artifact.details.label"));
		details.setStyleName("dpanel");
		details.setOpen(true);
		details.add(createDetailsForm(artifact));

		// Artifact description
		DisclosurePanel description = new DisclosurePanel(i18n().translate("views.artifact.description.label"));
		description.setStyleName("dpanel");
		description.setOpen(true);
		description.add(createDescriptionForm(artifact));

		leftCol.add(details);
		leftCol.add(description);
		leftCol.setCellWidth(details, "100%");
		leftCol.setCellWidth(description, "100%");

		// Artifact properties
		DisclosurePanel properties = new DisclosurePanel(i18n().translate("views.artifact.properties.label"));
		properties.setStyleName("dpanel");
		properties.setOpen(true);
		properties.add(createPropertiesForm(artifact));

		// Classifications
		DisclosurePanel classifications = new DisclosurePanel(i18n().translate("views.artifact.classifications.label"));
		classifications.setStyleName("dpanel");
		classifications.setOpen(true);
		classifications.add(createClassificationsForm(artifact));

		// Artifact links/urls
		DisclosurePanel links = new DisclosurePanel(i18n().translate("views.artifact.links.label"));
		links.setStyleName("dpanel");
		links.setOpen(true);
		links.add(createLinks(artifact));

		rightCol.add(properties);
		rightCol.add(classifications);
		rightCol.add(links);
		rightCol.setCellWidth(details, "100%");
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
			artifactDesc = i18n().translate("views.artifact.no-description.message");
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
		formLayoutPanel.add(i18n().translate("views.artifact.details.uuid-label"), new InlineLabel(artifact.getUuid()));
		formLayoutPanel.add(i18n().translate("views.artifact.details.name-label"), new InlineLabel(artifact.getName()));
		formLayoutPanel.add(i18n().translate("views.artifact.details.created-by-label"), new InlineLabel(artifact.getCreatedBy()));
		formLayoutPanel.add(i18n().translate("views.artifact.details.created-on-label"), new InlineLabel(createdOn));
		formLayoutPanel.add(i18n().translate("views.artifact.details.updated-by-label"), new InlineLabel(artifact.getUpdatedBy()));
		formLayoutPanel.add(i18n().translate("views.artifact.details.updated-on-label"), new InlineLabel(updatedOn));

		wrapper.add(formLayoutPanel);
		return wrapper;
	}

	/**
	 * Creates the properties form that shows all of the artifact's custom s-ramp properites.
	 * @param artifact
	 */
	private Widget createPropertiesForm(ArtifactDetails artifact) {
		Set<String> propertyNames = artifact.getPropertyNames();
		if (propertyNames.isEmpty()) {
			return new InlineLabel(i18n().translate("views.artifact.no-properties.message"));
		}
		propertyNames = new TreeSet<String>(propertyNames);

		FlowPanel wrapper = new FlowPanel();
		wrapper.setStyleName("dpanel-content");

		SimpleFormLayoutPanel formLayoutPanel = new SimpleFormLayoutPanel();
		for (String propertyName : propertyNames) {
			String propertyValue = artifact.getProperty(propertyName);
			formLayoutPanel.add(propertyName, new InlineLabel(propertyValue));
		}

		wrapper.add(formLayoutPanel);
		return wrapper;
	}

	/**
	 * Creates the classifications form that shows all of the artifact's S-RAMP classifications.
	 * @param artifact
	 */
	private Widget createClassificationsForm(ArtifactDetails artifact) {
		List<String> classifiedBy = artifact.getClassifiedBy();
		if (classifiedBy.isEmpty()) {
			return new InlineLabel(i18n().translate("views.artifact.no-classifications.message"));
		}
		Set<String> orderedClassifications = new TreeSet<String>(classifiedBy);

		FlowPanel wrapper = new FlowPanel();
		wrapper.setStyleName("dpanel-content");

		UnorderedListPanel ulPanel = new UnorderedListPanel();
		for (String classification : orderedClassifications) {
			ulPanel.add(new Label(classification));
		}

		wrapper.add(ulPanel);
		return wrapper;
	}

	/**
	 * Creates any links relevant to the artifact.
	 * @param artifact
	 */
	private Widget createLinks(ArtifactDetails artifact) {
		String url = GWT.getModuleBaseURL() + "services/artifactDownload";
		url += "?uuid=" + artifact.getUuid() + "&type=" + artifact.getType();

		FlowPanel wrapper = new FlowPanel();
		wrapper.setStyleName("dpanel-content");

		if (!artifact.isDerived()) {
			Anchor downloadLink = new Anchor(i18n().translate("views.artifact.links.download"), url);
			wrapper.add(downloadLink);
		}
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
