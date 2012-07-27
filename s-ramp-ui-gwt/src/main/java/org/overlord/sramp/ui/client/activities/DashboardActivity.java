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
package org.overlord.sramp.ui.client.activities;

import org.overlord.sramp.ui.client.IClientFactory;
import org.overlord.sramp.ui.client.places.DashboardPlace;
import org.overlord.sramp.ui.client.views.IDashboardView;
import org.overlord.sramp.ui.client.widgets.BreadcrumbPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Concrete implementation of the dashboard activity.
 *
 * @author eric.wittmann@redhat.com
 */
public class DashboardActivity extends AbstractActivity<DashboardPlace, IDashboardView> implements IDashboardActivity {

	/**
	 * Constructor.
	 */
	public DashboardActivity(DashboardPlace place, IClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#createView(com.google.gwt.event.shared.EventBus)
	 */
	@Override
	protected IDashboardView createView(EventBus eventBus) {
		IDashboardView view = getClientFactory().createDashboardView();
		view.setActivity(this);
		return view;
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#doStart(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
	 */
	@Override
	protected void doStart(AcceptsOneWidget panel, EventBus eventBus) {
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#updateBreadcrumb(org.overlord.sramp.ui.client.widgets.BreadcrumbPanel)
	 */
	@Override
	protected void updateBreadcrumb(BreadcrumbPanel breadcrumbPanel) {
	}
	
}
