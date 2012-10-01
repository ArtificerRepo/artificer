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
package org.overlord.sramp.client;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.mime.MimeTypes;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Class used to communicate with the S-RAMP server.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAtomApiClient {

	private String endpoint;

	/**
	 * Constructor.
	 * @param endpoint
	 */
	public SrampAtomApiClient(String endpoint) {
		this.endpoint = endpoint;
		if (this.endpoint.endsWith("/")) {
			this.endpoint = this.endpoint.substring(0, this.endpoint.length()-1);
		}
	}

	/**
	 * Gets an Atom {@link Entry} for the given S-RAMP artifact by UUID.
	 * @param artifactType
	 * @param artifactUuid
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	public Entry getFullArtifactEntry(ArtifactType artifactType, String artifactUuid)
			throws SrampClientException, SrampServerException {
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint,
					artifactType.getArtifactType().getModel(), artifactType.getArtifactType().getType(),
					artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			ClientResponse<Entry> response = request.get(Entry.class);
			return response.getEntity();
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Gets the content for an artifact as an input stream.  The caller must close the resulting
	 * @param artifactType the artifact type
	 * @param artifactUuid the S-RAMP uuid of the artifact
	 * @return an {@link InputStream} to the S-RAMP artifact content
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	public InputStream getArtifactContent(ArtifactType artifactType, String artifactUuid)
			throws SrampClientException, SrampServerException {
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", this.endpoint,
					artifactType.getArtifactType().getModel(), artifactType.getArtifactType().getType(),
					artifactUuid);
			URL url = new URL(atomUrl);
			return url.openStream();
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Please refer to javadoc in  {@link SrampAtomApiClient#uploadArtifact(String, String, InputStream, String)}
	 * @param artifactType
	 * @param content
	 * @param artifactFileName
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	public Entry uploadArtifact(ArtifactType artifactType, InputStream content, String artifactFileName)
			throws SrampClientException, SrampServerException {
		// Determine the mime type if it's not included in the artifact type.
		String mimeType = artifactType.getMimeType();
		if (mimeType == null) {
			mimeType = MimeTypes.getContentType(artifactFileName);
		}
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s", this.endpoint,
					artifactType.getArtifactType().getModel(), artifactType.getArtifactType().getType());
			ClientRequest request = new ClientRequest(atomUrl);
			if (artifactFileName != null)
				request.header("Slug", artifactFileName);
			if (mimeType != null)
				request.header("Content-Type", mimeType);
			request.body(artifactType.getMimeType(), content);

			ClientResponse<Entry> response = request.post(Entry.class);
			return response.getEntity();
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Performs a batch operation by uploading an s-ramp package archive to the s-ramp server
	 * for processing.  The contents of the s-ramp archive will be processed, and the results
	 * will be returned as a Map.  The Map is indexed by the S-RAMP Archive entry path, and each
	 * each value in the Map will either be a {@link BaseArtifactType} or an
	 * {@link SrampServerException}, depending on success vs. failure of that entry.
	 *
	 * @param archive the s-ramp package archive to upload
	 * @return the collection of results (one per entry in the s-ramp package)
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	@SuppressWarnings("resource")
	public Map<String, ?> uploadBatch(SrampArchive archive) throws SrampClientException, SrampServerException {
		File packageFile = null;
		InputStream packageStream = null;

		try {
			packageFile = archive.pack();
			packageStream = FileUtils.openInputStream(packageFile);
			ClientRequest request = new ClientRequest(this.endpoint);
			request.header("Content-Type", "application/zip");
			request.body(MediaType.APPLICATION_ZIP, packageStream);

			ClientResponse<MultipartInput> clientResponse = request.post(MultipartInput.class);
			MultipartInput response = clientResponse.getEntity();
			List<InputPart> parts = response.getParts();

			Map<String, Object> rval = new HashMap<String, Object>(parts.size());
			for (InputPart part : parts) {
				String contentId = part.getHeaders().getFirst("Content-ID");
				String path = contentId.substring(1, contentId.lastIndexOf('@'));
				HttpResponseBean rbean = part.getBody(HttpResponseBean.class, null);
				if (rbean.getCode() == 201) {
					Entry entry = (Entry) rbean.getBody();
					BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(entry);
					rval.put(path, artifact);
				} else if (rbean.getCode() == 409) {
					String errorReason = (String) rbean.getBody();
					SrampServerException exception = new SrampServerException("Conflict found for: " + path);
					exception.setRemoteStackTrace(errorReason);
					rval.put(path, exception);
				} else {
					// Only a non-compliant s-ramp impl could cause this
					throw new Exception("Unexpected return code '" + rbean.getCode() + "' for ID '" + contentId + "'.  The S-RAMP server is non-compliant.");
				}
			}
			return rval;
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		} finally {
			IOUtils.closeQuietly(packageStream);
			FileUtils.deleteQuietly(packageFile);
		}
	}

	/**
	 * Called to update the meta-data stored in the s-ramp repository for the given s-ramp
	 * artifact.
	 * @param artifact
	 * @throws SrampClientException
	 */
	public void updateArtifactMetaData(BaseArtifactType artifact) throws SrampClientException {
		try {
			ArtifactType type = ArtifactType.valueOf(artifact);
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			String artifactUuid = artifact.getUuid();
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);

			Entry entry = SrampAtomUtils.wrapSrampArtifact(artifact);

			request.body("application/atom+xml;type=entry", entry);
			request.put();
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Updates the content of the artifact.
	 * @param artifactType
	 * @param uuid
	 * @param content
	 * @throws SrampClientException
	 */
	public void updateArtifact(BaseArtifactType artifact, InputStream content) throws SrampClientException {
		try {
			ArtifactType type = ArtifactType.valueOf(artifact);

			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			String artifactUuid = artifact.getUuid();
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			request.body(type.getMimeType(), content);
			request.put();
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Delets an artifact from the s-ramp repository.
	 * @param uuid
	 * @param type
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	public void deleteArtifact(String uuid, ArtifactType type) throws SrampClientException, SrampServerException {
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			String artifactUuid = uuid;
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			request.delete();
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Provides a very simple mechanism for querying.  Defaults many of the parameters.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	public Feed query(String srampQuery) throws SrampClientException, SrampServerException {
		return query(srampQuery, 0, 20, "name", true);
	}

	/**
	 * Executes the given s-ramp query xpath and returns a Feed of the matching artifacts.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @param page which page of results to return (0 indexed)
	 * @param pageSize the size of the page of results to return
	 * @param orderBy the s-ramp property to use for sorting (name, uuid, createdOn, etc)
	 * @param ascending the direction of the sort
	 * @return an Atom {@link Feed}
	 * @throws SrampClientException
	 * @throws SrampServerException
	 */
	public Feed query(String srampQuery, int page, int pageSize, String orderBy, boolean ascending)
			throws SrampClientException, SrampServerException {
		try {
			String xpath = srampQuery;
			if (xpath == null)
				throw new Exception("Please supply an S-RAMP x-path formatted query.");
			// Remove the leading /s-ramp/ prior to POSTing to the atom endpoint
			if (xpath.startsWith("/s-ramp/"))
				xpath = xpath.substring(8);
			String atomUrl = this.endpoint;

			ClientRequest request = new ClientRequest(atomUrl);
			MultipartFormDataOutput formData = new MultipartFormDataOutput();
			formData.addFormData("query", xpath, MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("page", String.valueOf(page), MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("pageSize", String.valueOf(pageSize), MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("orderBy", orderBy, MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("ascending", String.valueOf(ascending), MediaType.TEXT_PLAIN_TYPE);

			request.body(MediaType.MULTIPART_FORM_DATA_TYPE, formData);
			ClientResponse<Feed> response = request.post(Feed.class);
			return response.getEntity();
		} catch (SrampServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

}
