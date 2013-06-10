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

import javax.jcr.Node;
import javax.jcr.observation.Event;

import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.common.audit.AuditEntryTypes;
import org.overlord.sramp.repository.jcr.JCRConstants;

/**
 * Handles artifact deleted event bundles.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactDeletedHandler extends AbstractAuditEventBundleHandler {

    /**
     * Constructor.
     */
    public ArtifactDeletedHandler() {
    }

    /**
     * @see org.overlord.sramp.repository.jcr.audit.AuditEventBundleHandler#handle(org.overlord.sramp.common.Sramp, org.overlord.sramp.repository.jcr.audit.AuditEventBundle)
     */
    @Override
    public void handle(Sramp sramp, AuditEventBundle eventBundle) throws Exception {
        log.debug("(AUDIT) Processing DELETE ARTIFACT event.");

        if (eventBundle.isEmpty()) {
            log.debug("No events found for artifact delete audit event bundle.");
            return;
        }

        Event deleteEvent = eventBundle.getArtifactDeleteEvent();
        if (deleteEvent == null) {
            log.debug("No (interesting) events found for artifact delete audit event bundle.");
            return;
        }

        Node artifactNode = eventBundle.getNode(deleteEvent);
        // Perhaps we don't want to audit derived artifacts?
        if (artifactNode.getProperty(JCRConstants.SRAMP_DERIVED).getBoolean() && !sramp.isDerivedArtifactAuditingEnabled()) {
            return;
        }
        createAuditEntryNode(artifactNode, AuditEntryTypes.ARTIFACT_DELETE,
                deleteEvent.getUserID(), deleteEvent.getDate());

        log.debug("Created one audit entry for the 'delete artifact' event.");

        if (eventBundle.getSession().isLive())
            eventBundle.getSession().save();
    }

}
