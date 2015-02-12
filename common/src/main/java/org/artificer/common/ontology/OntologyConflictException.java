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

import org.artificer.common.error.ArtificerConflictException;
import org.artificer.common.i18n.Messages;

/**
 * Exception thrown when the user attempts to add an ontology that already exists.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyConflictException extends ArtificerConflictException {

    private static final long serialVersionUID = -6428175672605313348L;

    /**
     * Constructor.
     */
    public OntologyConflictException() {
    }

    /**
     * Constructor.
     * @param ontologyUuid
     */
    public OntologyConflictException(String ontologyUuid) {
        super(Messages.i18n.format("ONTOLOGY_ALREADY_EXISTS", ontologyUuid)); //$NON-NLS-1$
    }

}
