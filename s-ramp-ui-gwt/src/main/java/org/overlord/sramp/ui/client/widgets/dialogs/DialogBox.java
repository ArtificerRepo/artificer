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

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Event.NativePreviewEvent;


/**
 * Base class for all dialog boxes.
 *
 * @author eric.wittmann@redhat.com
 */
public class DialogBox extends com.google.gwt.user.client.ui.DialogBox {

	/**
	 * Constructor.
	 * @param title
	 * @param autoHide
	 * @param modal
	 */
	public DialogBox(String title, boolean autoHide, boolean modal) {
		super(autoHide, modal);
		setGlassEnabled(true);
		setGlassStyleName("dialogGlass");
		setText(title);
    	setStyleName("dialog");
	}

	/**
	 * Constructor.
	 * @param title
	 */
	public DialogBox(String title) {
		this(title, false, true);
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.DialogBox#onPreviewNativeEvent(com.google.gwt.user.client.Event.NativePreviewEvent)
	 */
	@Override
	protected void onPreviewNativeEvent(NativePreviewEvent event) {
	    super.onPreviewNativeEvent(event);
	    if (event.getTypeInt() == Event.ONKEYDOWN && event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
	    	handleEscapePressed();
	    }
	}

	/**
	 * Subclasses may choose to do something (like close the dialog) when ESC is pressed.
	 */
	protected void handleEscapePressed() {
	}

}
