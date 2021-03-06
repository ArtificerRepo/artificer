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

/**
 * The Teiid VDB Import VDB model object.
 */
public final class VdbImport {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to Import VDB elements.
     */
    public interface ManifestId {

        /**
         * The import VDB import data policies attribute identifier.
         */
        String IMPORT_DATA_POLICIES = "import-data-policies";

        /**
         * The Import VDB name and property name attribute identifier.
         */
        String NAME = "name";

        /**
         * The Import VDB version attribute identifier.
         */
        String VERSION = "version";

    }

    /**
     * Import VDB artifact property names.
     */
    public interface PropertyId {

        /**
         * Indicates if Import VDB's data policies should be imported.
         */
        String IMPORT_DATA_POLICIES = "importDataPolicies";

        /**
         * The Import VDB version.
         */
        String VERSION = "vdbVersion";

    }

    /**
     * The artifact type of a Teiid VDB import VDB.
     */
    public static final VdbManifest.VdbManifestExtendedType ARTIFACT_TYPE = VdbManifest.VdbManifestExtendedType.IMPORT_VDB;

    /**
     * The default import data policies setting. Value is {@value} .
     */
    public static final boolean DEFAULT_IMPORT_DATA_POLICIES = true;

}
