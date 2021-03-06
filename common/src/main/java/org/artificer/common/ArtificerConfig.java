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
package org.artificer.common;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.StringUtils;
import org.overlord.commons.config.ConfigurationFactory;
import org.overlord.commons.config.JBossServer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * The core sramp configuration.
 */
public class ArtificerConfig {

    private static Configuration configuration = null;

    static {
        String configFile = System.getProperty(ArtificerConstants.ARTIFICER_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(ArtificerConstants.ARTIFICER_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        configuration = ConfigurationFactory.createConfig(configFile, "artificer.properties", refreshDelay, null, null);
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
            int index = requestUrl.lastIndexOf("/s-ramp/");
            if (index < 0) index = requestUrl.lastIndexOf("/s-ramp");
            if (index > 0) {
                baseUrl = requestUrl.substring(0, index);
            }
        }
        //If that failed, try constructing it from the JBoss system parameters
        if (baseUrl==null){
            baseUrl = JBossServer.getBaseUrl() + "/artificer-server" ;
        }
        return configuration.getString(ArtificerConstants.ARTIFICER_CONFIG_BASEURL, baseUrl);
    }

    /**
     * @return true if auditing is enabled
     */
    public static boolean isAuditingEnabled() {
        return configuration.getBoolean(ArtificerConstants.ARTIFICER_CONFIG_AUDITING, false);
    }

    /**
     * @return true if auditing is enabled for derived artifacts
     */
    public static boolean isDerivedArtifactAuditingEnabled() {
        return configuration.getBoolean(ArtificerConstants.ARTIFICER_CONFIG_DERIVED_AUDITING, false);
    }

    /**
     * @return true if JMS is enabled
     */
    public static boolean isJmsEnabled() {
        return configuration.getBoolean(ArtificerConstants.ARTIFICER_CONFIG_EVENT_JMS_ENABLED, false);
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
     * Gets a property from the s-ramp configuration.
     * @param propertyName
     * @param propertyDefault
     */
    public static boolean getConfigProperty(String propertyName, boolean propertyDefault) {
        return configuration.getBoolean(propertyName, propertyDefault);
    }

    /**
     * Returns a Map of all keys and values, where the keys begin with the given prefix.
     * @param prefix
     */
    public static Map<String, Object> getConfigProperties(String prefix) {
        Map<String, Object> properties = new HashMap<>();
        Iterator<String> keys = configuration.getKeys(prefix);
        while (keys.hasNext()) {
            String key = keys.next();
            properties.put(key, configuration.getProperty(key));
        }
        return properties;
    }
    
    public static String getMavenReadOnlyUsername() {
        return getConfigProperty(ArtificerConstants.ARTIFICER_CONFIG_MAVEN_READONLY_USERNAME, "mavenuser");
    }

    private static String getVersion() {
        return Version.get().getVersionString();
    }

    public static boolean containsKey(String key) {
        return configuration.containsKey(key);
    }

    public static boolean isSnapshotAllowed() {
        if (isSnapshot()) {
            // Mainly for development purposes, always allow snapshot deployments if Artificer is itself a snapshot.
            return true;
        } else {
            return getConfigProperty(ArtificerConstants.ARTIFICER_SNAPSHOT_ALLOWED, false);
        }
    }

    private static boolean isSnapshot() {
        String version = getVersion();
        return StringUtils.isNotBlank(version) && version.contains("SNAPSHOT");
    }
}
