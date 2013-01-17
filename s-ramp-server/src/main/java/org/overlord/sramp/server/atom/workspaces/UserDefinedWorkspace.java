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
 * Models the S-RAMP user-defined workspace.
 * @author eric.wittmann@redhat.com
 */
public class UserDefinedWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = 1837991326302484187L;

	/**
	 * Constructor.
	 * @param hrefBase
	 */
	public UserDefinedWorkspace(String hrefBase) {
		super(hrefBase, "User Defined Model");
	}

	/**
	 * @see org.overlord.sramp.common.server.atom.workspaces.AbstractWorkspace#configureWorkspace()
	 */
	@Override
	protected void configureWorkspace() {
        AppCollection userCollection = addCollection("/s-ramp/user", "User Defined Model Objects", MediaType.APPLICATION_OCTET_STREAM);

        addTypeCategory(userCollection, ArtifactTypeEnum.UserDefinedArtifactType);
    }
}
