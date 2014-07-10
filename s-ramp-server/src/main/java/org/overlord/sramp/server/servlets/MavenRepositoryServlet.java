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
package org.overlord.sramp.server.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;
import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.server.i18n.Messages;
import org.overlord.sramp.server.services.MavenRepositoryService;
import org.overlord.sramp.server.services.mvn.MavenArtifactWrapper;
import org.overlord.sramp.server.services.mvn.MavenMetaData;
import org.overlord.sramp.server.services.mvn.MavenMetaDataBuilder;
import org.overlord.sramp.server.services.mvn.MavenRepositoryException;
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

    private static boolean SNAPSHOT_ALLOWED;

    static {
        Sramp sramp = new Sramp();
        String value = sramp.getConfigProperty(SrampConstants.SRAMP_SNAPSHOT_ALLOWED, "false"); //$NON-NLS-1$
        if (StringUtils.isNotBlank(value) && value.equals("true")) { //$NON-NLS-1$
            SNAPSHOT_ALLOWED = true;
        } else {
            SNAPSHOT_ALLOWED = false;
        }
    }

    /**
     * Do get.
     *
     * @param req
     *            the req
     * @param resp
     *            the resp
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        // Get the URL request and prepare it to obtain the maven metadata
        // information
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

        // Builder class that converts the url into a Maven MetaData Object
        MavenMetaData metadata = MavenMetaDataBuilder.build(maven_url);

        // If it is possible to detect a maven metadata information and it is
        // found an artifact information
        if (metadata.isArtifact()) {

            // Here we have the gav info. So let's go to Sramp to
            // obtain the InputStream with the info
            MavenArtifactWrapper artifact = null;
            try {
                artifact = service.getArtifactContent(metadata);
                if (artifact != null) {
                    resp.setContentLength(artifact.getContentLength());
                    resp.addHeader("Content-Disposition", //$NON-NLS-1$
                            "attachment; filename=" + artifact.getFileName()); //$NON-NLS-1$
                    resp.setContentType(artifact.getContentType());
                    IOUtils.copy(artifact.getContent(), resp.getOutputStream());
                } else {
                    listItemsResponse(req, resp, maven_url);
                }
            } catch (MavenRepositoryException e) {
                logger.info(Messages.i18n.format(
                        "maven.servlet.artifact.content.get.exception", //$NON-NLS-1$
                        metadata.getGroupId(), metadata.getArtifactId(), metadata.getVersion(),
                        metadata.getFileName()));
                // Send a 500 error if there's an exception
                resp.sendError(500);
            } finally {
                if (artifact != null) {
                    IOUtils.closeQuietly(artifact.getContent());
                }
            }

        } else {
            // In case the metadata information is not an artifact, then the
            // maven url is listed
            listItemsResponse(req, resp, maven_url);
        }
    }

    /**
     * Do post.
     *
     * @param req
     *            the req
     * @param response
     *            the response
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse response) throws ServletException,
            IOException {
        uploadArtifact(req, response);
    }

    /**
     * Do put.
     *
     * @param req
     *            the req
     * @param response
     *            the response
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse response) throws ServletException,
            IOException {
        uploadArtifact(req, response);
    }

    /**
     * Upload artifact.
     *
     * @param req
     *            the req
     * @param response
     *            the response
     * @throws ServletException
     *             the servlet exception
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    private void uploadArtifact(HttpServletRequest req, HttpServletResponse response)
            throws ServletException, IOException {
        // Get the URL request and prepare it to obtain the maven metadata
        // information
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

        // Extract the relevant content from the POST'd form
        Map<String, String> responseMap = new HashMap<String, String>();

        InputStream content = null;
        // Parse the request
        content = req.getInputStream();

        // Builder class that converts the url into a Maven MetaData Object
        MavenMetaData metadata = MavenMetaDataBuilder.build(maven_url);
        try {
            if (metadata.isArtifact()) {
                if (SNAPSHOT_ALLOWED || !metadata.isSnapshotVersion()) {
                    String uuid = service.uploadArtifact(metadata, content);
                    responseMap.put("uuid", uuid); //$NON-NLS-1$
                } else {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.i18n.format("maven.servlet.put.snapshot.not.allowed")); //$NON-NLS-1$
                }

            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, Messages.i18n.format("maven.servlet.put.url.without.artifact")); //$NON-NLS-1$
            }

        } catch (Throwable e) {
            logger.error(Messages.i18n.format("maven.servlet.artifact.content.put.exception"), e); //$NON-NLS-1$
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Messages.i18n.format("maven.servlet.put.exception")); //$NON-NLS-1$
        } finally {
            if (content != null) {
                IOUtils.closeQuietly(content);
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
        if (!url.endsWith("/")) { //$NON-NLS-1$
            url = url + "/"; //$NON-NLS-1$
        }
        try {
            // Gets all the items from the maven url
            Set<String> items = service.getItems(url);

            // If there are items or the request is the root maven folder
            if ((items != null && items.size() > 0) || (url.equals("/") || url.equals(""))) { //$NON-NLS-1$ //$NON-NLS-2$
                // Dispatch the request to the JSP that would display the items
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
                } else {
                    url = ""; //$NON-NLS-1$
                }
                req.setAttribute("relativePath", url); //$NON-NLS-1$
                req.setAttribute("items", items); //$NON-NLS-1$
                dispatcher.forward(req, resp);
            } else {
                resp.setStatus(HttpStatus.SC_NOT_FOUND);
            }
        } catch (MavenRepositoryException e) {
            resp.sendError(HttpStatus.SC_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * Gets the root stack trace as a string.
     *
     * @param t
     *            the t
     * @return the root stack trace
     */
    public static String getRootStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter writer = new PrintWriter(sw);
        getRootCause(t).printStackTrace(writer);
        return sw.getBuffer().toString();
    }

    /**
     * Gets the root exception from the given {@link Throwable}.
     *
     * @param t
     *            the t
     * @return the root cause
     */
    public static Throwable getRootCause(Throwable t) {
        Throwable root = t;
        while (root.getCause() != null && root.getCause() != root)
            root = root.getCause();
        return root;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * javax.servlet.http.HttpServlet#service(javax.servlet.http.HttpServletRequest
     * , javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException {
        String method = req.getMethod();
        String url = req.getRequestURI();
        logger.info(Messages.i18n.format("maven.repository.servlet.service", method, url)); //$NON-NLS-1$
        super.service(req, resp);
    }

}
