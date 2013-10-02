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
 * The Teiid VDB schema (<code>*.xmi</code> model file) model object.
 */
public final class VdbSchema {

    /**
     * The VDB manifest (<code>vdb.xml</code>) identifiers related to schema/model elements.
     */
    public interface ManifestId extends Describable.XmlId, Propertied.XmlId {

        /**
         * The VDB schema/model metadata element identifier. The metadata is optional.
         */
        String METADATA = "metadata"; //$NON-NLS-1$

        /**
         * The schema/model metadata type attribute identifier.
         */
        String METADATA_TYPE = "type"; //$NON-NLS-1$

        /**
         * The schema/model name attribute identifier.
         */
        String NAME = "name"; //$NON-NLS-1$

        /**
         * The schema/model path in the VDB archive or the validation error model object path.
         */
        String PATH = "path"; //$NON-NLS-1$

        /**
         * The severity of a schema/model validation error attribute name.
         */
        String SEVERITY = "severity"; //$NON-NLS-1$

        /**
         * The VDB schema/model data source element identifier.
         */
        String SOURCE = "source"; //$NON-NLS-1$

        /**
         * The schema/model type attribute identifier.
         */
        String TYPE = "type"; //$NON-NLS-1$

        /**
         * A schema/model validation error.
         */
        String VALIDATION_ERROR = "validation-error"; //$NON-NLS-1$

        /**
         * The schema/model visible attribute identifier.
         */
        String VISIBLE = "visible"; //$NON-NLS-1$

    }

    /**
     * The type of metadata definition language.
     */
    public enum MetadataType {

        /**
         * DDL is the default model definition metadata type. Value is {@value} .
         */
        DDL

    }

    /**
     * VDB schema/model artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId, Propertied.XmlId {

        /**
         * The model built-in property.
         */
        String BUILT_IN = "builtIn"; //$NON-NLS-1$

        /**
         * The model checksum property.
         */
        String CHECKSUM = "checksum"; //$NON-NLS-1$

        /**
         * The model index name.
         */
        String INDEX_NAME = "indexName"; //$NON-NLS-1$

        /**
         * The model metadata definition.
         */
        String METADATA = "metadata"; //$NON-NLS-1$

        /**
         * The metadata type (defaults to 'DDL').
         */
        String METADATA_TYPE = "metadataType"; //$NON-NLS-1$

        /**
         * The model class.
         */
        String MODEL_CLASS = "modelClass"; //$NON-NLS-1$

        /**
         * The model unique identifier property.
         */
        String MODEL_UUID = "modelUuid"; //$NON-NLS-1$

        /**
         * The model path in the VDB archive.
         */
        String PATH_IN_VDB = "pathInVdb"; //$NON-NLS-1$

        /**
         * The model type (like physical or virtual).
         */
        String TYPE = "schemaType"; //$NON-NLS-1$

        /**
         * Indicates if the schema/model is visible for user queries.
         */
        String VISIBLE = "visible"; //$NON-NLS-1$

    }

    /**
     * The schema/model types.
     */
    public enum Type {

        /**
         * A physical schema/model type.
         */
        PHYSICAL,

        /**
         * A virtual/view schema/model type.
         */
        VIRTUAL

    }

    /**
     * The artifact type of a Teiid VDB schema/model manifest entry.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = VdbManifestExtendedType.SCHEMA;

    /**
     * The default model definition metadata type. Value is {@value} .
     */
    public static final MetadataType DEFAULT_METADATA_TYPE = MetadataType.DDL;

    /**
     * The default schema/model type. Value is {@value} .
     */
    public static final Type DEFAULT_TYPE = Type.PHYSICAL;

    /**
     * A relationship between a schema/model artifact and its source artifacts.
     */
    public static final TeiidRelationshipType SOURCES_RELATIONSHIP = VdbManifestRelationshipType.SCHEMA_TO_SOURCES;

    /**
     * A relationship between a schema/model artifact and its validation error artifacts.
     */
    public static final TeiidRelationshipType VALIDATION_ERRORS_RELATIONSHIP = VdbManifestRelationshipType.SCHEMA_TO_VALIDATION_ERRORS;

}
