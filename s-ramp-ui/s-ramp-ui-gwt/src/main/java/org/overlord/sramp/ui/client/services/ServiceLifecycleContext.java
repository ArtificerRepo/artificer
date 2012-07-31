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
package org.overlord.sramp.ui.client.services;

import org.overlord.sramp.ui.client.IClientFactory;

import com.google.web.bindery.event.shared.EventBus;

/**
 * Context passed to all {@link IService}s as they move through their lifecycle.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceLifecycleContext {

	private IClientFactory clientFactory;
	private EventBus eventBus;
	
	/**
	 * Constructor.
	 */
	public ServiceLifecycleContext() {
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
	 * @return the eventBus
	 */
	public EventBus getEventBus() {
		return eventBus;
	}

	/**
	 * @param eventBus the eventBus to set
	 */
	public void setEventBus(EventBus eventBus) {
		this.eventBus = eventBus;
	}
	
}
