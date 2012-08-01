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

import java.io.InputStream;
import java.net.URL;

import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.overlord.sramp.ArtifactType;

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
	 * @param artifactModel the artifact model (core, xsd, wsdl, etc)
	 * @param artifactType the artifact type (XmlDocument, XsdDocument, etc)
	 * @param artifactUuid the S-RAMP uuid of the artifact
	 * @return an Atom {@link Entry}
	 * @throws Exception 
	 */
	public Entry getFullArtifactEntry(String artifactModel, String artifactType, String artifactUuid) throws Exception {
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ApacheHttpClient4Executor.class.getClassLoader());
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint, artifactModel, artifactType, artifactUuid);
			ClientRequest request = new ClientRequest(atomUrl);
			ClientResponse<Entry> response = request.get(Entry.class);
			return response.getEntity();
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
	}
	
	/**
	 * Please see javadoc in {@link SrampAtomApiClient#getFullArtifactEntry(String, String, String)}.
	 */
	public Entry getFullArtifactEntry(ArtifactType artifactType, String artifactUuid) throws Exception {
		return getFullArtifactEntry(artifactType.getModel(), artifactType.name(), artifactUuid);
	}

	/**
	 * Gets the content for an artifact as an input stream.  The caller must close the resulting
	 * {@link InputStream} when done.
	 * @param artifactModel the artifact model (core, xsd, wsdl, etc)
	 * @param artifactType the artifact type (XmlDocument, XsdDocument, etc)
	 * @param artifactUuid the S-RAMP uuid of the artifact
	 * @return an {@link InputStream} to the S-RAMP artifact content
	 * @throws Exception 
	 */
	public InputStream getArtifactContent(String artifactModel, String artifactType, String artifactUuid) throws Exception {
		String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", this.endpoint, artifactModel, artifactType, artifactUuid);
		URL url = new URL(atomUrl);
		return url.openStream();
	}
	
	/**
	 * Please see javadoc in {@link SrampAtomApiClient#getArtifactContent(String, String, String)}.
	 */
	public InputStream getArtifactContent(ArtifactType artifactType, String artifactUuid) throws Exception {
		return getArtifactContent(artifactType.getModel(), artifactType.name(), artifactUuid);
	}

	/**
	 * Uploads an artifact to the s-ramp repository.
	 * @param artifactModel the new artifact's model
	 * @param artifactType the new artifact's type
	 * @param content the byte content of the artifact
	 * @param artifactFileName the file name of the artifact (optional, can be null)
	 * @return an Atom entry representing the new artifact in the s-ramp repository
	 * @throws Exception
	 */
	public Entry uploadArtifact(String artifactModel, String artifactType, InputStream content, String artifactFileName) throws Exception {
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ApacheHttpClient4Executor.class.getClassLoader());
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s", this.endpoint, artifactModel, artifactType);
			ClientRequest request = new ClientRequest(atomUrl);
			if (artifactFileName != null)
				request.header("Slug", artifactFileName);
			request.body(MediaType.APPLICATION_XML, content);

			ClientResponse<Entry> response = request.post(Entry.class);
			return response.getEntity();
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
	}
	
	/**
	 * Please refer to javadoc in  {@link SrampAtomApiClient#uploadArtifact(String, String, InputStream, String)}
	 */
	public Entry uploadArtifact(ArtifactType artifactType, InputStream content, String artifactFileName) throws Exception {
		return uploadArtifact(artifactType.getModel(), artifactType.name(), content, artifactFileName);
	}

	/**
	 * Executes the given s-ramp query xpath and returns a Feed of the matching artifacts.
	 * @param srampQuery the s-ramp query (xpath formatted)
	 * @return an Atom {@link Feed}
	 * @throws Exception
	 */
	public Feed query(String srampQuery, int page, int pageSize, String orderBy, boolean ascending) throws Exception {
		String xpath = srampQuery;
		if (xpath == null)
			throw new Exception("Please supply an S-RAMP x-path formatted query.");
		// Remove the leading /s-ramp/ prior to POSTing to the atom endpoint
		if (xpath.startsWith("/s-ramp/"))
			xpath = xpath.substring(8);
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ApacheHttpClient4Executor.class.getClassLoader());
		try {
			String atomUrl = this.endpoint;
			ClientRequest request = new ClientRequest(atomUrl);
			MultipartFormDataOutput formData = new MultipartFormDataOutput();
			formData.addFormData("query", srampQuery, MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("page", String.valueOf(page), MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("pageSize", String.valueOf(pageSize), MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("orderBy", orderBy, MediaType.TEXT_PLAIN_TYPE);
			formData.addFormData("ascending", String.valueOf(ascending), MediaType.TEXT_PLAIN_TYPE);
			
			request.body(MediaType.MULTIPART_FORM_DATA_TYPE, formData);
			ClientResponse<Feed> response = request.post(Feed.class);
			return response.getEntity();
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
	}
	
}
