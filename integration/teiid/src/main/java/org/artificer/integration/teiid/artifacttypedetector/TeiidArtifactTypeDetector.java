/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.integration.teiid.artifacttypedetector;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.integration.ArchiveContext;
import org.artificer.integration.artifacttypedetector.AbstractArtifactTypeDetector;
import org.artificer.integration.teiid.model.TeiidArtifactType;
import org.artificer.integration.teiid.model.TeiidModel;
import org.artificer.integration.teiid.model.Vdb;
import org.artificer.integration.teiid.model.VdbManifest;

/**
 * @author Brett Meyer.
 */
public class TeiidArtifactTypeDetector extends AbstractArtifactTypeDetector {
    @Override
    public ArtifactType detect(ArtifactContent content) {
        // VDB manifest
        if (VdbManifest.FILE_NAME.equals(content.getFilename())) {
            return ArtifactType.valueOf(TeiidArtifactType.VDB_MANIFEST.extendedType(), true);
        }
        // Teiid models
        if (content.getFilename().endsWith(TeiidModel.FILE_EXT)) {
            return ArtifactType.valueOf(TeiidArtifactType.MODEL.extendedType(), true);
        }
        // VDB configuration info def
        if (Vdb.CONFIGURATION_INFO_FILE_NAME.equals(content.getFilename())) {
            return ArtifactType.valueOf(Vdb.VdbExtendedType.CONFIG_INFO.extendedType(), true);
        }
        return null;
    }

    @Override
    public ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext) {
        if (archiveContext.isExpandedFromArchive()) {
            return detect(content);
        } else if (archiveContext.hasArchiveEntry("META-INF/vdb.xml")) {
            return ArtifactType.valueOf(Vdb.ARTIFACT_TYPE, true);
        }
        return null;
    }

    @Override
    public boolean isArchive(ArtifactContent content) {
        if (content.getFilename().endsWith(".vdb")) {
            return true;
        } else {
            return super.isArchive(content);
        }
    }

    @Override
    public boolean allowExpansionFromArchive(ArtifactContent content, ArchiveContext archiveContext) {
        if (content.getFilename().endsWith(TeiidModel.FILE_EXT)) {
            return true;
        }
        return super.allowExpansionFromArchive(content, archiveContext);
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
