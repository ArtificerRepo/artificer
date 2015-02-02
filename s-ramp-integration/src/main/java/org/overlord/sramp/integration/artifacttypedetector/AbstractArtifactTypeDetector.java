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
package org.overlord.sramp.integration.artifacttypedetector;

import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.integration.ArchiveContext;

/**
 * @author Brett Meyer.
 */
public abstract class AbstractArtifactTypeDetector implements ArtifactTypeDetector {

    @Override
    public boolean isArchive(ArtifactContent content) {
        String filename = content.getFilename();
        return filename.endsWith(".jar") || filename.endsWith(".ear") || filename.endsWith(".war")
                || filename.endsWith(".zip");
    }

    @Override
    public boolean allowExpansionFromArchive(ArtifactContent content, ArchiveContext archiveContext) {
        if (
                // core
                content.getFilename().endsWith(".xsd") || content.getFilename().endsWith(".wsdl") || content.getFilename().endsWith(".wspolicy")
                // Maven Facade support
                || content.getFilename().endsWith(".sha1") || content.getFilename().endsWith(".pom")) {
            return true;
        }
        if (content.getFilename().endsWith(".xml") && !content.getFilename().equals("pom.xml")) {
            return true;
        }
        return false;
    }
}
