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
package org.overlord.sramp.server.servlet;

import java.io.IOException;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.server.i18n.Messages;
import org.overlord.sramp.server.services.MavenRepositoryService;
import org.overlord.sramp.server.services.mvn.MavenArtifactWrapper;
import org.overlord.sramp.server.services.mvn.MavenRepositoryServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Servlet that gets a path as maven formatted url and either returns the list
 * of contents as HTML response, or it returns the artifact content.
 *
 * @author David Virgil Naranjo
 */
public class MavenRepositoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JSP_LOCATION_LIST_DIR = "/list_items.jsp"; //$NON-NLS-1$

    private static final String URL_CONTEXT_STR = "maven/repository"; //$NON-NLS-1$

    private static Logger logger = LoggerFactory.getLogger(MavenRepositoryServlet.class);

    MavenRepositoryService service = new MavenRepositoryServiceImpl();

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        String url = req.getRequestURI();
        String maven_url = ""; //$NON-NLS-1$
        if (url.contains(URL_CONTEXT_STR)) {
            maven_url = url.substring(url.indexOf(URL_CONTEXT_STR) + URL_CONTEXT_STR.length());
        } else {
            maven_url = url;
        }

        if (maven_url.startsWith("/")) { //$NON-NLS-1$
            maven_url = maven_url.substring(1);
        }

        String[] tokens = maven_url.split("/"); //$NON-NLS-1$
        if (tokens != null && tokens.length > 0) {
            String groupId = ""; //$NON-NLS-1$
            String version = ""; //$NON-NLS-1$
            String artifactId = ""; //$NON-NLS-1$
            String file_name = ""; //$NON-NLS-1$

            if (tokens.length >= 4) {
                file_name = tokens[tokens.length - 1];
                MavenArtifactWrapper artifact = null;
                if (file_name.contains(".")) { //$NON-NLS-1$
                    version = tokens[tokens.length - 2];
                    for (int i = 0; i < tokens.length - 2; i++) {
                        if (i < tokens.length - 3) {
                            if (i != 0) {
                                groupId += "."; //$NON-NLS-1$
                            }
                            groupId += tokens[i];
                        } else {
                            artifactId = tokens[i];
                        }
                    }
                    // Here we have the gav info. So let's go to Sramp to
                    // obtain the InputStream with the info
                    try {
                        artifact = service.getArtifactContent(file_name, groupId, artifactId, version);
                        if (artifact != null) {
                            resp.setContentLength(artifact.getContentLength());
                            resp.addHeader("Content-Disposition", //$NON-NLS-1$
                                    "attachment; filename=" + artifact.getFileName()); //$NON-NLS-1$
                            resp.setContentType(artifact.getContentType());
                            IOUtils.copy(artifact.getContent(), resp.getOutputStream());
                        } else {
                            // Send a 404 if we couldn't find the artifact
                            resp.sendError(404);
                        }
                    } catch (SrampAtomException e) {
                        logger.info(Messages.i18n.format("maven.servlet.artifact.content.get.exception", //$NON-NLS-1$
                                groupId, artifactId, version, file_name));
                        // Send a 500 error if there's an exception
                        resp.sendError(500);
                    } finally {
                        if (artifact != null) {
                            IOUtils.closeQuietly(artifact.getContent());
                        }
                    }
                }

                if (artifact == null) {
                    listItemsResponse(req, resp, maven_url);
                }
            } else {
                listItemsResponse(req, resp, maven_url);
            }
        }
    }

    /**
     * List items response.
     *
     * @param req
     *            the req
     * @param resp
     *            the resp
     * @param url
     *            the url
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void listItemsResponse(HttpServletRequest req, HttpServletResponse resp, String url)
            throws ServletException, IOException {
        try {
            Set<String> items = service.getItems(url);
            if ((items != null && items.size() > 0) || (url.equals("/") || url.equals(""))) { //$NON-NLS-1$ //$NON-NLS-2$
                RequestDispatcher dispatcher = req.getRequestDispatcher(JSP_LOCATION_LIST_DIR);
                if (StringUtils.isNotBlank(url) && !url.equals("/")) { //$NON-NLS-1$
                    String[] urlTokens = url.split("/"); //$NON-NLS-1$
                    String parentPath = ""; //$NON-NLS-1$
                    if (urlTokens.length > 1) {
                        for (int i = 0; i < urlTokens.length - 1; i++) {
                            parentPath += urlTokens[i] + "/"; //$NON-NLS-1$
                        }
                    }
                    parentPath = "/" + parentPath; //$NON-NLS-1$
                    req.setAttribute("parentPath", parentPath); //$NON-NLS-1$
                }
                req.setAttribute("relativePath", url); //$NON-NLS-1$
                req.setAttribute("items", items); //$NON-NLS-1$
                dispatcher.forward(req, resp);
            } else {
                resp.setStatus(HttpStatus.SC_NOT_FOUND);
            }
        } catch (SrampAtomException e) {
            resp.sendError(HttpStatus.SC_NOT_FOUND, e.getMessage());
        }
    }

}
