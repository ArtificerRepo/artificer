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
package org.overlord.sramp.ui.server.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.jar.DefaultMetaDataFactory;
import org.overlord.sramp.client.jar.DiscoveredArtifact;
import org.overlord.sramp.client.jar.JarToSrampArchive;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.overlord.sramp.ui.server.services.ArtifactTypeGuessingService;
import org.overlord.sramp.ui.server.util.ExceptionUtils;

/**
 * A standard servlet that artifact content is POSTed to in order to add new artifacts
 * to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUploadServlet extends HttpServlet {

	private static final long serialVersionUID = ArtifactUploadServlet.class.hashCode();

    @Inject
    private SrampApiClientAccessor clientAccessor;
    @Inject
    private ArtifactTypeGuessingService artifactTypeGuesser;

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
			Map<String, String> responseMap;
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
				responseMap = uploadArtifact(artifactType, fileName, artifactContent);
			} catch (SrampAtomException e) {
				responseMap = new HashMap<String, String>();
				responseMap.put("exception", "true");
				responseMap.put("exception-message", e.getMessage());
				responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e));
			} catch (Throwable e) {
				responseMap = new HashMap<String, String>();
				responseMap.put("exception", "true");
				responseMap.put("exception-message", e.getMessage());
				responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e));
			} finally {
				IOUtils.closeQuietly(artifactContent);
			}
			writeToResponse(responseMap, response);
		} else {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
					"Request contents type is not supported by the servlet.");
		}
	}

	/**
	 * Upload the artifact to the S-RAMP repository.
	 * @param artifactType the type of s-ramp artifact
	 * @param fileName the file name of the artifact being uploaded
	 * @param artifactContent the content of the artifact
	 * @throws Exception
	 */
	private Map<String, String> uploadArtifact(String artifactType, String fileName,
			InputStream artifactContent) throws Exception {
		File tempFile = stashResourceContent(artifactContent);
		Map<String, String> responseParams = new HashMap<String, String>();

		if (artifactType == null || artifactType.trim().length() == 0) {
		    artifactType = artifactTypeGuesser.guess(fileName);
		}

		try {
		    if ("SrampArchive".equals(artifactType)) {
		        uploadPackage(tempFile, responseParams);
		    } else {
		        uploadSingleArtifact(artifactType, fileName, tempFile, responseParams);
		    }
        } finally {
            FileUtils.deleteQuietly(tempFile);
        }

		return responseParams;
	}

    /**
     * Uploads an S-RAMP package to the repository.
     * @param tempFile
     * @param responseParams
     */
    private void uploadPackage(File tempFile, Map<String, String> responseParams) throws Exception {
        SrampArchive archive = null;
        try {
            archive = new SrampArchive(tempFile);
            Map<String, ?> batch = clientAccessor.getClient().uploadBatch(archive);
            int numSuccess = 0;
            int numFailed = 0;
            for (String key : batch.keySet()) {
                Object object = batch.get(key);
                if (object instanceof BaseArtifactType) {
                    numSuccess++;
                } else {
                    numFailed++;
                }
            }
            // TODO turn these things into constants
            responseParams.put("batch", "true");
            responseParams.put("batchTotal", String.valueOf(numSuccess + numFailed));
            responseParams.put("batchNumSuccess", String.valueOf(numSuccess));
            responseParams.put("batchNumFailed", String.valueOf(numFailed));
        } finally {
            SrampArchive.closeQuietly(archive);
        }

    }

    /**
     * Uploads a single artifact to the S-RAMP repository.
     * @param artifactType
     * @param fileName
     * @param client
     * @param tempFile
     * @param responseParams
     * @throws Exception
     */
    private void uploadSingleArtifact(String artifactType, String fileName,
            File tempFile, Map<String, String> responseParams) throws Exception {
        String uuid = null;
		// First, upload the artifact, no matter what kind
        InputStream contentStream = null;
		try {
			contentStream = FileUtils.openInputStream(tempFile);
			ArtifactType at = ArtifactType.valueOf(artifactType);
			BaseArtifactType artifact = clientAccessor.getClient().uploadArtifact(at, contentStream, fileName);
			responseParams.put("model", at.getArtifactType().getModel());
			responseParams.put("type", at.getArtifactType().getType());
			responseParams.put("uuid", artifact.getUuid());
			uuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Check if this is an expandable file type.  If it is, then expand it!
		if (isExpandable(fileName)) {
			JarToSrampArchive j2sramp = null;
			SrampArchive archive = null;
			try {
				final String parentUUID = uuid;
				j2sramp = new JarToSrampArchive(tempFile);
				j2sramp.setMetaDataFactory(new DefaultMetaDataFactory() {
					@Override
					public BaseArtifactType createMetaData(DiscoveredArtifact artifact) {
						BaseArtifactType metaData = super.createMetaData(artifact);
						SrampModelUtils.addGenericRelationship(metaData, "expandedFromDocument", parentUUID);
						return metaData;
					}
				});
				archive = j2sramp.createSrampArchive();
				clientAccessor.getClient().uploadBatch(archive);
			} finally {
				SrampArchive.closeQuietly(archive);
				JarToSrampArchive.closeQuietly(j2sramp);
			}
		}
    }

	/**
	 * Make a temporary copy of the resource by saving the content to a temp file.
	 * @param resourceInputStream
	 * @throws IOException
	 */
	private File stashResourceContent(InputStream resourceInputStream) throws IOException {
		File resourceTempFile = null;
		OutputStream oStream = null;
		try {
			resourceTempFile = File.createTempFile("s-ramp-ui-upload", ".tmp");
			oStream = FileUtils.openOutputStream(resourceTempFile);
		} catch (IOException e) {
			FileUtils.deleteQuietly(resourceTempFile);
			throw e;
		} finally {
			IOUtils.copy(resourceInputStream, oStream);
			IOUtils.closeQuietly(resourceInputStream);
			IOUtils.closeQuietly(oStream);
		}
		return resourceTempFile;
	}

	/**
	 * Returns true if the uploaded file should be expanded.  We support expanding JAR, WAR,
	 * and EAR files.
	 * @param fileName the name of the uploaded file
	 * @return true if the file should be expanded
	 */
	private boolean isExpandable(String fileName) {
		String name = fileName.toLowerCase();
		return name.endsWith(".jar") || name.endsWith(".war") || name.endsWith(".ear");
	}

	/**
	 * Writes the response values back to the http response.  This allows the calling code to
	 * parse the response values for display to the user.
	 *
	 * TODO replace with Jackson!
	 *
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
			if (val != null) {
				val = val.replace("\"", "\\\"");
				val = val.replace("\n", "\\n");
				builder.append(val);
			}
			builder.append("\"");
		}
		builder.append("})");
		byte [] jsonData = builder.toString().getBytes("UTF-8");
		response.setContentLength(jsonData.length);
		response.getOutputStream().write(jsonData);
		response.getOutputStream().flush();
	}
}
