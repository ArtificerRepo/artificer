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
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Table;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
@Entity
@Table(appliesTo = "ArtificerRelationship", indexes = {
        @Index(name = "relationship_name_idx", columnNames = "name")})
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ArtificerRelationship implements Serializable {

    private long id;

    private String name;

    private ArtificerRelationshipType type;

    private List<ArtificerTarget> targets = new ArrayList<>();

    private Map<String, String> otherAttributes = new HashMap<>();

    private ArtificerArtifact owner;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArtificerRelationshipType getType() {
        return type;
    }

    public void setType(ArtificerRelationshipType type) {
        this.type = type;
    }

    @OneToMany(mappedBy = "relationship", orphanRemoval = true, cascade = CascadeType.ALL)
    public List<ArtificerTarget> getTargets() {
        return targets;
    }

    public void setTargets(List<ArtificerTarget> targets) {
        this.targets = targets;
    }

    @ElementCollection
    public Map<String, String> getOtherAttributes() {
        return otherAttributes;
    }

    public void setOtherAttributes(Map<String, String> otherAttributes) {
        this.otherAttributes = otherAttributes;
    }

    @ManyToOne(optional = false)
    public ArtificerArtifact getOwner() {
        return owner;
    }

    public void setOwner(ArtificerArtifact owner) {
        this.owner = owner;
    }
}
