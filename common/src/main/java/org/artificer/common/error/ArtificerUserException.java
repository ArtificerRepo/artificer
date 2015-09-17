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

import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.i18n.Messages;

/**
 * Exception thrown when the user/client did something they shouldn't have done.  This
 * represents things like invalid query syntax, asking for a UUID that doesn't exist,
 * etcetera.  It does *not* represent unexpected server error like out of memory or
 * out of disk space.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerUserException extends ArtificerServerException {

    private static final long serialVersionUID = -1974585440431377826L;

    /**
     * Constructor.
     */
    public ArtificerUserException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public ArtificerUserException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public ArtificerUserException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtificerUserException(String msg, String stackTrace) {
        super(msg, stackTrace);
    }

    /**
     * Constructor.
     * @param cause
     */
    public ArtificerUserException(Throwable cause) {
        super(cause);
    }

    public static ArtificerUserException invalidClassifiedBy(String classifiedBy) {
        return new ArtificerUserException(Messages.i18n.format("INVALID_CLASSIFIED_BY", classifiedBy));
    }

    public static ArtificerUserException derivedArtifactCreate(ArtifactTypeEnum artifactType) {
        return new ArtificerUserException(Messages.i18n.format("CANNOT_CREATE_DERIVED_ARTY", artifactType));
    }

    public static ArtificerUserException filenameRequired() {
        return new ArtificerUserException(Messages.i18n.format("FILENAME_REQUIRED"));
    }

    public static ArtificerUserException derivedArtifactDelete(ArtifactTypeEnum artifactType) {
        return new ArtificerUserException(Messages.i18n.format("CANNOT_DELETE_DERIVED_ARTY", artifactType));
    }

    public static ArtificerUserException auditEntryNotFound(String artifactUuid, String auditEntryUuid) {
        return new ArtificerUserException(Messages.i18n.format("AUDIT_ENTRY_NOT_FOUND", artifactUuid, auditEntryUuid));
    }
}
