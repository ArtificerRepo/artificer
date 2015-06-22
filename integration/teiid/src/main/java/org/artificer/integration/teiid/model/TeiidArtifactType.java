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
 * The Teiid extended artifact types. Files of these types can be uploaded to S-RAMP.
 */
public enum TeiidArtifactType implements TeiidExtendedType {

    /**
     * The Teiid model, or schema, file extended artifact type.
     */
    MODEL("Model"),

    /**
     * The Teiid VDB file extended artifact type.
     */
    VDB("Vdb"),

    /**
     * The VDB manifest file extended artifact type.
     */
    VDB_MANIFEST("VdbManifest");

    /**
     * The prefix used for each Teiid artifact extended type.
     */
    static final String PREFIX = "Teiid";

    private final String extendedType;

    private TeiidArtifactType( final String extendedType ) {
        this.extendedType = PREFIX + extendedType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see TeiidExtendedType#convert(java.lang.String)
     */
    @Override
    public TeiidExtendedType convert( final String proposedExtendedType ) {
        for (final TeiidArtifactType type : values()) {
            if (type.extendedType().equals(proposedExtendedType)) {
                return type;
            }
        }

        throw new IllegalArgumentException(Messages.I18N.format("invalidTeiidArtifactType", proposedExtendedType));
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
        for (final TeiidArtifactType type : values()) {
            if (type.extendedType().equals(proposedExtendedType)) {
                return true;
            }
        }

        return false;
    }

}
