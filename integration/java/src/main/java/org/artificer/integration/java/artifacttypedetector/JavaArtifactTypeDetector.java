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
package org.artificer.integration.java.artifacttypedetector;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.integration.ArchiveContext;
import org.artificer.integration.artifacttypedetector.AbstractArtifactTypeDetector;
import org.artificer.integration.java.model.JavaModel;

/**
 * @author Brett Meyer.
 */
public class JavaArtifactTypeDetector extends AbstractArtifactTypeDetector {

    @Override
    public ArtifactType detect(ArtifactContent content) {
        if ("pom.xml".equals(content.getFilename())) {
            return ArtifactType.valueOf(JavaModel.TYPE_MAVEN_POM_XML, true);
        }
        if ("beans.xml".equals(content.getFilename())) {
            return ArtifactType.valueOf(JavaModel.TYPE_BEANS_XML, true);
        }
        return null;
    }

    @Override
    public ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext) {
        if (archiveContext.isExpandedFromArchive()) {
            // NOTE: By default, classes are not allowed by AbstractArtifactTypeDetector#allowExpansionFromArchive.
            // However, in case another detector allows them under certain circumstances
            // (see SwitchYardArtifactTypeDetector), handle the detection here.
            if (content.getFilename().endsWith(".class")) {
                return ArtifactType.valueOf(JavaModel.TYPE_JAVA_CLASS, true);
            }
            return detect(content);
        }

        if (archiveContext.hasArchiveEntry("META-INF/application.xml")
                || content.getFilename().endsWith(".ear")) {
            return ArtifactType.valueOf(JavaModel.TYPE_ENTERPRISE_APPLICATION, true);
        }
        if (archiveContext.hasArchiveEntry("WEB-INF/web.xml")
                || content.getFilename().endsWith(".war")) {
            return ArtifactType.valueOf(JavaModel.TYPE_WEB_APPLICATION, true);
        }
        if (archiveContext.hasArchiveEntry("META-INF/MANIFEST.MF")
                || content.getFilename().endsWith(".jar")) {
            return ArtifactType.valueOf(JavaModel.TYPE_ARCHIVE, true);
        }

        return null;
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
