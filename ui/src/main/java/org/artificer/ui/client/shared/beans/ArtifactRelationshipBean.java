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

import java.io.Serializable;
import java.util.Date;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ArtifactRelationshipBean implements Serializable {

    private static final long serialVersionUID = ArtifactRelationshipBean.class.hashCode();

    private String relationshipType;
    private String targetUuid;
    private String targetName;
    private String targetType;
    private Date targetLastModified;

    /**
     * Constructor.
     */
    public ArtifactRelationshipBean() {
    }

    /**
     * @return the relationshipType
     */
    public String getRelationshipType() {
        return relationshipType;
    }

    /**
     * @return the targetUuid
     */
    public String getTargetUuid() {
        return targetUuid;
    }

    /**
     * @return the targetName
     */
    public String getTargetName() {
        return targetName;
    }

    /**
     * @return the targetType
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * @return the targetLastModified
     */
    public Date getTargetLastModified() {
        return targetLastModified;
    }

    /**
     * @param relationshipType the relationshipType to set
     */
    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    /**
     * @param targetUuid the targetUuid to set
     */
    public void setTargetUuid(String targetUuid) {
        this.targetUuid = targetUuid;
    }

    /**
     * @param targetName the targetName to set
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    /**
     * @param targetType the targetType to set
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    /**
     * @param targetLastModified the targetLastModified to set
     */
    public void setTargetLastModified(Date targetLastModified) {
        this.targetLastModified = targetLastModified;
    }

}
