/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.events;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.common.ontology.ArtificerOntology;

/**
 * EventProducer is a service contract that allows events to be published to external subscribers (ex: JMS).
 * 
 * @author Brett Meyer
 */
public interface EventProducer {
    
    /**
     * A new artifact was created and persisted.
     * 
     * @param artifact The new artifact
     */
    public void artifactCreated(BaseArtifactType artifact);
    
    /**
     * An artifact's metadata or content was updated.
     * 
     * @param updatedArtifact The new version of the artifact
     * @param oldArtifact The previous version of the artifact
     */
    public void artifactUpdated(BaseArtifactType updatedArtifact, BaseArtifactType oldArtifact);
    
    /**
     * An artifact was deleted.
     * 
     * @param artifact The deleted artifact.
     */
    public void artifactDeleted(BaseArtifactType artifact);
    
    /**
     * A new ontology was created and persisted.
     * 
     * @param ontology The new ontology
     */
    public void ontologyCreated(ArtificerOntology ontology);
    
    /**
     * An ontology was replaced
     * 
     * @param updatedOntology The new version of the ontology
     * @param oldOntology The previous version of the ontology
     */
    public void ontologyUpdated(ArtificerOntology updatedOntology, ArtificerOntology oldOntology);
    
    /**
     * An ontology was deleted
     * 
     * @param ontology The deleted ontology
     */
    public void ontologyDeleted(ArtificerOntology ontology);
    
    /**
     * Called by SrampLifeCycle during startup.  This method should be utilized for internal logic setup, rather than
     * a constructor or through static means, in order to prevent timing issues.
     */
    public void startup();
    
    /**
     * Called by SrampLifeCycle during shutdown, intended for cleanup
     */
    public void shutdown();
}
