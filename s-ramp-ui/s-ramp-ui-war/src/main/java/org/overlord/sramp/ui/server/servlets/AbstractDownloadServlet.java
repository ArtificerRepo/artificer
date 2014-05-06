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
package org.overlord.sramp.ui.server.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

/**
 * Abstract Download server with the common functionalities used in the ontology
 * and artifact download servlets
 * 
 * @author David Virgil Naranjo
 */
public abstract class AbstractDownloadServlet extends HttpServlet {

    /**
     *
     */
    private static final long serialVersionUID = 337653436287287059L;

    /**
     * Do download content.
     *
     * @param content
     *            the content
     * @param contentType
     *            the content type
     * @param disposition
     *            the disposition
     * @param httpResponse
     *            the http response
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    protected void doDownloadContent(InputStream content, String contentType, String disposition,
            HttpServletResponse httpResponse) throws IOException {
        if (StringUtils.isNotBlank(disposition)) {
            httpResponse.setHeader("Content-Disposition", disposition); //$NON-NLS-1$
        }
        if (StringUtils.isNotBlank(contentType)) {
            httpResponse.setHeader("Content-Type", contentType); //$NON-NLS-1$
        }

        // Make sure the browser doesn't cache it
        Date now = new Date();
        httpResponse.setDateHeader("Date", now.getTime()); //$NON-NLS-1$
        httpResponse.setDateHeader("Expires", now.getTime() - 86400000L); //$NON-NLS-1$
        httpResponse.setHeader("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
        httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate"); //$NON-NLS-1$ //$NON-NLS-2$
        try {
            IOUtils.copy(content, httpResponse.getOutputStream());
        } finally {
            IOUtils.closeQuietly(content);
        }
    }
}
