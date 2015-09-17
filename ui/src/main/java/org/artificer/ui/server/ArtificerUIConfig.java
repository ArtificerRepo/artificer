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
package org.artificer.ui.server;

import org.apache.commons.configuration.Configuration;
import org.artificer.ui.server.i18n.Messages;
import org.overlord.commons.config.ConfigurationFactory;
import org.overlord.commons.config.JBossServer;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;

/**
 * Global access to configuration information.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerUIConfig {

    public static final String ARTIFICER_UI_CONFIG_FILE_NAME = "artificer-ui.config.file.name";
    public static final String ARTIFICER_UI_CONFIG_FILE_REFRESH = "artificer-ui.config.file.refresh";

    public static final String ARTIFICER_API_ENDPOINT = "artificer-ui.atom-api.endpoint";
    public static final String ARTIFICER_API_VALIDATING = "artificer-ui.atom-api.validating";
    public static final String ARTIFICER_API_AUTH_PROVIDER = "artificer-ui.atom-api.authentication.provider";
    public static final String ARTIFICER_API_BASIC_AUTH_USER = "artificer-ui.atom-api.authentication.basic.user";
    public static final String ARTIFICER_API_BASIC_AUTH_PASS = "artificer-ui.atom-api.authentication.basic.password";

    public static Configuration config;
    static {
        String configFile = System.getProperty(ARTIFICER_UI_CONFIG_FILE_NAME);
        String refreshDelayStr = System.getProperty(ARTIFICER_UI_CONFIG_FILE_REFRESH);
        Long refreshDelay = 5000l;
        if (refreshDelayStr != null) {
            refreshDelay = new Long(refreshDelayStr);
        }

        config = ConfigurationFactory.createConfig(
                configFile,
                "artificer-ui.properties",
                refreshDelay,
                "/META-INF/config/org.artificer.ui.server.api.properties",
                ArtificerApiClientAccessor.class);
        String defaultSrampApiEndpoint = JBossServer.getBaseUrl() + "/artificer-server";
        System.out.println(Messages.i18n.format("Config.Loaded", ArtificerUIConfig.config.getString(ARTIFICER_API_ENDPOINT, defaultSrampApiEndpoint)));
    }

    /**
     * @return the configuration
     */
    public static Configuration getConfig() {
        return config;
    }

}
