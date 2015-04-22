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

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.artificer.repository.hibernate.audit.ArtificerAuditEntry;
import org.artificer.repository.hibernate.query.ArtificerTikaBridge;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.FieldBridge;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.IndexedEmbedded;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Brett Meyer.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Cacheable
@Indexed
@Analyzer(impl = StandardAnalyzer.class)
public class ArtificerArtifact implements Serializable {

    private long id;

    private String uuid;

    private String name;

    private String model;

    private String type;

    private String description;

    private ArtificerUser createdBy;

    private ArtificerUser modifiedBy;

    private List<ArtificerRelationship> relationships = new ArrayList<>();

    // Note: At first glance, it seems like these should probably be actual references to ArtificerOntologyClass.
    // However, the hierarchical queries become *much* easier when it's a simple element collection...
    private List<String> classifiers = new ArrayList<>();
    private List<String> normalizedClassifiers = new ArrayList<>();

    private List<ArtificerComment> comments = new ArrayList<>();

    private boolean trashed;

    private ArtificerArtifact derivedFrom;

    private List<ArtificerArtifact> derivedArtifacts = new ArrayList<>();

    private Set<ArtificerProperty> properties = new HashSet<>();

    // TODO: Temporary!
    private String version;

    private boolean derived;

    private List<ArtificerAuditEntry> auditEntries = new ArrayList<>();

    // Note: content and contentPath are mutually exclusive.  See FileManager impls.
    private byte[] content;

    // Note: content and contentPath are mutually exclusive.  See FileManager impls.
    private String contentPath;

    private String mimeType;

    private long contentSize;

    private String contentHash;

    private String contentEncoding;

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

    @Field
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Field
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "username", column = @Column(name="createdByUsername")),
            @AttributeOverride(name = "lastActionTime", column = @Column(name="createdTime"))
    })
    public ArtificerUser getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(ArtificerUser createdBy) {
        this.createdBy = createdBy;
    }

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "username", column = @Column(name="modifiedByUsername")),
            @AttributeOverride(name = "lastActionTime", column = @Column(name="modifiedTime"))
    })
    public ArtificerUser getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(ArtificerUser modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.ALL)
    public List<ArtificerRelationship> getRelationships() {
        return relationships;
    }

    public void setRelationships(List<ArtificerRelationship> relationships) {
        this.relationships = relationships;
    }

    @ElementCollection
    public List<String> getClassifiers() {
        return classifiers;
    }

    public void setClassifiers(List<String> classifiers) {
        this.classifiers = classifiers;
    }

    @ElementCollection
    public List<String> getNormalizedClassifiers() {
        return normalizedClassifiers;
    }

    public void setNormalizedClassifiers(List<String> normalizedClassifiers) {
        this.normalizedClassifiers = normalizedClassifiers;
    }

    @OneToMany(mappedBy = "artifact", orphanRemoval = true, cascade = CascadeType.ALL)
    @IndexedEmbedded
    public List<ArtificerComment> getComments() {
        return comments;
    }

    public void setComments(List<ArtificerComment> comments) {
        this.comments = comments;
    }

    public boolean isTrashed() {
        return trashed;
    }

    public void setTrashed(boolean trashed) {
        this.trashed = trashed;
    }

    public boolean isDerived() {
        return derived;
    }

    public void setDerived(boolean derived) {
        this.derived = derived;
    }

    @ManyToOne
    public ArtificerArtifact getDerivedFrom() {
        return derivedFrom;
    }

    public void setDerivedFrom(ArtificerArtifact derivedFrom) {
        this.derivedFrom = derivedFrom;
    }

    @OneToMany(mappedBy = "derivedFrom", orphanRemoval = true, cascade = CascadeType.ALL)
    public List<ArtificerArtifact> getDerivedArtifacts() {
        return derivedArtifacts;
    }

    public void setDerivedArtifacts(List<ArtificerArtifact> derivedArtifacts) {
        this.derivedArtifacts = derivedArtifacts;
    }

    @OneToMany(mappedBy = "owner", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @IndexedEmbedded
    public Set<ArtificerProperty> getProperties() {
        return properties;
    }

    public void setProperties(Set<ArtificerProperty> properties) {
        this.properties = properties;
    }

    @Transient
    public boolean isDocument() {
        return false;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @OneToMany(mappedBy = "artifact", orphanRemoval = true)
    public List<ArtificerAuditEntry> getAuditEntries() {
        return auditEntries;
    }

    public void setAuditEntries(List<ArtificerAuditEntry> auditEntries) {
        this.auditEntries = auditEntries;
    }

    @Lob
    @Field
    @FieldBridge(impl = ArtificerTikaBridge.class)
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Field
    @FieldBridge(impl = ArtificerTikaBridge.class)
    public String getContentPath() {
        return contentPath;
    }

    public void setContentPath(String contentPath) {
        this.contentPath = contentPath;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public void setContentEncoding(String contentEncoding) {
        this.contentEncoding = contentEncoding;
    }

    public final Map<String, String> snapshotProperties() {
        Map<String, String> snapshotProperties = new HashMap<>();

        snapshotProperties.put("name", name);
        snapshotProperties.put("description", description);
        snapshotProperties.put("version", version);

        for (ArtificerProperty property : properties) {
            snapshotProperties.put(property.getKey(), property.getValue());
        }

        return snapshotProperties;
    }

    protected void addSnapshotProperties(Map<String, String> snapshotProperties) {}
}
