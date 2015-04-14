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
    private Boolean targetDerived;
    private Boolean relationshipGeneric;

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getTargetUuid() {
        return targetUuid;
    }

    public void setTargetUuid(String targetUuid) {
        this.targetUuid = targetUuid;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Boolean getTargetDerived() {
        return targetDerived;
    }

    public void setTargetDerived(Boolean targetDerived) {
        this.targetDerived = targetDerived;
    }

    public Boolean getRelationshipGeneric() {
        return relationshipGeneric;
    }

    public void setRelationshipGeneric(Boolean relationshipGeneric) {
        this.relationshipGeneric = relationshipGeneric;
    }
}
