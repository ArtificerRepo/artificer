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
package org.overlord.sramp.repository;

import java.io.InputStream;
import java.util.Collection;

import org.overlord.sramp.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * Service used to persist artifacts to some (permanent?) storage.
 */
public interface PersistenceManager {

    /**
     * Persists a single artifact.
     * @param baseArtifactType, the s-ramp artifact
     * @param content, the artifact content
     * @throws RepositoryException
     */
    public BaseArtifactType persistArtifact(BaseArtifactType baseArtifactType, InputStream content) throws RepositoryException;

    /**
     * Persists a single derived artifact.
     * @param sourceArtifact the source of the derived content
     * @param artifacts the derived artifacts to persist
     * @throws RepositoryException
     */
    public void persistDerivedArtifacts(BaseArtifactType sourceArtifact, Collection<DerivedArtifactType> artifacts) throws RepositoryException;

	/**
	 * Gets a previously persisted artifact by its UUID.
	 * @param uuid the UUID of the s-ramp artifact
	 * @param artifactType the type of the artifact
	 * @return an instance of a {@link BaseArtifactType} or null if not found
	 * @throws RepositoryException
	 */
	public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws RepositoryException;

	/**
	 * Gets the content (media) for a previously persisted artifact by its UUID.
	 * <br/><br/>
	 * <b><i>Note: it is the responsibility of the calling method to close the
	 * resulting {@link InputStream}.</i></b>
	 * @param uuid the S-RAMP uuid of the artifact.
	 * @param artifactType the type of the artifact
	 * @return an {@link InputStream} over the artifact content
	 * @throws RepositoryException
	 */
	public InputStream getArtifactContent(String uuid, ArtifactType artifactType) throws RepositoryException;

	/**
	 * Updates a previously persisted artifact.  Note that this method only updates the meta data
	 * of the artifact, not the content.  This will not create or delete any derived artifacts.
	 * @param artifact the s-ramp artifact being updated
	 * @param type the type of the artifact
	 * @throws RepositoryException
	 */
	public void updateArtifact(BaseArtifactType artifact, ArtifactType type) throws RepositoryException;

	/**
	 * Called to update the content for an existing artifact.
	 * @param uuid a UUID of an existing artifact
	 * @param artifactType the artifact's type
	 * @param content the new artifact content
	 * @throws RepositoryException
	 */
	public void updateArtifactContent(String uuid, ArtifactType artifactType, InputStream content) throws RepositoryException;

	/**
	 * Deletes a previously persisted artifact from the S-RAMP repository.
	 * @param uuid
	 * @param artifactType
	 * @throws RepositoryException
	 */
	public void deleteArtifact(String uuid, ArtifactType artifactType) throws RepositoryException;

	/**
	 * TODO remove this
	 * @param uuid
	 * @param type
	 */
    public void printArtifactGraph(String uuid, ArtifactType type);

    public void shutdown();
}
