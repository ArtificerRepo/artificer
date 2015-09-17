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

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * @author Brett Meyer
 */
public class ArtifactUpdateEvent {
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private BaseArtifactType updatedArtifact = null;
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
    private BaseArtifactType oldArtifact = null;
    
    public ArtifactUpdateEvent() {
        
    }
    
    public ArtifactUpdateEvent(BaseArtifactType updatedArtifact, BaseArtifactType oldArtifact) {
        this.updatedArtifact = updatedArtifact;
        this.oldArtifact = oldArtifact;
    }

    /**
     * @return the updatedArtifact
     */
    public BaseArtifactType getUpdatedArtifact() {
        return updatedArtifact;
    }

    /**
     * @param updatedArtifact the updatedArtifact to set
     */
    public void setUpdatedArtifact(BaseArtifactType updatedArtifact) {
        this.updatedArtifact = updatedArtifact;
    }

    /**
     * @return the oldArtifact
     */
    public BaseArtifactType getOldArtifact() {
        return oldArtifact;
    }

    /**
     * @param oldArtifact the oldArtifact to set
     */
    public void setOldArtifact(BaseArtifactType oldArtifact) {
        this.oldArtifact = oldArtifact;
    }

}
