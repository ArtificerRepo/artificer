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
package org.overlord.sramp.demos.query;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.QueryResultSet;

/**
 * Demonstrates a number of different queries supported by S-RAMP.  Also, more generally,
 * shows how to query the S-RAMP repository using the S-RAMP client.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-atom";

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

		// First let's upload a bunch of artifacts so that we have something to query.  I've
		// created an S-RAMP archive package to make this easy.  The archive contains the
		// following artifacts (you can crack it open with a zip utility if you like):
		//
		// 		wsrm/schemas/wsrm-1.1-schema-200702.xsd
		// 		wsrm/wsdl/wsrm-1.1-wsdl-200702e1.wsdl
		// 		wsrmp/schemas/wsrmp-1.2-schema-200702.xsd
		//
		// Additionally, each artifact has a custom property named "standard" with either
		// the value "wsrm" or "wsrmp" as appropriate.  This should give us some meta-data
		// on which to search.
		SrampArchive archive = new SrampArchive(QueryDemo.class.getResourceAsStream("archive-package.sramp"));
		try {
			System.out.print("Uploading some content to the S-RAMP repository...");
			client.uploadBatch(archive);
			System.out.println("done!");
		} finally {
			SrampArchive.closeQuietly(archive);
		}

		// First, a simple query for the XSDs.
		System.out.print("Querying the S-RAMP repository for Schemas...");
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument");
		System.out.println("success: " + rset.size() + " Schema(s) found (expected AT LEAST 2)");

		// Now a simple query for the WSDLs.
		System.out.print("Querying the S-RAMP repository for WSDLs...");
		rset = client.query("/s-ramp/wsdl/WsdlDocument");
		System.out.println("success: " + rset.size() + " WSDL(s) found (expected AT LEAST 1)");

		// Try searching for everything with a version of 1.1 (should be at least 2)
		System.out.print("Querying the S-RAMP repository for all artifacts version 1.1...");
		rset = client.query("/s-ramp[@version = '1.1']");
		System.out.println("success: " + rset.size() + " artifact(s) found (expected AT LEAST 2)");

		// Try searching for everything with a version of 1.2 (should be at least 1)
		System.out.print("Querying the S-RAMP repository for all artifacts version 1.2...");
		rset = client.query("/s-ramp[@version = '1.2']");
		System.out.println("success: " + rset.size() + " artifact(s) found (expected AT LEAST 1)");

		// Find just a single artifact by name and version
		System.out.print("Querying the S-RAMP repository for a unique artifact by name + version...");
		rset = client.query("/s-ramp[@name = 'wsrm-1.1-schema-200702.xsd' and @version = '1.1']");
		System.out.println("success: " + rset.size() + " artifact(s) found (expected 1)");

		// If we search for conflicting meta-data we should get 0 results, right?
		System.out.print("Querying the S-RAMP repository conflicting meta data...");
		rset = client.query("/s-ramp[@name = 'wsrm-1.1-schema-200702.xsd' and @version = '1.2']");
		System.out.println("success: " + rset.size() + " artifact(s) found (expected 0)");

		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
