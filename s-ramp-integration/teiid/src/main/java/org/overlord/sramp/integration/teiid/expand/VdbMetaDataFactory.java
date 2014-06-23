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

package org.overlord.sramp.integration.teiid.expand;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.DiscoveredArtifact;
import org.overlord.sramp.integration.teiid.model.TeiidArtifactType;
import org.overlord.sramp.integration.teiid.model.TeiidModel;
import org.overlord.sramp.integration.teiid.model.Vdb;
import org.overlord.sramp.integration.teiid.model.VdbManifest;

/**
 * The meta-data factory used when expanding a Teiid VDB into an S-RAMP archive.
 */
public final class VdbMetaDataFactory extends DefaultMetaDataFactory {

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory#createArtifact(org.overlord.sramp.atom.archive.expand.DiscoveredArtifact)
     */
    @Override
    protected BaseArtifactType createArtifact( final DiscoveredArtifact discoveredArtifact ) {
        // VDB manifest
        if (VdbManifest.FILE_NAME.equals(discoveredArtifact.getName())) {
            final ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(TeiidArtifactType.VDB_MANIFEST.extendedType());
            return artifact;
        }

        // Teiid models
        if (discoveredArtifact.getName().endsWith(TeiidModel.FILE_EXT)) {
            final ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(TeiidArtifactType.MODEL.extendedType());
            return artifact;
        }

        // VDB configuration info def
        if (Vdb.CONFIGURATION_INFO_FILE_NAME.equals(discoveredArtifact.getName())) {
            final ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(Vdb.VdbExtendedType.CONFIG_INFO.extendedType());
            return artifact;
        }

        return super.createArtifact(discoveredArtifact);
    }

}
