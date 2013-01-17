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
package org.overlord.sramp.demos.deriver;

import java.io.InputStream;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Demonstrates how to create a custom deriver for user-defined artifacts.  Note
 * that this demo assumes that you have compiled and installed the {@link WebXmlDeriver}
 * according to the instructions found in README.md.
 *
 * @author eric.wittmann@redhat.com
 */
public class CustomDeriverDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-server";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Customer Deriver Demo ***\n");

		// Figure out the endpoint of the S-RAMP repository's Atom API
		String endpoint = System.getProperty("sramp.endpoint");
		if (endpoint == null || endpoint.trim().length() == 0) {
			endpoint = DEFAULT_ENDPOINT;
		}
		System.out.println("S-RAMP Endpoint: " + endpoint);
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);

		// First, add the demo/sample web.xml to the repository.
		String webXmlArtifactUUID = addWebXmlToRepository(client);

		// Now, do some queries to make sure that the derived artifacts were properly
		// created and linked (via S-RAMP relationships) to the original artifact.
		queryForDerivedArtifacts(client, webXmlArtifactUUID);

		// All done.
		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

    /**
     * @param client
     * @throws Exception
     */
    private static String addWebXmlToRepository(SrampAtomApiClient client) throws Exception {
        System.out.println("Adding the sample 'web.xml' artifact to the repository.");
        InputStream webXmlIS = CustomDeriverDemo.class.getResourceAsStream("web.xml");
        ArtifactType artifactType = ArtifactType.valueOf("WebXmlDocument");
        BaseArtifactType artifact = client.uploadArtifact(artifactType, webXmlIS, "web.xml");
        System.out.println("Sample 'web.xml' artifact successfully added.");
        return artifact.getUuid();
    }

    /**
     * @param client
     * @param webXmlArtifactUUID
     * @throws Exception
     */
    private static void queryForDerivedArtifacts(SrampAtomApiClient client, String webXmlArtifactUUID) throws Exception {
        // Check that we can query for the source web.xml artifact
        System.out.println("Querying the repository to verify derived web.xml content.");
        String query = String.format("/s-ramp/user/WebXmlDocument[@uuid = '%1$s']", webXmlArtifactUUID);
        System.out.println("\t" + query);
        QueryResultSet resultSet = client.query(query);
        if (resultSet.size() != 1) {
            throw new Exception("Error querying for the original web.xml artifact!");
        }

        // Find all derived artifacts by querying by the relatedDocument relationship
        System.out.println("Querying the repository for all web.xml derived artifacts.");
        query = String.format("/s-ramp/user[relatedDocument[@uuid = '%1$s']]", webXmlArtifactUUID);
        System.out.println("\t" + query);
        resultSet = client.query(query);
        if (resultSet.size() != 12) {
            throw new Exception("Expected 12 derived artifacts but found only "  + resultSet.size() + "!");
        }
    }

}
