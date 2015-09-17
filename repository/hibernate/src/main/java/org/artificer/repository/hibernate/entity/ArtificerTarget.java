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
package org.artificer.repository.hibernate.entity;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.Cacheable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
@Entity
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ArtificerTarget implements Serializable {

    private long id;

    private ArtificerArtifact target;

    // Realistically, this shouldn't be necessary -- could just use target#getType.  However, see the reflection
    // nonsense in HibernateEntityToSrampVisitor#getRelationship.  It's easier if we have the enum name String here,
    // rather than try to handle it dynamically.
    private String targetType;

    private Map<String, String> otherAttributes = new HashMap<>();

    private ArtificerRelationship relationship;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(optional = false)
    public ArtificerArtifact getTarget() {
        return target;
    }

    public void setTarget(ArtificerArtifact target) {
        this.target = target;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    @ElementCollection
    public Map<String, String> getOtherAttributes() {
        return otherAttributes;
    }

    public void setOtherAttributes(Map<String, String> otherAttributes) {
        this.otherAttributes = otherAttributes;
    }

    @ManyToOne(optional = false)
    public ArtificerRelationship getRelationship() {
        return relationship;
    }

    public void setRelationship(ArtificerRelationship relationship) {
        this.relationship = relationship;
    }
}
