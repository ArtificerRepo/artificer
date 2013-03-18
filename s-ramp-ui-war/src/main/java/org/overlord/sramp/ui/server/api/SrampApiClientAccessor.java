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
package org.overlord.sramp.ui.server.api;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.SystemConfiguration;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.auth.AuthenticationProvider;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author eric.wittmann@redhat.com
 */
@ApplicationScoped
public class SrampApiClientAccessor {

    static {
        SrampUIConfig.config = new CompositeConfiguration();
        SrampUIConfig.config.addConfiguration(new SystemConfiguration());
        try {
            SrampUIConfig.config.addConfiguration(new PropertiesConfiguration(SrampApiClientAccessor.class.getResource("/META-INF/config/org.overlord.sramp.ui.server.api.properties")));
        } catch (ConfigurationException e) {}
        System.out.println("S-RAMP user interface configuration loaded.  S-RAMP Atom API endpoint: " + SrampUIConfig.config.getString("s-ramp-ui.atom-api.endpoint"));
    }

    private transient SrampAtomApiClient client;

	/**
	 * C'tor.
	 */
	public SrampApiClientAccessor() {
		String endpoint = (String) SrampUIConfig.config.getProperty("s-ramp-ui.atom-api.endpoint");
        boolean validating = "true".equals(SrampUIConfig.config.getProperty("s-ramp-ui.atom-api.validating"));
        AuthenticationProvider authProvider = null;
        String authProviderClass = (String) SrampUIConfig.config.getProperty("s-ramp-ui.atom-api.authentication.provider");
        try {
            if (authProviderClass != null && authProviderClass.trim().length() > 0) {
                Class<?> c = Class.forName(authProviderClass);
                authProvider = (AuthenticationProvider) c.newInstance();
            }
            client = new SrampAtomApiClient(endpoint, authProvider, validating);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
	}

	/**
	 * @return the atom api client
	 */
	public SrampAtomApiClient getClient() {
	    return client;
	}

}
