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
import org.overlord.sramp.ui.client.services.artifact.IArtifactService;
import org.overlord.sramp.ui.client.util.RemoteServiceAsyncCallback;
import org.overlord.sramp.ui.client.views.IArtifactView;
import org.overlord.sramp.ui.client.widgets.BreadcrumbPanel;
import org.overlord.sramp.ui.shared.beans.ArtifactDetails;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;

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
		getView().onArtifactLoading(getPlace());
		
		getService(IArtifactService.class).getArtifactDetailsAsync(getPlace().getModel(),
				getPlace().getType(), getPlace().getUuid(),
				new RemoteServiceAsyncCallback<ArtifactDetails>() {
					@Override
					public void onSuccess(ArtifactDetails artifact) {
						getView().onArtifactLoaded(artifact);
					}
					@Override
					protected void onRemoteServiceFailure(RemoteServiceException caught) {
						getView().onArtifactLoadError(caught);
					}
				});
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
