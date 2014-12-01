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
package org.overlord.sramp.server.atom.services;

import org.overlord.sramp.common.SrampUserException;
import org.slf4j.Logger;

/**
 * Base class for all Atom API binding resources.
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractResource {

    /**
     * Use the given logger to log the error.
     * @param logger
     * @param message
     * @param error
     */
    protected static final void logError(Logger logger, String message, Throwable error) {
        if (!(error instanceof SrampUserException)) {
            logger.error(message, error);
        }
    }
}
