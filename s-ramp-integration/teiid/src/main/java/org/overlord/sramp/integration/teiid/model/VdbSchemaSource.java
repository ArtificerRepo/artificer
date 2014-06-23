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
 * The Teiid VDB schema source model object.
 */
public final class VdbSchemaSource {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to schema/model source elements.
     */
    public interface ManifestId {

        /**
         * The schema/model source JNDI name attribute identifier.
         */
        String JNDI_NAME = "connection-jndi-name"; //$NON-NLS-1$

        /**
         * The source name attribute identifier.
         */
        String NAME = "name"; //$NON-NLS-1$

        /**
         * The schema/model source translator name attribute identifier.
         */
        String TRANSLATOR_NAME = "translator-name"; //$NON-NLS-1$

    }

    /**
     * VDB schema/model source artifact property names.
     */
    public interface PropertyId {

        /**
         * The source connection JNDI name.
         */
        String JNDI_NAME = "jndiName"; //$NON-NLS-1$

        /**
         * The source translator name.
         */
        String TRANSLATOR_NAME = "translatorName"; //$NON-NLS-1$

    }

    /**
     * The artifact type of a Teiid VDB schema/model source.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.SOURCE;

    /**
     * A relationship between a source artifact and its schema/model artifact.
     */
    public static final TeiidRelationshipType SCHEMA_RELATIONSHIP = VdbManifestRelationshipType.SOURCE_TO_SCHEMAS;

    /**
     * A relationship between a source artifact and its translator artifact.
     */
    public static final TeiidRelationshipType TRANSLATOR_RELATIONSHIP = VdbManifestRelationshipType.SOURCE_TO_TRANSLATOR;

}
