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
	
}
