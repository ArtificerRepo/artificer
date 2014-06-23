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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.ServletException;
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
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.atom.archive.expand.registry.ZipToSrampArchiveRegistry;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.ui.server.api.SrampApiClientAccessor;
import org.overlord.sramp.ui.server.i18n.Messages;
import org.overlord.sramp.ui.server.services.ArtifactTypeGuessingService;
import org.overlord.sramp.ui.server.util.ExceptionUtils;

/**
 * A standard servlet that artifact content is POSTed to in order to add new artifacts
 * to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactUploadServlet extends AbstractUploadServlet {

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
						if (item.getFieldName().equals("artifactType")) { //$NON-NLS-1$
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
				responseMap.put("exception", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				responseMap.put("exception-message", e.getMessage()); //$NON-NLS-1$
				responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e)); //$NON-NLS-1$
			} catch (Throwable e) {
				responseMap = new HashMap<String, String>();
				responseMap.put("exception", "true"); //$NON-NLS-1$ //$NON-NLS-2$
				responseMap.put("exception-message", e.getMessage()); //$NON-NLS-1$
				responseMap.put("exception-stack", ExceptionUtils.getRootStackTrace(e)); //$NON-NLS-1$
			} finally {
				IOUtils.closeQuietly(artifactContent);
			}
			writeToResponse(responseMap, response);
		} else {
			response.sendError(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE,
			        Messages.i18n.format("UploadServlet.ContentTypeNotSupported")); //$NON-NLS-1$
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
		    if ("SrampArchive".equals(artifactType)) { //$NON-NLS-1$
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
            responseParams.put("batch", "true"); //$NON-NLS-1$ //$NON-NLS-2$
            responseParams.put("batchTotal", String.valueOf(numSuccess + numFailed)); //$NON-NLS-1$
            responseParams.put("batchNumSuccess", String.valueOf(numSuccess)); //$NON-NLS-1$
            responseParams.put("batchNumFailed", String.valueOf(numFailed)); //$NON-NLS-1$
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
        ArtifactType at = ArtifactType.valueOf(artifactType);
        String uuid = null;
		// First, upload the artifact, no matter what kind
        InputStream contentStream = null;
		try {
			contentStream = FileUtils.openInputStream(tempFile);
			BaseArtifactType artifact = clientAccessor.getClient().uploadArtifact(at, contentStream, fileName);
			responseParams.put("model", at.getArtifactType().getModel()); //$NON-NLS-1$
			responseParams.put("type", at.getArtifactType().getType()); //$NON-NLS-1$
			responseParams.put("uuid", artifact.getUuid()); //$NON-NLS-1$
			uuid = artifact.getUuid();
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		// Check if this is an expandable file type.  If it is, then expand it!
        ZipToSrampArchive expander = null;
        SrampArchive archive = null;
        try {
            expander = ZipToSrampArchiveRegistry.createExpander(at, tempFile);
            if (expander != null) {
                expander.setContextParam(DefaultMetaDataFactory.PARENT_UUID, uuid);
                archive = expander.createSrampArchive();
                clientAccessor.getClient().uploadBatch(archive);
            }
        } finally {
            SrampArchive.closeQuietly(archive);
            ZipToSrampArchive.closeQuietly(expander);
        }
    }
}
