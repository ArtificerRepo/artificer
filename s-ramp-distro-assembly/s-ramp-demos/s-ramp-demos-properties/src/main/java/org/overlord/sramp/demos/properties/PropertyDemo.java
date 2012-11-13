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
package org.overlord.sramp.demos.properties;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.SrampModelUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.QueryResultSet;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Demonstrates how to manage both core and custom properties on an S-RAMP
 * artifact, in addition to how to query for artifacts based on their
 * properties.
 *
 * @author eric.wittmann@redhat.com
 */
public class PropertyDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-atom/s-ramp";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Query Demo ***\n");

		// Figure out the endpoint of the S-RAMP repository's Atom API
		String endpoint = System.getProperty("sramp.endpoint");
		if (endpoint == null || endpoint.trim().length() == 0) {
			endpoint = DEFAULT_ENDPOINT;
		}
		System.out.println("S-RAMP Endpoint: " + endpoint);
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);

		// Before we do anything else, we need to upload some artifacts to the
		// S-RAMP repository.
		ArtifactType type = ArtifactType.valueOf("Document");
		System.out.print("Uploading two artifacts...");
		BaseArtifactType artifact1 = client.uploadArtifact(type, PropertyDemo.class.getResourceAsStream("sample-document-1.txt"), "sample-document-1.txt");
		BaseArtifactType artifact2 = client.uploadArtifact(type, PropertyDemo.class.getResourceAsStream("sample-document-2.txt"), "sample-document-2.txt");
		System.out.println("uploaded.");

		// And then we can change their names if we want!
		artifact1.setName("property-demo-document-1.txt");
		artifact2.setName("property-demo-document-2.txt");

		// Now, we can modify the artifacts by adding some custom properties to them.
		SrampModelUtils.setCustomProperty(artifact1, "property-demo", "true");
		SrampModelUtils.setCustomProperty(artifact1, "artifact-num", "one");
		SrampModelUtils.setCustomProperty(artifact1, "hello", "world");
		SrampModelUtils.setCustomProperty(artifact2, "property-demo", "true");
		SrampModelUtils.setCustomProperty(artifact2, "artifact-num", "two");
		SrampModelUtils.setCustomProperty(artifact2, "foo", "bar");

		// And now update both artifacts so that the repository knows about these
		// new properties.
		System.out.print("Updating (meta-data for) both artifacts...");
		client.updateArtifactMetaData(artifact1);
		client.updateArtifactMetaData(artifact2);
		System.out.println("updated.");

		// Next, fetch the artifact meta-data from the server and make sure the properties
		// we set above are really there.
		System.out.print("Re-fetching (meta-data for) both artifacts...");
		BaseArtifactType metaData1 = client.getArtifactMetaData(type, artifact1.getUuid());
		BaseArtifactType metaData2 = client.getArtifactMetaData(type, artifact2.getUuid());
		System.out.println("fetched.");
		if (metaData1.getProperty().size() != 3) {
			System.out.println("Properties not found on artifact 1!  Oh noes!");
			System.exit(1);
		} else {
			System.out.println("All 3 properties accounted for (artifact 1)!");
		}
		if (metaData2.getProperty().size() != 3) {
			System.out.println("Properties not found on artifact 2!  Oh noes!");
			System.exit(1);
		} else {
			System.out.println("All 3 properties accounted for (artifact 2)!");
		}

		// Now we know that adding properties works.  Note that of course you can also
		// remove properties and change their values, and that will work the same way
		// (just remember to call updateArtifactMetaData after you make changes).

		// The next thing I want to show you is how to query for artifacts in the
		// repository by their properties.  You can query by either base property or
		// by custom user property.
		String q = "/s-ramp/core/Document[@name = 'property-demo-document-1.txt']";
		QueryResultSet resultSet = client.query(q);
		if (resultSet.size() == 0) {
			System.out.println("Failed to find property-demo-document-1.txt!");
			System.exit(1);
		}
		// Find all of the documents with the 'property-demo' property set to 'true' (should
		// be all of them)
		q = "/s-ramp/core/Document[@property-demo = 'true']";
		resultSet = client.query(q);
		if (resultSet.size() == 0) {
			System.out.println("Failed to find document(s) with @property-demo set to 'true'!");
			System.exit(1);
		} else {
			System.out.println("Search 1 succeeded.");
		}
		long total = resultSet.size();
		// Find only the ones with 'artifact-num' set to 'one' (should be half of them)
		q = "/s-ramp/core/Document[@artifact-num = 'one']";
		resultSet = client.query(q);
		if (resultSet.size() == 0) {
			System.out.println("Failed to find document(s) with @artifact-num set to 'one'!");
			System.exit(1);
		}
		long ones = resultSet.size();
		if (!(total/2 == ones)) {
			System.out.println("The wrong number of documents with @artifact-num set to 'one' found!");
			System.exit(1);
		} else {
			System.out.println("Search 2 succeeded.");
		}

		// All done.
		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
