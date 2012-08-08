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

import org.overlord.sramp.ui.client.events.OkCancelEvent;
import org.overlord.sramp.ui.client.events.OkCancelEvent.Handler;
import org.overlord.sramp.ui.client.events.OkCancelEvent.HasOkCancelHandlers;
import org.overlord.sramp.ui.client.events.OkCancelEvent.OkCancelKind;
import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A base class that provides some basic functionality for dialogs that have an OK and Cancel
 * button.
 * 
 * @author eric.wittmann@redhat.com
 */
public class OkCancelDialogBox extends DialogBox implements HasOkCancelHandlers {

	private FlowPanel contentWrapper;
	private Button okButton;
	private Button cancelButton;

	/**
     * Constructor.
     * @param title
     */
    public OkCancelDialogBox(String title) {
    	super(title);
    	addStyleName("okCancelDialog");
    	
    	ILocalizationService i18n = Services.getServices().getService(ILocalizationService.class);

    	contentWrapper = new FlowPanel();
    	contentWrapper.setStyleName("okCancelDialogContent");
    	okButton = new Button(i18n.translate("dialogs.ok"));
    	cancelButton = new Button(i18n.translate("dialogs.cancel"));

		VerticalPanel vpanel = new VerticalPanel();
    	vpanel.add(contentWrapper);
    	HorizontalPanel buttonPanel = new HorizontalPanel();
    	buttonPanel.setStyleName("buttonPanel");
    	buttonPanel.addStyleName("okCancelButtonPanel");
    	buttonPanel.add(okButton);
    	buttonPanel.add(cancelButton);
    	vpanel.add(buttonPanel);
    	
    	super.setWidget(vpanel);
    	
    	okButton.setStyleName("okButton");
    	okButton.addStyleName("button");
    	cancelButton.setStyleName("cancelButton");
    	cancelButton.addStyleName("button");
    	
    	okButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireOk();
			}
		});
    	cancelButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fireCancel();
			}
		});
    }
    
    /**
     * @see com.google.gwt.user.client.ui.DecoratedPopupPanel#setWidget(com.google.gwt.user.client.ui.Widget)
     */
    @Override
    public void setWidget(Widget w) {
    	this.contentWrapper.clear();
    	this.contentWrapper.add(w);
    }

    /**
	 * Fires the OK event.
	 */
	protected void fireOk() {
		OkCancelEvent.fire(this, OkCancelKind.ok);
	}

	/**
	 * Fires the cancel event.
	 */
	protected void fireCancel() {
		this.hide();
		OkCancelEvent.fire(this, OkCancelKind.cancel);
	}

	/**
     * @see org.overlord.sramp.ui.client.events.OkCancelEvent.HasOkCancelHandlers#addOkCancelHandler(org.overlord.sramp.ui.client.events.OkCancelEvent.Handler)
     */
    @Override
    public HandlerRegistration addOkCancelHandler(Handler handler) {
        return addHandler(handler, OkCancelEvent.getType());
    }

	/**
	 * @see org.overlord.sramp.ui.client.widgets.dialogs.DialogBox#handleEscapePressed()
	 */
	@Override
	protected void handleEscapePressed() {
    	fireCancel();
	}

    /**
     * Creates a standard OK button click handler.
     */
    protected ClickHandler createOkButtonClickHandler() {
    	return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
			}
		};
    }

	/**
     * Creates a standard Cancel button click handler.
     */
    protected ClickHandler createCancelButtonClickHandler() {
    	return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		};
    }

}
