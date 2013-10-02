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
package org.overlord.sramp.integration.teiid.model;

import org.overlord.sramp.integration.teiid.model.VdbManifest.VdbManifestExtendedType;
import org.overlord.sramp.integration.teiid.model.VdbManifest.VdbManifestRelationshipType;

/**
 * The Teiid VDB validation error model object.
 */
public final class VdbValidationError {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to validation error elements.
     */
    public interface ManifestId {

        /**
         * The path attribue identifier.
         */
        String PATH = "path"; //$NON-NLS-1$

        /**
         * The severity attribute identifier.
         */
        String SEVERITY = "severity"; //$NON-NLS-1$

    }

    /**
     * Validation error artifact property names. Note: the path is the name.
     */
    public interface PropertyId {

        /**
         * The validation error message property.
         */
        String MESSAGE = "message"; //$NON-NLS-1$

        /**
         * The validation error severity property.
         */
        String SEVERITY = "severity"; //$NON-NLS-1$

    }

    /**
     * The artifact type of a Teiid VDB data policy permission.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.VALIDATION_ERROR;

    /**
     * The path used when no path is specified. Value is {@value} .
     */
    public static final String ROOT_PATH = "/"; //$NON-NLS-1$

    /**
     * A relationship between a validation error artifact and its source artifact.
     */
    public static final TeiidRelationshipType SOURCE_RELATIONSHIP = VdbManifestRelationshipType.VALIDATION_ERROR_TO_SOURCE;

}
