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
import java.util.Map;

/**
 * Factory for creating/getting event bundle handlers.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditEventBundleHandlerFactory {

    private static final Map<String, AuditEventBundleHandler> handlers = new HashMap<String, AuditEventBundleHandler>();
    static {
        handlers.put(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_ADDED_PHASE1, new ArtifactAddedHandler());
        handlers.put(JCRAuditConstants.AUDIT_BUNDLE_ARTIFACT_UPDATED, new ArtifactUpdatedHandler());
    }
    private static final AuditEventBundleHandler noopHandler = new AuditEventBundleHandler() {
        @Override
        public void handle(AuditEventBundle eventBundle) {
        }
    };

    /**
     * Factory method for getting handlers.
     * @param eventBundle
     */
    public static AuditEventBundleHandler getHandler(AuditEventBundle eventBundle) {
        AuditEventBundleHandler handler = handlers.get(eventBundle.getEventBundleType());
        if (handler == null) {
            handler = noopHandler;
        }
        return handler;
    }

}
