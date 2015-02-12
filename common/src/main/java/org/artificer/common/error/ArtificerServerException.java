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

import org.artificer.common.ArtificerException;

/**
 * Exception thrown when the server fails unexpectedly (e.g. storage error).
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerServerException extends ArtificerException {

    private static final long serialVersionUID = 2648287148198104189L;

    /**
     * Constructor.
     */
    public ArtificerServerException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public ArtificerServerException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public ArtificerServerException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param cause
     */
    public ArtificerServerException(Throwable cause) {
        super(cause);
    }

}
