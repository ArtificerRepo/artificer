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
package org.overlord.sramp.integration.kie.expand;

import java.io.File;
import java.io.InputStream;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;

/**
 * Creates an s-ramp archive from a Kie Archive (JAR).
 *
 */
public class KieJarToSrampArchive extends ZipToSrampArchive {

    /**
     * Constructor.
     * @param jar
     * @throws ZipToSrampArchiveException
     */
    public KieJarToSrampArchive(File jar) throws ZipToSrampArchiveException {
        super(jar);
        this.setArtifactFilter(new KieJarArtifactFilter());
        this.setMetaDataFactory(new KieJarMetaDataFactory());
    }

    /**
     * Constructor.
     * @param jarStream
     * @throws ZipToSrampArchiveException
     */
    public KieJarToSrampArchive(InputStream jarStream) throws ZipToSrampArchiveException {
        super(jarStream);
        this.setArtifactFilter(new KieJarArtifactFilter());
        this.setMetaDataFactory(new KieJarMetaDataFactory());
    }

}
