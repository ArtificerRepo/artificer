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
package org.overlord.sramp.repository.error;

import org.overlord.sramp.common.error.SrampConflictException;
import org.overlord.sramp.common.i18n.Messages;

/**
 * Exception thrown when the user attempts add a new artifact with a duplicate UUID.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactConflictException extends SrampConflictException {

    private static final long serialVersionUID = 1131976536249817281L;

    /**
     * Constructor.
     */
    public ArtifactConflictException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public ArtifactConflictException(String uuid) {
        super(Messages.i18n.format("ARTIFACT_ALREADY_EXISTS", uuid)); //$NON-NLS-1$
    }

}
