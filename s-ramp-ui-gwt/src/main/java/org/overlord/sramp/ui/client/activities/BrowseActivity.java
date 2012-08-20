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

import java.util.List;

import org.overlord.sramp.ui.client.IClientFactory;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.places.DashboardPlace;
import org.overlord.sramp.ui.client.services.query.IQueryService;
import org.overlord.sramp.ui.client.util.RemoteServiceAsyncCallback;
import org.overlord.sramp.ui.client.views.IBrowseView;
import org.overlord.sramp.ui.client.widgets.BreadcrumbPanel;
import org.overlord.sramp.ui.shared.beans.ArtifactSummary;
import org.overlord.sramp.ui.shared.beans.PageInfo;
import org.overlord.sramp.ui.shared.rsvcs.RemoteServiceException;
import org.overlord.sramp.ui.shared.types.ArtifactFilter;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;

/**
 * Concrete implementation of the browse activity.
 *
 * @author eric.wittmann@redhat.com
 */
public class BrowseActivity extends AbstractActivity<BrowsePlace, IBrowseView> implements IBrowseActivity {
	
	/**
	 * Constructor.
	 */
	public BrowseActivity(BrowsePlace place, IClientFactory clientFactory) {
		super(place, clientFactory);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#createView(com.google.gwt.event.shared.EventBus)
	 */
	@Override
	protected IBrowseView createView(EventBus eventBus) {
		IBrowseView view = getClientFactory().createBrowseView();
		view.setActivity(this);
		return view;
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#doStart(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
	 */
	@Override
	protected void doStart(AcceptsOneWidget panel, EventBus eventBus) {
		getView().onQueryStarting(getPlace());
		
		final PageInfo page = new PageInfo();
		page.setPage(getPlace().getPage(0));
		page.setPageSize(getPlace().getPageSize(getView().getDefaultPageSize()));
		page.setOrderBy(getPlace().getOrderBy(getView().getDefaultOrderBy()));
		page.setAscending(getPlace().isAscending(Boolean.TRUE));
		ArtifactFilter filter = null;
		if (getPlace().getTypeFilter() == null)
			filter = ArtifactFilter.all;
		else
			filter = ArtifactFilter.valueOf(getPlace().getTypeFilter());
		
		getService(IQueryService.class).findArtifactsAsync(page, filter, new RemoteServiceAsyncCallback<List<ArtifactSummary>>() {
			@Override
			public void onSuccess(List<ArtifactSummary> result) {
				getView().onQueryComplete(result, result.size() == page.getPageSize());
			}
			@Override
			protected void onRemoteServiceFailure(RemoteServiceException caught) {
				getView().onQueryFailed(caught);
			}
		});
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.activities.AbstractActivity#updateBreadcrumb(org.overlord.sramp.ui.client.widgets.BreadcrumbPanel)
	 */
	@Override
	protected void updateBreadcrumb(BreadcrumbPanel breadcrumbPanel) {
		breadcrumbPanel.addCrumb(i18n().translate("widgets.breadcrumb.dashboard"), new DashboardPlace());
		breadcrumbPanel.addCrumb(i18n().translate("widgets.breadcrumb.browse"), null);
	}

}
