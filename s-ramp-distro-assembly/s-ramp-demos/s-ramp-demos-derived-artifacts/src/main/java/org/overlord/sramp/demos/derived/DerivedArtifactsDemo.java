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
package org.overlord.sramp.demos.derived;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.WsdlDocument;

/**
 * Demonstrates how to get and set artifact classifications.  Also shows how to query
 * for artifacts by their classifications.
 *
 * @author eric.wittmann@redhat.com
 */
public class DerivedArtifactsDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-server";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Derived Artifacts Demo ***\n");

		// Figure out the endpoint of the S-RAMP repository's Atom API
		String endpoint = System.getProperty("sramp.endpoint");
		if (endpoint == null || endpoint.trim().length() == 0) {
			endpoint = DEFAULT_ENDPOINT;
		}
		System.out.println("S-RAMP Endpoint: " + endpoint);
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);

		// The first thing we're going to do in this demonstration is add
		// a WSDL artifact to the repository.  This will cause the S-RAMP
		// repo to not only add the WSDL artifact but also add a number of
		// additional artifacts derived from the content of the WSDL file.
		System.out.print("Uploading 'sample.wsdl' to the repo...");
		WsdlDocument wsdlArtifact = (WsdlDocument) client.uploadArtifact(
				ArtifactType.WsdlDocument(),
				DerivedArtifactsDemo.class.getResourceAsStream("sample.wsdl"),
				"sample.wsdl");
		System.out.println("uploaded.");

		// Now we should have a number of artifacts in the repo - the WSDL
		// document and all its derived artifacts.  Let's do some querying
		// to prove this.

		// Find all artifacts that are related to the one we just uploaded.  This
		// query should return all of the derived artifacts, because every derived
		// artifact in S-RAMP has a 'relatedDocument' relationship between it and
		// the artifact it was derived from.
		System.out.println("-- It's query time. --");
		String q = String.format("/s-ramp/wsdl[relatedDocument[@uuid = '%1$s']]", wsdlArtifact.getUuid());
		QueryResultSet resultSet = client.query(q, 0, 100, "name", true);
		// There should be 30 of these, based on the structure of the 'sample.wsdl'
		// file.  Feel free to crack open the sample.wsdl file and see if you can
		// figure out which WSDL concepts become derived artifacts!  Remember to
		// only count the wsdl artifacts, not soap or schema artifacts.
		if (!(resultSet.size() == 30)) {
			System.out.println("Didn't find the right number of derived artifacts!  " + resultSet.size());
			for (ArtifactSummary as : resultSet) {
				System.out.println("  - " + as.getName() + " (" + as.getType().getType() + ")");
			}
			System.exit(1);
		} else {
			System.out.println("Query #1 succeeded.");
		}

		// Now let's find all of the messages only.  If you look in the sample.wsdl
		// file you will notice there are 5 messages.
		q = String.format("/s-ramp/wsdl/Message[relatedDocument[@uuid = '%1$s']]", wsdlArtifact.getUuid());
		resultSet = client.query(q);
		if (!(resultSet.size() == 5)) {
			System.out.println("Didn't find the right number of WSDL message artifacts!");
			System.exit(1);
		} else {
			System.out.println("Query #2 succeeded.");
		}

		// Now let's find all of the element style message parts!  We expect three of these.
		q = String.format("/s-ramp/wsdl/Part[relatedDocument[@uuid = '%1$s'] and element]", wsdlArtifact.getUuid());
		resultSet = client.query(q);
		if (!(resultSet.size() == 3)) {
			System.out.println("Didn't find the right number of element style parts!");
			System.exit(1);
		} else {
			System.out.println("Query #3 succeeded.");
		}

		// All done.
		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
