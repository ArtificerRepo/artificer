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
    public String persistDerivedArtifact(DerivedArtifactType artifact);
    
    public void printArtifactGraph(String uuid, ArtifactType type);
}
