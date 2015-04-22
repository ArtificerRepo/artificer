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
package org.artificer.repository.hibernate.audit;

import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.artificer.repository.hibernate.entity.ArtificerUser;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Entity
@Immutable
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class ArtificerAuditEntry implements Serializable {

    private long id;

    private String uuid;

    private String type;

    private List<ArtificerAuditItem> items = new ArrayList<>();

    private ArtificerUser modifiedBy;

    private ArtificerArtifact artifact;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @OneToMany(mappedBy = "auditEntry", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public List<ArtificerAuditItem> getItems() {
        return items;
    }

    public void setItems(List<ArtificerAuditItem> items) {
        this.items = items;
    }

    @Embedded
    public ArtificerUser getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(ArtificerUser modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @ManyToOne
    public ArtificerArtifact getArtifact() {
        return artifact;
    }

    public void setArtifact(ArtificerArtifact artifact) {
        this.artifact = artifact;
    }
}
