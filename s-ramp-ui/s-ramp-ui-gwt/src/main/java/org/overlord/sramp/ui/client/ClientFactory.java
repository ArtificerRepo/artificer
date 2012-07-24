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
package org.overlord.sramp.ui.client;

import org.overlord.sramp.ui.client.views.ArtifactView;
import org.overlord.sramp.ui.client.views.BrowseView;
import org.overlord.sramp.ui.client.views.DashboardView;
import org.overlord.sramp.ui.client.views.IArtifactView;
import org.overlord.sramp.ui.client.views.IBrowseView;
import org.overlord.sramp.ui.client.views.IDashboardView;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.SimpleEventBus;

/**
 * A factory for creating/getting a variety of services and UI components.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ClientFactory implements IClientFactory {

	private final EventBus eventBus = new SimpleEventBus();
	private final PlaceController placeController = new PlaceController(eventBus);

	/**
	 * Constructor.
	 */
	public ClientFactory() {
	}

	/**
	 * @see org.overlord.sramp.ui.client.IClientFactory#getEventBus()
	 */
	@Override
	public EventBus getEventBus() {
		return eventBus;
	}

	/**
	 * @see org.overlord.sramp.ui.client.IClientFactory#getPlaceController()
	 */
	@Override
	public PlaceController getPlaceController() {
		return placeController;
	}

	/**
	 * @see org.overlord.sramp.ui.client.IClientFactory#createDashboardView()
	 */
	@Override
	public IDashboardView createDashboardView() {
		return new DashboardView();
	}

	/**
	 * @see org.overlord.sramp.ui.client.IClientFactory#createBrowseView()
	 */
	@Override
	public IBrowseView createBrowseView() {
		return new BrowseView();
	}

	/**
	 * @see org.overlord.sramp.ui.client.IClientFactory#createArtifactView()
	 */
	@Override
	public IArtifactView createArtifactView() {
		return new ArtifactView();
	}

}
