/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.ui.server.filters;

import org.keycloak.KeycloakSecurityContext;
import org.overlord.sramp.common.i18n.AbstractMessages;
import org.overlord.sramp.ui.server.api.KeycloakBearerTokenAuthenticationProvider;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

/**
 * A filter that attempts to pull the KeycloakSecurityContext and its bearer token String from the ServletRequest.
 * If found, it's provided to KeycloakBearerTokenAuthenticationProvider's ThreadLocal variable for use during authentication.
 *
 * @author Brett Meyer
 */
public class KeycloakBearerTokenFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        KeycloakSecurityContext session = (KeycloakSecurityContext) httpServletRequest.getAttribute(KeycloakSecurityContext.class.getName());
        String bearerToken;
        if (session != null) {
            bearerToken = session.getTokenString();
        } else {
            bearerToken = "LOGGED_OUT";
        }
        KeycloakBearerTokenAuthenticationProvider.setBearerToken(bearerToken);
        chain.doFilter(request, response);
        KeycloakBearerTokenAuthenticationProvider.clearBearerToken();
    }

    @Override
    public void destroy() {
    }

}
