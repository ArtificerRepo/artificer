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
package org.overlord.sramp.ui.client.widgets.dialogs;

import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog shown when an unexpected error occurs.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ErrorDialog extends DialogBox {
	
	/**
	 * Constructor.
	 * @param title
	 * @param message
	 * @param error
	 */
	public ErrorDialog(String title, String message, RemoteServiceException error) {
		super(title);
		
    	addStyleName("errorDialog");
    	
    	ILocalizationService i18n = Services.getServices().getService(ILocalizationService.class);

    	FlowPanel contentWrapper = new FlowPanel();
    	contentWrapper.setStyleName("errorDialogContent");
    	Button closeButton = new Button(i18n.translate("dialogs.close"));
    	
    	contentWrapper.add(new InlineLabel(message));
    	
    	Label divider = new Label();
    	divider.setStyleName("divider");

		VerticalPanel vpanel = new VerticalPanel();
    	HorizontalPanel buttonPanel = new HorizontalPanel();
    	buttonPanel.setStyleName("errorButtonPanel");
    	buttonPanel.addStyleName("buttonPanel");
    	buttonPanel.add(closeButton);
    	
    	vpanel.add(contentWrapper);
    	if (error != null) {
	    	TextArea stacktracePanel = new TextArea();
	    	stacktracePanel.setValue(error.getRootStackTrace());
	    	stacktracePanel.setStyleName("stacktrace");
	    	stacktracePanel.getElement().setAttribute("wrap", "off");
	    	vpanel.add(stacktracePanel);
    	}
    	vpanel.add(divider);
    	vpanel.add(buttonPanel);
    	
    	setWidget(vpanel);
    	
    	closeButton.setStyleName("closeButton");
    	closeButton.addStyleName("button");
    	
    	closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}

	/**
	 * @see org.overlord.sramp.ui.client.widgets.dialogs.DialogBox#handleEscapePressed()
	 */
	@Override
	protected void handleEscapePressed() {
    	hide();
	}
}
