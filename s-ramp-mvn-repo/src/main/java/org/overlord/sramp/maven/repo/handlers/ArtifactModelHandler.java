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
package org.overlord.sramp.maven.repo.handlers;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.maven.repo.models.DirectoryListing;


/**
 * A handler that lists all of the Artifact Types for a given Artifact Model.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactModelHandler extends AbstractDirectoryListingHandler {

	private String artifactModel;
	
	/**
	 * Constructor.
	 */
	public ArtifactModelHandler(String artifactModel) {
		this.artifactModel = artifactModel;
	}

	/**
	 * @see org.overlord.sramp.maven.repo.handlers.AbstractDirectoryListingHandler#generateDirectoryListing(org.overlord.sramp.maven.repo.models.DirectoryListing)
	 */
	@Override
	protected void generateDirectoryListing(DirectoryListing directoryListing) {
		directoryListing.addDirectoryEntry("..");
		for (ArtifactType type : ArtifactType.values()) {
			if (type.getModel().equals(this.artifactModel)) {
				directoryListing.addDirectoryEntry(type.name());
			}
		}
	}
	
}
