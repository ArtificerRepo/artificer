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
 * A handler that lists all of the generated Maven files for the specific artifact
 * version selected.  This handler should, at a minimum, show a directory listing
 * with a file for the artifact content (*.xsd, *.wsdl, etc) and a file for the
 * generated artifact pom.xml.
 * 
 * The filenames are in the following format:
 * 
 * {uuid}-{version}.{type}
 * {uuid}-{version}.pom
 * 
 * The former will allow the artifact content to be downloaded, while the latter will
 * download a generated Maven compatible pom.xml for the artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactVersionHandler extends AbstractDirectoryListingHandler {

	private MavenRepositoryPath repositoryPath;

	/**
	 * Constructor.
	 * @param repositoryPath
	 */
	public ArtifactVersionHandler(MavenRepositoryPath repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	/**
	 * @see org.overlord.sramp.maven.repo.handlers.AbstractDirectoryListingHandler#generateDirectoryListing(org.overlord.sramp.maven.repo.models.DirectoryListing)
	 */
	@Override
	protected void generateDirectoryListing(DirectoryListing directoryListing) throws Exception {
		Entry fullEntry = SRAMPAtomApiClient.getInstance().getFullArtifactEntry(repositoryPath.getArtifactModel(), 
				repositoryPath.getArtifactType(), repositoryPath.getArtifactUuid());
		if (fullEntry == null) {
			throw new IllegalArgumentException("No S-RAMP artifact found with UUID: " + repositoryPath.getArtifactUuid());
		}
		ArtifactType type = ArtifactType.valueOf(repositoryPath.getArtifactType());
		Artifact srampArty = fullEntry.getAnyOtherJAXBObject(Artifact.class);
		BaseArtifactType artifact = type.unwrap(srampArty);
		String uuid = this.repositoryPath.getArtifactUuid();
		String name = artifact.getName();
		String version = this.repositoryPath.getArtifactVersion();
		String fileExtension = type.getModel();
		
		String artifactFile = String.format("%1$s-%2$s.%3$s", uuid, version, fileExtension);
		String pomFile = String.format("%1$s-%2$s.pom", uuid, version);

		Date lastModified = new Date();
		XMLGregorianCalendar lastModifiedXML = artifact.getLastModifiedTimestamp();
		if (lastModifiedXML != null)
			lastModified = lastModifiedXML.toGregorianCalendar().getTime();

		directoryListing.addDirectoryEntry("..");
		directoryListing.addFileEntry(artifactFile, name, lastModified, -1L);
		directoryListing.addFileEntry(pomFile, "Generated POM for " + name, lastModified, -1L);
	}

}
