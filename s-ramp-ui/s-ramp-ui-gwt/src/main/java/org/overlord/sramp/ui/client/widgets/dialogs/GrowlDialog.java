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

import org.overlord.sramp.ui.client.services.growl.GrowlConstants;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.Window;
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
	private boolean mouseIn = false;

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
	
	/**
	 * @see com.google.gwt.user.client.ui.DialogBox#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
	 */
	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
	    super.onPreviewNativeEvent(event);
	    if (event.getTypeInt() == Event.ONMOUSEMOVE) {
	    	int x = event.getNativeEvent().getClientX();
	    	int y = event.getNativeEvent().getClientY();
	    	handleMouseMove(x, y);
	    }
	}

	/**
	 * Called every time the mouse moves.  This method tries to determine proper mouse-in and
	 * mouse-out events.
	 * @param clientX
	 * @param clientY
	 */
	private void handleMouseMove(int clientX, int clientY) {
		if (isMouseInMe(clientX, clientY)) {
			if (this.mouseIn) {
				// do nothing
			} else {
				this.mouseIn = true;
				onMouseIn();
			}
		} else {
			if (this.mouseIn) {
				onMouseOut();
				this.mouseIn = false;
			}
		}
	}

	/**
	 * Returns true if the given screen coordinates lie within the boundary of the growl dialog.
	 * @param clientX
	 * @param clientY
	 */
	private boolean isMouseInMe(int clientX, int clientY) {
		try {
			String bottomStyle = getElement().getStyle().getBottom();
			int bottom = new Integer(bottomStyle.split("px")[0]).intValue();
			bottom = Window.getClientHeight() - bottom;
			
			int top = bottom - GrowlConstants.GROWL_HEIGHT;
			int left = Window.getClientWidth() - GrowlConstants.GROWL_WIDTH - GrowlConstants.GROWL_MARGIN;
			int right = left + GrowlConstants.GROWL_WIDTH;
			return clientX >= left && clientX <= right && clientY >= top && clientY <= bottom;
		} catch (Throwable t) {
			return false;
		}
	}

	/**
	 * Called when the mouse enters the dialog.
	 */
	protected void onMouseIn() {
		addStyleName("growlDialog-hover");
	}

	/**
	 * Called when the mouse leaves the dialog.
	 */
	protected void onMouseOut() {
		removeStyleName("growlDialog-hover");
	}

}
