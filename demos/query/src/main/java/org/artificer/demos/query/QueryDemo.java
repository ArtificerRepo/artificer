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
package org.artificer.demos.query;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.QueryResultSet;

/**
 * Demonstrates a number of different queries supported by S-RAMP.  Also, more generally,
 * shows how to query the S-RAMP repository using the S-RAMP client.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryDemo {

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

        String endpoint = System.getProperty("artificer.endpoint");
        String username = System.getProperty("artificer.auth.username");
        String password = System.getProperty("artificer.auth.password");
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
        ArtificerAtomApiClient client = new ArtificerAtomApiClient(endpoint, username, password, true);

        // Have we already run this demo?
        QueryResultSet rs = client.buildQuery("/s-ramp[@from-demo = ?]")
                .parameter(QueryDemo.class.getSimpleName()).count(1).query();
        if (rs.size() > 0) {
            System.out.println("It looks like you already ran this demo!");
            System.out.println("I'm going to skip the uploading of resources and just do the queries!");
        } else {
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
    		ArtificerArchive archive = new ArtificerArchive(QueryDemo.class.getResourceAsStream("archive-package.sramp"));
    		try {
    			System.out.print("Uploading some content to the S-RAMP repository...");
    			client.uploadBatch(archive);
    			System.out.println("done!");
    		} finally {
    			ArtificerArchive.closeQuietly(archive);
    		}
        }

		// First, a simple query for the XSDs.
		System.out.print("Querying the S-RAMP repository for Schemas...");
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument");
		if (rset.size() >= 2)
		    System.out.println("success: " + rset.size() + " Schema(s) found (expected AT LEAST 2)");
		else
            System.out.println("** PROBLEM ** : " + rset.size() + " Schema(s) found (expected AT LEAST 2)");

		// Now a simple query for the WSDLs.
		System.out.print("Querying the S-RAMP repository for WSDLs...");
		rset = client.query("/s-ramp/wsdl/WsdlDocument");
        if (rset.size() >= 1)
            System.out.println("success: " + rset.size() + " WSDL(s) found (expected AT LEAST 1)");
        else
            System.out.println("** PROBLEM ** : " + rset.size() + " WSDL(s) found (expected AT LEAST 1)");

		// Try searching for everything with a version of 1.1 (should be at least 2)
		System.out.print("Querying the S-RAMP repository for all artifacts version 1.1...");
		rset = client.query("/s-ramp[@version = '1.1']");
        if (rset.size() >= 2)
            System.out.println("success: " + rset.size() + " artifact(s) found (expected AT LEAST 2)");
        else
            System.out.println("** PROBLEM ** : " + rset.size() + " artifact(s) found (expected AT LEAST 2)");

		// Try searching for everything with a version of 1.2 (should be at least 1)
		System.out.print("Querying the S-RAMP repository for all artifacts version 1.2...");
		rset = client.query("/s-ramp[@version = '1.2']");
        if (rset.size() >= 1)
            System.out.println("success: " + rset.size() + " artifact(s) found (expected AT LEAST 1)");
        else
            System.out.println("** PROBLEM ** : " + rset.size() + " artifact(s) found (expected AT LEAST 1)");

		// Find just a single artifact by name and version
		System.out.print("Querying the S-RAMP repository for a unique artifact by name + version...");
		rset = client.query("/s-ramp[@name = 'wsrm-1.1-schema-200702.xsd' and @version = '1.1']");
        if (rset.size() >= 1)
            System.out.println("success: " + rset.size() + " artifact(s) found (expected AT LEAST 1)");
        else
            System.out.println("** PROBLEM ** : " + rset.size() + " artifact(s) found (expected AT LEAST 1)");

        // Let's do the *same* search, but use the "buildQuery" method on the client
        System.out.print("Querying the S-RAMP repository for a unique artifact by name + version (again)...");
        rset = client.buildQuery("/s-ramp[@name = ? and @version = ?]")
                .parameter("wsrm-1.1-schema-200702.xsd")
                .parameter("1.1").query();
        if (rset.size() >= 1)
            System.out.println("success: " + rset.size() + " artifact(s) found (expected AT LEAST 1)");
        else
            System.out.println("** PROBLEM ** : " + rset.size() + " artifact(s) found (expected AT LEAST 1)");

		// If we search for conflicting meta-data we should get 0 results, right?
		System.out.print("Querying the S-RAMP repository for conflicting meta data...");
		rset = client.query("/s-ramp[@name = 'wsrm-1.1-schema-200702.xsd' and @version = '1.2']");
		if (rset.size() == 0)
            System.out.println("success: " + rset.size() + " artifact(s) found (expected 0)");
		else
		    System.out.println("** PROBLEM ** : " + rset.size() + " artifact(s) found (expected 0)");

		// Finally, I think the buildQuery style is superior - here's a full example
        System.out.print("Search for all artifacts with a name starting with 'w'...");
        rset = client.buildQuery("/s-ramp[xp2:matches(@name, ?)]")
                .parameter("w.*")
                .count(1)
                .ascending()
                .query();
        if (rset.size() == 1)
            System.out.println("success: " + rset.size() + " artifact(s) found (expected SOME :))");
        else
            System.out.println("** PROBLEM ** : " + rset.size() + " artifact(s) found (expected SOME)");

		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
