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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Dialog implementation used for async notifications (growls).
 *
 * @author eric.wittmann@redhat.com
 */
public class GrowlDialog extends com.google.gwt.user.client.ui.DialogBox {
	
	private Label title;
	private Anchor closeButton;
	private Label message;

	/**
	 * Constructor.
	 * @param title
	 * @param message
	 */
	public GrowlDialog(String title, String message) {
		super(false, false);  // auto-hide=false, modal=false
		
		this.title = new Label(title);
		this.title.setStyleName("growlTitle");
		closeButton = new Anchor("X");
		closeButton.setStyleName("close");
		this.message = new Label(message);
		this.message.setStyleName("growlMessage");
		
		HorizontalPanel titlePanel = new HorizontalPanel();
		titlePanel.setWidth("100%");
		titlePanel.setStyleName("growlTitleBar");
		titlePanel.add(this.title);
		titlePanel.add(closeButton);
		titlePanel.setCellHorizontalAlignment(closeButton, HasHorizontalAlignment.ALIGN_RIGHT);
		titlePanel.setCellVerticalAlignment(closeButton, HasVerticalAlignment.ALIGN_MIDDLE);
		
		VerticalPanel main = new VerticalPanel();
		main.setStyleName("growlContent");
		main.add(titlePanel);
		main.add(this.message);
		
		setWidget(main);
		setStyleName("growlDialog");
		
		closeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
	}
	
	/**
	 * Adds a handler that will get called back when the user clicks the close 
	 * button.
	 * @param closeHandler
	 */
	public void addCloseHandler(ClickHandler closeHandler) {
		this.closeButton.addClickHandler(closeHandler);
	}
	
}
