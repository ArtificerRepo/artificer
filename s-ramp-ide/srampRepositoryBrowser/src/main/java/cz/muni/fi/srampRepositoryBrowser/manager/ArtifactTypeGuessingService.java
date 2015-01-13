/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cz.muni.fi.srampRepositoryBrowser.manager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.overlord.sramp.common.ArtifactTypeEnum;

/**
 * Service used to guess artifact types from filenames.
 * 
 * @author eric.wittmann@redhat.com
 */

public class ArtifactTypeGuessingService {
	private static Map<String, String> nameMap = new HashMap<String, String>();
	private static Map<String, String> extensionMap = new HashMap<String, String>();
	static {
		nameMap.put("switchyard.xml", "SwitchYardXmlDocument"); //$NON-NLS-1$
		nameMap.put("pom.xml", "MavenPom"); //$NON-NLS-1$ //$NON-NLS-2$
		nameMap.put("beans.xml", "BeanArchiveDescriptor"); //$NON-NLS-1$
		extensionMap.put("xml", ArtifactTypeEnum.XmlDocument.getType()); //$NON-NLS-1$
		extensionMap.put("wsdl", ArtifactTypeEnum.WsdlDocument.getType()); //$NON-NLS-1$
		extensionMap.put("xsd", ArtifactTypeEnum.XsdDocument.getType()); //$NON-NLS-1$
		extensionMap.put("wspolicy", ArtifactTypeEnum.PolicyDocument.getType()); //$NON-NLS-1$
		extensionMap.put("zip", "ZipArchive"); //$NON-NLS-1$ //$NON-NLS-2$
		extensionMap.put("jar", "JavaArchive"); //$NON-NLS-1$
		extensionMap.put("war", "JavaWebApplication"); //$NON-NLS-1$
		extensionMap.put("ear", "JavaEnterpriseApplication"); //$NON-NLS-1$
		extensionMap.put("sramp", "SrampArchive"); //$NON-NLS-1$ //$NON-NLS-2$
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
				extension = fileName.substring(idx + 1).toLowerCase();
			}
			if (nameMap.containsKey(shortName)) {
				return nameMap.get(shortName);
			}
			if (extensionMap.containsKey(extension)) {
				return extensionMap.get(extension);
			}
		} catch (Exception e) {
		}
		return "Document"; //$NON-NLS-1$
	}
}