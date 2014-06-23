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

import org.overlord.sramp.atom.archive.expand.ArtifactFilter;
import org.overlord.sramp.atom.archive.expand.CandidateArtifact;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveContext;

/**
 * The artifact filter used when expanding a Teiid VDB. Artifacts will be created for all files contained in the VDB except for
 * index files.
 */
public final class VdbArtifactFilter implements ArtifactFilter {

    private static final String INDEX_FILE_EXT = ".INDEX"; //$NON-NLS-1$

    /**
     * @param path the path being checked (can be <code>null</code> or empty)
     * @return <code>true</code> if the path should be included in the archive
     */
    public static boolean accepts( final String path ) {
        return ((path != null) && !path.isEmpty() && !path.endsWith(INDEX_FILE_EXT)); // don't include index files
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.atom.archive.expand.ArtifactFilter#accepts(org.overlord.sramp.atom.archive.expand.CandidateArtifact)
     */
    @Override
    public boolean accepts( final CandidateArtifact artifact ) {
        return accepts(artifact.getName());
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.atom.archive.expand.ArtifactFilter#setContext(org.overlord.sramp.atom.archive.expand.ZipToSrampArchiveContext)
     */
    @Override
    public void setContext( final ZipToSrampArchiveContext context ) {
        // nothing to do
    }

}
