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
package org.overlord.sramp.ui.server.api;

import java.security.KeyPair;
import java.security.KeyStore;

import org.apache.http.HttpRequest;
import org.overlord.commons.auth.util.SAMLAssertionUtil;
import org.overlord.commons.auth.util.SAMLBearerTokenUtil;
import org.overlord.sramp.client.auth.AuthenticationProvider;
import org.overlord.sramp.client.auth.BasicAuthenticationProvider;
import org.overlord.sramp.ui.server.SrampUIConfig;

/**
 * An authentication provider that uses SAML Bearer Tokens.  The S-RAMP
 * Atom API must be configured to accept and consume a SAML Assertion.
 * For more information see {@link org.overlord.commons.auth.jboss7.SAMLBearerTokenLoginModule}.
 *
 * @author eric.wittmann@redhat.com
 */
public class SAMLBearerTokenAuthenticationProvider implements AuthenticationProvider {

    /**
     * Constructor.
     */
    public SAMLBearerTokenAuthenticationProvider() {
    }

    /**
     * @see org.overlord.sramp.client.auth.HttpHeaderAuthenticationProvider#provideAuthentication(org.apache.http.HttpRequest)
     */
    @Override
    public void provideAuthentication(HttpRequest request) {
        String headerValue = BasicAuthenticationProvider.createBasicAuthHeader("SAML-BEARER-TOKEN", createSAMLBearerTokenAssertion()); //$NON-NLS-1$
        request.setHeader("Authorization", headerValue); //$NON-NLS-1$
    }

    /**
     * Creates the SAML Bearer Token that will be used to authenticate to the
     * S-RAMP Atom API.
     */
    private static String createSAMLBearerTokenAssertion() {
        String issuer = SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_ISSUER);
        String service = SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_SERVICE);
        String samlAssertion = SAMLAssertionUtil.createSAMLAssertion(issuer, service);
        boolean signAssertion = "true".equals(SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_SIGN_ASSERTIONS)); //$NON-NLS-1$
        if (signAssertion) {
            String keystorePath = SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_KEYSTORE);
            String keystorePassword = SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_KEYSTORE_PASSWORD);
            String keyAlias = SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_KEY_ALIAS);
            String keyAliasPassword = SrampUIConfig.config.getString(SrampUIConfig.SRAMP_API_SAML_AUTH_KEY_PASSWORD);
            try {
                KeyStore keystore = SAMLBearerTokenUtil.loadKeystore(keystorePath, keystorePassword);
                KeyPair keyPair = SAMLBearerTokenUtil.getKeyPair(keystore, keyAlias, keyAliasPassword);
                samlAssertion = SAMLBearerTokenUtil.signSAMLAssertion(samlAssertion, keyPair);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return samlAssertion;
    }

}
