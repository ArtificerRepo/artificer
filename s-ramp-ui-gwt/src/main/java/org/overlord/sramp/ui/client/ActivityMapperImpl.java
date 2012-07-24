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

import org.overlord.sramp.ui.client.activities.ArtifactActivity;
import org.overlord.sramp.ui.client.activities.BrowseActivity;
import org.overlord.sramp.ui.client.activities.DashboardActivity;
import org.overlord.sramp.ui.client.places.ArtifactPlace;
import org.overlord.sramp.ui.client.places.BrowsePlace;
import org.overlord.sramp.ui.client.places.DashboardPlace;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;

/**
 * Simple activity mapper - maps incoming places to their appropriate activities.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ActivityMapperImpl implements ActivityMapper {

	private IClientFactory clientFactory;

	/**
	 * Constructor.
	 * @param clientFactory
	 */
	public ActivityMapperImpl(IClientFactory clientFactory) {
		super();
		this.clientFactory = clientFactory;
	}

	/**
	 * @see com.google.gwt.activity.shared.ActivityMapper#getActivity(com.google.gwt.place.shared.Place)
	 */
	@Override
	public Activity getActivity(Place place) {
		if (place instanceof DashboardPlace)
			return new DashboardActivity((DashboardPlace) place, clientFactory);
		if (place instanceof BrowsePlace)
			return new BrowseActivity((BrowsePlace) place, clientFactory);
		if (place instanceof ArtifactPlace)
			return new ArtifactActivity((ArtifactPlace) place, clientFactory);
		return null;
	}
}
