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
package org.overlord.sramp.governance.auth;

import org.apache.http.HttpRequest;
import org.overlord.commons.auth.jboss7.SAMLBearerTokenLoginModule;
import org.overlord.commons.auth.jboss7.SAMLBearerTokenUtil;
import org.overlord.sramp.client.auth.AuthenticationProvider;
import org.overlord.sramp.client.auth.BasicAuthenticationProvider;
import org.overlord.sramp.governance.Governance;

/**
 * An authentication provider that uses SAML Bearer Tokens.  The S-RAMP
 * Atom API must be configured to accept and consume a SAML Assertion.
 * For more information see {@link SAMLBearerTokenLoginModule}.
 *
 * @author eric.wittmann@redhat.com
 */
public class SAMLBearerTokenAuthenticationProvider implements AuthenticationProvider {

    private static Governance governance = new Governance();

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
        String headerValue = BasicAuthenticationProvider.createBasicAuthHeader("SAML-BEARER-TOKEN", createSAMLBearerTokenAssertion());
        request.setHeader("Authorization", headerValue);
    }

    /**
     * Creates the SAML Bearer Token that will be used to authenticate to the
     * S-RAMP Atom API.
     */
    private static String createSAMLBearerTokenAssertion() {
        String issuer = governance.getSrampSamlIssuer();
        String service = governance.getSrampSamlService();
        return SAMLBearerTokenUtil.createSAMLAssertion(issuer, service);
    }

}
