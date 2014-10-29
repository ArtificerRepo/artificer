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
package org.overlord.sramp.integration.fabric.expand;

import java.io.File;
import java.io.InputStream;

import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;

/**
 * Provides a Fabric version of the {@link ZipToSrampArchive}.
 * 
 * @author David Virgil Naranjo
 */
public class FabricProfileToSrampArchive extends ZipToSrampArchive {

    /**
     * Constructor.
     *
     * @param jar
     *            the jar
     * @throws ZipToSrampArchiveException
     *             the zip to sramp archive exception
     */
    public FabricProfileToSrampArchive(File jar) throws ZipToSrampArchiveException {
        super(jar);

    }

    /**
     * Constructor.
     *
     * @param jarStream
     *            the jar stream
     * @throws ZipToSrampArchiveException
     *             the zip to sramp archive exception
     */
    public FabricProfileToSrampArchive(InputStream jarStream) throws ZipToSrampArchiveException {
        super(jarStream);

    }

}
