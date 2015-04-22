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

package org.artificer.common.ontology;

import org.artificer.common.error.ArtificerUserException;
import org.artificer.common.i18n.Messages;

import java.net.URI;
import java.util.List;

/**
 * A simple ontology validator.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyValidator {

    /**
     * Validates the ontology.
     * @param ontology
     */
    public static void validateOntology(ArtificerOntology ontology) throws Exception {
        try {
            new URI(ontology.getId());
        } catch (Exception e) {
            throw new ArtificerUserException(Messages.i18n.format("INVALID_ONTOLOGY_ID", ontology.getId())); //$NON-NLS-1$
        }
        List<ArtificerOntologyClass> classes = ontology.getAllClasses();
        for (ArtificerOntologyClass oclass : classes) {
            try {
                new URI(oclass.getId());
            } catch (Exception e) {
                throw new ArtificerUserException(Messages.i18n.format("INVALID_CLASS_ID", oclass.getId())); //$NON-NLS-1$
            }
        }
    }

}
