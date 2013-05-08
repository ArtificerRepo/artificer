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

import java.util.List;

import javax.jcr.Node;
import javax.jcr.observation.Event;

import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.common.audit.AuditEntryTypes;

/**
 * Handles an event bundle containing (potentially) multiple "derived artifact added" events.
 * @author eric.wittmann@redhat.com
 */
public class DerivedArtifactAddedHandler extends AbstractAuditEventBundleHandler {

    /**
     * Constructor.
     */
    public DerivedArtifactAddedHandler() {
    }

    /**
     * @see org.overlord.sramp.repository.jcr.audit.AuditEventBundleHandler#handle(org.overlord.sramp.common.Sramp, org.overlord.sramp.repository.jcr.audit.AuditEventBundle)
     */
    @Override
    public void handle(Sramp sramp, AuditEventBundle eventBundle) throws Exception {
        if (!sramp.isDerivedArtifactAuditingEnabled())
            return;
        log.debug("Processing DERIVED ARTIFACTS ADDED audit event bundle.");
        List<Event> derivedArtifactAddedEvents = eventBundle.getDerivedArtifactAddEvents();
        if (derivedArtifactAddedEvents.isEmpty()) {
            return;
        }

        for (Event derivedArtifactAddedEvent : derivedArtifactAddedEvents) {
            Node artifactNode = eventBundle.getNode(derivedArtifactAddedEvent);
            /*Node auditEntryNode = */createAuditEntryNode(artifactNode, AuditEntryTypes.ARTIFACT_ADD,
                    derivedArtifactAddedEvent.getUserID(), derivedArtifactAddedEvent.getDate());
            // TODO add specific property details to audit entry
        }

        if (eventBundle.getSession().isLive())
            eventBundle.getSession().save();
    }

}
