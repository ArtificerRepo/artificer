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
package org.overlord.sramp.demos.simpleclient;

import java.io.InputStream;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.SrampModelUtils;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * A simple S-RAMP client demo. This class strives to demonstrate how to use the S-RAMP client.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleClientDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-atom/s-ramp";
	private static final String[] FILES = { "ws-humantask.xsd", "ws-humantask-context.xsd",
			"ws-humantask-policy.xsd", "ws-humantask-types.xsd", "ws-humantask-api.wsdl",
			"ws-humantask-leantask-api.wsdl", "ws-humantask-protocol.wsdl" };

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Simple Client Demo ***\n");

		// Figure out the endpoint of the S-RAMP repository's Atom API
		String endpoint = System.getProperty("sramp.endpoint");
		if (endpoint == null || endpoint.trim().length() == 0) {
			endpoint = DEFAULT_ENDPOINT;
		}
		System.out.println("S-RAMP Endpoint: " + endpoint);

		// Upload some artifacts to the S-RAMP repository
		System.out.println("Uploading some XML schemas...");
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);
		for (String file : FILES) {
			// Get an InputStream over the file content
			InputStream is = SimpleClientDemo.class.getResourceAsStream(file);

			// We need to know the artifact type
			ArtifactType type = ArtifactType.XsdDocument;
			if (file.endsWith(".wsdl")) {
				type = ArtifactType.WsdlDocument;
			}

			// Upload that content to S-RAMP
			System.out.print("\tUploading artifact " + file + "...");
			Entry newArtifact = client.uploadArtifact(type, is, file);
			System.out.println("done.");

			// Update the artifact meta-data (set the version and add a custom property)
			BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(newArtifact);
			artifact.setVersion("1.1");
			SrampModelUtils.setCustomProperty(artifact, "demo", "simple-client");

			// Tell the server about the updated meta-data
			System.out.print("\tUpdating meta-data for artifact " + file + "...");
			client.updateArtifactMetaData(artifact);
			System.out.println("done.");
		}

		// Now query the S-RAMP repository (for the Schemas only)
		System.out.print("Querying the S-RAMP repository for Schemas...");
		Feed feed = client.query("/s-ramp/xsd/XsdDocument");
		System.out.println("success: " + feed.getEntries().size() + " Schemas found:");
		for (Entry entry : feed.getEntries()) {
			System.out.println("\t * " + entry.getTitle() + " (" + entry.getId() + ")");
		}

		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}
}
