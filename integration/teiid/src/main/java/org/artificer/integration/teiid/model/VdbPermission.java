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
package org.artificer.integration.teiid.model;

import org.artificer.integration.teiid.model.VdbManifest.VdbManifestExtendedType;
import org.artificer.integration.teiid.model.VdbManifest.VdbManifestRelationshipType;

/**
 * The Teiid VDB data permission model object.
 */
public final class VdbPermission {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to data policy permission elements.
     */
    public interface ManifestId {

        /**
         * The allow alter element identifier.
         */
        String ALTERABLE = "allow-alter";

        /**
         * The condition element identifier.
         */
        String CONDITION = "condition";

        /**
         * The allow create element identifier.
         */
        String CREATABLE = "allow-create";

        /**
         * The allow delete element identifier.
         */
        String DELETABLE = "allow-delete";

        /**
         * The allow execute element identifier.
         */
        String EXECUTABLE = "allow-execute";

        /**
         * The allow language element identifier.
         */
        String LANGUAGABLE = "allow-language";

        /**
         * The mask element identifier.
         */
        String MASK = "mask";

        /**
         * The allow read element identifier.
         */
        String READABLE = "allow-read";

        /**
         * The resource name element identifier.
         */
        String RESOURCE_NAME = "resource-name";

        /**
         * The allow update element identifier.
         */
        String UPDATABLE = "allow-update";

    }

    /**
     * Data permission artifact property names.
     */
    public interface PropertyId {

        /**
         * Indicates if the permission resource is alterable.
         */
        String ALTERABLE = "alterable";

        /**
         * The optional permission condition.
         */
        String CONDITION = "condition";

        /**
         * Indicates if the permission resource is creatable.
         */
        String CREATABLE = "creatable";

        /**
         * Indicates if the permission resource is deletable.
         */
        String DELETABLE = "deletable";

        /**
         * Indicates if the permission resource is executable.
         */
        String EXECUTABLE = "executable";

        /**
         * Indicates if the permission resource allows language.
         */
        String LANGUAGABLE = "languagable";

        /**
         * The mask property.
         */
        String MASK = "mask";

        /**
         * Indicates if the permission resource is readable.
         */
        String READABLE = "readable";

        /**
         * Indicates if the permission resource is updatable.
         */
        String UPDATABLE = "updatable";

    }

    /**
     * The artifact type of a Teiid VDB data policy permission.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.PERMISSION;

    /**
     * A relationship between a permission artifact and its data policy artifact.
     */
    public static final TeiidRelationshipType DATA_POLICY_RELATIONSHIP = VdbManifestRelationshipType.PERMISSION_TO_DATA_POLICY;

}
