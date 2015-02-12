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

package org.artificer.repository.jcr.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.modeshape.jcr.api.ServletCredentials;
import org.artificer.repository.jcr.JCRRepositoryFactory;

/**
 * This filter can be used to supply a source of credentials that can be used when logging
 * in to the JCR repository (ModeShape).  It uses the inbound request as the source of
 * authentication.
 *
 * @author eric.wittmann@redhat.com
 */
public class ServletCredentialsFilter implements Filter {
    
    /**
     * Constructor.
     */
    public ServletCredentialsFilter() {
    }

    /**
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
            ServletException {
        ServletCredentials credentials = new ServletCredentials((HttpServletRequest) request);
        try {
            JCRRepositoryFactory.setLoginCredentials(credentials);
            chain.doFilter(request, response);
        } finally {
            JCRRepositoryFactory.clearLoginCredentials();
        }
    }

    /**
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }

}
