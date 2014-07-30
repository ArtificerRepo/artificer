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
package org.overlord.sramp.integration.rtgov.expand;

import java.io.File;
import java.io.InputStream;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;
import org.overlord.sramp.atom.archive.expand.registry.TypeHintInfo;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.integration.java.expand.JarToSrampArchive;
import org.overlord.sramp.integration.rtgov.model.RTGovModel;

/**
 * Provides an RTGov version of the {@link ZipToSrampArchive}.  The /ext artifact types are determined by the
 * existence of the following files in the root of the JAR or WAR:
 * 
 * RTGovEPN: epn.json
 * RTGovAV: av.json
 * RTGovIP: ip.json
 * RTGovACS: acs.json
 * 
 * RTGov includes template-based tooling to facilitate creating the artifacts.  These need to be classified
 * differently and, therefore, have their own types:
 * 
 * RTGovEPNTemplate: epn-template.json
 * RTGovAVTemplate: av-template.json
 * RTGovIPTemplate: ip-template.json
 * RTGovACSTemplate: acs-template.json
 *
 * @author Brett Meyer
 */
@Component(name = "RTGov to Sramp Archive Provider", immediate = true)
@Service(value = org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider.class)
public class RTGovToSrampArchiveProvider implements ZipToSrampArchiveProvider {

    /**
     * Constructor.
     */
    public RTGovToSrampArchiveProvider() {
    }

	/**
     * @see ZipToSrampArchiveProvider#accept(ArtifactType)
     */
    @Override
    public boolean accept(ArtifactType artifactType) {
        return RTGovModel.accept(artifactType);
    }

    /**
     * @see ZipToSrampArchiveProvider#createExtractor(ArtifactType, File)
     */
    @Override
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, File zipFile)
            throws ZipToSrampArchiveException {
        return new JarToSrampArchive(zipFile);
    }

    /**
     * @see ZipToSrampArchiveProvider#createExtractor(ArtifactType, InputStream)
     */
    @Override
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, InputStream zipStream)
            throws ZipToSrampArchiveException {
        return new JarToSrampArchive(zipStream);
    }

    @Override
   	public TypeHintInfo getArchiveTypeHints() {
   		return new TypeHintInfo(20, RTGovModel.HINTS);
   	}

}
