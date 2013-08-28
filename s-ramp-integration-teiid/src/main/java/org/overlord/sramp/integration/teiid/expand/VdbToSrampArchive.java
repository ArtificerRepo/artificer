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
package org.overlord.sramp.integration.teiid.expand;

import java.io.File;
import java.io.InputStream;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveException;

/**
 * Creates an S-RAMP archive from a Teiid VDB.
 */
public final class VdbToSrampArchive extends ZipToSrampArchive {

    /**
     * @param vdb the VDB file (cannot be <code>null</code>)
     * @throws ZipToSrampArchiveException if there is a problem processing the VDB archive
     */
    public VdbToSrampArchive( final File vdb ) throws ZipToSrampArchiveException {
        super(vdb);
        init();
    }

    /**
     * @param vdbStream the VDB file input stream (cannot be <code>null</code>)
     * @throws ZipToSrampArchiveException if there is a problem processing the archive
     */
    public VdbToSrampArchive( final InputStream vdbStream ) throws ZipToSrampArchiveException {
        super(vdbStream);
        init();
    }

    private void init() {
        setArtifactFilter(new VdbArtifactFilter());
        setMetaDataFactory(new VdbMetaDataFactory());
    }

}
