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

import java.util.Calendar;
import java.util.UUID;

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * Handles artifact updated event bundles.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUpdatedHandler extends AbstractAuditEventBundleHandler {

    /**
     * @see org.overlord.sramp.repository.jcr.audit.AuditEventBundleHandler#handle(org.overlord.sramp.repository.jcr.audit.AuditEventBundle)
     */
    @Override
    public void handle(AuditEventBundle eventBundle) throws Exception {
        log.debug("(AUDIT) Processing UPDATE ARTIFACT event.");

        if (eventBundle.isEmpty()) {
            log.debug("No events found for artifact update audit event bundle.");
            return;
        }

        Event artifactUpdateEvent = eventBundle.getArtifactUpdateEvent();
        if (artifactUpdateEvent == null) {
            log.debug("No (interesting) events found for artifact update audit event bundle.");
            return;
        }

        String auditUuid = UUID.randomUUID().toString();
        Node artifactNode = eventBundle.getNode(artifactUpdateEvent);
        Node auditEntryNode = artifactNode.addNode("audit:" + auditUuid, JCRConstants.SRAMP_AUDIT_ENTRY);
        long eventDate = artifactUpdateEvent.getDate();
        Calendar eventCal = Calendar.getInstance();
        eventCal.setTimeInMillis(eventDate);

        auditEntryNode.setProperty("audit:uuid", auditUuid);
        auditEntryNode.setProperty("audit:sortId", eventDate);
        auditEntryNode.setProperty("audit:type", AuditEntryTypes.ARTIFACT_UPDATE);
        auditEntryNode.setProperty("audit:who", artifactUpdateEvent.getUserID());
        auditEntryNode.setProperty("audit:when", eventCal);

        log.debug("Created one audit entry for the 'update artifact' event.");

        Node propAddedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_ADDED);
        Node propChangedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_CHANGED);
        Node propRemovedNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_REMOVED);
        for (Event event : eventBundle) {
            if (isArtifactPropertyEvent(eventBundle, event, Event.PROPERTY_ADDED)) {
                addPropertyToAuditItem(eventBundle, propAddedNode, event);
            } else if (isArtifactPropertyEvent(eventBundle, event, Event.PROPERTY_CHANGED)) {
                addPropertyToAuditItem(eventBundle, propChangedNode, event);
            } else if (isArtifactPropertyEvent(eventBundle, event, Event.PROPERTY_REMOVED)) {
                addPropertyToAuditItem(eventBundle, propRemovedNode, event);
            }
        }

        if (eventBundle.getSession().isLive())
            eventBundle.getSession().save();
    }

    /**
     * Returns true if the given event is a property event for an artifact.
     * @param event
     * @param type
     * @throws RepositoryException
     * @throws ItemNotFoundException
     */
    private boolean isArtifactPropertyEvent(AuditEventBundle eventBundle, Event event, int type)
            throws ItemNotFoundException, RepositoryException {
        if (event.getType() != type) {
            return false;
        }
        Node artifactNode = eventBundle.getNode(event);
        if (!artifactNode.isNodeType(JCRConstants.SRAMP_BASE_ARTIFACT_TYPE)) {
            return false;
        }
        if (artifactNode.getProperty(JCRConstants.SRAMP_DERIVED).getBoolean()) {
            return false;
        }
        String path = event.getPath();
        String propertyName = path.substring(path.lastIndexOf('/') + 1);
        if (JCRAuditConstants.propertyExcludes.contains(propertyName)) {
            return false;
        }
        return true;
    }

}
