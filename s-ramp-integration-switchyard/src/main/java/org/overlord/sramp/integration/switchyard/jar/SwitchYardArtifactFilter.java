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
import java.util.HashSet;
import java.util.Set;

import org.overlord.sramp.atom.archive.jar.ArtifactFilter;
import org.overlord.sramp.atom.archive.jar.CandidateArtifact;
import org.overlord.sramp.atom.archive.jar.JarToSrampArchiveContext;

/**
 * The artifact filter used when expanding a SwitchYard application JAR or WAR.  This
 * uses context hints found in the switchyard.xml file to determine which artifacts
 * should be included in the expansion.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardArtifactFilter implements ArtifactFilter {

    private static Set<String> validExtensions = new HashSet<String>();
    static {
        validExtensions.add("xml");
        validExtensions.add("xsd");
        validExtensions.add("wsdl");
        validExtensions.add("wspolicy");
        validExtensions.add("class");
    }
    private static Set<String> exclusions = new HashSet<String>();
    static {
        exclusions.add("pom.xml");
    }

    private SwitchYardAppIndex syIndex;

    /**
     * Constructor.
     */
    public SwitchYardArtifactFilter() {
    }

    /**
     * @see org.overlord.sramp.atom.archive.jar.ArtifactFilter#setContext(org.overlord.sramp.atom.archive.jar.JarToSrampArchiveContext)
     */
    @Override
    public void setContext(JarToSrampArchiveContext context) {
        syIndex = (SwitchYardAppIndex) context.get("switchyard.index");
        if (syIndex == null) {
            File switchyardXmlFile = context.getJarEntry("META-INF/switchyard.xml");
            if (switchyardXmlFile != null) {
                syIndex = new SwitchYardAppIndex(switchyardXmlFile);
                context.put("switchyard.index", switchyardXmlFile);
            }
        }
    }

    /**
     * @see org.overlord.sramp.atom.archive.jar.ArtifactFilter#accepts(org.overlord.sramp.atom.archive.jar.CandidateArtifact)
     */
    @Override
    public boolean accepts(CandidateArtifact artifact) {
        String name = artifact.getName();
        if (exclusions.contains(name)) {
            return false;
        }
        String ext = null;
        if (name.contains(".")) {
            ext = name.substring(name.lastIndexOf('.') + 1);
        }
        if (validExtensions.contains(ext)) {
            if (syIndex == null) {
                return true;
            } else {
                return doIndexedAccept(artifact, ext);
            }
        } else {
            return false;
        }
    }

    /**
     * Accept only if appropriate based on the SwitchYard index.
     * @param artifact
     * @param extension
     */
    private boolean doIndexedAccept(CandidateArtifact artifact, String extension) {
        if ("class".equals(extension)) {
            String entryPath = artifact.getEntryPath();
            String classname = entryPath.replace('/', '.').replace('\\', '.');
            classname = classname.substring(0, classname.length() - 6);
            return syIndex.contains(classname);
        } else {
            return true;
        }
    }

}
