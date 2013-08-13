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

import java.util.Map;

import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;
import org.overlord.sramp.ui.client.services.i18n.LocalizationService;

/**
 * Concrete implementation of the {@link IServices} interface.
 *
 * @author eric.wittmann@redhat.com
 */
public class Services implements IServices {

	private static Services services;
	
	/**
	 * Gets the services singleton.
	 */
	public static final IServices getServices() {
		return services;
	}
	
	/**
	 * Initializes the Services infrastructure.
	 * @param registeredServices
	 * @param context 
	 * @param servicesListener
	 */
	public static void init(final Map<Class<? extends IService>, IService> registeredServices,
			ServiceLifecycleContext context, final IServicesListener servicesListener) {
		services = new Services(registeredServices);
		IServiceLifecycleListener lifecycleListener = new ServiceLifecycleListener() {
			@Override
			public void onStarted() {
				super.onStarted();
				if (getNumStarted() == registeredServices.size())
					servicesListener.onAllServicesStarted();
			}
			@Override
			public void onError(Throwable error) {
				servicesListener.onError(error);
			}
		};
		services.startAll(context, lifecycleListener);
	}

	private Map<Class<? extends IService>, IService> serviceMap;
	
	/**
	 * Constructor.
	 * @param registeredServices 
	 */
	private Services(Map<Class<? extends IService>, IService> registeredServices) {
		this.serviceMap = registeredServices;
	}

	/**
	 * Starts all of the services.
	 * @param context 
	 * @param lifecycleListener
	 */
	private void startAll(ServiceLifecycleContext context, IServiceLifecycleListener lifecycleListener) {
		for (IService service : this.serviceMap.values()) {
			service.start(context, lifecycleListener);
		}
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.IServices#getService(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends IService> T getService(Class<T> serviceType) throws ServiceNotFoundException {
		IService svc = serviceMap.get(serviceType);
		if (svc == null) {
			if (serviceType.equals(ILocalizationService.class)) {
				svc = new LocalizationService();
			}
		}
		if (svc != null) {
			serviceMap.put(serviceType, svc);
			return (T) svc;
		} else {
			throw new ServiceNotFoundException();
		}
	}
	
	/**
	 * Internal service lifecycle listener implementation.
	 *
	 * @author eric.wittmann@redhat.com
	 */
	private abstract static class ServiceLifecycleListener implements IServiceLifecycleListener {
		private int numStarted = 0;
		
		/**
		 * Constructor.
		 */
		public ServiceLifecycleListener() {
		}

		/**
		 * @see org.overlord.sramp.ui.client.services.IServiceLifecycleListener#onStarted()
		 */
		@Override
		public void onStarted() {
			this.numStarted++;
		}

		/**
		 * @return the numStarted
		 */
		public int getNumStarted() {
			return numStarted;
		}

	}

}
