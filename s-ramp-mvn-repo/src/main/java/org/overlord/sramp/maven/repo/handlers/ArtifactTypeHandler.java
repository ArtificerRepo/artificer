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

import org.overlord.sramp.maven.repo.MavenRepositoryPath;
import org.overlord.sramp.maven.repo.models.DirectoryListing;

/**
 * A handler that lists all of the Artifact instances for a given specific Artifact Type.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactTypeHandler extends AbstractDirectoryListingHandler {

	private MavenRepositoryPath repositoryPath;
	
	/**
	 * Constructor.
	 * @param repositoryPath
	 */
	public ArtifactTypeHandler(MavenRepositoryPath repositoryPath) {
		this.repositoryPath = repositoryPath;
	}
	
	/**
	 * @see org.overlord.sramp.maven.repo.handlers.AbstractDirectoryListingHandler#generateDirectoryListing(org.overlord.sramp.maven.repo.models.DirectoryListing)
	 */
	@Override
	protected void generateDirectoryListing(DirectoryListing directoryListing) {
//		SRAMPAtomFeed feed = SRAMPAtomApiClient.getInstance().getFeed(repositoryPath.getArtifactModel(), repositoryPath.getArtifactType());
	}

}
