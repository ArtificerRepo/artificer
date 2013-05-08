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

import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;

import org.overlord.sramp.common.Sramp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A JCR event listener that is responsible for recording audit information whenever an artifact
 * node is created or changed.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditEventListener implements EventListener {

    private static Logger log = LoggerFactory.getLogger(AuditEventListener.class);

    private final Sramp sramp;
    private final Session session;

    /**
     * Constructor.
     * @param sramp
     * @param session
     */
    public AuditEventListener(Sramp sramp, Session session) {
        this.sramp = sramp;
        this.session = session;
    }

    /**
     * @see javax.jcr.observation.EventListener#onEvent(javax.jcr.observation.EventIterator)
     */
    @Override
    public void onEvent(EventIterator events) {
        try {
            AuditEventBundle auditingEvents = new AuditEventBundle(session, events);
            AuditEventBundleHandler handler = AuditEventBundleHandlerFactory.getHandler(auditingEvents);
            handler.handle(sramp, auditingEvents);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
