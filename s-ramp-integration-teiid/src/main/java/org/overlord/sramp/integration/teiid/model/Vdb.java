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
 * The Teiid VDB archive model object.
 */
public final class Vdb {

    /**
     * The VDB extended artifact types.
     */
    public enum VdbExtendedType implements TeiidExtendedType {

        /**
         * The VDB configuration info file contained in a VDB. This file is no longer supported but put here in case a deriver
         * would be written for files of this type.
         */
        CONFIG_INFO("ConfigInfo"); //$NON-NLS-1$

        private final String extendedType;

        private VdbExtendedType( final String extendedType ) {
            this.extendedType = PREFIX + extendedType;
        }

        /**
         * {@inheritDoc}
         * 
         * @see org.overlord.sramp.integration.teiid.model.TeiidExtendedType#convert(java.lang.String)
         */
        @Override
        public TeiidExtendedType convert( final String proposedExtendedType ) {
            for (final VdbExtendedType type : values()) {
                if (type.extendedType().equals(proposedExtendedType)) {
                    return type;
                }
            }

            throw new IllegalArgumentException(Messages.I18N.format("invalidVdbExtendedType", proposedExtendedType)); //$NON-NLS-1$
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
            for (final VdbExtendedType type : values()) {
                if (type.extendedType().equals(proposedExtendedType)) {
                    return true;
                }
            }

            return false;
        }

    }

    /**
     * The artifact type of a Teiid VDB archive. Value is {@value} .
     */
    public static final String ARTIFACT_TYPE = TeiidArtifactType.VDB.extendedType();

    /**
     * The name of the configuration info definition file. Value is {@value} .
     */
    public static final String CONFIGURATION_INFO_FILE_NAME = "ConfigurationInfo.def"; //$NON-NLS-1$

    /**
     * The default version for a VDB. Value is {@value} .
     */
    public static final String DEFAULT_VERSION = "1"; //$NON-NLS-1$

    /**
     * The prefix used for each Teiid VDB artifact extended type.
     */
    static final String PREFIX = TeiidArtifactType.PREFIX + "Vdb"; //$NON-NLS-1$

}
