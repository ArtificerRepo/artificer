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
package org.overlord.sramp.ui.client.services.breadcrumb;

import org.overlord.sramp.ui.client.services.AbstractService;
import org.overlord.sramp.ui.client.services.IServiceLifecycleListener;
import org.overlord.sramp.ui.client.widgets.BreadcrumbPanel;

/**
 * Concrete implementation of the breadcrumb service.
 *
 * @author eric.wittmann@redhat.com
 */
public class BreadcrumbService extends AbstractService implements IBreadcrumbService {
	
	private BreadcrumbPanel breadcrumbPanel;

	/**
	 * Constructor.
	 */
	public BreadcrumbService() {
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.AbstractService#start(org.overlord.sramp.ui.client.services.IServiceLifecycleListener)
	 */
	@Override
	public void start(IServiceLifecycleListener serviceListener) {
		this.breadcrumbPanel = new BreadcrumbPanel();
		super.start(serviceListener);
	}
	
	/**
	 * @see org.overlord.sramp.ui.client.services.breadcrumb.IBreadcrumbService#getBreadcrumbPanel()
	 */
	@Override
	public BreadcrumbPanel getBreadcrumbPanel() {
		return this.breadcrumbPanel;
	}
	
}
