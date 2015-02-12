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
package org.artificer.ui.server.api;

import org.artificer.ui.server.ArtificerUIConfig;
import org.overlord.commons.config.JBossServer;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.auth.AuthenticationProvider;

import java.util.Locale;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerApiClientAccessor {

    private transient static ThreadLocal<ArtificerAtomApiClient> client = new ThreadLocal<ArtificerAtomApiClient>();

    private transient static ThreadLocal<Locale> tLocale = new ThreadLocal<Locale>();

    public static void setLocale(Locale locale) {
        tLocale.set(locale);
    }
    public static void clearLocale() {
        tLocale.set(null);
    }

    /**
     * Creates a new s-ramp client from configuration.
     */
    private static ArtificerAtomApiClient createClient() {
    	String defaultSrampApiEndpoint = JBossServer.getBaseUrl() + "/artificer-server"; //$NON-NLS-1$
        String endpoint = ArtificerUIConfig.getConfig().getString(ArtificerUIConfig.ARTIFICER_API_ENDPOINT, defaultSrampApiEndpoint);
        boolean validating = "true".equals(ArtificerUIConfig.getConfig().getString(ArtificerUIConfig.ARTIFICER_API_VALIDATING, "true")); //$NON-NLS-1$ //$NON-NLS-2$
        AuthenticationProvider authProvider = null;
        String authProviderClass = ArtificerUIConfig.getConfig().getString(ArtificerUIConfig.ARTIFICER_API_AUTH_PROVIDER);
        try {
            if (authProviderClass != null && authProviderClass.trim().length() > 0) {
                Class<?> c = Class.forName(authProviderClass);
                authProvider = (AuthenticationProvider) c.newInstance();
            }
            return new ArtificerAtomApiClient(endpoint, authProvider, validating);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

	/**
	 * @return the atom api client
	 */
	public static ArtificerAtomApiClient getClient() {
	    if (client.get() == null) {
	        client.set(createClient());
	    }
	    client.get().setLocale(tLocale.get());
	    return client.get();
	}

}
