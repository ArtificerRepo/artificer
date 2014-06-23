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
        String ALTERABLE = "allow-alter"; //$NON-NLS-1$

        /**
         * The condition element identifier.
         */
        String CONDITION = "condition"; //$NON-NLS-1$

        /**
         * The allow create element identifier.
         */
        String CREATABLE = "allow-create"; //$NON-NLS-1$

        /**
         * The allow delete element identifier.
         */
        String DELETABLE = "allow-delete"; //$NON-NLS-1$

        /**
         * The allow execute element identifier.
         */
        String EXECUTABLE = "allow-execute"; //$NON-NLS-1$

        /**
         * The allow language element identifier.
         */
        String LANGUAGABLE = "allow-language"; //$NON-NLS-1$

        /**
         * The mask element identifier.
         */
        String MASK = "mask"; //$NON-NLS-1$

        /**
         * The allow read element identifier.
         */
        String READABLE = "allow-read"; //$NON-NLS-1$

        /**
         * The resource name element identifier.
         */
        String RESOURCE_NAME = "resource-name"; //$NON-NLS-1$

        /**
         * The allow update element identifier.
         */
        String UPDATABLE = "allow-update"; //$NON-NLS-1$

    }

    /**
     * Data permission artifact property names.
     */
    public interface PropertyId {

        /**
         * Indicates if the permission resource is alterable.
         */
        String ALTERABLE = "alterable"; //$NON-NLS-1$

        /**
         * The optional permission condition.
         */
        String CONDITION = "condition"; //$NON-NLS-1$

        /**
         * Indicates if the permission resource is creatable.
         */
        String CREATABLE = "creatable"; //$NON-NLS-1$

        /**
         * Indicates if the permission resource is deletable.
         */
        String DELETABLE = "deletable"; //$NON-NLS-1$

        /**
         * Indicates if the permission resource is executable.
         */
        String EXECUTABLE = "executable"; //$NON-NLS-1$

        /**
         * Indicates if the permission resource allows language.
         */
        String LANGUAGABLE = "languagable"; //$NON-NLS-1$

        /**
         * The mask property.
         */
        String MASK = "mask"; //$NON-NLS-1$

        /**
         * Indicates if the permission resource is readable.
         */
        String READABLE = "readable"; //$NON-NLS-1$

        /**
         * Indicates if the permission resource is updatable.
         */
        String UPDATABLE = "updatable"; //$NON-NLS-1$

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
