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
package org.overlord.sramp.maven.repo.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.maven.repo.MavenRepositoryPath;
import org.overlord.sramp.maven.repo.atom.SRAMPAtomApiClient;
import org.overlord.sramp.maven.repo.handlers.util.ArtifactResponseHeaderVisitor;
import org.overlord.sramp.maven.repo.servlets.MavenRepositoryRequestHandler;
import org.overlord.sramp.visitors.ArtifactVisitorHelper;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * A handler that downloads/streams the content of an artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactContentHandler implements MavenRepositoryRequestHandler {

	private MavenRepositoryPath repositoryPath;

	/**
	 * Constructor.
	 * @param repositoryPath
	 */
	public ArtifactContentHandler(MavenRepositoryPath repositoryPath) {
		this.repositoryPath = repositoryPath;
	}

	/**
	 * @see org.overlord.sramp.maven.repo.servlets.MavenRepositoryRequestHandler#handle(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			// Query the artifact meta data
			Entry fullEntry = SRAMPAtomApiClient.getInstance().getFullArtifactEntry(repositoryPath.getArtifactModel(), 
					repositoryPath.getArtifactType(), repositoryPath.getArtifactUuid());
			if (fullEntry == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
			// Unwrap the meta data
			ArtifactType type = ArtifactType.valueOf(repositoryPath.getArtifactType());
			Artifact srampArty = fullEntry.getAnyOtherJAXBObject(Artifact.class);
			BaseArtifactType artifact = type.unwrap(srampArty);
			// Visit the artifact and set content-type and content-size headers on the response
			ArtifactResponseHeaderVisitor visitor = new ArtifactResponseHeaderVisitor(response);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);

			// Get the artifact content as an input stream
			InputStream artifactContent = SRAMPAtomApiClient.getInstance().getArtifactContent(repositoryPath.getArtifactModel(), 
					repositoryPath.getArtifactType(), repositoryPath.getArtifactUuid());
			
			// Copy the content to the response output stream (stream to the client)
			OutputStream oStream = response.getOutputStream();
			try {
				IOUtils.copy(artifactContent, oStream);
			} finally {
				IOUtils.closeQuietly(artifactContent);
				IOUtils.closeQuietly(oStream);
			}
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

}
