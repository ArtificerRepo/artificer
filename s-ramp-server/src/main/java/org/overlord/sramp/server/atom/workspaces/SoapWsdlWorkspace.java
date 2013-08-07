/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.server.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.common.ArtifactTypeEnum;

/**
 * Models the S-RAMP soapWsdl workspace.
 *
 * @author eric.wittmann@redhat.com
 */
public class SoapWsdlWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = 1498525113317933041L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public SoapWsdlWorkspace(String hrefBase) {
		super(hrefBase, "SOAP-WSDL Model"); //$NON-NLS-1$
	}

	/**
	 * @see org.overlord.sramp.common.server.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
        AppCollection soapWsdlCollection = addCollection("/s-ramp/soapWsdl", "SOAP-WSDL Model Objects", MediaType.APPLICATION_ZIP); //$NON-NLS-1$ //$NON-NLS-2$
        AppCollection soapAddressCollection = addCollection("/s-ramp/soapWsdl/SoapAddress", "SOAP Addresses", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        AppCollection soapBindingCollection = addCollection("/s-ramp/soapWsdl/SoapBinding", "SOAP Bindings", ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        addTypeCategory(soapWsdlCollection, ArtifactTypeEnum.SoapAddress);
        addTypeCategory(soapWsdlCollection, ArtifactTypeEnum.SoapBinding);

        addTypeCategory(soapAddressCollection, ArtifactTypeEnum.SoapAddress);
        addTypeCategory(soapBindingCollection, ArtifactTypeEnum.SoapBinding);
    }
}
