/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.common;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.overlord.commons.config.ConfigurationFactory;
import org.overlord.commons.config.JBossServer;


/**
 * The core sramp configuration.
 */
public class SrampConfig {

    private static Configuration configuration = null;

    static {
        String configFile = System.getProperty(SrampConstants.SRAMP_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(SrampConstants.SRAMP_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        configuration = ConfigurationFactory.createConfig(configFile, "sramp.properties", refreshDelay, null, null); //$NON-NLS-1$
    }

    /**
     * Gets the base URL of the S-RAMP server.  This can be used to override the automatic
     * detection of the base URL when calling the Atom API endpoints.
     * @param requestUrl
     */
    public static String getBaseUrl(String requestUrl) {

        String baseUrl = null;
        //Try grabbing it from the call
        if (requestUrl!=null) {
            int index = requestUrl.lastIndexOf("/s-ramp/"); //$NON-NLS-1$
            if (index < 0) index = requestUrl.lastIndexOf("/s-ramp"); //$NON-NLS-1$
            if (index > 0) {
                baseUrl = requestUrl.substring(0, index);
            }
        }
        //If that failed, try constructing it from the JBoss system parameters
        if (baseUrl==null){
            baseUrl = JBossServer.getBaseUrl() + "/s-ramp-server" ; //$NON-NLS-1$
        }
        return configuration.getString(SrampConstants.SRAMP_CONFIG_BASEURL, baseUrl);
    }

    /**
     * @return true if auditing is enabled
     */
    public static boolean isAuditingEnabled() {
        return configuration.getBoolean(SrampConstants.SRAMP_CONFIG_AUDITING, false);
    }

    /**
     * @return true if auditing is enabled for derived artifacts
     */
    public static boolean isDerivedArtifactAuditingEnabled() {
        return configuration.getBoolean(SrampConstants.SRAMP_CONFIG_DERIVED_AUDITING, false);
    }

    /**
     * Gets a property from the s-ramp configuration.
     * @param propertyName
     * @param propertyDefault
     */
    public static String getConfigProperty(String propertyName, String propertyDefault) {
        return configuration.getString(propertyName, propertyDefault);
    }

    /**
     * @return the name of the JCR repository being used
     */
    public static String getJCRRepositoryName() {
        return getConfigProperty(SrampConstants.SRAMP_CONFIG_JCR_REPO_NAME, "sramp"); //$NON-NLS-1$
    }

    /**
     * @return the name of the JCR repository being used
     */
    public static String getMavenReadOnlyUsername() {
        return getConfigProperty(SrampConstants.SRAMP_CONFIG_MAVEN_READONLY_USERNAME, "mavenuser"); //$NON-NLS-1$
    }

    private static String getVersion() {
        return Version.get().getVersionString();
    }

    public static boolean containsKey(String key) {
        return configuration.containsKey(key);
    }

    public static boolean isSnapshotAllowed() {
        // Need to support both system properties (wagon, cli) and sramp.properties (maven facade)
        if ("true".equalsIgnoreCase(System.getProperty(SrampConstants.SRAMP_SNAPSHOT_ALLOWED))) { //$NON-NLS-1$
            return true;
        }
        if (containsKey(SrampConstants.SRAMP_SNAPSHOT_ALLOWED)) {
            String value = getConfigProperty(SrampConstants.SRAMP_SNAPSHOT_ALLOWED, "false"); //$NON-NLS-1$
            return StringUtils.isNotBlank(value) && value.equals("true"); //$NON-NLS-1$
        } else {
            return isSnapshot();
        }
    }

    private static boolean isSnapshot() {
        String version = getVersion();
        return StringUtils.isNotBlank(version) && version.contains("SNAPSHOT"); //$NON-NLS-1$
    }
}
