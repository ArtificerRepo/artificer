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
package org.overlord.sramp.demos.relationships;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;

/**
 * Demonstrates how user defined relationships between artifacts can be
 * established, and how to query for artifacts based on those relationships.
 * For a demonstration about derived artifacts and their relationships,
 * please see the 's-ramp-demos-derived-artifacts' demo.
 *
 * @author eric.wittmann@redhat.com
 */
public class RelationshipDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-server";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "overlord";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Query Demo ***\n");

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
                .parameter(RelationshipDemo.class.getSimpleName()).count(1).query();
        if (rs.size() > 0) {
            System.out.println("It looks like you already ran this demo!");
            System.out.println("I'm going to quit, because I don't want to clutter up");
            System.out.println("your repository with duplicate stuff.");
            System.exit(1);
        }

		// First thing to do is add a few artifacts to the S-RAMP repo.
		ArtifactType type = ArtifactType.valueOf("Document");
		System.out.print("Uploading three artifacts...");
		BaseArtifactType artifact1 = client.uploadArtifact(type, RelationshipDemo.class.getResourceAsStream("relationship-demo-doc-1.txt"), "relationship-document-1.txt");
		BaseArtifactType artifact2 = client.uploadArtifact(type, RelationshipDemo.class.getResourceAsStream("relationship-demo-doc-2.txt"), "relationship-document-2.txt");
		BaseArtifactType artifact3 = client.uploadArtifact(type, RelationshipDemo.class.getResourceAsStream("relationship-demo-doc-3.txt"), "relationship-document-3.txt");
		System.out.println("uploaded.");

		// Tag these artifacts as coming from this demo.
        SrampModelUtils.setCustomProperty(artifact1, "from-demo", RelationshipDemo.class.getSimpleName());
        SrampModelUtils.setCustomProperty(artifact2, "from-demo", RelationshipDemo.class.getSimpleName());
        SrampModelUtils.setCustomProperty(artifact3, "from-demo", RelationshipDemo.class.getSimpleName());

		// Now let's set up some relationships.  Let's make artifacts 2 and 3 "relatedTo"
		// artifact 1.
		SrampModelUtils.addGenericRelationship(artifact2, "relatedTo", artifact1.getUuid());
		SrampModelUtils.addGenericRelationship(artifact3, "relatedTo", artifact1.getUuid());

		// Now make sure to update the changed artifacts
		System.out.print("Updating artifacts...");
        client.updateArtifactMetaData(artifact1);
        client.updateArtifactMetaData(artifact2);
		client.updateArtifactMetaData(artifact3);
		System.out.println("updated.");

		// Let's fetch those again and make sure we have some relationships
		System.out.print("Fetching meta-data for artifacts 2 and 3...");
		BaseArtifactType metaData2 = client.getArtifactMetaData(type, artifact2.getUuid());
		BaseArtifactType metaData3 = client.getArtifactMetaData(type, artifact3.getUuid());
		System.out.println("fetched.");
		if (metaData2.getRelationship().size() != 1) {
			System.out.println("Uh oh - the relationship was missing on artifact 2!");
			System.exit(1);
		} else {
			System.out.println("Found a relationship on artifact 2.");
		}
		if (metaData3.getRelationship().size() != 1) {
			System.out.println("Uh oh - the relationship was missing on artifact 3!");
			System.exit(1);
		} else {
			System.out.println("Found a relationship on artifact 3.");
		}

		// Alright, now I'll show you how to query for those artifacts by the relationship.
		System.out.println("-- Time to query by relationship --");
		String q = "/s-ramp/core/Document[@name = 'relationship-document-1.txt']";
		QueryResultSet resultSet = client.query(q);
		if (resultSet.size() == 0) {
			System.out.println("Failed to find an artifact we just added!");
			System.exit(1);
		} else {
			System.out.println("Query #1 passed.");
		}
		// Find all artifacts related to artifact 1 (should return artifacts 2 and 3)
		q = String.format("/s-ramp/core/Document[relatedTo[@uuid = '%1$s']]", artifact1.getUuid());
		resultSet = client.query(q);
		if (resultSet.size() == 0) {
			System.out.println("Failed to find artifacts by our 'relatedTo' relationship!");
			System.exit(1);
		} else {
			System.out.println("Query #2 passed.");
		}

		// All done.
		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
