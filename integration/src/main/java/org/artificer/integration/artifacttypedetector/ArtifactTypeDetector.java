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
package org.artificer.integration.artifacttypedetector;

import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.integration.ArchiveContext;

/**
 * This contract allows modules and clients to automatically detect an artifact's type, given various
 * contextual-clues.  The implementations form a prioritized chain, most-specialized at the top and most-generic
 * at the bottom (see #getPriority).
 *
 * It's important to note that execution is *exclusive* and ends processing for
 * the rest of the chain.  So, custom implementations *extending* built-in detectors isn't really necessary -- simply
 * return null and they'll be executed eventually.
 *
 * @author Brett Meyer.
 */
public interface ArtifactTypeDetector {

    /**
     * Attempt to detect the ArtifactType from the given ArtifactContent (ie, from the filename, InputStream contents, etc.)
     *
     * Returning 'null' signifies that the artifact type wasn't handled by this module and the remaining detectors
     * should be attempted.
     *
     * @param content
     * @return ArtifactType if a specific type is detected, else null.
     */
    public ArtifactType detect(ArtifactContent content);

    /**
     * Similar to #detect(ArtifactContent), attempt to detect the ArtifactType from the given ArtifactContent.
     * However, this artifact is either within a jar/war/ear/zip archive, or is an archive itself.  The ArchiveContext
     * is provided, in addition, to allow contextual-clues such as the archive name/type, entries within the archive, etc.
     *
     * Returning 'null' signifies that the artifact type wasn't handled by this module and the remaining detectors
     * should be attempted.
     *
     * @param content
     * @param archiveContext
     * @return ArtifactType if a specific type is detected, else null.
     */
    public ArtifactType detect(ArtifactContent content, ArchiveContext archiveContext);

    /**
     * If any detector in the chain returns true here, the given artifact is treated as an archive.  See
     * AbstractArtifactTypeDetector for the default impl.  This allows custom detectors to identify archives
     * in an extensible way.
     *
     * @param content
     * @return boolean
     */
    public boolean isArchive(ArtifactContent content);

    /**
     * Provides a means to filter the artifacts expanded from the given archive.  To allow the artifact to be included
     * in expansion, return true.
     *
     * Note that this should always return false by default.  Return true *only* if 1.) this detector is responsible
     * for the given type of archive and 2.) the relevant artifact should be allowed.
     *
     * @param content
     * @return boolean
     */
    public boolean allowExpansionFromArchive(ArtifactContent content, ArchiveContext archiveContext);

    /**
     * The implementations of this contract form a prioritized chain, most-specialized at the top and most-generic
     * at the bottom.  Most build-in implementations have priorities of 0 (lowest in the chain) through 2.  Use a higher
     * value to place a higher priority on your impl.
     *
     * @return int
     */
    public int getPriority();
}
