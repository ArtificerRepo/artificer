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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.observation.Event;

import org.overlord.sramp.common.audit.AuditItemTypes;
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * Handles an artifact add event bundle.
 * @author eric.wittmann@redhat.com
 */
public class ArtifactAddedHandler extends AbstractAuditEventBundleHandler {

    /**
     * @see org.overlord.sramp.repository.jcr.audit.AuditEventBundleHandler#handle(org.overlord.sramp.repository.jcr.audit.AuditEventBundle)
     */
    @Override
    public void handle(AuditEventBundle eventBundle) throws Exception {
        log.debug("Processing ADD ARTIFACT audit event bundle.");
        Event addEvent = eventBundle.getArtifactAddEvent();
        if (addEvent == null) {
            throw new ItemNotFoundException();
        }

        Node artifactNode = eventBundle.getNode(addEvent);
        Node auditEntryNode = createAuditEntryNode(artifactNode, addEvent.getUserID(), addEvent.getDate());

        Node auditItemNode = createAuditItemNode(auditEntryNode, AuditItemTypes.PROPERTY_ADDED);
        for (Event event : eventBundle) {
            if (isArtifactPropertyAddEvent(eventBundle, event)) {
                addPropertyToAuditItem(eventBundle, auditItemNode, event);
            }
        }

        // TODO add specific property details to audit entry

        if (eventBundle.getSession().isLive())
            eventBundle.getSession().save();
    }

    /**
     * Returns true if the given event is a Property Add event for an artifact.
     * @param event
     * @throws RepositoryException
     * @throws ItemNotFoundException
     */
    private boolean isArtifactPropertyAddEvent(AuditEventBundle eventBundle, Event event)
            throws ItemNotFoundException, RepositoryException {
        if (event.getType() != Event.PROPERTY_ADDED) {
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
