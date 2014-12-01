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
package org.overlord.sramp.integration;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.overlord.sramp.atom.archive.ArchiveUtils;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * This utility class provides context useful when working with uploaded archives (jars, wars, ears, zips, etc.), most
 * notably during artifact type detection (see ArtifactTypeDetector).  It provides access to the archive artifact
 * content itself, the archive "working directory" (contents are exploded into a temp dir), and the archive's S-RAMP
 * artifact (once built).
 *
 * Since ArtifactTypeDetector is a stateless service, processing may also need additional, module-specific context.
 * A Map of custom context objects is available.
 *
 * @author Brett Meyer
 */
public class ArchiveContext {

    private final ArtifactContent artifactContent;

    private final File archiveWorkDir;

    private ArtifactType archiveArtifactType = null;

    private Map<String, Object> customContext = new HashMap<String, Object>();

    public ArchiveContext(ArtifactContent artifactContent, File archiveWorkDir) {
        this.artifactContent = artifactContent;
        this.archiveWorkDir = archiveWorkDir;
    }

    /**
     * Obtain a reference to a file in the archive's working directly, using the given *relative* path/filename.
     *
     * @param entryPath
     * @return File
     */
    public File getArchiveEntry(String entryPath) {
        File file = new File(archiveWorkDir, entryPath);
        if (file.isFile())
            return file;
        else
            return null;
    }

    /**
     * Returns true if the archive working directory contains a file at the given path/filename, false if not.
     *
     * @param entryPath
     * @return boolean
     */
    public boolean hasArchiveEntry(String entryPath) {
        return getArchiveEntry(entryPath) != null;
    }

    /**
     * Returns true if processing the archive artifact itself, false if not (ie, processing artifacts expanded from
     * within the archive).
     *
     * @return boolean
     */
    public boolean isArchive() {
        // Note: This assumes that the archive artifact is set immediately after being built.
        return archiveArtifactType == null;
    }

    /**
     * Returns true if processing artifacts expanded from within the archive, false if not (ie, processing the archive
     * artifact itself).
     *
     * @return boolean
     */
    public boolean isExpandedFromArchive() {
        // Note: This assumes that the archive artifact is set immediately after being built.
        return archiveArtifactType != null;
    }

    /**
     * Returns true if the archive is 1.) in the Extended model and 2.) of the given extended type.
     *
     * @param archiveExtendedType
     * @return boolean
     */
    public boolean isExtendedTypeArchive(String archiveExtendedType) {
        if (archiveArtifactType != null) {
            return archiveArtifactType.isExtendedType()
                    && archiveArtifactType.getExtendedType().equalsIgnoreCase(archiveExtendedType);
        }
        return false;
    }

    /**
     * Obtain the archive's S-RAMP artifact type.
     *
     * @return BaseArtifactType
     */
    public ArtifactType getArchiveArtifactType() {
        return archiveArtifactType;
    }

    public void setArchiveArtifactType(ArtifactType archiveArtifactType) {
        this.archiveArtifactType = archiveArtifactType;
    }

    /**
     * Returns all Files within the archive.
     *
     * @return Collection<File>
     */
    public Collection<File> expand() {
        return FileUtils.listFiles(archiveWorkDir, FileFileFilter.FILE, TrueFileFilter.INSTANCE);
    }

    /**
     * Utility method that strips the archive work directory path from the given path.
     *
     * @param path
     * @return String
     */
    public String stripWorkDir(String path) {
        return path.replace(archiveWorkDir.getAbsolutePath(), "");
    }

    /**
     * Since this context can be used by stateless services, processing may also need additional, module-specific
     * context.  Add a custom context Object to the Map.
     *
     * @param key
     * @param value
     */
    public void addCustomContext(String key, Object value) {
        customContext.put(key, value);
    }

    /**
     * Since this context can be used by stateless services, processing may also need additional, module-specific
     * context.  Returns true if the Map contains the custom context key.
     *
     * @param key
     * @return boolean
     */
    public boolean hasCustomContext(String key) {
        return customContext.containsKey(key);
    }

    /**
     * Since this context can be used by stateless services, processing may also need additional, module-specific
     * context.  Retrievs the custom context Object from the Map.
     *
     * @param key
     * @return
     */
    public Object getCustomContext(String key) {
        return customContext.get(key);
    }

    public void cleanup() {
        if (archiveWorkDir != null) {
            archiveWorkDir.delete();
        }
    }

    public static ArchiveContext createArchiveContext(ArtifactContent content) throws IOException {
        File archiveWorkDir = File.createTempFile(UUID.randomUUID().toString(), ".work");
        archiveWorkDir.delete();
        archiveWorkDir.mkdir();
        ArchiveUtils.unpackToWorkDir(content.getFile(), archiveWorkDir);

        return new ArchiveContext(content, archiveWorkDir);
    }

}
