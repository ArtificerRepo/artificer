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
package org.overlord.sramp.atom.services.errors;

import org.overlord.sramp.ArtifactTypeEnum;
import org.overlord.sramp.SrampUserException;

/**
 * Exception thrown the user attempts to access a derived artifact in a way that is
 * not allowed (e.g. attempt to delete one).
 *
 * @author eric.wittmann@redhat.com
 */
public class DerivedArtifactAccessException extends SrampUserException {

    private static final long serialVersionUID = -2247193241132739490L;

    /**
     * Constructor.
     */
    public DerivedArtifactAccessException() {
    }

    /**
     * Constructor.
     * @param artifactType
     */
    public DerivedArtifactAccessException(ArtifactTypeEnum artifactType) {
        super("Failed to create artifact because '" + artifactType + "' is a derived type.");
    }

}
