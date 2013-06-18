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
package org.overlord.sramp.atom.archive.expand.registry;

import java.io.File;
import java.io.InputStream;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;
import org.overlord.sramp.common.ArtifactType;

/**
 * Provides instances of {@link ZipToSrampArchive} for specific artifact types.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ZipToSrampArchiveProvider {

    /**
     * Returns true if this provider is capable of providing an expander for the given artifact type.
     * @param artifactType
     */
    public boolean accept(ArtifactType artifactType);

    /**
     * Creates a {@link ZipToSrampArchive} expander for the given artifact type and zip content.
     * @param artifactType
     * @param zipFile
     * @throws ZipToSrampArchiveException
     */
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, File zipFile) throws ZipToSrampArchiveException;

    /**
     * Creates a {@link ZipToSrampArchive} expander for the given artifact type and zip content.
     * @param artifactType
     * @param zipStream
     * @throws ZipToSrampArchiveException
     */
    public ZipToSrampArchive createExtractor(ArtifactType artifactType, InputStream zipStream) throws ZipToSrampArchiveException;

}
