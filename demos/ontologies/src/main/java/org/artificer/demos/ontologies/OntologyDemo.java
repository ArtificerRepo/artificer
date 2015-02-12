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
package org.artificer.demos.ontologies;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;

/**
 * Demonstrates how to manage ontologies in the S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class OntologyDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-server";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "overlord";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Ontology Demo ***\n");

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

		// Presumably we start off without any ontologies installed in the S-RAMP repository.
		// So we need to get the ball rolling, by adding some!  Let's check first.
		System.out.println("Searching for ontologies.");
		List<OntologySummary> ontologies = client.getOntologies();
		Set<String> foundOntologies = new HashSet<String>();
		if (ontologies.isEmpty()) {
			System.out.println("No ontologies found (good).");
		} else {
			System.out.println("Found some ontologies (ok):");
			for (OntologySummary ontology : ontologies) {
				System.out.println("  " + ontology.getBase());
				foundOntologies.add(ontology.getBase());
			}
		}

		// Let's go ahead and add a couple of ontologies (unless they already
		// exist in the S-RAMP repo).
		if (!foundOntologies.contains("http://www.example.org/sample-ontology-1.owl")) {
			System.out.print("Uploading sample ontology 1...");
			client.uploadOntology(OntologyDemo.class.getResourceAsStream("sample-ontology-1.owl.xml"));
			System.out.println("done.");
		}
		if (!foundOntologies.contains("http://www.example.org/sample-ontology-2.owl")) {
			System.out.print("Uploading sample ontology 2...");
			client.uploadOntology(OntologyDemo.class.getResourceAsStream("sample-ontology-2.owl.xml"));
			System.out.println("done.");
		}

		// Now we can query the ontologies to see which ones are installed.
		System.out.println("There should now be (at least) two ontologies in the repo.");
		System.out.print("Checking for ontologies...");
		ontologies = client.getOntologies();
		System.out.printf("found %1$d:\n", ontologies.size());
		Map<String, String> ontologyUuids = new HashMap<String, String>();
		for (OntologySummary ontology : ontologies) {
			System.out.println("  " + ontology.getBase());
			ontologyUuids.put(ontology.getBase(), ontology.getUuid());
		}

		// Now let's say we don't want sample-ontology-2 anymore.  We can delete it.
		String sampleOntology2Uuid = ontologyUuids.get("http://www.example.org/sample-ontology-2.owl");
		System.out.print("Deleting sample-ontology-2...");
		client.deleteOntology(sampleOntology2Uuid);
		System.out.println("deleted.");

		// Now let's query for our ontologies again.
		System.out.println("There should now be one less ontology in the repo.");
		System.out.print("Checking for ontologies...");
		ontologies = client.getOntologies();
		System.out.printf("found %1$d:\n", ontologies.size());
		for (OntologySummary ontology : ontologies) {
			System.out.println("  " + ontology.getBase());
			ontologyUuids.put(ontology.getBase(), ontology.getUuid());
		}

		// All done
		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
