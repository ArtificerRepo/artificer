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

import org.artificer.common.audit.AuditEntryTypes;
import org.artificer.common.audit.AuditItemTypes;
import org.artificer.repository.audit.ArtifactDiff;
import org.artificer.repository.hibernate.HibernateEntityFactory;
import org.artificer.repository.hibernate.entity.ArtificerArtifact;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.jboss.downloads.artificer._2013.auditing.AuditItemType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A class that is able to compare an artifact and output
 * the differences in properties, relationships, and classifiers.  This class
 * is used by the auditing code to record changes made to a node.
 *
 * @author Brett Meyer.
 */
public class HibernateAuditor {

    private final Map<String, String> oldProperties;
    private final List<String> oldClassifiers;

    /**
     * Constructor.  Creates an initial snapshot of information found in the
     * included artifact.  The information will be used for a later comparison.
     */
    public HibernateAuditor(ArtificerArtifact oldArtifact) {
        oldProperties = oldArtifact.snapshotProperties();
        oldClassifiers = oldArtifact.getClassifiers();
    }

    /**
     * Called to compare the initial snapshot information with the current state
     * of the artifact node.
     *
     * @param newArtifact
     */
    public ArtificerAuditEntry diff(ArtificerArtifact newArtifact) {
        ArtifactDiff diff = new ArtifactDiff();

        Map<String, String> newProperties = newArtifact.snapshotProperties();
        List<String> newClassifiers = newArtifact.getClassifiers();

        for (String name : newProperties.keySet()) {
            String newValue = newProperties.get(name);
            newValue = newValue == null ? "" : newValue;
            if (oldProperties.containsKey(name)) {
                String oldValue = oldProperties.get(name);
                oldValue = oldValue == null ? "" : oldValue;
                if (!oldValue.equals(newValue)) {
                    diff.getUpdatedProperties().put(name, newValue);
                }
                // Remove it so that, at the end of this, the map of properties contains all
                // properties that were removed.
                oldProperties.remove(name);
            } else {
                diff.getAddedProperties().put(name, newValue);
            }
        }

        for (String newClassifier : newClassifiers) {
            if (!oldClassifiers.contains(newClassifier)) {
                diff.getAddedClassifiers().add(newClassifier);
            }
            // Remove it so that, at the end of this, the classifier set contains only
            // classifiers that were removed.
            oldClassifiers.remove(newClassifier);
        }

        // Process property deletes
        for (Map.Entry<String, String> entry : oldProperties.entrySet()) {
            String name = entry.getKey();
            diff.getDeletedProperties().add(name);
        }

        // Process classifier deletes
        for (String classifier : oldClassifiers) {
            diff.getDeletedClassifiers().add(classifier);
        }

        ArtificerAuditEntry artificerAuditEntry = new ArtificerAuditEntry();
        artificerAuditEntry.setUuid(UUID.randomUUID().toString());
        artificerAuditEntry.setModifiedBy(HibernateEntityFactory.user());
        artificerAuditEntry.setType(AuditEntryTypes.ARTIFACT_UPDATE);

        createAuditItem(artificerAuditEntry, AuditItemTypes.PROPERTY_ADDED, diff.getAddedProperties());
        createAuditItem(artificerAuditEntry, AuditItemTypes.PROPERTY_CHANGED, diff.getUpdatedProperties());
        Map<String, String> deletedProperties = new HashMap<>();
        for (String deletedProperty : diff.getDeletedProperties()) {
            deletedProperties.put(deletedProperty, "");
        }
        createAuditItem(artificerAuditEntry, AuditItemTypes.PROPERTY_REMOVED, deletedProperties);
        Map<String, String> addedClassifiers = new HashMap<>();
        int idx = 0;
        for (String addedClassifier : diff.getAddedClassifiers()) {
            addedClassifiers.put("classifier-" + idx++, addedClassifier);
        }
        createAuditItem(artificerAuditEntry, AuditItemTypes.CLASSIFIERS_ADDED, addedClassifiers);
        Map<String, String> deletedClassifiers = new HashMap<>();
        idx = 0;
        for (String deletedClassifier : diff.getDeletedClassifiers()) {
            deletedClassifiers.put("classifier-" + idx++, deletedClassifier);
        }
        createAuditItem(artificerAuditEntry, AuditItemTypes.CLASSIFIERS_REMOVED, deletedClassifiers);

        artificerAuditEntry.setArtifact(newArtifact);
        newArtifact.getAuditEntries().add(artificerAuditEntry);

        return artificerAuditEntry;
    }

    public static ArtificerAuditEntry createAddEntry(ArtificerArtifact artifact) {
        ArtificerAuditEntry artificerAuditEntry = new ArtificerAuditEntry();
        artificerAuditEntry.setUuid(UUID.randomUUID().toString());
        artificerAuditEntry.setModifiedBy(HibernateEntityFactory.user());
        artificerAuditEntry.setType(AuditEntryTypes.ARTIFACT_ADD);

        createAuditItem(artificerAuditEntry, AuditItemTypes.PROPERTY_ADDED, artifact.snapshotProperties());
        Map<String, String> addedClassifiers = new HashMap<>();
        int idx = 0;
        for (String addedClassifier : artifact.getClassifiers()) {
            addedClassifiers.put("classifier-" + idx++, addedClassifier);
        }
        createAuditItem(artificerAuditEntry, AuditItemTypes.CLASSIFIERS_ADDED, addedClassifiers);

        artificerAuditEntry.setArtifact(artifact);
        artifact.getAuditEntries().add(artificerAuditEntry);

        return artificerAuditEntry;
    }

    public static ArtificerAuditEntry createDeleteEntry(ArtificerArtifact artifact) {
        ArtificerAuditEntry artificerAuditEntry = new ArtificerAuditEntry();
        artificerAuditEntry.setUuid(UUID.randomUUID().toString());
        artificerAuditEntry.setModifiedBy(HibernateEntityFactory.user());
        artificerAuditEntry.setType(AuditEntryTypes.ARTIFACT_DELETE);

        artificerAuditEntry.setArtifact(artifact);
        artifact.getAuditEntries().add(artificerAuditEntry);

        return artificerAuditEntry;
    }

    private static void createAuditItem(ArtificerAuditEntry artificerAuditEntry,
            String type, Map<String, String> properties) {
        if (!properties.isEmpty()) {
            ArtificerAuditItem artificerAuditItem = new ArtificerAuditItem();
            artificerAuditItem.setType(type);
            artificerAuditItem.getProperties().putAll(properties);
            artificerAuditItem.setAuditEntry(artificerAuditEntry);

            artificerAuditEntry.getItems().add(artificerAuditItem);
        }
    }

    public static List<AuditEntry> auditEntries(List<ArtificerAuditEntry> artificerAuditEntries) {
        List<AuditEntry> auditEntries = new ArrayList<>();
        for (ArtificerAuditEntry artificerAuditEntry : artificerAuditEntries) {
            auditEntries.add(auditEntry(artificerAuditEntry));
        }
        return auditEntries;
    }

    public static AuditEntry auditEntry(ArtificerAuditEntry artificerAuditEntry) {
        AuditEntry auditEntry = new AuditEntry();
        auditEntry.setWhen(HibernateEntityFactory.calendar(artificerAuditEntry.getModifiedBy().getLastActionTime()));
        auditEntry.setWho(artificerAuditEntry.getModifiedBy().getUsername());
        auditEntry.setUuid(artificerAuditEntry.getUuid());
        auditEntry.setType(artificerAuditEntry.getType());

        for (ArtificerAuditItem artificerAuditItem : artificerAuditEntry.getItems()) {
            AuditItemType auditItem = new AuditItemType();
            auditItem.setType(artificerAuditItem.getType());
            for (String key : artificerAuditItem.getProperties().keySet()) {
                String value = artificerAuditItem.getProperties().get(key);
                AuditItemType.Property property = new AuditItemType.Property();
                property.setName(key);
                property.setValue(value);
                auditItem.getProperty().add(property);
            }
            auditEntry.getAuditItem().add(auditItem);
        }

        return auditEntry;
    }

    public static ArtificerAuditEntry auditEntry(AuditEntry srampAuditEntry, ArtificerArtifact artifact) {
        ArtificerAuditEntry auditEntry = new ArtificerAuditEntry();
        auditEntry.setModifiedBy(HibernateEntityFactory.user());
        auditEntry.setUuid(srampAuditEntry.getUuid());
        auditEntry.setType(srampAuditEntry.getType());

        for (AuditItemType srampAuditItem : srampAuditEntry.getAuditItem()) {
            ArtificerAuditItem auditItem = new ArtificerAuditItem();
            auditItem.setType(srampAuditItem.getType());
            for (AuditItemType.Property srampProperty : srampAuditItem.getProperty()) {
                auditItem.getProperties().put(srampProperty.getName(), srampProperty.getValue());
            }
            auditEntry.getItems().add(auditItem);
            auditItem.setAuditEntry(auditEntry);
        }

        auditEntry.setArtifact(artifact);
        artifact.getAuditEntries().add(auditEntry);

        return auditEntry;
    }
}
