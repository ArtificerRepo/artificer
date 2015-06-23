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
package org.artificer.common.query;

import java.io.Serializable;

/**
 * If a client requests all relationships *targeting* a given artifact (ie, "reverse relationship"), we need a way
 * to pair the source artifact with the relationship type.
 *
 * @author Brett Meyer.
 */
public class ReverseRelationship implements Serializable {

    private String name;

    private RelationshipType type;

    private ArtifactSummary sourceArtifact;

    public ReverseRelationship(String name, RelationshipType type, ArtifactSummary sourceArtifact) {
        this.name = name;
        this.type = type;
        this.sourceArtifact = sourceArtifact;
    }

    // The source fields are expanded, rather than directly accepting ArtifactSummary.  We need a constructor to be
    // used for JPQL conversion, which only has the primitives available.
    public ReverseRelationship(String name, RelationshipType type,
            String sourceUuid, String sourceName, String sourceModel, String sourceType) {
        this.name = name;
        this.type = type;
        sourceArtifact = new ArtifactSummary(sourceUuid, sourceName, sourceModel, sourceType);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RelationshipType getType() {
        return type;
    }

    public void setType(RelationshipType type) {
        this.type = type;
    }

    public ArtifactSummary getSourceArtifact() {
        return sourceArtifact;
    }

    public void setSourceArtifact(ArtifactSummary sourceArtifact) {
        this.sourceArtifact = sourceArtifact;
    }
}
