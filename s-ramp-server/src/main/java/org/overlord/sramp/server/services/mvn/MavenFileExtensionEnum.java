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
package org.overlord.sramp.server.services.mvn;

import org.apache.commons.lang.StringUtils;

/**
 * Enumeration that include all the maven file extension that needs special
 * treatment. This is the case of md5 and sha1. It is included the file
 * extension, the maven type where they can be found in s-ramp and the custom
 * property where they can be fetched.
 * 
 * @author David Virgil Naranjo
 */
public enum MavenFileExtensionEnum {

    HASH_MD5("md5", "maven.hash.md5"),  //$NON-NLS-1$ //$NON-NLS-2$
    HASH_SHA1("sha1", "maven.hash.sha1");  //$NON-NLS-1$ //$NON-NLS-2$

    private final String extension;
    private final String customProperty;

    /**
     * Instantiates a new maven file extension enum.
     *
     * @param extension
     *            the extension
     * @param customProperty
     *            the custom property
     */
    MavenFileExtensionEnum(String extension, String customProperty) {
        this.extension = extension;
        this.customProperty = customProperty;
    }

    /**
     * Gets the extension.
     *
     * @return the extension
     */
    public String getExtension() {
        return extension;
    }

    /**
     * Gets the custom property.
     *
     * @return the custom property
     */
    public String getCustomProperty() {
        return customProperty;
    }

    /**
     * Value.
     *
     * @param fileName
     *            the file name
     * @return the maven file extension enum
     */
    public static MavenFileExtensionEnum value(String fileName) {
        MavenFileExtensionEnum[] values = MavenFileExtensionEnum.values();
        if (StringUtils.isNotBlank(fileName) && fileName.contains(".")) { //$NON-NLS-1$
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1); //$NON-NLS-1$
            for (MavenFileExtensionEnum ext : values) {
                if (ext.getExtension().equals(extension)) {
                    return ext;
                }
            }
        }

        return null;
    }


}
