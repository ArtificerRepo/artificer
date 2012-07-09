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

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.maven.repo.MavenRepositoryPath;
import org.overlord.sramp.maven.repo.atom.SRAMPAtomApiClient;
import org.overlord.sramp.maven.repo.models.DirectoryListing;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * A handler that lists all of the Artifact instances for a given specific Artifact Type.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUuidHandler extends AbstractDirectoryListingHandler {

	private MavenRepositoryPath repositoryPath;

	/**
	 * Constructor.
	 * @param repositoryPath
	 */
	public ArtifactUuidHandler(MavenRepositoryPath repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	/**
	 * @see org.overlord.sramp.maven.repo.handlers.AbstractDirectoryListingHandler#generateDirectoryListing(org.overlord.sramp.maven.repo.models.DirectoryListing)
	 */
	@Override
	protected void generateDirectoryListing(DirectoryListing directoryListing) throws Exception {
		Entry fullEntry = SRAMPAtomApiClient.getInstance().getFullArtifactEntry(repositoryPath.getArtifactModel(), 
				repositoryPath.getArtifactType(), repositoryPath.getArtifactUuid());
		ArtifactType type = ArtifactType.valueOf(repositoryPath.getArtifactType());
		Artifact srampArty = fullEntry.getAnyOtherJAXBObject(Artifact.class);
		BaseArtifactType artifact = type.unwrap(srampArty);
		String version = artifact.getVersion();
		if (version == null || version.trim().length() == 0) {
			version = "1.0";
		}
		Date lastModified = new Date();
		XMLGregorianCalendar lastModifiedXML = artifact.getLastModifiedTimestamp();
		if (lastModifiedXML != null)
			lastModified = lastModifiedXML.toGregorianCalendar().getTime();

		directoryListing.addDirectoryEntry("..");
		directoryListing.addDirectoryEntry(version, lastModified);
	}

}
