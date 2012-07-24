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
import org.overlord.sramp.ui.client.views.IView;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.AcceptsOneWidget;


/**
 * Base class for all s-ramp-ui activities.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractActivity<P extends Place, V extends IView<?>> extends com.google.gwt.activity.shared.AbstractActivity implements IActivity {

	private P place;
	private V view;
	private IClientFactory clientFactory;
	
	/**
	 * Constructor.
	 * @param place
	 * @param clientFactory
	 */
	public AbstractActivity(P place, IClientFactory clientFactory) {
		setPlace(place);
		setClientFactory(clientFactory);
	}

	/**
	 * @see org.overlord.sramp.ui.client.activities.IActivity#goTo(com.google.gwt.place.shared.Place)
	 */
	@Override
	public void goTo(Place place) {
		getClientFactory().getPlaceController().goTo(place);
	}

	/**
	 * @return the place
	 */
	public P getPlace() {
		return place;
	}

	/**
	 * @param place the place to set
	 */
	public void setPlace(P place) {
		this.place = place;
	}

	/**
	 * @return the view
	 */
	public V getView() {
		return view;
	}

	/**
	 * @param view the view to set
	 */
	public void setView(V view) {
		this.view = view;
	}

	/**
	 * @return the clientFactory
	 */
	public IClientFactory getClientFactory() {
		return clientFactory;
	}

	/**
	 * @param clientFactory the clientFactory to set
	 */
	public void setClientFactory(IClientFactory clientFactory) {
		this.clientFactory = clientFactory;
	}
	
	/**
	 * @see com.google.gwt.activity.shared.Activity#start(com.google.gwt.user.client.ui.AcceptsOneWidget, com.google.gwt.event.shared.EventBus)
	 */
	@Override
	public final void start(AcceptsOneWidget panel, EventBus eventBus) {
		V view = createView(eventBus);
		setView(view);
		panel.setWidget(view.asWidget());
		doStart(panel, eventBus);
	}

	/**
	 * Called to create/get the view.
	 * @param eventBus
	 */
	protected abstract V createView(EventBus eventBus);

	/**
	 * Start the activity.
	 * @param panel
	 * @param eventBus
	 */
	protected void doStart(AcceptsOneWidget panel, EventBus eventBus) {
	}

}
