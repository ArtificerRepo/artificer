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
import java.util.List;

import org.overlord.sramp.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * Service used to persist artifacts to some (permanent?) storage.
 */
public interface PersistenceManager {

	/**
	 * Persists a single artifact.
	 * @param name the name of the artifact
	 * @param type the artifact type
	 * @param content the artifact content
	 * @throws UnsupportedFiletypeException
	 */
    public BaseArtifactType persistArtifact(String name, ArtifactType type, InputStream content) throws UnsupportedFiletypeException;
    
    /**
     * Persists a single derived artifact.
     * @param artifact the derived artifact to persist
     */
    public void persistDerivedArtifact(DerivedArtifactType artifact);

	/**
	 * Gets a previously persisted artifact by its UUID.
	 * @param uuid the UUID of the s-ramp artifact
	 * @param artifactType the type of the artifact
	 * @return an instance of a {@link BaseArtifactType}
	 */
	public BaseArtifactType getArtifact(String uuid, ArtifactType type);

	/**
	 * Gets the content (media) for a previously persisted artifact by its UUID.  
	 * <br/><br/>
	 * <b><i>Note: it is the responsibility of the calling method to close the 
	 * resulting {@link InputStream}.</i></b>
	 * @param uuid the S-RAMP uuid of the artifact.
	 * @param artifactType the type of the artifact
	 * @return an {@link InputStream} over the artifact content
	 */
	public InputStream getArtifactContent(String uuid, ArtifactType artifactType);

	/**
	 * Updates a previously persisted artifact.  Note that this method only updates the meta data
	 * of the artifact, not the content.  This will not create or delete any derived artifacts.
	 * @param artifact the s-ramp artifact being updated
	 * @param type the type of the artifact
	 */
	public void updateArtifact(BaseArtifactType artifact, ArtifactType type);

	/**
	 * Gets a list of S-RAMP artifacts of the given type.
	 * @param type the S-RAMP artifact type
	 * @return a {@link List} of S-RAMP artifacts
	 */
	public List<BaseArtifactType> getArtifacts(ArtifactType type);
    
    public void printArtifactGraph(String uuid, ArtifactType type);
}
