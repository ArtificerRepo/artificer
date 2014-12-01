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
package org.overlord.sramp.integration.switchyard.artifacttypedetector;

import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.integration.ArchiveContext;
import org.overlord.sramp.integration.artifacttypedetector.AbstractArtifactTypeDetector;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;

import java.io.File;

/**
 * @author Brett Meyer.
 */
public class SwitchYardArtifactTypeDetector extends AbstractArtifactTypeDetector {

    @Override
    public ArtifactType detect(ArtifactContent content) {
        if (content.getFilename().equals("switchyard.xml")) { //$NON-NLS-1$
            return ArtifactType.valueOf(SwitchYardModel.SwitchYardXmlDocument, true);
        }
        return null;
    }

    @Override
    public ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext) {
        if (archiveContext.isExpandedFromArchive()) {
            return detect(content);
        } else {
            // In an archive itself.  Detect whether or not is a SY app.
            File switchYardXml = getSwitchYardXml(archiveContext);
            if (switchYardXml != null) {
                SwitchYardAppIndex switchYardAppIndex = new SwitchYardAppIndex(switchYardXml);
                archiveContext.addCustomContext("switchyard.index", switchYardAppIndex);
                return ArtifactType.valueOf(SwitchYardModel.SwitchYardApplication, true);
            }
        }
        return null;
    }

    @Override
    public boolean allowExpansionFromArchive(ArtifactContent content, ArchiveContext archiveContext) {
        if (content.getFilename().endsWith(".class")) {
            // allow classes that are referenced in switchyard.xml
            String entryPath = content.getPath();
            String classname = entryPath.replace('/', '.').replace('\\', '.');
            classname = classname.substring(0, classname.length() - 6);
            if (archiveContext.hasCustomContext("switchyard.index")) {
                SwitchYardAppIndex switchYardAppIndex = (SwitchYardAppIndex) archiveContext.getCustomContext(
                        "switchyard.index");
                if (switchYardAppIndex.contains(classname)) {
                    return true;
                }
            } else if (archiveContext.isExtendedTypeArchive(SwitchYardModel.SwitchYardApplication)) {
                // This seems odd, but is necessary (and legacy).  If a client uploads a jar and explicitly gives a
                // "SwitchYardApplication" type, but it does not include a switchyard.xml (ie, no SwitchYardAppIndex),
                // we need to expand all classes by default!  This is in order to support multi-module SY applications
                // where common classes are in a separate jar.
                return true;
            }
        }
        return super.allowExpansionFromArchive(content, archiveContext);
    }

    private File getSwitchYardXml(ArchiveContext archiveContext) {
        if (archiveContext.hasArchiveEntry("WEB-INF/switchyard.xml")) {
            return archiveContext.getArchiveEntry("WEB-INF/switchyard.xml");
        }
        if (archiveContext.hasArchiveEntry("WEB-INF/classes/META-INF/switchyard.xml")) {
            return archiveContext.getArchiveEntry("WEB-INF/classes/META-INF/switchyard.xml");
        }
        if (archiveContext.hasArchiveEntry("META-INF/switchyard.xml")) {
            return archiveContext.getArchiveEntry("META-INF/switchyard.xml");
        }
        return null;
    }

    @Override
    public int getPriority() {
        return 2;
    }
}
