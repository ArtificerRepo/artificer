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

import java.util.HashMap;
import java.util.Map;

import org.overlord.sramp.ui.client.services.artifact.ArtifactService;
import org.overlord.sramp.ui.client.services.artifact.IArtifactService;
import org.overlord.sramp.ui.client.services.breadcrumb.BreadcrumbService;
import org.overlord.sramp.ui.client.services.breadcrumb.IBreadcrumbService;
import org.overlord.sramp.ui.client.services.growl.GrowlService;
import org.overlord.sramp.ui.client.services.growl.IGrowlService;
import org.overlord.sramp.ui.client.services.i18n.ILocalizationService;
import org.overlord.sramp.ui.client.services.i18n.LocalizationService;
import org.overlord.sramp.ui.client.services.place.IPlaceService;
import org.overlord.sramp.ui.client.services.place.PlaceService;
import org.overlord.sramp.ui.client.services.query.IQueryService;
import org.overlord.sramp.ui.client.services.query.QueryService;

/**
 * The centralized list of all {@link IService}s.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServiceList {

	private static final Object[][] SERVICES = {
		
		{ ILocalizationService.class, new LocalizationService() },
		{ IPlaceService.class, new PlaceService() },
		{ IBreadcrumbService.class, new BreadcrumbService() },
		{ IQueryService.class, new QueryService() },
		{ IArtifactService.class, new ArtifactService() },
		{ IGrowlService.class, new GrowlService() },
		
	};
	
	/**
	 * Gets the map of registered services.
	 */
	@SuppressWarnings("unchecked")
	public static final Map<Class<? extends IService>, IService> getRegisteredServices() {
		Map<Class<? extends IService>, IService> services = new HashMap<Class<? extends IService>, IService>();
		for (Object[] serviceSpec : SERVICES) {
			Class<? extends IService> svcClass = (Class<? extends IService>) serviceSpec[0];
			IService svc = (IService) serviceSpec[1];
			services.put(svcClass, svc);
		}
		return services;
	}
	
}
