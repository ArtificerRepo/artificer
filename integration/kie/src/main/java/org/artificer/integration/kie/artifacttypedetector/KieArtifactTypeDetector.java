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
package org.artificer.integration.kie.artifacttypedetector;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.integration.ArchiveContext;
import org.artificer.integration.artifacttypedetector.AbstractArtifactTypeDetector;
import org.artificer.integration.kie.model.KieJarModel;

/**
 * @author Brett Meyer.
 */
public class KieArtifactTypeDetector extends AbstractArtifactTypeDetector {

    @Override
    public ArtifactType detect(ArtifactContent content) {
        if (content.getFilename().equals("kmodule.xml")) {
            return ArtifactType.valueOf(KieJarModel.KieXmlDocument, true);
        }
        if (content.getFilename().endsWith(".bpmn") || content.getFilename().endsWith(".bpmn2")) {
            return ArtifactType.valueOf(KieJarModel.BpmnDocument, true);
        }
        if (content.getFilename().endsWith(".drl")) {
            return ArtifactType.valueOf(KieJarModel.DroolsDocument, true);
        }
        return null;
    }

    @Override
    public ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext) {
        if (archiveContext.isExpandedFromArchive()) {
            return detect(content);
        } else if (archiveContext.hasArchiveEntry("META-INF/kmodule.xml")) {
            return ArtifactType.valueOf(KieJarModel.TYPE_ARCHIVE, true);
        }
        return null;
    }

    @Override
    public boolean allowExpansionFromArchive(ArtifactContent content, ArchiveContext archiveContext) {
        if (archiveContext.isExtendedTypeArchive(KieJarModel.TYPE_ARCHIVE)) {
            String filename = content.getFilename();
            if (filename.endsWith(".bpmn") || filename.endsWith(".bpmn2") || filename.endsWith(".drl")) {
                return true;
            }
            if (filename.endsWith(".xml")) {

            }
        }
        return super.allowExpansionFromArchive(content, archiveContext);
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
