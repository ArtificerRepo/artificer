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
package org.overlord.sramp.atom.archive.jar;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A context available to the artifact filter and meta-data factory
 * classes during creation of the s-ramp archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class JarToSrampArchiveContext {

    private File jarWorkDir;
    private Map<String, Object> state = new HashMap<String, Object>();

    /**
     * Constructor.
     * @param jarWorkDir
     */
    public JarToSrampArchiveContext(File jarWorkDir) {
        this.jarWorkDir = jarWorkDir;
    }

    /**
     * Gets a {@link File} for the given JAR-relative path.  Call this
     * method to get a resource in the JAR.
     * @param entryPath
     * @return a File or null if none found
     */
    public File getJarEntry(String entryPath) {
        File file = new File(this.jarWorkDir, entryPath);
        if (file.isFile())
            return file;
        else
            return null;
    }

    /**
     * Returns true if a JAR entry exists.
     * @param entryPath
     * @return true if the JAR contains the given entry
     */
    public boolean hasJarEntry(String entryPath) {
        return getJarEntry(entryPath) != null;
    }

    /**
     * Stores an arbitrary object into the context's state.
     * @param name
     * @param value
     */
    public void put(String name, Object value) {
        this.state.put(name, value);
    }

    /**
     * Gets an arbitrary object from the context's state.
     * @param name
     */
    public Object get(String name) {
        return this.state.get(name);
    }

}
