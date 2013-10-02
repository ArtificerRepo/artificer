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

import org.overlord.sramp.integration.teiid.Messages;

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
        String DATA_POLICY = "data-role"; //$NON-NLS-1$

        /**
         * The VDB entry element identifier. Zero or more VDB entries are allowed.
         */
        String ENTRY = "entry"; //$NON-NLS-1$

        /**
         * The import VDB element identifier. Zero or more import VDB names are allowed.
         */
        String IMPORT_VDB = "import-vdb"; //$NON-NLS-1$

        /**
         * The VDB name attribute identifier.
         */
        String NAME = "name"; //$NON-NLS-1$

        /**
         * The VDB schema/model element identifier. Zero or more VDB models are allowed.
         */
        String SCHEMA = "model"; //$NON-NLS-1$

        /**
         * The VDB translator element identifier. Zero or more VDB translators are allowed.
         */
        String TRANSLATOR = "translator"; //$NON-NLS-1$

        /**
         * The VDB validation error element identifier. Zero or more VDB translators are allowed for each model.
         */
        String VALIDATION_ERROR = "validation-error"; //$NON-NLS-1$

        /**
         * The VDB element identifier that is the one and only child element under <code>xs:schema</code>.
         */
        String VDB_ELEMENT = "vdb"; //$NON-NLS-1$

        /**
         * The VDB version attribute identifier.
         */
        String VERSION = "version"; //$NON-NLS-1$

    }

    /**
     * The VDB manifest artifact property names.
     */
    public interface PropertyId extends Describable.PropertyId, Propertied.XmlId {

        /**
         * A property indicating if the VDB is a preview VDB.
         */
        String PREVIEW = "preview"; //$NON-NLS-1$

        /**
         * The VDB use connector metadata property.
         */
        String USE_CONNECTOR_METADATA = "UseConnectorMetadata"; //$NON-NLS-1$

        /**
         * The VDB version.
         */
        String VERSION = "vdbVersion"; //$NON-NLS-1$

    }

    /**
     * The VDB manifest-related extended artifact types.
     */
    enum VdbManifestExtendedType implements TeiidExtendedType {

        /**
         * The VDB manifest data policy extended artifact type.
         */
        DATA_POLICY("DataPolicy"), //$NON-NLS-1$

        /**
         * The VDB manifest entry extended artifact type.
         */
        ENTRY("Entry"), //$NON-NLS-1$

        /**
         * The VDB manifest import VDB extended artifact type.
         */
        IMPORT_VDB("ImportVdb"), //$NON-NLS-1$

        /**
         * The VDB manifest permission extended artifact type.
         */
        PERMISSION("Permission"), //$NON-NLS-1$

        /**
         * The VDB manifest schema/model extended artifact type.
         */
        SCHEMA("Schema"), //$NON-NLS-1$

        /**
         * The VDB manifest schema source extended artifact type.
         */
        SOURCE("Source"), //$NON-NLS-1$

        /**
         * The VDB manifest translator extended artifact type.
         */
        TRANSLATOR("Translator"), //$NON-NLS-1$

        /**
         * The VDB manifest validation error artifact type.
         */
        VALIDATION_ERROR("ValidationError"); //$NON-NLS-1$

        private final String extendedType;

        private VdbManifestExtendedType( final String extendedType ) {
            this.extendedType = PREFIX + extendedType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.overlord.sramp.integration.teiid.model.TeiidExtendedType#convert(java.lang.String)
         */
        @Override
        public TeiidExtendedType convert( final String proposedExtendedType ) {
            for (final VdbManifestExtendedType type : values()) {
                if (type.extendedType().equals(proposedExtendedType)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(Messages.I18N.format("invalidVdbManifestExtendedType", proposedExtendedType)); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.overlord.sramp.integration.teiid.model.TeiidExtendedType#extendedType()
         */
        @Override
        public String extendedType() {
            return this.extendedType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.overlord.sramp.integration.teiid.model.TeiidExtendedType#isValid(java.lang.String)
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
        CONTAINS("Contains"), //$NON-NLS-1$

        /**
         * A relationship between a data policy artifact and its permission artifacts.
         */
        DATA_POLICY_TO_PERMISSIONS("DataPolicyPermissions"), //$NON-NLS-1$

        /**
         * A relationship between a permission artifact and its data policy artifact.
         */
        PERMISSION_TO_DATA_POLICY("PermissionDataPolicy"), //$NON-NLS-1$

        /**
         * A relationship between a schema/model artifact and its source artifacts.
         */
        SCHEMA_TO_SOURCES("SchemaSources"), //$NON-NLS-1$

        /**
         * A relationship between a schema/model artifact and its validation error artifacts.
         */
        SCHEMA_TO_VALIDATION_ERRORS("SchemaValidationErrors"), //$NON-NLS-1$

        /**
         * A relationship between a source artifact and its schema/model artifacts.
         */
        SOURCE_TO_SCHEMAS("SourceSchemas"), //$NON-NLS-1$

        /**
         * A relationship between a source artifact and its translator artifact.
         */
        SOURCE_TO_TRANSLATOR("SourceTranslator"), //$NON-NLS-1$

        /**
         * A relationship between a translator artifact and the source artifacts that reference it.
         */
        TRANSLATOR_TO_SOURCES("TranslatorSources"), //$NON-NLS-1$

        /**
         * A relationship between a validation error artifact and the source artifacts that reference it.
         */
        VALIDATION_ERROR_TO_SOURCE("ValidationErrorSource"); //$NON-NLS-1$

        private final String relationshipType;

        private VdbManifestRelationshipType( final String relationshipType ) {
            this.relationshipType = PREFIX + relationshipType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.overlord.sramp.integration.teiid.model.TeiidRelationshipType#convert(java.lang.String)
         */
        @Override
        public TeiidRelationshipType convert( final String proposedRelationshipType ) {
            for (final VdbManifestRelationshipType type : values()) {
                if (type.relationshipType().equals(proposedRelationshipType)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(Messages.I18N.format("invalidVdbRelationshipType", proposedRelationshipType)); //$NON-NLS-1$
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.overlord.sramp.integration.teiid.model.TeiidRelationshipType#isValid(java.lang.String)
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
         * @see org.overlord.sramp.integration.teiid.model.TeiidRelationshipType#relationshipType()
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
    public static final String FILE_NAME = "vdb.xml"; //$NON-NLS-1$

    /**
     * The path of the manifest file (including the name) contained in the VDB archive.
     */
    public static final String PATH = "META-INF/" + FILE_NAME; //$NON-NLS-1$

    private static final String PREFIX = Vdb.PREFIX;

}
