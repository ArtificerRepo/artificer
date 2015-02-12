/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.client.shared.exceptions;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * Exceptions thrown by the server part of the UI to the client.
 *
 * @author eric.wittmann@redhat.com
 */
@Portable
public class ArtificerUiException extends Exception {

    private static final long serialVersionUID = ArtificerUiException.class.hashCode();

    /**
     * Constructor.
     */
    public ArtificerUiException() {
    }

    /**
     * Constructor.
     * @param message
     */
    public ArtificerUiException(String message) {
        super(message);
    }

    /**
     * Constructor.
     * @param message
     * @param cause
     */
    public ArtificerUiException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param cause
     */
    public ArtificerUiException(Throwable cause) {
        super(cause);
    }

}
