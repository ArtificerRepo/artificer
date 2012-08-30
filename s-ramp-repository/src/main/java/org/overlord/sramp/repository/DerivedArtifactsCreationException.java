/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository;

/**
 * Exception thrown when a problem is discovered during indexing/sequencing of
 * an s-ramp artifact (creating the derived artifacts for a given non-derived
 * artifact).
 */
public class DerivedArtifactsCreationException extends RepositoryException {

    private static final long serialVersionUID = -1205817784608428279L;

    /**
     * Constructor.
     */
    public DerivedArtifactsCreationException() {
        super();
    }

    /**
     * Constructor.
     * @param message
     * @param rootCause
     */
    public DerivedArtifactsCreationException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    /**
     * Constructor.
     * @param message
     */
    public DerivedArtifactsCreationException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param rootCause
     */
    public DerivedArtifactsCreationException(Throwable rootCause) {
        super(rootCause);
    }

}
