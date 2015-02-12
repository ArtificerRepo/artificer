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
package org.artificer.ui.server.api;

import org.apache.http.HttpRequest;
import org.artificer.client.auth.AuthenticationProvider;

/**
 * An authentication provider for Keycloak Bearer Tokens.
 *
 * @author Brett Meyer
 */
public class KeycloakBearerTokenAuthenticationProvider implements AuthenticationProvider {

    private transient static ThreadLocal<String> tBearerToken = new ThreadLocal<String>();

    public static void setBearerToken(String bearerToken) {
        tBearerToken.set(bearerToken);
    }
    public static void clearBearerToken() {
        tBearerToken.set(null);
    }

    /**
     * Constructor.
     */
    public KeycloakBearerTokenAuthenticationProvider() {
    }

    @Override
    public void provideAuthentication(HttpRequest request) {
        String bearerToken = tBearerToken.get();
        request.setHeader("Authorization", "Bearer " + bearerToken); //$NON-NLS-1$
    }

}
