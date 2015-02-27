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
package org.artificer.client;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.artificer.atom.ArtificerAtomUtils;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.beans.HttpResponseBean;
import org.artificer.client.audit.AuditResultSet;
import org.artificer.client.auth.AuthenticationProvider;
import org.artificer.client.auth.BasicAuthenticationProvider;
import org.artificer.client.i18n.Messages;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerConstants;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.common.MediaType;
import org.artificer.common.error.ArtificerServerException;
import org.jboss.downloads.artificer._2013.auditing.AuditEntry;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Class used to communicate with the S-RAMP server via the S-RAMP Atom API.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerAtomApiClient {

	private String endpoint;
    private String srampEndpoint;
    private String artificerEndpoint;
	private boolean validating;
	private Set<String> enabledFeatures = new HashSet<String>();
	private Locale locale;

    private AuthenticationProvider authProvider;

	/**
	 * Constructor.
	 * @param endpoint
	 */
	public ArtificerAtomApiClient(String endpoint) {
		this.endpoint = endpoint;
		if (this.endpoint.endsWith("/")) {
			this.endpoint = this.endpoint.substring(0, this.endpoint.length() - 1);
		}
        if (this.endpoint.endsWith("/s-ramp")) {
            this.endpoint = this.endpoint.substring(0, this.endpoint.length() - 7);
        }
        if (this.endpoint.endsWith("/artificer")) {
            this.endpoint = this.endpoint.substring(0, this.endpoint.length() - 10);
        }

        srampEndpoint = endpoint + "/s-ramp";
        artificerEndpoint = endpoint + "/artificer";
	}

	/**
	 * Constructor.
	 * @param endpoint
	 * @param validating
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public ArtificerAtomApiClient(String endpoint, boolean validating) throws ArtificerClientException, ArtificerServerException {
		this(endpoint);
		this.validating = validating;
		if (this.validating) {
			discoverAvailableFeatures();
		}
	}

    /**
     * Constructor.
     * @param endpoint
     * @param username
     * @param password
     * @param validating
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public ArtificerAtomApiClient(final String endpoint, final String username, final String password,
								  final boolean validating) throws ArtificerClientException, ArtificerServerException {
        this(endpoint, new BasicAuthenticationProvider(username, password), validating);
    }

    /**
     * Constructor.
     * @param endpoint
     * @param authenticationProvider
     * @param validating
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public ArtificerAtomApiClient(final String endpoint, AuthenticationProvider authenticationProvider,
								  final boolean validating) throws ArtificerClientException, ArtificerServerException {
        this(endpoint);
        this.authProvider = authenticationProvider;
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
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	private void discoverAvailableFeatures() throws ArtificerClientException, ArtificerServerException {
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
	 * @throws ArtificerClientException
	 */
	private void assertFeatureEnabled(String feature) throws ArtificerClientException {
		if (this.validating) {
			if (!this.enabledFeatures.contains(feature)) {
				throw new ArtificerClientException(Messages.i18n.format("FEATURE_NOT_SUPPORTED"));
			}
		}
	}

	/**
	 * Asserts that the given feature is enabled.  If it is not, then an exception
	 * is thrown.  If this client is not set to validating, then this method will
	 * always pass.
	 * @param feature
	 * @throws ArtificerClientException
	 */
	private void assertFeatureEnabled(ArtifactType feature) throws ArtificerClientException {
		if (this.validating) {
			if (!this.enabledFeatures.contains(feature.getArtifactType().getType())) {
                throw new ArtificerClientException(Messages.i18n.format("FEATURE_NOT_SUPPORTED"));
			}
		}
	}

	/**
	 * Gets the S-RAMP service document.
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public AppService getServiceDocument() throws ArtificerClientException, ArtificerServerException {
	    ClientResponse<AppService> response = null;
		try {
			String atomUrl = String.format("%1$s/servicedocument", srampEndpoint);
			ClientRequest request = createClientRequest(atomUrl);
			response = request.get(AppService.class);
			return response.getEntity();
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
		} finally {
		    closeQuietly(response);
		}
	}

    /**
     * Gets the full meta-data listing for an Artifact in the S-RAMP repository.  This method
     * does not require the type of artifact.  However, it should be noted that if you
     * <b>have</b> the artifact type, you should instead call:
     *
     *   {@link ArtificerAtomApiClient#getArtifactMetaData(ArtifactType, String)}
     *
     * Use this variant only if you don't know the artifact type (you only know the UUID).
     * The reason is that the client must first do a query to determine the artifact type
     * and then make another call to fetch the meta data.
     *
     * @param artifactUuid
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public BaseArtifactType getArtifactMetaData(String artifactUuid) throws ArtificerClientException,
			ArtificerServerException {
        try {
            QueryResultSet uuidRS = buildQuery("/s-ramp[@uuid = ?]").parameter(artifactUuid).count(1).query();
            if (uuidRS.size() == 0)
                throw new ArtificerClientException(Messages.i18n.format("ARTIFACT_NOT_FOUND", artifactUuid));
            ArtifactType artifactType = uuidRS.iterator().next().getType();
            return getArtifactMetaData(artifactType, artifactUuid);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        }
    }

    /**
     * Convenience method to get the artifact meta-data given an artifact summary (which are typically
     * returned when performing s-ramp queries).
     * @param artifact
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public BaseArtifactType getArtifactMetaData(ArtifactSummary artifact) throws ArtificerClientException,
			ArtificerServerException {
        return getArtifactMetaData(artifact.getType(), artifact.getUuid());
    }

	/**
	 * Gets the full meta-data listing for an Artifact in the S-RAMP repository.
	 * @param artifactType
	 * @param artifactUuid
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public BaseArtifactType getArtifactMetaData(ArtifactType artifactType, String artifactUuid)
			throws ArtificerClientException, ArtificerServerException {
		assertFeatureEnabled(artifactType);
		ClientResponse<Entry> response = null;
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", srampEndpoint,
					artifactType.getArtifactType().getModel(), artifactType.getArtifactType().getType(),
					artifactUuid);
			ClientRequest request = createClientRequest(atomUrl);
			response = request.get(Entry.class);
			Entry entry = response.getEntity();
			return ArtificerAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
		} finally {
		    closeQuietly(response);
		}
	}

	/**
	 * Gets the content for an artifact as an input stream.  The caller must close the resulting
	 * @param artifactType the artifact type
	 * @param artifactUuid the S-RAMP uuid of the artifact
	 * @return an {@link InputStream} to the S-RAMP artifact content
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public InputStream getArtifactContent(ArtifactType artifactType, String artifactUuid)
			throws ArtificerClientException, ArtificerServerException {
		assertFeatureEnabled(artifactType);
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", srampEndpoint,
					artifactType.getArtifactType().getModel(), artifactType.getArtifactType().getType(),
					artifactUuid);

	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        if (this.authProvider != null) {
	            httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
	                @Override
	                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
	                    authProvider.provideAuthentication(request);
	                }
	            });
	        }
	        HttpResponse response = httpClient.execute(new HttpGet(atomUrl));
	        HttpEntity entity = response.getEntity();
	        return entity.getContent();
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
		}
	}

    /**
     * Convenience method for getting the artifact content given an artifact summary (which are typically
     * returned when performing s-ramp queries).
     * @param artifact
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public InputStream getArtifactContent(ArtifactSummary artifact) throws ArtificerClientException, ArtificerServerException {
        return getArtifactContent(artifact.getType(), artifact.getUuid());
    }


    /**
     * Creates a new artifact in the S-RAMP repository.  Use this method when creating
     * logical artifacts in the repository (i.e. artifacts without document content).
     * @param artifact
     */
    public BaseArtifactType createArtifact(BaseArtifactType artifact) throws ArtificerClientException, ArtificerServerException {
        ArtifactType artifactType = ArtifactType.valueOf(artifact);
        if (ArtificerModelUtils.isDocumentArtifact(artifact)) {
            throw new ArtificerClientException(Messages.i18n.format("MISSING_ARTIFACT_CONTEN"));
        }

        assertFeatureEnabled(artifactType);
        ClientResponse<Entry> response = null;
        try {
            String type = artifactType.getType();
            String atomUrl = String.format("%1$s/%2$s/%3$s", srampEndpoint,
                    artifactType.getModel(), type);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, ArtificerAtomUtils.wrapSrampArtifact(artifact));

            response = request.post(Entry.class);
            Entry entry = response.getEntity();
            return ArtificerAtomUtils.unwrapSrampArtifact(artifactType, entry);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

	/**
	 * Creates a new artifact in the repository by uploading a document.  The document will
	 * become the core of a new S-RAMP artifact.
	 * @param artifactType
	 * @param content
	 * @param artifactFileName
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public BaseArtifactType uploadArtifact(ArtifactType artifactType, InputStream content, String artifactFileName)
			throws ArtificerClientException, ArtificerServerException {
        if (artifactType == null) {
            return uploadArtifact(content, artifactFileName);
        }

		assertFeatureEnabled(artifactType);
        ClientResponse<Entry> response = null;
		try {
			String type = artifactType.getType();
			String atomUrl = String.format("%1$s/%2$s/%3$s", srampEndpoint,
					artifactType.getArtifactType().getModel(), type);
			ClientRequest request = createClientRequest(atomUrl);
			if (artifactFileName != null)
				request.header("Slug", artifactFileName);
			request.body(artifactType.getMimeType(), content);

			response = request.post(Entry.class);
			Entry entry = response.getEntity();
			return ArtificerAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
		}
	}

    /**
     * Creates a new artifact in the repository by uploading a document.  The document will
     * become the core of a new S-RAMP artifact.
     *
     * @param content
     * @param artifactFileName
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public BaseArtifactType uploadArtifact(InputStream content, String artifactFileName)
            throws ArtificerClientException, ArtificerServerException {
        ClientResponse<Entry> response = null;
        try {
            ClientRequest request = createClientRequest(srampEndpoint + "/autodetect");
            request.header("Slug", artifactFileName);
            request.body("application/octet-stream", content);

            response = request.post(Entry.class);
            Entry entry = response.getEntity();
            return ArtificerAtomUtils.unwrapSrampArtifact(entry);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

	/**
     * Creates a new artifact in the repository by uploading a document.  The document will
     * become the core of a new S-RAMP artifact.
	 * @param baseArtifactType
	 * @param content
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public BaseArtifactType uploadArtifact(BaseArtifactType baseArtifactType, InputStream content)
			throws ArtificerClientException, ArtificerServerException {
		ArtifactType artifactType = ArtifactType.valueOf(baseArtifactType);
		assertFeatureEnabled(artifactType);
        ClientResponse<Entry> response = null;
		try {
			String type = artifactType.getType();
			String atomUrl = String.format("%1$s/%2$s/%3$s", srampEndpoint,
                    artifactType.getArtifactType().getModel(), type);
			ClientRequest request = createClientRequest(atomUrl);

			MultipartRelatedOutput output = new MultipartRelatedOutput();

			//1. Add first part, the S-RAMP entry
			Entry atomEntry = ArtificerAtomUtils.wrapSrampArtifact(baseArtifactType);

			MediaType mediaType = new MediaType("application", "atom+xml");
			output.addPart(atomEntry, mediaType);

			//2. Add second part, the content
			request.body(artifactType.getMimeType(), content);
			MediaType mediaType2 = MediaType.getInstance(artifactType.getMimeType());
			output.addPart(content, mediaType2);

			//3. Send the request
			request.body(MultipartConstants.MULTIPART_RELATED, output);
			response = request.post(Entry.class);
			Entry entry = response.getEntity();
			return ArtificerAtomUtils.unwrapSrampArtifact(artifactType, entry);
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
		}
	}

	/**
	 * Performs a batch operation by uploading an s-ramp package archive to the s-ramp server
	 * for processing.  The contents of the s-ramp archive will be processed, and the results
	 * will be returned as a Map.  The Map is indexed by the S-RAMP Archive entry path, and each
	 * each value in the Map will either be a {@link BaseArtifactType} or an
	 * {@link ArtificerServerException}, depending on success vs. failure of that entry.
	 *
	 * @param archive the s-ramp package archive to upload
	 * @return the collection of results (one per entry in the s-ramp package)
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public Map<String, ?> uploadBatch(ArtificerArchive archive) throws ArtificerClientException, ArtificerServerException {
		File packageFile = null;
		InputStream packageStream = null;

        ClientResponse<MultipartInput> clientResponse = null;
		try {
	        if (archive.getEntries().isEmpty()) {
	            return new HashMap<String, Object>();
	        }

	        packageFile = archive.pack();
			packageStream = FileUtils.openInputStream(packageFile);
			ClientRequest request = createClientRequest(srampEndpoint);
			request.header("Content-Type", "application/zip");
			request.body(MediaType.APPLICATION_ZIP, packageStream);

			clientResponse = request.post(MultipartInput.class);
			MultipartInput response = clientResponse.getEntity();
			List<InputPart> parts = response.getParts();

			Map<String, Object> rval = new HashMap<String, Object>(parts.size());
			for (InputPart part : parts) {
				String contentId = part.getHeaders().getFirst("Content-ID");
				String path = contentId.substring(1, contentId.lastIndexOf('@'));
				HttpResponseBean rbean = part.getBody(HttpResponseBean.class, null);
				if (rbean.getCode() == 201) {
					Entry entry = (Entry) rbean.getBody();
					BaseArtifactType artifact = ArtificerAtomUtils.unwrapSrampArtifact(entry);
					rval.put(path, artifact);
				} else if (rbean.getCode() == 409) {
					if (MediaType.APPLICATION_ARTIFICER_SERVER_EXCEPTION.equals(rbean.getHeaders().get("Content-Type"))) {
						ArtificerServerException exception = (ArtificerServerException) rbean.getBody();
						rval.put(path, exception);
					} else {
						String errorReason = (String) rbean.getBody();
						ArtificerServerException exception = new ArtificerServerException(errorReason);
						rval.put(path, exception);
					}
				} else {
					// Only a non-compliant s-ramp impl could cause this
					ArtificerServerException exception = new ArtificerServerException(Messages.i18n.format("BAD_RETURN_CODE", rbean.getCode(), contentId)); 
					rval.put(path, exception);
				}
			}
			return rval;
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
		} finally {
			IOUtils.closeQuietly(packageStream);
			FileUtils.deleteQuietly(packageFile);
            closeQuietly(clientResponse);
		}
	}

	/**
	 * Called to update the meta-data stored in the s-ramp repository for the given s-ramp
	 * artifact.
	 * @param artifact
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public void updateArtifactMetaData(BaseArtifactType artifact) throws ArtificerClientException, ArtificerServerException {
		ArtifactType type = ArtifactType.valueOf(artifact);
		assertFeatureEnabled(type);
        ClientResponse<?> response = null;
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("ext".equals(type.getArtifactType().getModel()) && type.getExtendedType()!=null) {
				artifactType = type.getExtendedType();
			}
			String artifactUuid = artifact.getUuid();
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", srampEndpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = createClientRequest(atomUrl);

			Entry entry = ArtificerAtomUtils.wrapSrampArtifact(artifact);

			request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, entry);
			response = request.put();
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
		}
	}

	/**
	 * Updates the content of the artifact.
	 * @param artifact
	 * @param content
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public void updateArtifactContent(BaseArtifactType artifact, InputStream content)
			throws ArtificerClientException, ArtificerServerException {
		ArtifactType type = ArtifactType.valueOf(artifact);
		assertFeatureEnabled(type);
        ClientResponse<?> response = null;
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("ext".equals(type.getArtifactType().getModel()) && type.getExtendedType()!=null) {
				artifactType = type.getExtendedType();
			}
			String artifactUuid = artifact.getUuid();
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", srampEndpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = createClientRequest(atomUrl);
			request.body(type.getMimeType(), content);
			response = request.put();
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
		}
	}

	/**
	 * Delets an artifact from the s-ramp repository.
	 * @param uuid
	 * @param type
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public void deleteArtifact(String uuid, ArtifactType type) throws ArtificerClientException, ArtificerServerException {
		assertFeatureEnabled(type);
        ClientResponse<?> response = null;
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("ext".equals(type.getArtifactType().getModel()) && type.getExtendedType()!=null) {
				artifactType = type.getExtendedType();
			}
			String artifactUuid = uuid;
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", srampEndpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = createClientRequest(atomUrl);
			response = request.delete();
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
		}
	}

	/**
	 * Deletes an artifact's content from the s-ramp repository.
	 * @param uuid
	 * @param type
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public void deleteArtifactContent(String uuid, ArtifactType type) throws ArtificerClientException, ArtificerServerException {
		assertFeatureEnabled(type);
		ClientResponse<?> response = null;
		try {
			String artifactModel = type.getArtifactType().getModel();
			String artifactType = type.getArtifactType().getType();
			if ("ext".equals(type.getArtifactType().getModel()) && type.getExtendedType()!=null) {
				artifactType = type.getExtendedType();
			}
			String artifactUuid = uuid;
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", srampEndpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = createClientRequest(atomUrl);
			response = request.delete();
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
		} finally {
			closeQuietly(response);
		}
	}

	/**
	 * Provides a very simple mechanism for querying.  Defaults many of the parameters.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public QueryResultSet query(String srampQuery) throws ArtificerClientException, ArtificerServerException {
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
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public QueryResultSet query(String srampQuery, int startIndex, int count, String orderBy,
            boolean ascending) throws ArtificerClientException, ArtificerServerException {
        return query(srampQuery, startIndex, count, orderBy, ascending, null);
    }

	/**
	 * Executes the given s-ramp query xpath and returns a Feed of the matching artifacts.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @param startIndex which index within the result to start (0 indexed)
	 * @param count the size of the page of results to return
	 * @param orderBy the s-ramp property to use for sorting (name, uuid, createdOn, etc)
	 * @param ascending the direction of the sort
	 * @param propertyNames an optional collection of names of custom s-ramp properties to be returned as part of the result set
	 * @return an Atom {@link Feed}
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
	public QueryResultSet query(String srampQuery, int startIndex, int count, String orderBy, boolean ascending,
	        Collection<String> propertyNames) throws ArtificerClientException, ArtificerServerException {
        ClientResponse<Feed> response = null;
		try {
			String xpath = srampQuery;
			if (xpath == null)
				throw new Exception(Messages.i18n.format("INVALID_QUERY_FORMAT"));
			// Remove the leading /s-ramp/ prior to POSTing to the atom endpoint
			if (xpath.startsWith("/s-ramp/"))
				xpath = xpath.substring(8);
			String atomUrl = srampEndpoint;

			// Do a GET if multiple propertyNames are provided.  We would like to always
			// do a POST but the RESTEasy client doesn't seem to have a way to send multiple
			// values for a single multipart/form-data part name.
			if (propertyNames == null || propertyNames.size() < 2) {
    			ClientRequest request = createClientRequest(atomUrl);
    			MultipartFormDataOutput formData = new MultipartFormDataOutput();
    			formData.addFormData("query", xpath, MediaType.TEXT_PLAIN_TYPE);
    			formData.addFormData("startIndex", String.valueOf(startIndex), MediaType.TEXT_PLAIN_TYPE);
    			formData.addFormData("count", String.valueOf(count), MediaType.TEXT_PLAIN_TYPE);
    			formData.addFormData("orderBy", orderBy, MediaType.TEXT_PLAIN_TYPE);
    			formData.addFormData("ascending", String.valueOf(ascending), MediaType.TEXT_PLAIN_TYPE);
    			if (propertyNames != null) {
    			    for (String propertyName : propertyNames) {
                        formData.addFormData("propertyName", propertyName, MediaType.TEXT_PLAIN_TYPE);
                    }
    			}

    			request.body(MediaType.MULTIPART_FORM_DATA_TYPE, formData);
    			response = request.post(Feed.class);
    			return new QueryResultSet(response.getEntity());
			} else {
			    StringBuilder urlBuilder = new StringBuilder();
			    urlBuilder.append(atomUrl);
                urlBuilder.append("?query=");
                urlBuilder.append(URLEncoder.encode(srampQuery, "UTF8"));
                urlBuilder.append("&startIndex=");
                urlBuilder.append(String.valueOf(startIndex));
                urlBuilder.append("&count=");
                urlBuilder.append(String.valueOf(count));
                urlBuilder.append("&orderBy=");
                urlBuilder.append(URLEncoder.encode(orderBy, "UTF8"));
                urlBuilder.append("&ascending=");
                urlBuilder.append(String.valueOf(ascending));
                for (String propName : propertyNames) {
                    urlBuilder.append("&propertyName=");
                    urlBuilder.append(URLEncoder.encode(propName, "UTF8"));
                }
                ClientRequest request = createClientRequest(urlBuilder.toString());
                response = request.get(Feed.class);
                return new QueryResultSet(response.getEntity());
			}
		} catch (ArtificerServerException e) {
			throw e;
		} catch (Throwable e) {
			throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
		}
	}

	/**
	 * Build a query that can be parameterized and then executed.  The format
	 * of the query can either be a complete valid query or a query with JDBC style
	 * parameters (using the ? placeholder for parameters).
	 *
	 * Additionally, the start index, count, order-by, ascending, and extra propertyNames
	 * can all be set after calling this method.
	 *
	 * <code>
	 *   String uuid = ...
	 *   client.buildQuery("/s-ramp/core/Document[@uuid = ?]")
	 *         .parameter(uuid)
	 *         .startIndex(3)
	 *         .count(20)
	 *         .orderBy("name")
	 *         .ascending()
	 *         .propertyName("custom-prop-1")
	 *         .propertyName("custom-prop-2")
	 *         .query();
	 * </code>
	 *
	 * @param query
	 * @return a client query object
	 */
	public ArtificerClientQuery buildQuery(String query) {
	    return new ArtificerClientQuery(this, query);
	}
    
    /**
     * Adds on ontology in RDF format to the S-RAMP repository.  This will only work if the S-RAMP
     * repository supports the ontology collection, which is not a part of the S-RAMP 1.0
     * specification.
     * @param ontology
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public RDF addOntology(RDF ontology) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology", srampEndpoint);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_RDF_XML_TYPE, ontology);

            response = request.post(Entry.class);
            Entry entry = response.getEntity();
            RDF rdf = ArtificerAtomUtils.unwrap(entry, RDF.class);
            rdf.getOtherAttributes().put(new QName(ArtificerConstants.SRAMP_NS, "uuid"), entry.getId().toString().replace("urn:uuid:", ""));
            return rdf;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Uploads an ontology to the S-RAMP repository.  This will only work if the S-RAMP
     * repository supports the ontology collection, which is not a part of the S-RAMP 1.0
     * specification.
     * @param content
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public RDF uploadOntology(InputStream content) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology", srampEndpoint);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_RDF_XML_TYPE, content);

            response = request.post(Entry.class);
            Entry entry = response.getEntity();
            RDF rdf = ArtificerAtomUtils.unwrap(entry, RDF.class);
            rdf.getOtherAttributes().put(new QName(ArtificerConstants.SRAMP_NS, "uuid"), entry.getId().toString().replace("urn:uuid:", ""));
            return rdf;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Uploads a new version of an ontology to the S-RAMP repository.  The ontology will be
     * replaced with this new version.  This may fail if the new version removes classes from
     * the ontology that are currently in-use.
     * @param ontologyUuid
     * @param content
     */
    public void updateOntology(String ontologyUuid, InputStream content) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<?> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology/%2$s", srampEndpoint, ontologyUuid);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_RDF_XML_TYPE, content);
            response = request.put();
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Uploads a new version of an ontology to the S-RAMP repository.  The ontology will be
     * replaced with this new version.  This may fail if the new version removes classes from
     * the ontology that are currently in-use.
     * @param ontologyUuid
     */
    public void updateOntology(String ontologyUuid, RDF ontology) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<?> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology/%2$s", srampEndpoint, ontologyUuid);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_RDF_XML_TYPE, ontology);
            response = request.put();
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets a list of all the ontologies currently installed in the S-RAMP repository.  This
     * will only work if the S-RAMP repository supports the ontology collection, which is not
     * a part of the S-RAMP 1.0 specification.
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public List<OntologySummary> getOntologies() throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology", srampEndpoint);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Feed.class);
            Feed feed = response.getEntity();
            List<OntologySummary> rval = new ArrayList<OntologySummary>(feed.getEntries().size());
            for (Entry entry : feed.getEntries()) {
                OntologySummary summary = new OntologySummary(entry);
                rval.add(summary);
            }
            return rval;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets a single ontology by UUID.  This returns all of the ontology meta-data
     * as well as all of the classes.
     * @param ontologyUuid
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public RDF getOntology(String ontologyUuid) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology/%2$s", srampEndpoint, ontologyUuid);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Entry.class);
            Entry entry = response.getEntity();
            RDF rdf = ArtificerAtomUtils.unwrapRDF(entry);
            rdf.getOtherAttributes().put(new QName(ArtificerConstants.SRAMP_NS, "uuid"), entry.getId().toString().replace("urn:uuid:", ""));
            return rdf;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Deletes a single ontology by its UUID.  Note that this will only work if the S-RAMP
     * repository supports the ontology collection, which is not a part of the S-RAMP 1.0
     * specification.
     * @param ontologyUuid
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public void deleteOntology(String ontologyUuid) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("ontology");
        ClientResponse<?> response = null;
        try {
            String atomUrl = String.format("%1$s/ontology/%2$s", srampEndpoint, ontologyUuid);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.delete();
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }
    
    /**
     * Adds a stored query to the S-RAMP repository.
     * 
     * @param storedQuery
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public StoredQuery createStoredQuery(StoredQuery storedQuery) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("query");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/query", srampEndpoint);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, ArtificerAtomUtils.wrapStoredQuery(storedQuery));
                    
            response = request.post(Entry.class);
            Entry entry = response.getEntity();
            return ArtificerAtomUtils.unwrapStoredQuery(entry);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Uploads a new version of a stored query to the S-RAMP repository.  The stored query will be
     * replaced with this new version.
     * 
     * @param queryName
     * @param storedQuery
     */
    public void updateStoredQuery(String queryName, StoredQuery storedQuery) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("query");
        ClientResponse<?> response = null;
        try {
            String atomUrl = String.format("%1$s/query/%2$s", srampEndpoint, queryName);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_ATOM_XML_ENTRY, ArtificerAtomUtils.wrapStoredQuery(storedQuery));
            response = request.put();
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets a list of all the stored queries currently installed in the S-RAMP repository.
     * 
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public List<StoredQuery> getStoredQueries() throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("query");
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/query", srampEndpoint);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Feed.class);
            Feed feed = response.getEntity();
            List<StoredQuery> storedQueries = new ArrayList<StoredQuery>(feed.getEntries().size());
            for (Entry entry : feed.getEntries()) {
                storedQueries.add(ArtificerAtomUtils.unwrapStoredQuery(entry));
            }
            return storedQueries;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets a single stored query by name.
     * 
     * @param queryName
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public StoredQuery getStoredQuery(String queryName) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("query");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/query/%2$s", srampEndpoint, queryName);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Entry.class);
            Entry entry = response.getEntity();
            return ArtificerAtomUtils.unwrapStoredQuery(entry);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Deletes a single stored query by name.
     * 
     * @param queryName
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public void deleteStoredQuery(String queryName) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("query");
        ClientResponse<?> response = null;
        try {
            String atomUrl = String.format("%1$s/query/%2$s", srampEndpoint, queryName);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.delete();
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }
    
    /**
     * See {@link #query(String)}
     * 
     * @param queryName
     * @return QueryResultSet
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public QueryResultSet queryWithStoredQuery(String queryName) throws ArtificerClientException, ArtificerServerException {
        return queryWithStoredQuery(queryName, 0, 20, "name", true);
    }

    /**
     * See {@link #query(String, int, int, String, boolean, Collection)}.
     * Note that {@link StoredQuery#getPropertyName()} is automatically given to #query.
     * 
     * @param startIndex
     * @param count
     * @param orderBy
     * @param ascending
     * @return SrampClientQuery
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public QueryResultSet queryWithStoredQuery(String queryName, int startIndex, int count, String orderBy,
            boolean ascending) throws ArtificerClientException, ArtificerServerException {
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/query/%2$s/results", srampEndpoint, queryName);

            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(atomUrl);
            urlBuilder.append("?startIndex=");
            urlBuilder.append(String.valueOf(startIndex));
            urlBuilder.append("&count=");
            urlBuilder.append(String.valueOf(count));
            urlBuilder.append("&orderBy=");
            urlBuilder.append(URLEncoder.encode(orderBy, "UTF8"));
            urlBuilder.append("&ascending=");
            urlBuilder.append(String.valueOf(ascending));
            
            ClientRequest request = createClientRequest(urlBuilder.toString());
            response = request.get(Feed.class);
            return new QueryResultSet(response.getEntity());
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * See {@link #buildQuery(String)}
     * 
     * @param storedQuery
     * @return SrampClientQuery
     */
    public ArtificerClientQuery buildQueryWithStoredQuery(StoredQuery storedQuery) {
        return buildQuery(storedQuery.getQueryExpression());
    }

	/**
	 * Adds a new audit entry on the artifact with the given UUID.
	 * @param artifactUuid
	 * @param auditEntry
	 * @throws ArtificerClientException
	 * @throws ArtificerServerException
	 */
    public AuditEntry addAuditEntry(String artifactUuid, AuditEntry auditEntry) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("audit");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/audit/artifact/%2$s", srampEndpoint, artifactUuid);
            ClientRequest request = createClientRequest(atomUrl);
            request.body(MediaType.APPLICATION_AUDIT_ENTRY_XML_TYPE, auditEntry);

            response = request.post(Entry.class);
            Entry entry = response.getEntity();
            if (entry == null)
                throw new ArtificerServerException(Messages.i18n.format("AUDIT_ENTRY_ADD_FAILED"));
            return ArtificerAtomUtils.unwrap(entry, AuditEntry.class);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets the audit trail for the artifact with the given UUID.
     * @param artifactUuid
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public AuditResultSet getAuditTrailForArtifact(String artifactUuid) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("audit");
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/audit/artifact/%2$s", srampEndpoint, artifactUuid);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Feed.class);
            Feed feed = response.getEntity();
            AuditResultSet rs = new AuditResultSet(feed);
            return rs;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets the audit trail for the artifact with the given UUID.
     * @param artifactUuid
     * @param startIndex
     * @param count
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public AuditResultSet getAuditTrailForArtifact(String artifactUuid, int startIndex, int count) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("audit");
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/audit/artifact/%2$s?startIndex=%3$s&count=%4$s",
                    srampEndpoint, artifactUuid, String.valueOf(startIndex), String.valueOf(count));
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Feed.class);
            Feed feed = response.getEntity();
            AuditResultSet rs = new AuditResultSet(feed);
            return rs;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets the audit trail for the artifact with the given UUID.
     * @param username
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public AuditResultSet getAuditTrailForUser(String username) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("audit");
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/audit/user/%2$s", srampEndpoint, username);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Feed.class);
            Feed feed = response.getEntity();
            AuditResultSet rs = new AuditResultSet(feed);
            return rs;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets the audit trail for the artifact with the given UUID.
     * @param username
     * @param startIndex
     * @param count
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public AuditResultSet getAuditTrailForUser(String username, int startIndex, int count) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("audit");
        ClientResponse<Feed> response = null;
        try {
            String atomUrl = String.format("%1$s/audit/user/%2$s?startIndex=%3$s&count=%4$s",
                    srampEndpoint, username, String.valueOf(startIndex), String.valueOf(count));
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Feed.class);
            Feed feed = response.getEntity();
            AuditResultSet rs = new AuditResultSet(feed);
            return rs;
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Gets the full audit entry for the given artifact + audit event pair.
     * @param artifactUuid
     * @param auditEntryUuid
     * @throws ArtificerClientException
     * @throws ArtificerServerException
     */
    public AuditEntry getAuditEntry(String artifactUuid, String auditEntryUuid) throws ArtificerClientException, ArtificerServerException {
        assertFeatureEnabled("audit");
        ClientResponse<Entry> response = null;
        try {
            String atomUrl = String.format("%1$s/audit/artifact/%2$s/%3$s", srampEndpoint, artifactUuid, auditEntryUuid);
            ClientRequest request = createClientRequest(atomUrl);
            response = request.get(Entry.class);
            Entry entry = response.getEntity();
            return ArtificerAtomUtils.unwrapAuditEntry(entry);
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Creates the RESTEasy client request object, configured appropriately.
     * @param atomUrl
     */
    protected ClientRequest createClientRequest(String atomUrl) {
        ClientExecutor executor = createClientExecutor();
        ClientRequest request = new ClientRequest(atomUrl, executor);
        return request;
    }

    /**
     * Creates the client executor that will be used by RESTEasy when
     * making the request.
     */
    private ClientExecutor createClientExecutor() {
        // TODO I think the http client is thread safe - so let's try to create just one of these
        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                Locale l = getLocale();
                if (l == null) {
                    l = Locale.getDefault();
                }
                request.addHeader("Accept-Language", l.toString());
            }
        });
        if (this.authProvider != null) {
            httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
                @Override
                public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                    authProvider.provideAuthentication(request);
                }
            });
        }
        return new ApacheHttpClient4Executor(httpClient);
    }

    /**
     * @return the locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale the locale to set
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public QueryResultSet reverseRelationships(String uuid) throws ArtificerClientException, ArtificerServerException {
        ClientResponse<Feed> response = null;
        try {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(artificerEndpoint);
            urlBuilder.append("/reverseRelationship/");
            urlBuilder.append(uuid);
            ClientRequest request = createClientRequest(urlBuilder.toString());
            response = request.get(Feed.class);
            return new QueryResultSet(response.getEntity());
        } catch (ArtificerServerException e) {
            throw e;
        } catch (Throwable e) {
            throw new ArtificerClientException(e);
        } finally {
            closeQuietly(response);
        }
    }

    /**
     * Quietly closes the resteasy client (releases the connection).
     * @param response
     */
    private void closeQuietly(ClientResponse<?> response) {
        if (response != null) {
            response.releaseConnection();
        }
    }

}
