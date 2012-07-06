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
package org.overlord.sramp.maven.repo.servlets;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * An interface used to handle incoming requests.  The main servlet dispatches inbound
 * requests to an instance of this interface.  The specific implementation of this 
 * interface is determined by the inbound request path.
 *
 * @author eric.wittmann@redhat.com
 */
public interface MavenRepositoryRequestHandler {

	/**
	 * Called to handle/process the request by performing some logic and sending the
	 * result to the response.
	 * @param request the inbound request to the maven repo
	 * @param servletContext the servlet context
	 * @param response the HTTP response
	 * @throws IOException
	 * @throws ServletException
	 */
	public void handle(ServletContext servletContext, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException;

}
