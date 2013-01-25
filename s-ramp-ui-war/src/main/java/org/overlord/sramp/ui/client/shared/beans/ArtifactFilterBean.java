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
package org.overlord.sramp.ui.client.shared.beans;

import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * All of the user's filter settings (configured on the left-hand sidebar of
 * the Artifacts page).
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ArtifactFilterBean {

    private String artifactType;
    private Date dateCreatedFrom;
    private Date dateCreatedTo;
    private Date dateModifiedFrom;
    private Date dateModifiedTo;
    private String createdBy;
    private String lastModifiedBy;
    private ArtifactOriginEnum origin = ArtifactOriginEnum.primary;

    /**
     * Constructor.
     */
    public ArtifactFilterBean() {
    }

    /**
     * @return the artifactType
     */
    public String getArtifactType() {
        return artifactType;
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

    /**
     * @param artifactType the artifactType to set
     */
    public ArtifactFilterBean setArtifactType(String artifactType) {
        this.artifactType = artifactType;
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
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((artifactType == null) ? 0 : artifactType.hashCode());
        result = prime * result + ((createdBy == null) ? 0 : createdBy.hashCode());
        result = prime * result + ((dateCreatedFrom == null) ? 0 : dateCreatedFrom.hashCode());
        result = prime * result + ((dateCreatedTo == null) ? 0 : dateCreatedTo.hashCode());
        result = prime * result + ((dateModifiedFrom == null) ? 0 : dateModifiedFrom.hashCode());
        result = prime * result + ((dateModifiedTo == null) ? 0 : dateModifiedTo.hashCode());
        result = prime * result + ((lastModifiedBy == null) ? 0 : lastModifiedBy.hashCode());
        result = prime * result + ((origin == null) ? 0 : origin.hashCode());
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
        if (artifactType == null) {
            if (other.artifactType != null)
                return false;
        } else if (!artifactType.equals(other.artifactType))
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
        return true;
    }
}
