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

import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Implements a single panel area with a title bar.
 *
 * @author eric.wittmann@redhat.com
 */
public class TitlePanel extends VerticalPanel implements HasOneWidget {

	private Label titleLabel;
	private SimplePanel contentWrapper;

	/**
	 * Constructor.
	 */
	public TitlePanel() {
		getElement().addClassName("titlePanel");

		titleLabel = new Label();
		titleLabel.getElement().setClassName("title");
		add(titleLabel);
		
		contentWrapper = new SimplePanel();
		contentWrapper.getElement().setClassName("titlePanelContent");
		add(contentWrapper);
	}

	/**
	 * Constructor.
	 */
	public TitlePanel(String title) {
		this();
		setTitle(title);
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {
		this.titleLabel.setText(title);
	}

	/**
	 * @see com.google.gwt.user.client.ui.AcceptsOneWidget#setWidget(com.google.gwt.user.client.ui.IsWidget)
	 */
	@Override
	public void setWidget(IsWidget w) {
		setWidget(w.asWidget());
	}

	/**
	 * @see com.google.gwt.user.client.ui.HasOneWidget#getWidget()
	 */
	@Override
	public Widget getWidget() {
		return contentWrapper.getWidget();
	}

	/**
	 * @see com.google.gwt.user.client.ui.HasOneWidget#setWidget(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void setWidget(Widget w) {
		contentWrapper.setWidget(w);
	}

	/**
	 * @see com.google.gwt.user.client.ui.VerticalPanel#add(com.google.gwt.user.client.ui.Widget)
	 */
	@Override
	public void add(Widget w) {
		if (w != this.titleLabel && w != this.contentWrapper)
			throw new IllegalStateException("Please call setWidget().");
		else
			super.add(w);
	}
	
	/**
	 * @see com.google.gwt.user.client.ui.Panel#clear()
	 */
	@Override
	public void clear() {
		this.contentWrapper.clear();
	}

}
