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
package org.overlord.sramp.repository.query;

import org.overlord.sramp.common.SrampUserException;

/**
 * Exception thrown when an s-ramp query is not valid.
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidQueryException extends SrampUserException {

    private static final long serialVersionUID = InvalidQueryException.class.hashCode();

    /**
     * Default constructor.
     */
    public InvalidQueryException() {
        super();
    }

    /**
     * Constructor.
     * @param message
     * @param rootCause
     */
    public InvalidQueryException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    /**
     * Constructor.
     * @param message
     */
    public InvalidQueryException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param rootCause
     */
    public InvalidQueryException(Throwable rootCause) {
        super(rootCause);
    }

}
