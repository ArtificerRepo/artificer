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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.atom.app.AppCategories;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.jboss.resteasy.plugins.providers.atom.app.AppService;
import org.jboss.resteasy.plugins.providers.atom.app.AppWorkspace;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartInput;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedOutput;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.MimeTypes;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

/**
 * Class used to communicate with the S-RAMP server via the S-RAMP Atom API.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampAtomApiClient {

	private String endpoint;
	private boolean validating;
	private Set<String> enabledFeatures = new HashSet<String>();

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
	 * Constructor.
	 * @param endpoint
	 * @param validating
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public SrampAtomApiClient(String endpoint, boolean validating) throws SrampClientException, SrampAtomException {
		this(endpoint);
		this.validating = validating;
		if (this.validating) {
			discoverAvailableFeatures();
		}
	}

	/**
	 * @return the s-ramp endpoint
	 */
	public String getEndpoint() {
		return this.endpoint;
	}

	/**
	 * This method will grab the /s-ramp/servicedocument from the S-RAMP repository
	 * and examine its contents in order to determine the features supported by the
	 * repository.
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	private void discoverAvailableFeatures() throws SrampClientException, SrampAtomException {
		AppService serviceDoc = getServiceDocument();
		for (AppWorkspace workspace : serviceDoc.getWorkspace()) {
			for (AppCollection collection : workspace.getCollection()) {
				for (AppCategories cats : collection.getCategories()) {
					for (Category category : cats.getCategory()) {
						this.enabledFeatures.add(category.getTerm());
					}
				}
			}
		}
	}

	/**
	 * Asserts that the given feature is enabled.  If it is not, then an exception
	 * is thrown.  If this client is not set to validating, then this method will
	 * always pass.
	 * @param feature
	 * @throws SrampClientException
	 */
	private void assertFeatureEnabled(String feature) throws SrampClientException {
		if (this.validating) {
			if (!this.enabledFeatures.contains(feature)) {
				throw new SrampClientException("The S-RAMP repository does not appear to support this feature.");
			}
		}
	}

	/**
	 * Asserts that the given feature is enabled.  If it is not, then an exception
	 * is thrown.  If this client is not set to validating, then this method will
	 * always pass.
	 * @param feature
	 * @throws SrampClientException
	 */
	private void assertFeatureEnabled(ArtifactType feature) throws SrampClientException {
		if (this.validating) {
			if (!this.enabledFeatures.contains(feature.getArtifactType().getType())) {
				throw new SrampClientException("The S-RAMP repository does not appear to support this feature.");
			}
		}
	}

	/**
	 * Gets the S-RAMP service document.
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public AppService getServiceDocument() throws SrampClientException, SrampAtomException {
		try {
			String atomUrl = String.format("%1$s/servicedocument", this.endpoint);
			ClientRequest request = new ClientRequest(atomUrl);
			ClientResponse<AppService> response = request.get(AppService.class);
			return response.getEntity();
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Gets the full meta-data listing for an Artifact in the S-RAMP repository.
	 * @param artifactType
	 * @param artifactUuid
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public BaseArtifactType getArtifactMetaData(ArtifactType artifactType, String artifactUuid)
			throws SrampClientException, SrampAtomException {
		assertFeatureEnabled(artifactType);
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint,
					artifactType.getArtifactType().getModel(), artifactType.getArtifactType().getType(),
					artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			ClientResponse<Entry> response = request.get(Entry.class);
			Entry entry = response.getEntity();
			return SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} catch (SrampAtomException e) {
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
	 * @throws SrampAtomException
	 */
	public InputStream getArtifactContent(ArtifactType artifactType, String artifactUuid)
			throws SrampClientException, SrampAtomException {
		assertFeatureEnabled(artifactType);
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
	 * @throws SrampAtomException
	 */
	public BaseArtifactType uploadArtifact(ArtifactType artifactType, InputStream content, String artifactFileName)
			throws SrampClientException, SrampAtomException {
		assertFeatureEnabled(artifactType);
		// Determine the mime type if it's not included in the artifact type.
		String mimeType = artifactType.getMimeType();
		if (mimeType == null) {
			mimeType = MimeTypes.getContentType(artifactFileName);
		}
		try {
			String type = artifactType.getArtifactType().getType();
			if ("user".equals(artifactType.getArtifactType().getModel()) && artifactType.getUserType()!=null) {
				type = artifactType.getUserType();
			}
			String atomUrl = String.format("%1$s/%2$s/%3$s", this.endpoint,
					artifactType.getArtifactType().getModel(), type);
			ClientRequest request = new ClientRequest(atomUrl);
			if (artifactFileName != null)
				request.header("Slug", artifactFileName);
			if (mimeType != null)
				request.header("Content-Type", mimeType);
			request.body(artifactType.getMimeType(), content);

			ClientResponse<Entry> response = request.post(Entry.class);
			Entry entry = response.getEntity();
			return SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Please refer to javadoc in  {@link SrampAtomApiClient#uploadArtifact(String, String, InputStream, String)}
	 * @param baseArtifactType
	 * @param content
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public BaseArtifactType uploadArtifact(BaseArtifactType baseArtifactType, InputStream content)
			throws SrampClientException, SrampAtomException {
		ArtifactType artifactType = ArtifactType.valueOf(baseArtifactType);
		assertFeatureEnabled(artifactType);
		String artifactFileName = baseArtifactType.getName();
		// Determine the mime type if it's not included in the artifact type.
		String mimeType = artifactType.getMimeType();
		if (mimeType == null) {
			mimeType = MimeTypes.getContentType(artifactFileName);
			artifactType.setMimeType(mimeType);
		}
		try {
			String type = artifactType.getArtifactType().getType();
			if ("user".equals(artifactType.getArtifactType().getModel()) && artifactType.getUserType()!=null) {
				type = artifactType.getUserType();
			}
			String atomUrl = String.format("%1$s/%2$s/%3$s", this.endpoint,
					artifactType.getArtifactType().getModel(), type);
			ClientRequest request = new ClientRequest(atomUrl);

			MultipartRelatedOutput output = new MultipartRelatedOutput();

			//1. Add first part, the S-RAMP entry
			Entry atomEntry = SrampAtomUtils.wrapSrampArtifact(baseArtifactType);

			MediaType mediaType = new MediaType("application", "atom+xml");
			output.addPart(atomEntry, mediaType);

			//2. Add second part, the content
			request.body(artifactType.getMimeType(), content);
			MediaType mediaType2 = MediaType.getInstance(artifactType.getMimeType());
			output.addPart(content, mediaType2);

			//3. Send the request
			request.body(MultipartConstants.MULTIPART_RELATED, output);
			ClientResponse<Entry> response = request.post(Entry.class);
			Entry entry = response.getEntity();
			return SrampAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} catch (SrampAtomException e) {
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
	 * {@link SrampAtomException}, depending on success vs. failure of that entry.
	 *
	 * @param archive the s-ramp package archive to upload
	 * @return the collection of results (one per entry in the s-ramp package)
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public Map<String, ?> uploadBatch(SrampArchive archive) throws SrampClientException, SrampAtomException {
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
					if (MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION.equals(rbean.getHeaders().get("Content-Type"))) {
						SrampAtomException exception = (SrampAtomException) rbean.getBody();
						rval.put(path, exception);
					} else {
						String errorReason = (String) rbean.getBody();
						SrampAtomException exception = new SrampAtomException(errorReason);
						rval.put(path, exception);
					}
				} else {
					// Only a non-compliant s-ramp impl could cause this
					SrampAtomException exception = new SrampAtomException("Unexpected return code '" + rbean.getCode() + "' for ID '" + contentId + "'.  The S-RAMP server is non-compliant.");
					rval.put(path, exception);
				}
			}
			return rval;
		} catch (SrampAtomException e) {
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
	 * @throws SrampAtomException
	 */
	public void updateArtifactMetaData(BaseArtifactType artifact) throws SrampClientException, SrampAtomException {
		ArtifactType type = ArtifactType.valueOf(artifact);
		assertFeatureEnabled(type);
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("user".equals(type.getArtifactType().getModel()) && type.getUserType()!=null) {
				artifactType = type.getUserType();
			}
			String artifactUuid = artifact.getUuid();
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);

			Entry entry = SrampAtomUtils.wrapSrampArtifact(artifact);

			request.body("application/atom+xml;type=entry", entry);
			request.put();
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Updates the content of the artifact.
	 * @param artifact
	 * @param content
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public void updateArtifactContent(BaseArtifactType artifact, InputStream content)
			throws SrampClientException, SrampAtomException {
		ArtifactType type = ArtifactType.valueOf(artifact);
		assertFeatureEnabled(type);
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("user".equals(type.getArtifactType().getModel()) && type.getUserType()!=null) {
				artifactType = type.getUserType();
			}
			String artifactUuid = artifact.getUuid();
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			request.body(type.getMimeType(), content);
			request.put();
		} catch (SrampAtomException e) {
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
	 * @throws SrampAtomException
	 */
	public void deleteArtifact(String uuid, ArtifactType type) throws SrampClientException, SrampAtomException {
		assertFeatureEnabled(type);
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("user".equals(type.getArtifactType().getModel()) && type.getUserType()!=null) {
				artifactType = type.getUserType();
			}
			String artifactUuid = uuid;
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			request.delete();
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Provides a very simple mechanism for querying.  Defaults many of the parameters.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public QueryResultSet query(String srampQuery) throws SrampClientException, SrampAtomException {
		return query(srampQuery, 0, 20, "name", true);
	}

	/**
	 * Executes the given s-ramp query xpath and returns a Feed of the matching artifacts.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @param startIndex which index within the result to start (0 indexed)
	 * @param count the size of the page of results to return
	 * @param orderBy the s-ramp property to use for sorting (name, uuid, createdOn, etc)
	 * @param ascending the direction of the sort
	 * @return an Atom {@link Feed}
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public QueryResultSet query(String srampQuery, int startIndex, int count, String orderBy, boolean ascending)
			throws SrampClientException, SrampAtomException {
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
			formData.addFormData("startIndex", String.valueOf(startIndex), MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("count", String.valueOf(count), MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("orderBy", orderBy, MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("ascending", String.valueOf(ascending), MediaType.TEXT_PLAIN_TYPE);

			request.body(MediaType.MULTIPART_FORM_DATA_TYPE, formData);
			ClientResponse<Feed> response = request.post(Feed.class);
			return new QueryResultSet(response.getEntity());
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Uploads an ontology to the S-RAMP repository.  This will only work if the S-RAMP
	 * repository supports the ontology collection, which is not a part of the S-RAMP 1.0
	 * specification.
	 * @param content
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public RDF uploadOntology(InputStream content) throws SrampClientException, SrampAtomException {
		assertFeatureEnabled("ontology");
		try {
			String atomUrl = String.format("%1$s/ontology", this.endpoint);
			ClientRequest request = new ClientRequest(atomUrl);
			request.body(MediaType.APPLICATION_RDF_XML_TYPE, content);

			ClientResponse<Entry> response = request.post(Entry.class);
			Entry entry = response.getEntity();
			RDF rdf = SrampAtomUtils.unwrap(entry, RDF.class);
			return rdf;
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Gets a list of all the ontologies currently installed in the S-RAMP repository.  This
	 * will only work if the S-RAMP repository supports the ontology collection, which is not
	 * a part of the S-RAMP 1.0 specification.
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public List<OntologySummary> getOntologies() throws SrampClientException, SrampAtomException {
		assertFeatureEnabled("ontology");
		try {
			String atomUrl = String.format("%1$s/ontology", this.endpoint);
			ClientRequest request = new ClientRequest(atomUrl);
			ClientResponse<Feed> response = request.get(Feed.class);
			Feed feed = response.getEntity();
			List<OntologySummary> rval = new ArrayList<OntologySummary>(feed.getEntries().size());
			for (Entry entry : feed.getEntries()) {
				OntologySummary summary = new OntologySummary(entry);
				rval.add(summary);
			}
			return rval;
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

	/**
	 * Deletes a single ontology by its UUID.  Note that this will only work if the S-RAMP
	 * repository supports the ontology collection, which is not a part of the S-RAMP 1.0
	 * specification.
	 * @param ontologyUuid
	 * @throws SrampClientException
	 * @throws SrampAtomException
	 */
	public void deleteOntology(String ontologyUuid) throws SrampClientException, SrampAtomException {
		assertFeatureEnabled("ontology");
		try {
			String atomUrl = String.format("%1$s/ontology/%2$s", this.endpoint, ontologyUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			request.delete();
		} catch (SrampAtomException e) {
			throw e;
		} catch (Throwable e) {
			throw new SrampClientException(e);
		}
	}

}
