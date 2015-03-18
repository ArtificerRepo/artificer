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

import org.apache.commons.lang.StringUtils;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.common.ArtifactType;
import org.artificer.ui.server.api.ArtificerApiClientAccessor;
import org.artificer.ui.server.util.ExceptionUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A standard servlet that to create new artifacts
 *
 * @author Brett Meyer
 */
public class ArtifactCreateServlet extends AbstractUploadServlet {

	private static final long serialVersionUID = ArtifactCreateServlet.class.hashCode();

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		Map<String, String> responseMap;
		try {
			String artifactTypeString = req.getParameter("artifactType");
			String artifactName = req.getParameter("artifactName");
			String artifactDescription = req.getParameter("artifactDescription");

			ArtifactType artifactType = ArtifactType.valueOf(artifactTypeString);
			BaseArtifactType artifact = artifactType.newArtifactInstance();
			artifact.setName(artifactName);
			if (StringUtils.isNotBlank(artifactDescription)) {
				artifact.setDescription(artifactDescription);
			}

			artifact = ArtificerApiClientAccessor.getClient().createArtifact(artifact);

			responseMap = new HashMap<String, String>();
			responseMap.put("model", artifactType.getModel());
			responseMap.put("type", artifactType.getType());
			responseMap.put("uuid", artifact.getUuid());
		} catch (ArtificerAtomException e) {
			responseMap = new HashMap<String, String>();
			responseMap.put("exception", "true");
			responseMap.put("exception-message", e.getMessage());
			responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e));
		} catch (Throwable e) {
			responseMap = new HashMap<String, String>();
			responseMap.put("exception", "true");
			responseMap.put("exception-message", e.getMessage());
			responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e));
		}
		writeToResponse(responseMap, response);
	}
}
