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
package org.artificer.client.auth;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

/**
 * Provides BASIC authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public class BasicAuthenticationProvider extends AuthorizationHeaderAuthenticationProvider {

    /**
     * Constructor.
     * @param username
     * @param password
     */
    public BasicAuthenticationProvider(String username, String password) {
        String authHeader = createBasicAuthHeader(username, password);
        setHeaderValue(authHeader);
    }

    /**
     * Creates the BASIC auth header value.
     * @param username
     * @param password
     */
    public static String createBasicAuthHeader(String username, String password) {
        try {
            String up = username + ":" + password; //$NON-NLS-1$
            String base64 = new String(Base64.encodeBase64(up.getBytes("UTF-8"))); //$NON-NLS-1$
            String authHeader = "Basic " + base64; //$NON-NLS-1$
            return authHeader;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
