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
import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.places.DashboardPlace;
import org.overlord.sramp.ui.client.views.IArtifactView;
import org.overlord.sramp.ui.client.widgets.BreadcrumbPanel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Concrete implementation of the artifact activity.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactActivity extends AbstractActivity<ArtifactPlace, IArtifactView> implements IArtifactActivity {

	/**
	 * Constructor.
	 */
	public ArtifactActivity(ArtifactPlace place, IClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#createView(com.google.gwt.event.shared.EventBus)
	 */
	@Override
	protected IArtifactView createView(EventBus eventBus) {
		IArtifactView view = getClientFactory().createArtifactView();
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
		breadcrumbPanel.addCrumb(i18n().translate("breadcrumb.dashboard"), new DashboardPlace());
		breadcrumbPanel.addCrumb(i18n().translate("breadcrumb.browse"), new BrowsePlace());
		breadcrumbPanel.addCrumb(i18n().translate("breadcrumb.artifact-details"), null);
	}
	
}
