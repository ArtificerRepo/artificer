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
import java.util.Map.Entry;
import java.util.Set;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * A class that is able to compare an artifact JCR {@link Node} and output
 * the differences in properties, relationships, and classifiers.  This class
 * is used by the auditing code to record changes made to a node.  It is
 * not used
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactJCRNodeDiffer {

    private Map<String, String> properties = new HashMap<String, String>();
    private Set<String> classifiers = new HashSet<String>();

    /**
     * Constructor.  Creates an initial snapshot of information found in the
     * included JCR node.  The information will be used for a later comparison.
     * @throws RepositoryException
     */
    public ArtifactJCRNodeDiffer(Node artifactNode) throws RepositoryException {
        snapshot(artifactNode);
    }

    /**
     * Takes a snapshot of the artifact meta-data found in the JCR node.
     * @param artifactNode
     * @throws RepositoryException
     */
    private void snapshot(Node artifactNode) throws RepositoryException {
        // Snapshot all properties and classifiers
        //////////////////////////////////////////
        PropertyIterator iterator = artifactNode.getProperties();
        while (iterator.hasNext()) {
            Property property = iterator.nextProperty();
            String propName = property.getName();
            // If the property is excluded, skip it
            if (JCRAuditConstants.propertyExcludes.contains(propName)) {
                continue;
            }
            // Handle multi-value properties.
            if (property.isMultiple()) {
                // Handle classified-by property (it's the only multi-value prop we care about)
                if (JCRAuditConstants.PROP_CLASSIFIED_BY.equals(propName)) {
                    Value[] values = property.getValues();
                    for (Value value : values) {
                        getClassifiers().add(value.getString());
                    }
                }
                continue;
            }
            String name = propName.substring(propName.indexOf(':') + 1);
            String value = property.getValue().getString();
            getProperties().put(name, value);
        }
    }

    /**
     * Called to compare the initial snapshot information with the current state
     * of the artifact node.
     *
     * @param artifactNode
     */
    public ArtifactJCRNodeDiff diff(Node artifactNode) throws RepositoryException {
        ArtifactJCRNodeDiff diff = new ArtifactJCRNodeDiff();

        // Process all properties and classifiers
        /////////////////////////////////////////
        PropertyIterator iterator = artifactNode.getProperties();
        while (iterator.hasNext()) {
            Property property = iterator.nextProperty();
            String propName = property.getName();
            // If the property is excluded, skip it
            if (JCRAuditConstants.propertyExcludes.contains(propName)) {
                continue;
            }
            // Handle multi-value properties.
            if (property.isMultiple()) {
                // Handle classified-by property (it's the only multi-value prop we care about)
                if (JCRAuditConstants.PROP_CLASSIFIED_BY.equals(propName)) {
                    Value[] values = property.getValues();
                    for (Value value : values) {
                        String classifier = value.getString();
                        if (!getClassifiers().contains(classifier)) {
                            diff.getAddedClassifiers().add(classifier);
                        }
                        // Remove it so that, at the end of this, the classifier set contains only
                        // classifiers that were removed (no longer present on the jcr node).
                        getClassifiers().remove(classifier);
                    }
                }
                continue;
            }
            // Process an auditable property.
            String name = propName.substring(propName.indexOf(':') + 1);
            String value = property.getValue().getString();
            if (getProperties().containsKey(name)) {
                String oldValue = getProperties().get(name);
                if (!oldValue.equals(value)) {
                    diff.getUpdatedProperties().put(name, value);
                }
                // Remove it so that, at the end of this, the map of properties contains all
                // properties that were removed (no longer present on the jcr node).
                getProperties().remove(name);
            } else {
                diff.getAddedProperties().put(name, value);
            }
        }

        // Process property deletes
        ///////////////////////////
        for (Entry<String, String> entry : getProperties().entrySet()) {
            String name = entry.getKey();
            diff.getDeletedProperties().add(name);
        }

        // Process classifier deletes
        /////////////////////////////
        for (String classifier : getClassifiers()) {
            diff.getDeletedClassifiers().add(classifier);
        }

        // Process relationship deletes
        ///////////////////////////////

        return diff;
    }

    /**
     * @return the classifiers
     */
    public Set<String> getClassifiers() {
        return classifiers;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }
}
