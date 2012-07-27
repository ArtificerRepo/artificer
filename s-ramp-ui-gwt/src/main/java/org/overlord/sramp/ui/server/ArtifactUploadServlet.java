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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.client.SrampClientUtils;
import org.overlord.sramp.ui.server.api.SrampAtomApiClient;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * A standard servlet that artifact content is POSTed to in order to add new artifacts
 * to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUploadServlet extends HttpServlet {

	private static final long serialVersionUID = ArtifactUploadServlet.class.hashCode();

	/**
	 * Constructor.
	 */
	public ArtifactUploadServlet() {
	}

	/**
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse response)
			throws ServletException, IOException {
		
		// Extract the relevant content from the POST'd form
		if (ServletFileUpload.isMultipartContent(req)) {
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
			
			// Parse the request
			String artifactType = null;
			String fileName = null;
			InputStream artifactContent = null;
			try {
				List<FileItem> items = upload.parseRequest(req);
				for (FileItem item : items) {
					if (item.isFormField()) {
						if (item.getFieldName().equals("artifactType")) {
							artifactType = item.getString();
						}
					} else {
						fileName = item.getName();
						if (fileName != null)
							fileName = FilenameUtils.getName(fileName);
						artifactContent = item.getInputStream();
					}
				}
				
				// Now that the content has been extracted, process it (upload the artifact to the s-ramp repo).
				Map<String, String> responseMap = uploadArtifact(artifactType, fileName, artifactContent);
				writeToResponse(responseMap, response);
			} catch (Exception e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
						"An error occurred while creating the file : " + e.getMessage());
				return;
			} finally {
				IOUtils.closeQuietly(artifactContent);
			}
		} else {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"Request contents type is not supported by the servlet.");
			return;
		}
	}

	/**
	 * Upload the artifact to the S-RAMP repository.
	 * @param artifactType the type of s-ramp artifact
	 * @param fileName the file name of the artifact being uploaded
	 * @param artifactContent the content of the artifact
	 * @throws Exception 
	 */
	private Map<String, String> uploadArtifact(String artifactType, String fileName, InputStream artifactContent) throws Exception {
		SrampAtomApiClient client = SrampAtomApiClient.getInstance();
		
		ArtifactType at = ArtifactType.valueOf(artifactType);
		Entry entry = client.uploadArtifact(at, artifactContent, fileName);
		BaseArtifactType artifact = SrampClientUtils.unwrapSrampArtifact(at, entry);

		Map<String, String> responseParams = new HashMap<String, String>();
		responseParams.put("uuid", artifact.getUuid());
		return responseParams;
	}

	/**
	 * Writes the response values back to the http response.  This allows the calling code to
	 * parse the response values for display to the user.
	 * @param responseMap the response params to write to the http response
	 * @param response the http response
	 * @throws IOException 
	 */
	private void writeToResponse(Map<String, String> responseMap, HttpServletResponse response) throws IOException {
		response.setContentType("application/json; charset=UTF8");
		StringBuilder builder = new StringBuilder();
		builder.append("({");
		boolean first = true;
		for (java.util.Map.Entry<String, String> entry : responseMap.entrySet()) {
			String key = entry.getKey();
			String val = entry.getValue();
			if (first)
				first = false;
			else
				builder.append(",");
			builder.append("\"");
			builder.append(key);
			builder.append("\" : \"");
			builder.append(val);
			builder.append("\"");
		}
		builder.append("})");
		byte [] jsonData = builder.toString().getBytes("UTF-8");
		response.setContentLength(jsonData.length);
		response.getOutputStream().write(jsonData);
		response.getOutputStream().flush();
	}
}
