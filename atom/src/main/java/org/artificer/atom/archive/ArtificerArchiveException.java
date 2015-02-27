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
package org.artificer.atom.archive;

import org.artificer.common.error.ArtificerServerException;

/**
 * Error thrown by the {@link ArtificerArchive} whenever something goes wrong in there.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerArchiveException extends ArtificerServerException {

    private static final long serialVersionUID = 4145271500140947910L;

	/**
     * Constructor.
     */
    public ArtificerArchiveException() {
        super();
    }

    /**
     * Constructor.
     * @param message
     * @param rootCause
     */
    public ArtificerArchiveException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    /**
     * Constructor.
     * @param message
     */
    public ArtificerArchiveException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param rootCause
     */
    public ArtificerArchiveException(Throwable rootCause) {
        super(rootCause);
    }

}
