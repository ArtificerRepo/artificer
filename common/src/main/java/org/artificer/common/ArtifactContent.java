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
package org.artificer.common;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * When S-RAMP is handed content's InputStream, it needs to be read multiple times (multiple times in the
 * extensions, then again for persistence), some of which automatically close the stream.  Further, we don't want to trust
 * that custom extensions will always "do the right thing" and call #reset.  So, this object is passed
 * around throughout the process.  Using a temp file, it creates on-demand streams.
 * 
 * @author Brett Meyer
 */
public class ArtifactContent {

    private final String path;

    private final String filename;
    
    private File tempFile = null;

    private List<InputStream> tempStreams = new ArrayList<InputStream>();
    
    public ArtifactContent(String path, InputStream is) throws IOException {
        if (path != null) {
            // Normalize and ensure it's a *relative* path.
            if (path.startsWith(File.separatorChar + "")) {
                path = path.replaceFirst(File.separatorChar + "", "");
            }
            this.path = path;
            int index = path.lastIndexOf(File.separatorChar);
            this.filename = path.substring(index + 1);
        } else {
            this.path = null;
            this.filename = null;
        }

        OutputStream os = null;
        try {
            tempFile = File.createTempFile(UUID.randomUUID().toString(), filename);
            os = FileUtils.openOutputStream(tempFile);
            IOUtils.copy(is, os);
        } catch (IOException e) {
            if (tempFile != null) {
                FileUtils.deleteQuietly(tempFile);
            }
            throw e;
        } finally {
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(os);
        }
    }

    public ArtifactContent(String fullPath, File file) throws IOException {
        this(fullPath, new FileInputStream(file));
    }

    /**
     * Obtain this artifact's InputStream, created on-demand.
     *
     * @return InputStream
     * @throws FileNotFoundException
     */
    public InputStream getInputStream() throws FileNotFoundException {
        if (tempFile != null) {
            InputStream is = new BufferedInputStream(new FileInputStream(tempFile));
            tempStreams.add(is);
            return is;
        } else {
            return null;
        }
    }

    /**
     * Obtain a reference to the artifact's temporary File on the filesystem.
     *
     * @return File
     */
    public File getFile() {
        return tempFile;
    }

    /**
     * Obtain the artifact's filename.
     *
     * @return String
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Obtain the artifact's path within its original context.  For example, if the artifact was expanded from an
     * archive or was published through a batch upload, the relative path within that context will be available here.
     *
     * @return String
     */
    public String getPath() {
        return path;
    }

    public long getSize() {
        return tempFile.length();
    }

    public void cleanup() {
        if (tempFile != null) {
            tempFile.delete();
        }
        for (InputStream is : tempStreams) {
            IOUtils.closeQuietly(is);
        }
    }
}
