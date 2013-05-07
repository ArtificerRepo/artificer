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
package org.overlord.sramp.common.audit;

import java.util.List;

import org.jboss.downloads.overlord.sramp._2013.auditing.AuditEntry;
import org.jboss.downloads.overlord.sramp._2013.auditing.AuditItemType;

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

}
