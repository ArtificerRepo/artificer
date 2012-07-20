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

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.plugins.providers.atom.Entry;

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
	 * Gets the content for an artifact as an input stream.
	 * @param artifactModel the artifact model (core, xsd, wsdl, etc)
	 * @param artifactType the artifact type (XmlDocument, XsdDocument, etc)
	 * @param artifactUuid the S-RAMP uuid of the artifact
	 * @return an {@link InputStream} to the S-RAMP artifact content
	 * @throws Exception 
	 */
	public InputStream getArtifactContent(String artifactModel, String artifactType, String artifactUuid) throws Exception {
		ClassLoader oldCtxCL = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(ApacheHttpClient4Executor.class.getClassLoader());
		try {
			String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", this.endpoint, artifactModel, artifactType, artifactUuid);
			URL url = new URL(atomUrl);
			return url.openStream();
		} finally {
			Thread.currentThread().setContextClassLoader(oldCtxCL);
		}
	}

}
