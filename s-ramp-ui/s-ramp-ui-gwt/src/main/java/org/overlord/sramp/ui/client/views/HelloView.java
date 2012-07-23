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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;

/**
 * 
 *
 * @author eric.wittmann@redhat.com
 */
public class HelloView extends Composite implements IHelloView {
	
	private Label messageLabel;

	/**
	 * Constructor.
	 */
	public HelloView() {
		messageLabel = new Label();
		this.initWidget(messageLabel);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.views.IHelloView#setMessage(java.lang.String)
	 */
	@Override
	public void setMessage(String message) {
		this.messageLabel.setText(message);
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IHelloView#setActivity(org.overlord.sramp.ui.client.views.IHelloView.IHelloActivity)
	 */
	@Override
	public void setActivity(IHelloActivity activity) {
	}

}
