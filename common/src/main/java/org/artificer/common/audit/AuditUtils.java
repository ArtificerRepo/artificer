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
package org.artificer.common.audit;

import java.util.List;

import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.jboss.downloads.artificer._2013.auditing.AuditItemType;
import org.jboss.downloads.artificer._2013.auditing.AuditItemType.Property;

/**
 * Some util methods for dealing with audit data structures.
 * @author eric.wittmann@redhat.com
 */
public class AuditUtils {

    /**
     * Gets an audit item of the given type or null if one does not exist.
     * @param auditEntry
     * @param propertyAdded
     */
    public static AuditItemType getAuditItem(AuditEntry auditEntry, String auditItemType) {
        List<AuditItemType> auditItems = auditEntry.getAuditItem();
        for (AuditItemType auditItem : auditItems) {
            if (auditItem.getType().equals(auditItemType)) {
                return auditItem;
            }
        }
        return null;
    }

    /**
     * Gets an existing audit item or else creates a new one.  Returns it either way.
     * @param auditEntry
     * @param auditItemType
     */
    public static AuditItemType getOrCreateAuditItem(AuditEntry auditEntry, String auditItemType) {
        AuditItemType ai = getAuditItem(auditEntry, auditItemType);
        if (ai == null) {
            ai = new AuditItemType();
            ai.setType(auditItemType);
            auditEntry.getAuditItem().add(ai);
        }
        return ai;
    }

    /**
     * Sets a property on the audit item.
     * @param auditItem
     * @param name
     * @param value
     */
    public static void setAuditItemProperty(AuditItemType auditItem, String name, String value) {
        List<Property> properties = auditItem.getProperty();
        Property theProp = null;
        for (Property property : properties) {
            if (name.equals(property.getName())) {
                theProp = property;
                break;
            }
        }
        if (theProp == null) {
            theProp = new Property();
            theProp.setName(name);
            auditItem.getProperty().add(theProp);
        }
        theProp.setValue(value);
    }

}
