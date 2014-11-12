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
package org.overlord.sramp.repository.jcr.audit;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jcr.Node;

/**
 * The result of diff'ing an artifact JCR {@link Node} to itself.  Basically this is
 * a class that contains all of the changes made to a JCR node.  Used by the audit
 * layer when recording changes.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactJCRNodeDiff {

    private Map<String, String> addedProperties = new HashMap<String, String>();
    private Map<String, String> updatedProperties = new HashMap<String, String>();
    private Set<String> deletedProperties = new HashSet<String>();
    private Set<String> addedClassifiers = new HashSet<String>();
    private Set<String> deletedClassifiers = new HashSet<String>();

    /**
     * Constructor.
     */
    public ArtifactJCRNodeDiff() {
    }

    /**
     * @return the addedProperties
     */
    public Map<String, String> getAddedProperties() {
        return addedProperties;
    }

    /**
     * @return the updatedProperties
     */
    public Map<String, String> getUpdatedProperties() {
        return updatedProperties;
    }

    /**
     * @return the deletedProperties
     */
    public Set<String> getDeletedProperties() {
        return deletedProperties;
    }

    /**
     * @return the addedClassifiers
     */
    public Set<String> getAddedClassifiers() {
        return addedClassifiers;
    }

    /**
     * @return the deletedClassifiers
     */
    public Set<String> getDeletedClassifiers() {
        return deletedClassifiers;
    }

    /**
     * @param addedProperties the addedProperties to set
     */
    public void setAddedProperties(Map<String, String> addedProperties) {
        this.addedProperties = addedProperties;
    }

    /**
     * @param updatedProperties the updatedProperties to set
     */
    public void setUpdatedProperties(Map<String, String> updatedProperties) {
        this.updatedProperties = updatedProperties;
    }

    /**
     * @param deletedProperties the deletedProperties to set
     */
    public void setDeletedProperties(Set<String> deletedProperties) {
        this.deletedProperties = deletedProperties;
    }

    /**
     * @param addedClassifiers the addedClassifiers to set
     */
    public void setAddedClassifiers(Set<String> addedClassifiers) {
        this.addedClassifiers = addedClassifiers;
    }

    /**
     * @param deletedClassifiers the deletedClassifiers to set
     */
    public void setDeletedClassifiers(Set<String> deletedClassifiers) {
        this.deletedClassifiers = deletedClassifiers;
    }

}
