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
package org.artificer.common;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.Serializable;

/**
 * If a client requests all relationships *targeting* a given artifact (ie, "reverse relationship"), we need a way
 * to pair the source artifact with the relationship type.
 *
 * @author Brett Meyer.
 */
public class ReverseRelationship implements Serializable {

    private String relationshipType;

    private BaseArtifactType sourceArtifact;

    private Boolean generic;

    public ReverseRelationship(String relationshipType, BaseArtifactType sourceArtifact, Boolean generic) {
        this.relationshipType = relationshipType;
        this.sourceArtifact = sourceArtifact;
        this.generic = generic;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public BaseArtifactType getSourceArtifact() {
        return sourceArtifact;
    }

    public void setSourceArtifact(BaseArtifactType sourceArtifact) {
        this.sourceArtifact = sourceArtifact;
    }

    public Boolean isGeneric() {
        return generic;
    }

    public void setGeneric(Boolean generic) {
        this.generic = generic;
    }
}
