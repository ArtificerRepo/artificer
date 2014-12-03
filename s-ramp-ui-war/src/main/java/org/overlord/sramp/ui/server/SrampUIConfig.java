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
package org.overlord.sramp.ui.server;

import org.apache.commons.configuration.Configuration;
import org.overlord.commons.config.ConfigurationFactory;
import org.overlord.commons.config.JBossServer;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.overlord.sramp.ui.server.i18n.Messages;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampUIConfig {

    public static final String SRAMP_UI_CONFIG_FILE_NAME     = "sramp-ui.config.file.name"; //$NON-NLS-1$
    public static final String SRAMP_UI_CONFIG_FILE_REFRESH  = "sramp-ui.config.file.refresh"; //$NON-NLS-1$

    public static final String SRAMP_API_ENDPOINT = "s-ramp-ui.atom-api.endpoint"; //$NON-NLS-1$
    public static final String SRAMP_API_VALIDATING = "s-ramp-ui.atom-api.validating"; //$NON-NLS-1$
    public static final String SRAMP_API_AUTH_PROVIDER = "s-ramp-ui.atom-api.authentication.provider"; //$NON-NLS-1$
    public static final String SRAMP_API_BASIC_AUTH_USER = "s-ramp-ui.atom-api.authentication.basic.user"; //$NON-NLS-1$
    public static final String SRAMP_API_BASIC_AUTH_PASS = "s-ramp-ui.atom-api.authentication.basic.password"; //$NON-NLS-1$

    public static Configuration config;
    static {
        String configFile = System.getProperty(SRAMP_UI_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(SRAMP_UI_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "sramp-ui.properties", //$NON-NLS-1$
                refreshDelay,
                "/META-INF/config/org.overlord.sramp.ui.server.api.properties", //$NON-NLS-1$
                SrampApiClientAccessor.class);
        String defaultSrampApiEndpoint = JBossServer.getBaseUrl() + "/s-ramp-server"; //$NON-NLS-1$
        System.out.println(Messages.i18n.format("Config.Loaded", SrampUIConfig.config.getString(SRAMP_API_ENDPOINT, defaultSrampApiEndpoint))); //$NON-NLS-1$
    }

    /**
     * @return the configuration
     */
    public static Configuration getConfig() {
        return config;
    }

}
