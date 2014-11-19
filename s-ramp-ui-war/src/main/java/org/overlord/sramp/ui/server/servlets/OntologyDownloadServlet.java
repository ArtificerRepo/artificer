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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.overlord.sramp.ui.server.i18n.Messages;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * The "OntologyDownloadServlet" .
 *
 * @author David Virgil Naranjo
 */
public class OntologyDownloadServlet extends AbstractDownloadServlet {

    private static final long serialVersionUID = ArtifactDownloadServlet.class.hashCode();

    /**
     * Constructor.
     */
    public OntologyDownloadServlet() {
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
        HttpServletResponse httpResponse = resp;
        try {
            SrampAtomApiClient client = SrampApiClientAccessor.getClient();
            String uuid = req.getParameter("uuid"); //$NON-NLS-1$
            if (StringUtils.isNotEmpty(uuid)) {
                doDownloadContent(httpResponse, client, uuid);
            } else {
                throw new Exception(Messages.i18n.format("OntologyDownloadServlet.no.uuid.param")); //$NON-NLS-1$
            }

        } catch (Exception e) {
            // TODO throw sensible error (http responses - 404, 500, etc)
            throw new ServletException(e);
        }
    }


    /**
     * Do download content.
     *
     * @param httpResponse
     *            the http response
     * @param client
     *            the client
     * @param uuid
     *            the uuid
     * @throws Exception
     *             the exception
     */
    protected void doDownloadContent(HttpServletResponse httpResponse, SrampAtomApiClient client, String uuid)
            throws Exception {
        InputStream ontologyContent = null;

        // Set the content-type

        String contentType = "application/rdf+xml"; //$NON-NLS-1$
        RDF ontologyRDF=client.getOntology(uuid);
        StringWriter writer=new StringWriter();
        JAXBContext jaxbContext = JAXBContext.newInstance(RDF.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        // output pretty printed
        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        jaxbMarshaller.marshal(ontologyRDF, writer);
        String content = writer.getBuffer().toString();
        ontologyContent = new ByteArrayInputStream(content.getBytes("UTF-8")); //$NON-NLS-1$
        // Set the content-size
        httpResponse.setHeader("Content-Size", content.getBytes().length + ""); //$NON-NLS-1$ //$NON-NLS-2$
        // Set the content-disposition
        String base = ontologyRDF.getOtherAttributes().get(
                new QName("http://www.w3.org/XML/1998/namespace", "base")); //$NON-NLS-1$ //$NON-NLS-2$
        String name = "sramp-ontology.owl"; //$NON-NLS-1$
        if (base.endsWith(".owl")) { //$NON-NLS-1$
            name = base.substring(base.lastIndexOf("/") + 1); //$NON-NLS-1$
        }
        String disposition = "attachment; filename=\"" + name + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        super.doDownloadContent(ontologyContent, contentType, disposition, httpResponse);

    }
}