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
package org.overlord.sramp.integration.switchyard.expand;

import java.io.File;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider;
import org.overlord.sramp.common.ArtifactType;

/**
 * Provides a SwitchYard version of the {@link ZipToSrampArchive}.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardAppToSrampArchiveProvider implements ZipToSrampArchiveProvider {

    private static final Set<String> acceptedTypes = new HashSet<String>();
    static {
        acceptedTypes.add("SwitchYardApplication"); //$NON-NLS-1$
    }

    /**
     * Constructor.
     */
    public SwitchYardAppToSrampArchiveProvider() {
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#accept(org.overlord.sramp.common.ArtifactType)
     */
    @Override
    public boolean accept(ArtifactType artifactType) {
        if (artifactType.isExtendedType()) {
            String extendedType = artifactType.getExtendedType();
            return acceptedTypes.contains(extendedType);
        }
        return false;
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#createExtractor(org.overlord.sramp.common.ArtifactType, java.io.File)
     */
    @Override
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, File zipFile) throws ZipToSrampArchiveException {
        return new SwitchYardAppToSrampArchive(zipFile);
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveProvider#createExtractor(org.overlord.sramp.common.ArtifactType, java.io.InputStream)
     */
    @Override
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, InputStream zipStream) throws ZipToSrampArchiveException {
        return new SwitchYardAppToSrampArchive(zipStream);
    }

}
