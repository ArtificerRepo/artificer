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
package org.overlord.sramp.ui.server.filters;

import java.io.IOException;
import java.util.Date;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * {@link Filter} to add cache control headers for resources such as CSS and images.
 */
public class ResourceCacheControlFilter implements Filter {

    /**
     * C'tor
     */
    public ResourceCacheControlFilter() {
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
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String requestURI = ((HttpServletRequest) request).getRequestURI();
        String v = request.getParameter("v");
        if (v == null)
        	v = "";

        // By default, cache aggressively.  However, if a file contains '.nocache.' as part of its
        // name, then tell the browser not to cache it.  Also, if the version of the resource being
        // requested contains 'SNAPSHOT' then don't cache.
        if (requestURI.contains(".nocache.")
                || v.contains("SNAPSHOT")
                || "true".equals(System.getProperty("s-ramp-ui.resource-caching.disabled", "false")))
        {
            Date now = new Date();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setDateHeader("Date", now.getTime());
            // one day old
            httpResponse.setDateHeader("Expires", now.getTime() - 86400000L);
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");
        } else {
            Date now = new Date();
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setDateHeader("Date", now.getTime());
            // Expire in one year
            httpResponse.setDateHeader("Expires", now.getTime() + 31536000000L);
            // Cache for one year
            httpResponse.setHeader("Cache-control", "public, max-age=31536000");
        }

        chain.doFilter(request, response);
    }

    /**
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
    }
}
