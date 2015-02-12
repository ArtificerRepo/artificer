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
package org.artificer.common.ontology;

import org.artificer.common.error.ArtificerUserException;
import org.artificer.common.i18n.Messages;

/**
 * Exception thrown when the user attempts to add a classified-by value to an
 * artifact that has not been defined by an ontology.
 *
 * @author eric.wittmann@redhat.com
 */
public class InvalidClassifiedByException extends ArtificerUserException {

    private static final long serialVersionUID = 8277077613160811686L;

    /**
     * Constructor.
     */
    public InvalidClassifiedByException() {
    }

    /**
     * Constructor.
     * @param classifiedBy
     */
    public InvalidClassifiedByException(String classifiedBy) {
        super(Messages.i18n.format("INVALID_CLASSIFIED_BY", classifiedBy)); //$NON-NLS-1$
    }

}
