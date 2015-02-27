/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.artificer.common.MediaType;
import org.artificer.common.ArtifactTypeEnum;

/**
 * Models the S-RAMP ServiceImplementation workspace.
 *
 * @author Brett Meyer
 */
public class ServiceImplementationWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = 1498525113317933041L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public ServiceImplementationWorkspace(String hrefBase) {
		super(hrefBase, "ServiceImplementation Model"); //$NON-NLS-1$
	}

	@Override
	protected void configureWorkspace() {
        AppCollection serviceImplementationCollection = addCollection("/s-ramp/serviceImplementation", "ServiceImplementation Model Objects", MediaType.APPLICATION_ZIP); //$NON-NLS-1$ //$NON-NLS-2$
        AppCollection serviceEndpointCollection = addCollection("/s-ramp/serviceImplementation/ServiceEndpoint", "ServiceEndpoint", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection serviceInstanceCollection = addCollection("/s-ramp/serviceImplementation/ServiceInstance", "ServiceInstance", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection serviceOperationCollection = addCollection("/s-ramp/serviceImplementation/ServiceOperation", "ServiceOperation", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        addTypeCategory(serviceImplementationCollection, ArtifactTypeEnum.ServiceEndpoint);
        addTypeCategory(serviceImplementationCollection, ArtifactTypeEnum.ServiceInstance);
        addTypeCategory(serviceImplementationCollection, ArtifactTypeEnum.ServiceOperation);

        addTypeCategory(serviceEndpointCollection, ArtifactTypeEnum.ServiceEndpoint);
        addTypeCategory(serviceInstanceCollection, ArtifactTypeEnum.ServiceInstance);
        addTypeCategory(serviceOperationCollection, ArtifactTypeEnum.ServiceOperation);
    }
}
