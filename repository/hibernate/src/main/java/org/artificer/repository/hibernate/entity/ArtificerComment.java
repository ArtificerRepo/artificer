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
import org.hibernate.search.annotations.Field;

import javax.persistence.Cacheable;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import java.io.Serializable;

/**
 * @author Brett Meyer.
 */
@Entity
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ArtificerComment implements Serializable {

    private long id;

    private String text;

    private ArtificerUser createdBy;

    private ArtificerArtifact artifact;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Field // @IndexedEmbedded by ArtificerArtifact
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Embedded
    public ArtificerUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ArtificerUser createdBy) {
        this.createdBy = createdBy;
    }

    @ManyToOne(optional = false)
    public ArtificerArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(ArtificerArtifact artifact) {
        this.artifact = artifact;
    }
}
