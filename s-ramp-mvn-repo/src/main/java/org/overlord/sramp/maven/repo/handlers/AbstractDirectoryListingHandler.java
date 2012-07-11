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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.overlord.sramp.maven.repo.models.DirectoryListing;
import org.overlord.sramp.maven.repo.servlets.MavenRepositoryHandlerFactory;
import org.overlord.sramp.maven.repo.servlets.MavenRepositoryRequestHandler;

/**
 * Base class for any handler that generates a directory listing.  For more details, see
 * the javadoc in {@link MavenRepositoryHandlerFactory}.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractDirectoryListingHandler implements MavenRepositoryRequestHandler {
	
	/**
	 * Default constructor.
	 */
	public AbstractDirectoryListingHandler() {
	}

	/**
	 * @see org.overlord.sramp.maven.repo.servlets.MavenRepositoryRequestHandler#handle(javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public final void handle(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		String urlPath = request.getContextPath() + request.getServletPath();
		String mavenPath = request.getPathInfo();
		if (mavenPath == null)
			mavenPath = "/";

		urlPath += mavenPath;
		if (!urlPath.endsWith("/"))
			urlPath += "/";
		DirectoryListing directoryListing = new DirectoryListing(mavenPath, urlPath);
		try {
			generateDirectoryListing(directoryListing);
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		request.setAttribute("model", directoryListing);
		
		servletContext.getRequestDispatcher("/WEB-INF/jsps/directoryListing.jsp").forward(request, response);
	}

	/**
	 * Method that subclasses must implement in order to populate the directory listing.
	 * @param directoryListing the directory listing to populate
	 * @throws Exception 
	 */
	protected abstract void generateDirectoryListing(DirectoryListing directoryListing) throws Exception;

}
