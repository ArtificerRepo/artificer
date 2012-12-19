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
package org.overlord.sramp;

/**
 * Exception thrown when the user/client did something they shouldn't have done.  This
 * represents things like invalid query syntax, asking for a UUID that doesn't exist,
 * etcetera.  It does *not* represent unexpected server errors like out of memory or
 * out of disk space.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class SrampUserException extends SrampException {

    private static final long serialVersionUID = -1974585440431377826L;

    /**
     * Constructor.
     */
    public SrampUserException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public SrampUserException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public SrampUserException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param cause
     */
    public SrampUserException(Throwable cause) {
        super(cause);
    }

}
