/*
 * Copyright 2012 JBoss Inc
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
package org.artificer.common.error;

import org.artificer.common.i18n.Messages;


/**
 * Exception thrown when the user attempts to access an audit entry that does not exist.
 *
 * @author eric.wittmann@redhat.com
 */
public class AuditEntryNotFoundException extends ArtificerUserException {

    private static final long serialVersionUID = 8446724007853150213L;

    /**
     * Constructor.
     * @param auditEntryUuid
     * @param artifactUuid
     */
    public AuditEntryNotFoundException(String artifactUuid, String auditEntryUuid) {
        super(Messages.i18n.format("AUDIT_ENTRY_NOT_FOUND", artifactUuid, auditEntryUuid)); //$NON-NLS-1$
    }

}
