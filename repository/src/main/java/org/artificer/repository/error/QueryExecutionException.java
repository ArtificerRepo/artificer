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
package org.artificer.repository.error;

import org.artificer.common.error.ArtificerServerException;

/**
 * Exception thrown when an s-ramp query fails.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryExecutionException extends ArtificerServerException {

    private static final long serialVersionUID = QueryExecutionException.class.hashCode();

    /**
     * Default constructor.
     */
    public QueryExecutionException() {
        super();
    }

    /**
     * Constructor.
     * @param message
     * @param rootCause
     */
    public QueryExecutionException(String message, Throwable rootCause) {
        super(message, rootCause);
    }

    /**
     * Constructor.
     * @param message
     */
    public QueryExecutionException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param rootCause
     */
    public QueryExecutionException(Throwable rootCause) {
        super(rootCause);
    }

}
