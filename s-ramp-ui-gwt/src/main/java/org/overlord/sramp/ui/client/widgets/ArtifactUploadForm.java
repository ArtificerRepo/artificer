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

import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * A widget that is used to upload an artifact to the server.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUploadForm extends FormPanel {

	final FileUpload upload = new FileUpload();

	/**
	 * Constructor.
	 * @param url
	 */
	public ArtifactUploadForm(String url) {
		ILocalizationService i18n = Services.getServices().getService(ILocalizationService.class);

		this.setAction(url);
		this.setEncoding(FormPanel.ENCODING_MULTIPART);
		this.setMethod(FormPanel.METHOD_POST);

		// Create a panel to hold all of the form widgets.
		VerticalPanel vpanel = new VerticalPanel();

		final ListBox artifactType = new ListBox();
		final Button submitButton = new Button(i18n.translate("widgets.artifact-upload.submit"));

		// Populate the type list box with options
		artifactType.setName("artifactType");
		artifactType.addItem(i18n.translate("widgets.artifact-upload.please-choose"), "");
		artifactType.addItem(i18n.translate("widgets.artifact-upload.choice.doc"), "Document");
		artifactType.addItem(i18n.translate("widgets.artifact-upload.choice.xml"), "XmlDocument");
		artifactType.addItem(i18n.translate("widgets.artifact-upload.choice.xsd"), "XsdDocument");
		artifactType.addItem(i18n.translate("widgets.artifact-upload.choice.wsdl"), "WsdlDocument");
		artifactType.addItem(i18n.translate("widgets.artifact-upload.choice.policy"), "PolicyDocument");
		artifactType.setSelectedIndex(0);

		// Configure the file upload widget
		upload.getElement().setClassName("file");
		upload.setName("artifact");

		// Hook into the submit button
		submitButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				submit();
			}
		});
		submitButton.setEnabled(false);

		// Add all the widgets to the form.
		vpanel.add(upload);
		vpanel.add(artifactType);
		vpanel.add(submitButton);

		// Create a change handler that will enable/disable the Submit button.
		ChangeHandler changeHandler = new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean validSelection = !"".equals(artifactType.getValue(artifactType.getSelectedIndex()));
				boolean validFile = upload.getFilename() != null && upload.getFilename().trim().length() > 0;
				submitButton.setEnabled(validSelection && validFile);
			}
		};
		upload.addChangeHandler(changeHandler);
		upload.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				String filename = upload.getFilename().toLowerCase();
				if (filename.endsWith(".xml")) {
					artifactType.setSelectedIndex(2);
				} else if (filename.endsWith(".xsd")) {
					artifactType.setSelectedIndex(3);
				} else if (filename.endsWith(".wsdl")) {
					artifactType.setSelectedIndex(4);
				} else if (filename.endsWith(".wspolicy")) {
					artifactType.setSelectedIndex(5);
				} else {
					artifactType.setSelectedIndex(1);
				}
				submitButton.setEnabled(true);
			}
		});
		artifactType.addChangeHandler(changeHandler);

		setWidget(vpanel);
	}

	/**
	 * Returns the filename chosen by the user.
	 */
	public String getFilename() {
		return this.upload.getFilename();
	}

}
