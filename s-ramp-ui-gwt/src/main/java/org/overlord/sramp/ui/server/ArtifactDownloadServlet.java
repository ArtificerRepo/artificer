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
package org.overlord.sramp.ui.server;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.atom.visitors.ArtifactContentTypeVisitor;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.ui.server.api.SrampAtomApiClient;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;

/**
 * A standard servlet that makes it easy to download artifact content.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactDownloadServlet extends HttpServlet {

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
		InputStream artifactContent = null;
		try {
			SrampAtomApiClient client = SrampAtomApiClient.getInstance();
			String uuid = req.getParameter("uuid");
			String type = req.getParameter("type");

			ArtifactType artyType = ArtifactType.valueOf(type);
			BaseArtifactType artifact = client.getArtifactMetaData(artyType, uuid);

			// Set the content-disposition
			String artifactName = artifact.getName();
			String disposition = String.format("attachment; filename=\"%1$s\"", artifactName);
			httpResponse.setHeader("Content-Disposition", disposition);

			// Set the content-type
			ArtifactContentTypeVisitor ctVizzy = new ArtifactContentTypeVisitor();
			ArtifactVisitorHelper.visitArtifact(ctVizzy, artifact);
			String contentType = ctVizzy.getContentType().toString();
			httpResponse.setHeader("Content-Type", contentType);

			// Set the content-size (if possible)
			if (artifact instanceof DocumentArtifactType) {
				DocumentArtifactType d = (DocumentArtifactType) artifact;
				long size = d.getContentSize();
				if (size != -1) {
					httpResponse.setHeader("Content-Size", String.valueOf(size));
				}
			}

			// Make sure the browser doesn't cache it
            Date now = new Date();
            httpResponse.setDateHeader("Date", now.getTime());
            httpResponse.setDateHeader("Expires", now.getTime() - 86400000L);
            httpResponse.setHeader("Pragma", "no-cache");
            httpResponse.setHeader("Cache-control", "no-cache, no-store, must-revalidate");

			artifactContent = client.getArtifactContent(artyType, uuid);
			IOUtils.copy(artifactContent, httpResponse.getOutputStream());
		} catch (Exception e) {
			// TODO throw sensible errors (http responses - 404, 500, etc)
			throw new ServletException(e);
		} finally {
			IOUtils.closeQuietly(artifactContent);
		}
	}
}
