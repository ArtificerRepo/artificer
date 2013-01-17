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
 * Models the S-RAMP core workspace.
 * @author kstam
 */
public class CoreWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = -4557417972386190034L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public CoreWorkspace(String hrefBase) {
		super(hrefBase, "Core Model");
	}

	/**
	 * @see org.overlord.sramp.common.server.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
        AppCollection coreCollection = addCollection("/s-ramp/core", "Core Model Objects", MediaType.APPLICATION_ZIP);
        AppCollection documentCollection = addCollection("/s-ramp/core/Document", "Documents", MediaType.APPLICATION_OCTET_STREAM);
        AppCollection xmlDocumentCollection = addCollection("/s-ramp/core/XmlDocument", "XML Documents", MediaType.APPLICATION_XML);

        addTypeCategory(coreCollection, ArtifactTypeEnum.Document);
        addTypeCategory(coreCollection, ArtifactTypeEnum.XmlDocument);

        addTypeCategory(documentCollection, ArtifactTypeEnum.Document);
        addTypeCategory(xmlDocumentCollection, ArtifactTypeEnum.XmlDocument);
    }
}
