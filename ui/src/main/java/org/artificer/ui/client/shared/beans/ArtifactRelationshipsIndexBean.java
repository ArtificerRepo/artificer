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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ArtifactRelationshipsIndexBean implements Serializable {

    private static final long serialVersionUID = ArtifactRelationshipsIndexBean.class.hashCode();

    private Map<String, ArtifactRelationshipsBean> relationships = new HashMap<String, ArtifactRelationshipsBean>();

    /**
     * Constructor.
     */
    public ArtifactRelationshipsIndexBean() {
    }

    /**
     * @return the relationships
     */
    public Map<String, ArtifactRelationshipsBean> getRelationships() {
        return relationships;
    }

    /**
     * Adds a single relationship.  The relationship gets added to the internal
     * state, indexed by its relationship type.
     * @param relationship
     */
    public void addRelationship(ArtifactRelationshipBean relationship) {
        String type = relationship.getRelationshipType();
        ArtifactRelationshipsBean list = relationships.get(type);
        if (list == null) {
            list = new ArtifactRelationshipsBean();
            relationships.put(type, list);
        }
        list.getRelationships().add(relationship);
    }

}
