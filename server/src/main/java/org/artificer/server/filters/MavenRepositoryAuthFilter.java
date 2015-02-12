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

package org.artificer.server.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.artificer.common.ArtificerConfig;
import org.artificer.server.i18n.Messages;

/**
 * A filter that supports authentication to the s-ramp maven repository facade.  This
 * filter will log the user in if authentication credentials are provided.  If not, a
 * fake read-only set of credentials will be used that will grant read-only access to
 * the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenRepositoryAuthFilter implements Filter {
    
    /**
     * Constructor.
     */
    public MavenRepositoryAuthFilter() {
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String authHeader = req.getHeader("Authorization"); //$NON-NLS-1$
        Creds credentials = parseAuthorizationHeader(authHeader);
        if  (credentials == null) {
            SimplePrincipal principal = new SimplePrincipal(ArtificerConfig.getMavenReadOnlyUsername());
            principal.addRole("readonly." + ArtificerConfig.getJCRRepositoryName()); //$NON-NLS-1$
            doFilterChain(request, response, chain, principal);
        } else {
            if (login(credentials, req, (HttpServletResponse) response)) {
                doFilterChain(request, response, chain, null);
            } else {
                sendAuthResponse(((HttpServletResponse) response));
            }
        }
    }

    /**
     * Sends a response that tells the client that authentication is required.
     * @param response
     * @throws IOException 
     */
    private void sendAuthResponse(HttpServletResponse response) throws IOException {
        response.setHeader("WWW-Authenticate", String.format("BASIC realm=\"maven\"")); //$NON-NLS-1$ //$NON-NLS-2$
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }

    /**
     * Further process the filter chain.
     * @param request
     * @param response
     * @param chain
     * @param principal
     * @throws IOException
     * @throws ServletException
     */
    protected void doFilterChain(ServletRequest request, ServletResponse response, FilterChain chain,
            SimplePrincipal principal) throws IOException, ServletException {
        if (principal == null) {
            chain.doFilter(request, response);
        } else {
            chain.doFilter(wrapRequest(request, principal), response);
        }
    }

    /**
     * Wrap/proxy the http request.
     * @param request
     * @param principal
     */
    private HttpServletRequest wrapRequest(final ServletRequest request, final SimplePrincipal principal) {
        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper((HttpServletRequest) request) {
            @Override
            public Principal getUserPrincipal() {
                return principal;
            }
            @Override
            public String getRemoteUser() {
                return principal.getName();
            }
            /**
             * @see javax.servlet.http.HttpServletRequestWrapper#isUserInRole(java.lang.String)
             */
            @Override
            public boolean isUserInRole(String role) {
                return principal.getRoles().contains(role);
            }
        };
        return wrapper;
    }

    /**
     * Parses the Authorization request header into a username and password.
     * @param authHeader
     */
    private Creds parseAuthorizationHeader(String authHeader) {
        if (authHeader == null)
            return null;
        if (!authHeader.toUpperCase().startsWith("BASIC ")) //$NON-NLS-1$
            return null;

        try {
            String userpassEncoded = authHeader.substring(6);
            byte[] decoded = Base64.decodeBase64(userpassEncoded);
            String data = new String(decoded, "UTF-8"); //$NON-NLS-1$
            int sepIdx = data.indexOf(':');
            if (sepIdx > 0) {
                String username = data.substring(0, sepIdx);
                String password = data.substring(sepIdx + 1);
                return new Creds(username, password);
            } else {
                throw new RuntimeException(Messages.i18n.format("MavenRepositoryAuthFilter.InvalidCredFormat")); //$NON-NLS-1$
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Validates the basic authentication credentials.
     * @param credentials
     * @param request
     * @param response
     * @throws IOException 
     */
    protected boolean login(Creds credentials, HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        try {
            request.login(credentials.username, credentials.password);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
    
    /**
     * Models inbound basic auth credentials (user/password).
     * @author eric.wittmann@redhat.com
     */
    protected static class Creds {
        public String username;
        public String password;
        
        /**
         * Constructor.
         */
        public Creds(String username, String password) {
            this.username = username;
            this.password = password;
        }
    }

    // TODO: Not currently needed elsewhere, but probably good to move this.
    private static class SimplePrincipal implements Principal {
        private String username;
        private Set<String> roles = new HashSet();

        public SimplePrincipal(String username) {
            this.username = username;
        }

        public String getName() {
            return this.username;
        }

        public void addRole(String role) {
            this.roles.add(role);
        }

        public Set<String> getRoles() {
            return this.roles;
        }
    }

}
