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
package org.artificer.ui.server.servlets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;

import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.artificer.atom.visitors.ArtifactContentTypeVisitor;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;

/**
 * A standard servlet that makes it easy to download artifact content.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactDownloadServlet extends AbstractDownloadServlet {

	private static final long serialVersionUID = ArtifactDownloadServlet.class.hashCode();

	/**
	 * Constructor.
	 */
	public ArtifactDownloadServlet() {
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException,
			IOException {
        HttpServletResponse httpResponse = resp;
		try {
			ArtificerAtomApiClient client = ArtificerApiClientAccessor.getClient();
			String uuid = req.getParameter("uuid"); //$NON-NLS-1$
            String type = req.getParameter("type"); //$NON-NLS-1$
            String as = req.getParameter("as"); //$NON-NLS-1$

            ArtifactType artyType = ArtifactType.valueOf(type);
            BaseArtifactType artifact = client.getArtifactMetaData(artyType, uuid);

            boolean downloadContent = !"meta-data".equals(as); //$NON-NLS-1$
            if (downloadContent) {
                doDownloadContent(httpResponse, client, artyType, artifact);
            } else {
                doDownloadMetaData(httpResponse, client, artyType, artifact);
            }
		} catch (Exception e) {
			// TODO throw sensible error (http responses - 404, 500, etc)
			throw new ServletException(e);
		}
	}

    /**
     * Downloads the content of the artifact.
     * 
     * @param httpResponse
     * @param client
     * @param artyType
     * @param artifact
     * @throws Exception
     */
    protected void doDownloadContent(HttpServletResponse httpResponse, ArtificerAtomApiClient client,
            ArtifactType artyType, BaseArtifactType artifact) throws Exception {
        InputStream artifactContent = null;
        // Set the content-disposition
        String artifactName = artifact.getName();
        String disposition = String.format("attachment; filename=\"%1$s\"", artifactName); //$NON-NLS-1$

        // Set the content-type
        ArtifactContentTypeVisitor ctVizzy = new ArtifactContentTypeVisitor();
        ArtifactVisitorHelper.visitArtifact(ctVizzy, artifact);
        String contentType = ctVizzy.getContentType().toString();

        // Set the content-size (if possible)
        if (artifact instanceof DocumentArtifactType) {
            DocumentArtifactType d = (DocumentArtifactType) artifact;
            long size = d.getContentSize();
            if (size != -1) {
                httpResponse.setHeader("Content-Size", String.valueOf(size)); //$NON-NLS-1$
            }
        }
        artifactContent = client.getArtifactContent(artyType, artifact.getUuid());
        super.doDownloadContent(artifactContent, contentType, disposition, httpResponse);

    }

    /**
     * Downloads the artifact meta-data as XML.
     * @param httpResponse
     * @param client
     * @param artyType
     * @param artifact
     * @throws Exception
     */
    private void doDownloadMetaData(HttpServletResponse httpResponse, ArtificerAtomApiClient client,
            ArtifactType artyType, BaseArtifactType artifact) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(Artifact.class);
        Artifact wrapper = new Artifact();
        Method method = Artifact.class.getMethod("set" + artifact.getClass().getSimpleName(), artifact.getClass()); //$NON-NLS-1$
        method.invoke(wrapper, artifact);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        jaxbContext.createMarshaller().marshal(wrapper, baos);
        InputStream is = new ByteArrayInputStream(baos.toByteArray());
        IOUtils.copy(is, httpResponse.getOutputStream());
    }
}
