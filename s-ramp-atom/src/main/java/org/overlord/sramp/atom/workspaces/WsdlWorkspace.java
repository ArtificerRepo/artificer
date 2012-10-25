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
package org.overlord.sramp.atom.workspaces;

import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.overlord.sramp.ArtifactTypeEnum;
import org.overlord.sramp.atom.MediaType;

/**
 * Models the S-RAMP wsdl workspace.
 *
 * @author eric.wittmann@redhat.com
 */
public class WsdlWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = -4557417972386190034L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public WsdlWorkspace(String hrefBase) {
		super(hrefBase, "WSDL Model");
	}

	/**
	 * @see org.overlord.sramp.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
        AppCollection wsdlCollection = addCollection("/s-ramp/wsdl", "WSDL Model Objects", MediaType.APPLICATION_ZIP);
        AppCollection wsdlDocumentCollection = addCollection("/s-ramp/wsdl/WsdlDocument", "WSDL Documents", MediaType.APPLICATION_XML);
        AppCollection wsdlServiceCollection = addCollection("/s-ramp/wsdl/WsdlService", "WSDL Services", "");
        AppCollection portCollection = addCollection("/s-ramp/wsdl/Port", "Ports", "");
        AppCollection wsdlExtensionCollection = addCollection("/s-ramp/wsdl/WsdlExtension", "WSDL Extensions", "");
        AppCollection partCollection = addCollection("/s-ramp/wsdl/Part", "Parts", "");
        AppCollection messageCollection = addCollection("/s-ramp/wsdl/Message", "Messages", "");
        AppCollection faultCollection = addCollection("/s-ramp/wsdl/Fault", "Faults", "");
        AppCollection portTypeCollection = addCollection("/s-ramp/wsdl/PortType", "Port Types", "");
        AppCollection operationCollection = addCollection("/s-ramp/wsdl/Operation", "Operations", "");
        AppCollection operationInputCollection = addCollection("/s-ramp/wsdl/OperationInput", "Operation Inputs", "");
        AppCollection operationOutputCollection = addCollection("/s-ramp/wsdl/OperationOutput", "Operation Outputs", "");
        AppCollection bindingCollection = addCollection("/s-ramp/wsdl/Binding", "Bindings", "");
        AppCollection bindingOperationCollection = addCollection("/s-ramp/wsdl/BindingOperation", "Binding Operations", "");
        AppCollection bindingOperationInputCollection = addCollection("/s-ramp/wsdl/BindingOperationInput", "Binding Operation Inputs", "");
        AppCollection bindingOperationOutputCollection = addCollection("/s-ramp/wsdl/BindingOperationOutput", "Bindig Operation Outputs", "");
        AppCollection bindingOperationFaultCollection = addCollection("/s-ramp/wsdl/BindingOperationFault", "Binding Operation Faults", "");

        addTypeCategory(wsdlCollection, ArtifactTypeEnum.WsdlDocument);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.WsdlService);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.Port);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.WsdlExtension);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.Part);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.Message);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.Fault);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.PortType);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.Operation);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.OperationInput);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.OperationOutput);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.Binding);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.BindingOperation);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.BindingOperationInput);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.BindingOperationOutput);
        addTypeCategory(wsdlCollection, ArtifactTypeEnum.BindingOperationFault);

        addTypeCategory(wsdlDocumentCollection, ArtifactTypeEnum.WsdlDocument);
        addTypeCategory(wsdlServiceCollection, ArtifactTypeEnum.WsdlService);
        addTypeCategory(portCollection, ArtifactTypeEnum.Port);
        addTypeCategory(wsdlExtensionCollection, ArtifactTypeEnum.WsdlExtension);
        addTypeCategory(partCollection, ArtifactTypeEnum.Part);
        addTypeCategory(messageCollection, ArtifactTypeEnum.Message);
        addTypeCategory(faultCollection, ArtifactTypeEnum.Fault);
        addTypeCategory(portTypeCollection, ArtifactTypeEnum.PortType);
        addTypeCategory(operationCollection, ArtifactTypeEnum.Operation);
        addTypeCategory(operationInputCollection, ArtifactTypeEnum.OperationInput);
        addTypeCategory(operationOutputCollection, ArtifactTypeEnum.OperationOutput);
        addTypeCategory(bindingCollection, ArtifactTypeEnum.Binding);
        addTypeCategory(bindingOperationCollection, ArtifactTypeEnum.BindingOperation);
        addTypeCategory(bindingOperationInputCollection, ArtifactTypeEnum.BindingOperationInput);
        addTypeCategory(bindingOperationOutputCollection, ArtifactTypeEnum.BindingOperationOutput);
        addTypeCategory(bindingOperationFaultCollection, ArtifactTypeEnum.BindingOperationFault);
    }
}
