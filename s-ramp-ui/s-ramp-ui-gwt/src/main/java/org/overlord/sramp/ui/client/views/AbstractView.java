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

import org.overlord.sramp.ui.client.activities.IActivity;
import org.overlord.sramp.ui.client.services.IService;
import org.overlord.sramp.ui.client.services.ServiceNotFoundException;
import org.overlord.sramp.ui.client.services.Services;
import org.overlord.sramp.ui.client.services.growl.IGrowlService;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;

import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for all view implementations.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractView<A extends IActivity> extends Composite implements IView<A> {
	
	private A activity;
	
	/**
	 * Constructor.
	 */
	public AbstractView() {
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.views.IView#getActivity()
	 */
	@Override
	public A getActivity() {
		return activity;
	}

	/**
	 * @see org.overlord.sramp.ui.client.views.IView#setActivity(org.overlord.sramp.ui.client.activities.IActivity)
	 */
	@Override
	public void setActivity(A activity) {
		this.activity = activity;
	}
	
	/**
	 * Gets a service.
	 * @param serviceType the type of service desired
	 * @return the service
	 * @throws ServiceNotFoundException
	 */
	protected <T extends IService> T getService(Class<T> serviceType) throws ServiceNotFoundException {
		return Services.getServices().getService(serviceType);
	}

	/**
	 * Convenience method for getting the localization service.
	 */
	protected ILocalizationService i18n() {
		return getService(ILocalizationService.class);
	}
	
	/**
	 * Convenience method for getting the growl service.
	 */
	protected IGrowlService growl() {
		return getService(IGrowlService.class);
	}

}
