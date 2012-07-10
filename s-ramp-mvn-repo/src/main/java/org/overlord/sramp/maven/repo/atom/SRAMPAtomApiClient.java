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
package org.overlord.sramp.maven.repo.atom;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.configuration.Configuration;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;

/**
 * The class used whenever an Atom API request for data needs to be made.
 *
 * @author eric.wittmann@redhat.com
 */
public class SRAMPAtomApiClient {

	private static SRAMPAtomApiClient sInstance;

	/**
	 * @param config
	 */
	public static void init(Configuration config) {
		sInstance = new SRAMPAtomApiClient(config);
	}
	
	/**
	 * Singleton accessor.
	 */
	public static SRAMPAtomApiClient getInstance() {
		if (sInstance == null)
			throw new RuntimeException("SRAMPAtomApiClient not yet initialized!");
		else
			return sInstance;
	}

	private String endpoint;

	/**
	 * Private singleton constructor.
	 * @param config 
	 */
	private SRAMPAtomApiClient(Configuration config) {
		this.endpoint = (String) config.getProperty("s-ramp.atom-api.endpoint");
	}

	/**
	 * Gets a {@link Feed} of Atom {@link Entry} objects for the given Artifact Model
	 * and Artifact Type.
	 * @param artifactModel the S-RAMP artifact model
	 * @param artifactType the S-RAMP artifact type
	 * @return an Atom Feed
	 * @throws Exception
	 */
	public Feed getFeed(String artifactModel, String artifactType) throws Exception {
		String atomUrl = String.format("%1$s/%2$s/%3$s", this.endpoint, artifactModel, artifactType);
		ClientRequest request = new ClientRequest(atomUrl);
		ClientResponse<Feed> response = request.get(Feed.class);
		return response.getEntity();
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
		String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s", this.endpoint, artifactModel, artifactType, artifactUuid);
		ClientRequest request = new ClientRequest(atomUrl);
		ClientResponse<Entry> response = request.get(Entry.class);
		return response.getEntity();
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
		String atomUrl = String.format("%1$s/%2$s/%3$s/%4$s/media", this.endpoint, artifactModel, artifactType, artifactUuid);
		URL url = new URL(atomUrl);
		return url.openStream();
	}


}
