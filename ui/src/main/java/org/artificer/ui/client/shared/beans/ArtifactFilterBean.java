/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.client.shared.beans;

import org.jboss.errai.common.client.api.annotations.Portable;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * All of the user's filter settings (configured on the left-hand sidebar of
 * the Artifacts page).
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ArtifactFilterBean implements Serializable {

    private static final long serialVersionUID = 3789397680981626569L;

    private String keywords = "";
    private String artifactType = "";
    private String uuid = "";
    private String name = "";
    private Date dateCreatedFrom;
    private Date dateCreatedTo;
    private Date dateModifiedFrom;
    private Date dateModifiedTo;
    private String createdBy = "";
    private String lastModifiedBy = "";
    private ArtifactOriginEnum origin = ArtifactOriginEnum.ALL;
    /**
     * The classifiers selected by the user in the 'Classifiers' filter section.  This is
     * a simple map of Ontology Base (URI) to Set of Ontology Class IDs.  In other words,
     * each ontology in the UI will be a key, and the values will be the IDs of the classes
     * selected in the classifier selection dialog.
     */
    private Map<String, Set<String>> classifiers = new HashMap<String, Set<String>>();
    /**
     * The custom property filters.  This is just a name/value pair.  The user can specify
     * as many of these as she likes.
     */
    private Map<String, String> customProperties = new HashMap<String, String>();

    /**
     * Constructor.
     */
    public ArtifactFilterBean() {
    }

    public String getKeywords() {
        return keywords;
    }

    /**
     * @return the artifactType
     */
    public String getArtifactType() {
        return artifactType;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    /**
     * @return the dateCreatedFrom
     */
    public Date getDateCreatedFrom() {
        return dateCreatedFrom;
    }

    /**
     * @return the dateCreatedTo
     */
    public Date getDateCreatedTo() {
        return dateCreatedTo;
    }

    /**
     * @return the dateModifiedFrom
     */
    public Date getDateModifiedFrom() {
        return dateModifiedFrom;
    }

    /**
     * @return the dateModifiedTo
     */
    public Date getDateModifiedTo() {
        return dateModifiedTo;
    }

    /**
     * @return the createdBy
     */
    public String getCreatedBy() {
        return createdBy;
    }

    /**
     * @return the lastModifiedBy
     */
    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    /**
     * @return the origin
     */
    public ArtifactOriginEnum getOrigin() {
        return origin;
    }

    public ArtifactFilterBean setKeywords(String keywords) {
        this.keywords = keywords;
        return this;
    }

    /**
     * @param artifactType the artifactType to set
     */
    public ArtifactFilterBean setArtifactType(String artifactType) {
        this.artifactType = artifactType;
        return this;
    }

    public ArtifactFilterBean setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public ArtifactFilterBean setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * @param dateCreatedFrom the dateCreatedFrom to set
     */
    public ArtifactFilterBean setDateCreatedFrom(Date dateCreatedFrom) {
        this.dateCreatedFrom = dateCreatedFrom;
        return this;
    }

    /**
     * @param dateCreatedTo the dateCreatedTo to set
     */
    public ArtifactFilterBean setDateCreatedTo(Date dateCreatedTo) {
        this.dateCreatedTo = dateCreatedTo;
        return this;
    }

    /**
     * @param dateModifiedFrom the dateModifiedFrom to set
     */
    public ArtifactFilterBean setDateModifiedFrom(Date dateModifiedFrom) {
        this.dateModifiedFrom = dateModifiedFrom;
        return this;
    }

    /**
     * @param dateModifiedTo the dateModifiedTo to set
     */
    public ArtifactFilterBean setDateModifiedTo(Date dateModifiedTo) {
        this.dateModifiedTo = dateModifiedTo;
        return this;
    }

    /**
     * @param createdBy the createdBy to set
     */
    public ArtifactFilterBean setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * @param lastModifiedBy the lastModifiedBy to set
     */
    public ArtifactFilterBean setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
        return this;
    }

    /**
     * @param origin the origin to set
     */
    public ArtifactFilterBean setOrigin(ArtifactOriginEnum origin) {
        this.origin = origin;
        return this;
    }

    /**
     * @return the classifiers
     */
    public Map<String, Set<String>> getClassifiers() {
        return classifiers;
    }

    /**
     * Sets the classifiers.
     */
    public ArtifactFilterBean setClassifiers(Map<String, Set<String>> classifiers) {
        this.classifiers = classifiers;
        return this;
    }

    /**
     * @return the customProperties
     */
    public Map<String, String> getCustomProperties() {
        return customProperties;
    }

    /**
     * @param customProperties the customProperties to set
     */
    public void setCustomProperties(Map<String, String> customProperties) {
        this.customProperties = customProperties;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((keywords == null) ? 0 : keywords.hashCode());
        result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
        result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((dateCreatedFrom == null) ? 0 : dateCreatedFrom.hashCode());
        result = prime * result + ((dateCreatedTo == null) ? 0 : dateCreatedTo.hashCode());
        result = prime * result + ((dateModifiedFrom == null) ? 0 : dateModifiedFrom.hashCode());
        result = prime * result + ((dateModifiedTo == null) ? 0 : dateModifiedTo.hashCode());
        result = prime * result + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
        result = prime * result + ((classifiers == null) ? 0 : classifiers.hashCode());
        result = prime * result + ((customProperties == null) ? 0 : customProperties.hashCode());
        return result;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ArtifactFilterBean other = (ArtifactFilterBean) obj;
        if (keywords == null) {
            if (other.keywords != null)
                return false;
        } else if (!keywords.equals(other.keywords))
            return false;
        if (artifactType == null) {
            if (other.artifactType != null)
                return false;
        } else if (!artifactType.equals(other.artifactType))
            return false;
        if (uuid == null) {
            if (other.uuid != null)
                return false;
        } else if (!uuid.equals(other.uuid))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (createdBy == null) {
            if (other.createdBy != null)
                return false;
        } else if (!createdBy.equals(other.createdBy))
            return false;
        if (dateCreatedFrom == null) {
            if (other.dateCreatedFrom != null)
                return false;
        } else if (!dateCreatedFrom.equals(other.dateCreatedFrom))
            return false;
        if (dateCreatedTo == null) {
            if (other.dateCreatedTo != null)
                return false;
        } else if (!dateCreatedTo.equals(other.dateCreatedTo))
            return false;
        if (dateModifiedFrom == null) {
            if (other.dateModifiedFrom != null)
                return false;
        } else if (!dateModifiedFrom.equals(other.dateModifiedFrom))
            return false;
        if (dateModifiedTo == null) {
            if (other.dateModifiedTo != null)
                return false;
        } else if (!dateModifiedTo.equals(other.dateModifiedTo))
            return false;
        if (lastModifiedBy == null) {
            if (other.lastModifiedBy != null)
                return false;
        } else if (!lastModifiedBy.equals(other.lastModifiedBy))
            return false;
        if (origin != other.origin)
            return false;
        if (classifiers == null) {
            if (other.classifiers != null)
                return false;
        } else if (!classifiers.equals(other.classifiers))
            return false;
        if (customProperties == null) {
            if (other.customProperties != null)
                return false;
        } else if (!customProperties.equals(other.customProperties))
            return false;
        return true;
    }
}
