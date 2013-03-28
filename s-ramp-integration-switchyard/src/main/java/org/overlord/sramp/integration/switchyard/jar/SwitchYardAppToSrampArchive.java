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
package org.overlord.sramp.integration.switchyard.jar;

import java.io.File;
import java.io.InputStream;

import org.overlord.sramp.atom.archive.jar.JarToSrampArchive;
import org.overlord.sramp.atom.archive.jar.JarToSrampArchiveException;

/**
 * Creates an s-ramp archive from a SwitchYard application (JAR or WAR).
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardAppToSrampArchive extends JarToSrampArchive {

    /**
     * Constructor.
     * @param jar
     * @throws JarToSrampArchiveException
     */
    public SwitchYardAppToSrampArchive(File jar) throws JarToSrampArchiveException {
        super(jar);
        this.setArtifactFilter(new SwitchYardArtifactFilter());
        this.setMetaDataFactory(new SwitchYardMetaDataFactory());
    }

    /**
     * Constructor.
     * @param jarStream
     * @throws JarToSrampArchiveException
     */
    public SwitchYardAppToSrampArchive(InputStream jarStream) throws JarToSrampArchiveException {
        super(jarStream);
        this.setArtifactFilter(new SwitchYardArtifactFilter());
        this.setMetaDataFactory(new SwitchYardMetaDataFactory());
    }

}
