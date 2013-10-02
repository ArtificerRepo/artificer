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
 * The Teiid VDB data policy/role model object.
 */
public final class VdbDataPolicy {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to data policy elements.
     */
    public interface ManifestId extends Describable.XmlId {

        /**
         * The any authenticated attribute identifier.
         */
        String ANY_AUTHENTICATED = "any-authenticated"; //$NON-NLS-1$

        /**
         * The data policy name attribute identifier.
         */
        String NAME = "name"; //$NON-NLS-1$

        /**
         * The data permission element identifier.
         */
        String PERMISSION = "permission"; //$NON-NLS-1$

        /**
         * The mapped role name element identifier.
         */
        String ROLE_NAME = "mapped-role-name"; //$NON-NLS-1$

        /**
         * The allow create temporary tables attribute identifier.
         */
        String TEMP_TABLE_CREATABLE = "allow-create-temporary-tables"; //$NON-NLS-1$

    }

    /**
     * Data policy artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId {

        /**
         * Indicates if data policy has any authenticated.
         */
        String ANY_AUTHENTICATED = "anyAuthenticated"; //$NON-NLS-1$

        /**
         * A collection of role names.
         */
        String ROLE_NAMES = "roleNames"; //$NON-NLS-1$

        /**
         * Indicates if data policy can create temp tables.
         */
        String TEMP_TABLE_CREATABLE = "tempTableCreatable"; //$NON-NLS-1$

    }

    /**
     * The artifact type of a Teiid VDB data policy.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.DATA_POLICY;

    /**
     * A relationship between a data policy artifact and its permission artifacts.
     */
    public static final TeiidRelationshipType PERMISSIONS_RELATIONSHIP = VdbManifestRelationshipType.DATA_POLICY_TO_PERMISSIONS;

}
