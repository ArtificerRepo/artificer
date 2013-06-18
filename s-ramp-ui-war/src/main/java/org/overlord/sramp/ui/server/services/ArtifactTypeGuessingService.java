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
package org.overlord.sramp.ui.server.services;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;

/**
 * Service used to guess artifact types from filenames.
 *
 * @author eric.wittmann@redhat.com
 */
@Singleton
public class ArtifactTypeGuessingService {

    private static Map<String, String> nameMap = new HashMap<String, String>();
    private static Map<String, String> extensionMap = new HashMap<String, String>();
    static {
        nameMap.put("switchyard.xml", SwitchYardModel.SwitchYardXmlDocument);
        nameMap.put("pom.xml", "MavenPom");
        nameMap.put("beans.xml", JavaModel.TYPE_BEANS_XML);
        extensionMap.put("xml", ArtifactTypeEnum.XmlDocument.getType());
        extensionMap.put("wsdl", ArtifactTypeEnum.WsdlDocument.getType());
        extensionMap.put("xsd", ArtifactTypeEnum.XsdDocument.getType());
        extensionMap.put("wspolicy", ArtifactTypeEnum.PolicyDocument.getType());
        extensionMap.put("zip", "ZipArchive");
        extensionMap.put("jar", JavaModel.TYPE_ARCHIVE);
        extensionMap.put("war", JavaModel.TYPE_WEB_APPLICATION);
        extensionMap.put("ear", JavaModel.TYPE_ENTERPRISE_APPLICATION);
        extensionMap.put("sramp", "SrampArchive");
    }

    /**
     * Constructor.
     */
    public ArtifactTypeGuessingService() {
    }

    /**
     * Guesses the artifact type from the filename.
     *
     * TODO this should be made extensible!
     *
     * @param fileName
     */
    public String guess(String fileName) {
        try {
            String extension = null;
            String shortName = new File(fileName).getName();
            int idx = fileName.lastIndexOf('.');
            if (idx != -1) {
                extension = fileName.substring(idx+1).toLowerCase();
            }
            if (nameMap.containsKey(shortName)) {
                return nameMap.get(shortName);
            }
            if (extensionMap.containsKey(extension)) {
                return extensionMap.get(extension);
            }
        } catch (Exception e) {
        }
        return "Document";
    }

}
