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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;

/**
 * A simple S-RAMP client demo. This class strives to demonstrate how to use the S-RAMP client.
 *
 * @author eric.wittmann@redhat.com
 */
public class SimpleClientDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-server";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "overlord";
	private static final String[] FILES = { "ws-humantask.xsd", "ws-humantask-context.xsd",
			"ws-humantask-policy.xsd", "ws-humantask-types.xsd", "ws-humantask-leantask-api.wsdl",
			"ws-humantask-protocol.wsdl" };

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Simple Client Demo ***\n");

        String endpoint = System.getProperty("sramp.endpoint");
        String username = System.getProperty("sramp.auth.username");
        String password = System.getProperty("sramp.auth.password");
        if (endpoint == null || endpoint.trim().length() == 0) {
            endpoint = DEFAULT_ENDPOINT;
        }
        if (username == null || username.trim().length() == 0) {
            username = DEFAULT_USER;
        }
        if (password == null || password.trim().length() == 0) {
            password = DEFAULT_PASSWORD;
        }
        System.out.println("S-RAMP Endpoint: " + endpoint);
        System.out.println("S-RAMP User: " + username);
        SrampAtomApiClient client = new SrampAtomApiClient(endpoint, username, password, true);

        // Have we already run this demo?
        QueryResultSet rs = client.buildQuery("/s-ramp[@from-demo = ?]")
                .parameter(SimpleClientDemo.class.getSimpleName()).count(1).query();
        if (rs.size() > 0) {
            System.out.println("It looks like you already ran this demo!");
            System.out.println("I'm going to quit, because I don't want to clutter up");
            System.out.println("your repository with duplicate stuff.");
            System.exit(1);
        }

        // Upload some artifacts to the S-RAMP repository
        System.out.println("Uploading some XML schemas...");
		for (String file : FILES) {
			// Get an InputStream over the file content
			InputStream is = SimpleClientDemo.class.getResourceAsStream(file);

			try {
				// We need to know the artifact type
				ArtifactType type = ArtifactType.XsdDocument();
				if (file.endsWith(".wsdl")) {
					type = ArtifactType.WsdlDocument();
				}

				// Upload that content to S-RAMP
				System.out.print("\tUploading artifact " + file + "...");
				BaseArtifactType artifact = client.uploadArtifact(type, is, file);
				System.out.println("done.");

				// Update the artifact meta-data (set the version and add a custom property)
				artifact.setVersion("1.1");
	            SrampModelUtils.setCustomProperty(artifact, "from-demo", SimpleClientDemo.class.getSimpleName());

				// Tell the server about the updated meta-data
				System.out.print("\tUpdating meta-data for artifact " + file + "...");
				client.updateArtifactMetaData(artifact);
				System.out.println("done.");
			} finally {
				is.close();
			}
		}

		// Now query the S-RAMP repository (for the Schemas only)
		System.out.print("Querying the S-RAMP repository for Schemas...");
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument");
		System.out.println("success: " + rset.size() + " Schemas found:");
		for (ArtifactSummary entry : rset) {
			System.out.println("\t * " + entry.getName() + " (" + entry.getUuid() + ")");
		}

		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}
}
