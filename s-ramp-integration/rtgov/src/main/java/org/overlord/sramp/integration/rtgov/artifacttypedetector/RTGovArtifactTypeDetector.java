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
package org.overlord.sramp.integration.rtgov.artifacttypedetector;

import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.integration.ArchiveContext;
import org.overlord.sramp.integration.artifacttypedetector.AbstractArtifactTypeDetector;
import org.overlord.sramp.integration.rtgov.model.RTGovModel;

/**
 * @author Brett Meyer.
 */
public class RTGovArtifactTypeDetector extends AbstractArtifactTypeDetector {

    @Override
    public ArtifactType detect(ArtifactContent content) {
        return RTGovModel.detect(content.getFilename());
    }

    @Override
    public ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext) {
        return detect(content);
    }

    @Override
    public int getPriority() {
        return 20;
    }
}
