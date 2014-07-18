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

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.overlord.commons.config.JBossServer;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.auth.AuthenticationProvider;
import org.overlord.sramp.ui.server.SrampUIConfig;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author eric.wittmann@redhat.com
 */
@Singleton
public class SrampApiClientAccessor {

    private transient static ThreadLocal<SrampAtomApiClient> client = new ThreadLocal<SrampAtomApiClient>();
    private transient static ThreadLocal<Locale> tlocale = new ThreadLocal<Locale>();
    public static void setLocale(Locale locale) {
        tlocale.set(locale);
    }
    public static void clearLocale() {
        tlocale.set(null);
    }

    @Inject
    private SrampUIConfig config;

	/**
	 * C'tor.
	 */
	public SrampApiClientAccessor() {
	}

    /**
     * Creates a new s-ramp client from configuration.
     * @param config
     */
    protected SrampAtomApiClient createClient() {
    	String defaultSrampApiEndpoint = JBossServer.getBaseUrl() + "/s-ramp-server"; //$NON-NLS-1$
        String endpoint = config.getConfig().getString(SrampUIConfig.SRAMP_API_ENDPOINT, defaultSrampApiEndpoint);
        boolean validating = "true".equals(config.getConfig().getString(SrampUIConfig.SRAMP_API_VALIDATING, "true")); //$NON-NLS-1$ //$NON-NLS-2$
        AuthenticationProvider authProvider = null;
        String authProviderClass = config.getConfig().getString(SrampUIConfig.SRAMP_API_AUTH_PROVIDER);
        try {
            if (authProviderClass != null && authProviderClass.trim().length() > 0) {
                Class<?> c = Class.forName(authProviderClass);
                authProvider = (AuthenticationProvider) c.newInstance();
            }
            return new SrampAtomApiClient(endpoint, authProvider, validating);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * @return the atom api client
	 */
	public SrampAtomApiClient getClient() {
	    if (client.get() == null) {
	        client.set(createClient());
	    }
	    client.get().setLocale(tlocale.get());
	    return client.get();
	}

}
