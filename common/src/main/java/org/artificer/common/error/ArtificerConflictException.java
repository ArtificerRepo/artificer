/*
 * Copyright 2014 JBoss Inc
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


import org.artificer.common.i18n.Messages;

/**
 * Superclass for Exceptions that result in 409/CONFLICT responses.  See SrampConflictExceptionProvider.
 *
 * @author Brett Meyer
 */
public class ArtificerConflictException extends ArtificerUserException {
    
    private static final long serialVersionUID = 8618489938635309807L;

    public ArtificerConflictException() {
        super();
    }

    public ArtificerConflictException(String message) {
        super(message);
    }

    public ArtificerConflictException(String msg, String stackTrace) {
        super(msg, stackTrace);
    }

    public static ArtificerConflictException artifactConflict(String uuid) {
       return new ArtificerConflictException(Messages.i18n.format("ARTIFACT_ALREADY_EXISTS", uuid));
    }

    public static ArtificerConflictException derivedRelationshipCreation(String relationshipType) {
        return new ArtificerConflictException(Messages.i18n.format("DERIVED_RELATIONSHIP_CREATION", relationshipType));
    }

    public static ArtificerConflictException relationshipConstraint(String uuid) {
        return new ArtificerConflictException(Messages.i18n.format("RELATIONSHIP_CONSTRAINT", uuid));
    }

    public static ArtificerConflictException reservedName(String name) {
        return new ArtificerConflictException(Messages.i18n.format("RESERVED_WORD", name));
    }

    public static ArtificerConflictException customPropertyConstraint(String uuid) {
        return new ArtificerConflictException(Messages.i18n.format("CUSTOM_PROPERTY_CONSTRAINT", uuid));
    }

    public static ArtificerConflictException classifierConstraint(String uuid) {
        return new ArtificerConflictException(Messages.i18n.format("CLASSIFIER_CONSTRAINT", uuid));
    }

    public static ArtificerConflictException ontologyConflict(String ontologyUuid) {
        return new ArtificerConflictException(Messages.i18n.format("ONTOLOGY_ALREADY_EXISTS", ontologyUuid));
    }

    public static ArtificerConflictException storedQueryConflict() {
        return new ArtificerConflictException(Messages.i18n.format("STOREDQUERY_NAME_REQUIRED"));
    }

    public static ArtificerConflictException storedQueryConflict(String queryName) {
        return new ArtificerConflictException(Messages.i18n.format("STOREDQUERY_ALREADY_EXISTS", queryName));
    }

    public static ArtificerConflictException duplicateName(String name) {
        return new ArtificerConflictException(Messages.i18n.format("DUPLICATE_NAME", name));
    }
}
