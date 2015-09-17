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

import org.artificer.integration.teiid.Messages;

/**
 * The Teiid VDB manifest model object.
 */
public final class VdbManifest {

    /**
     * The VDB manifest (<code>vdb.xml</code>) VDB-related identifiers.
     */
    public interface ManifestId extends Describable.XmlId {

        /**
         * The VDB data role element identifier. Zero or more data roles are allowed.
         */
        String DATA_POLICY = "data-role";

        /**
         * The VDB entry element identifier. Zero or more VDB entries are allowed.
         */
        String ENTRY = "entry";

        /**
         * The import VDB element identifier. Zero or more import VDB names are allowed.
         */
        String IMPORT_VDB = "import-vdb";

        /**
         * The VDB name attribute identifier.
         */
        String NAME = "name";

        /**
         * The VDB schema/model element identifier. Zero or more VDB models are allowed.
         */
        String SCHEMA = "model";

        /**
         * The VDB translator element identifier. Zero or more VDB translators are allowed.
         */
        String TRANSLATOR = "translator";

        /**
         * The VDB validation error element identifier. Zero or more VDB translators are allowed for each model.
         */
        String VALIDATION_ERROR = "validation-error";

        /**
         * The VDB element identifier that is the one and only child element under <code>xs:schema</code>.
         */
        String VDB_ELEMENT = "vdb";

        /**
         * The VDB version attribute identifier.
         */
        String VERSION = "version";

    }

    /**
     * The VDB manifest artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId, Propertied.XmlId {

        /**
         * A property indicating if the VDB is a preview VDB.
         */
        String PREVIEW = "preview";

        /**
         * The VDB use connector metadata property.
         */
        String USE_CONNECTOR_METADATA = "UseConnectorMetadata";

        /**
         * The VDB version.
         */
        String VERSION = "vdbVersion";

    }

    /**
     * The VDB manifest-related extended artifact types.
     */
    enum VdbManifestExtendedType implements TeiidExtendedType {

        /**
         * The VDB manifest data policy extended artifact type.
         */
        DATA_POLICY("DataPolicy"),

        /**
         * The VDB manifest entry extended artifact type.
         */
        ENTRY("Entry"),

        /**
         * The VDB manifest import VDB extended artifact type.
         */
        IMPORT_VDB("ImportVdb"),

        /**
         * The VDB manifest permission extended artifact type.
         */
        PERMISSION("Permission"),

        /**
         * The VDB manifest schema/model extended artifact type.
         */
        SCHEMA("Schema"),

        /**
         * The VDB manifest schema source extended artifact type.
         */
        SOURCE("Source"),

        /**
         * The VDB manifest translator extended artifact type.
         */
        TRANSLATOR("Translator"),

        /**
         * The VDB manifest validation error artifact type.
         */
        VALIDATION_ERROR("ValidationError");

        private final String extendedType;

        private VdbManifestExtendedType( final String extendedType ) {
            this.extendedType = PREFIX + extendedType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see TeiidExtendedType#convert(java.lang.String)
         */
        @Override
        public TeiidExtendedType convert( final String proposedExtendedType ) {
            for (final VdbManifestExtendedType type : values()) {
                if (type.extendedType().equals(proposedExtendedType)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(Messages.I18N.format("invalidVdbManifestExtendedType", proposedExtendedType));
        }

        /**
         * {@inheritDoc}
         * 
         * @see TeiidExtendedType#extendedType()
         */
        @Override
        public String extendedType() {
            return this.extendedType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see TeiidExtendedType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid( final String proposedExtendedType ) {
            for (final VdbManifestExtendedType type : values()) {
                if (type.extendedType().equals(proposedExtendedType)) {
                    return true;
                }
            }

            return false;
        }

    }

    /**
     * Relationships between artifacts of a VDB manifest.
     */
    enum VdbManifestRelationshipType implements TeiidRelationshipType {

        /**
         * A relationship between a VDB manifest artifact and its artifacts.
         */
        CONTAINS("Contains"),

        /**
         * A relationship between a data policy artifact and its permission artifacts.
         */
        DATA_POLICY_TO_PERMISSIONS("DataPolicyPermissions"),

        /**
         * A relationship between a permission artifact and its data policy artifact.
         */
        PERMISSION_TO_DATA_POLICY("PermissionDataPolicy"),

        /**
         * A relationship between a schema/model artifact and its source artifacts.
         */
        SCHEMA_TO_SOURCES("SchemaSources"),

        /**
         * A relationship between a schema/model artifact and its validation error artifacts.
         */
        SCHEMA_TO_VALIDATION_ERRORS("SchemaValidationErrors"),

        /**
         * A relationship between a source artifact and its schema/model artifacts.
         */
        SOURCE_TO_SCHEMAS("SourceSchemas"),

        /**
         * A relationship between a source artifact and its translator artifact.
         */
        SOURCE_TO_TRANSLATOR("SourceTranslator"),

        /**
         * A relationship between a translator artifact and the source artifacts that reference it.
         */
        TRANSLATOR_TO_SOURCES("TranslatorSources"),

        /**
         * A relationship between a validation error artifact and the source artifacts that reference it.
         */
        VALIDATION_ERROR_TO_SOURCE("ValidationErrorSource");

        private final String relationshipType;

        private VdbManifestRelationshipType( final String relationshipType ) {
            this.relationshipType = PREFIX + relationshipType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see TeiidRelationshipType#convert(java.lang.String)
         */
        @Override
        public TeiidRelationshipType convert( final String proposedRelationshipType ) {
            for (final VdbManifestRelationshipType type : values()) {
                if (type.relationshipType().equals(proposedRelationshipType)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(Messages.I18N.format("invalidVdbRelationshipType", proposedRelationshipType));
        }

        /**
         * {@inheritDoc}
         * 
         * @see TeiidRelationshipType#isValid(java.lang.String)
         */
        @Override
        public boolean isValid( final String proposedRelationshipType ) {
            for (final VdbManifestRelationshipType type : values()) {
                if (type.relationshipType().equals(proposedRelationshipType)) {
                    return true;
                }
            }

            return false;
        }

        /**
         * {@inheritDoc}
         * 
         * @see TeiidRelationshipType#relationshipType()
         */
        @Override
        public String relationshipType() {
            return this.relationshipType;
        }

    }

    /**
     * The artifact type of a Teiid VDB manifest.
     */
    public static final TeiidExtendedType ARTIFACT_TYPE = TeiidArtifactType.VDB_MANIFEST;

    /**
     * A relationship between a VDB artifact and its derived artifacts.
     */
    public static final TeiidRelationshipType CONTAINS_RELATIONSHIP = VdbManifestRelationshipType.CONTAINS;

    /**
     * The name of the Teiid manifest file that is contained in the VDB archive.
     */
    public static final String FILE_NAME = "vdb.xml";

    /**
     * The path of the manifest file (including the name) contained in the VDB archive.
     */
    public static final String PATH = "META-INF/" + FILE_NAME;

    private static final String PREFIX = Vdb.PREFIX;

}
