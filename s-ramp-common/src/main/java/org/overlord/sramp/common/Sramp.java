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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The core sramp configuration.
 */
public class Sramp {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Sramp() {
        super();
        if (configuration == null) {
            read();
        }
    }

    private static Configuration configuration = null;

    /**
     * Sets up the configuration (a combination of system properties and an external
     * configuration file.
     */
    public synchronized void read() {
        try {
            CompositeConfiguration config = new CompositeConfiguration();
            config.addConfiguration(new SystemPropertiesConfiguration());
            //config.addConfiguration(new JNDIConfiguration("java:comp/env/overlord/s-ramp"));
            String configFile = config.getString(SrampConstants.SRAMP_CONFIG_FILE_NAME);
            Long refreshDelay = config.getLong(SrampConstants.SRAMP_CONFIG_FILE_REFRESH, 5000l);
            URL url = findSrampConfig(configFile);
            if (url == null) {
                log.warn("Cannot find " + configFile);
            } else {
                PropertiesConfiguration propertiesConfiguration = new PropertiesConfiguration(url);
                FileChangedReloadingStrategy fileChangedReloadingStrategy = new FileChangedReloadingStrategy();
                fileChangedReloadingStrategy.setRefreshDelay(refreshDelay);
                propertiesConfiguration.setReloadingStrategy(fileChangedReloadingStrategy);
                config.addConfiguration(propertiesConfiguration);
            }
            configuration = config;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    /**
     * Try to find the sramp.properties configuration file.  This will look for the
     * config file in a number of places, depending on the value for 'config file'
     * found on the system properties.
     * @param configFile
     * @throws MalformedURLException
     */
    private URL findSrampConfig(String configFile) throws MalformedURLException {
        // If a config file was given (via system properties) then try to
        // find it.  If not, then look for a 'standard' config file.
        if (configFile != null) {
            // Check on the classpath
            URL fromClasspath = Sramp.class.getClassLoader().getResource(configFile);
            if (fromClasspath != null)
                return fromClasspath;

            // Check on the file system
            File file = new File(configFile);
            if (file.isFile())
                return file.toURI().toURL();
        } else {
            // Check the current user's home directory
            String userHomeDir = System.getProperty("user.home");
            if (userHomeDir != null) {
                File dirFile = new File(userHomeDir);
                if (dirFile.isDirectory()) {
                    File cfile = new File(dirFile, "sramp.properties");
                    if (cfile.isFile())
                        return cfile.toURI().toURL();
                }
            }

            // Next, check for JBoss
            String jbossConfigDir = System.getProperty("jboss.server.config.dir");
            if (jbossConfigDir != null) {
                File dirFile = new File(jbossConfigDir);
                if (dirFile.isDirectory()) {
                    File cfile = new File(dirFile, "sramp.properties");
                    if (cfile.isFile())
                        return cfile.toURI().toURL();
                }
            }
            String jbossConfigUrl = System.getProperty("jboss.server.config.url");
            if (jbossConfigUrl != null) {
                File dirFile = new File(jbossConfigUrl);
                if (dirFile.isDirectory()) {
                    File cfile = new File(dirFile, "sramp.properties");
                    if (cfile.isFile())
                        return cfile.toURI().toURL();
                }
            }
        }

        return null;
    }

    /**
     * Gets the base URL of the S-RAMP server.  This can be used to override the automatic
     * detection of the base URL when calling the Atom API endpoints.
     * @param requestUrl
     */
    public String getBaseUrl(String requestUrl) {
        String baseUrl = "http://localhost:8080/s-ramp-server";
        if (requestUrl!=null) {
            int index = requestUrl.indexOf("/s-ramp/");
            if (index < 0) index = requestUrl.indexOf("/s-ramp");
            if (index > 0) {
                baseUrl = requestUrl.substring(0, index);
            }
        }
        return configuration.getString(SrampConstants.SRAMP_CONFIG_BASEURL, baseUrl);
    }

    /**
     * @return true if auditing is enabled
     */
    public boolean isAuditingEnabled() {
        return configuration.getBoolean(SrampConstants.SRAMP_CONFIG_AUDITING, true);
    }

    /**
     * @return true if auditing is enabled for derived artifacts
     */
    public boolean isDerivedArtifactAuditingEnabled() {
        return configuration.getBoolean(SrampConstants.SRAMP_CONFIG_DERIVED_AUDITING, true);
    }

    /**
     * @return the user to login as to perform auditing tasks
     */
    public String getAuditUser() {
        return configuration.getString(SrampConstants.SRAMP_CONFIG_AUDIT_USER, "auditor");
    }

    /**
     * @return the auditing user's password
     */
    public String getAuditPassword() {
        return configuration.getString(SrampConstants.SRAMP_CONFIG_AUDIT_PASS, "overlord-auditor");
    }

}
