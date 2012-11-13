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
package org.overlord.sramp.demos.classifications;

import java.io.InputStream;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Demonstrates how to get and set artifact classifications.  Also shows how to query
 * for artifacts by their classifications.
 *
 * @author eric.wittmann@redhat.com
 */
public class ClassificationDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-atom/s-ramp";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Classification Demo ***\n");

		// Figure out the endpoint of the S-RAMP repository's Atom API
		String endpoint = System.getProperty("sramp.endpoint");
		if (endpoint == null || endpoint.trim().length() == 0) {
			endpoint = DEFAULT_ENDPOINT;
		}
		System.out.println("S-RAMP Endpoint: " + endpoint);
		SrampAtomApiClient client = new SrampAtomApiClient(endpoint);

		// The first thing we need to do is install the "regions.owl" ontology
		// into the S-RAMP repository.  We can't do anything with classifications
		// until at least one ontology (which defines the classifications/classes)
		// exists in the repo.  More information about how to manage the S-RAMP
		// ontologies can be found in the "s-ramp-demos-ontologies" demo.
		System.out.println("Installing 'regions.owl' ontology.");
		installRegionsOntology(client);

		// Next, let's upload an artifact to the S-RAMP repository.  The call to
		// upload will return the new artifact's meta data.  Once we have that,
		// we can add classifications to it.  Note that those classifications must
		// be defined by the ontology we installed above.
		InputStream content = ClassificationDemo.class.getResourceAsStream("sample-document-1.txt");
		ArtifactType type = ArtifactType.valueOf("Document");
		BaseArtifactType metaData = client.uploadArtifact(type, content, "sample-document-1.txt");
		System.out.println("Artifact 1 successfully added with UUID: " + metaData.getUuid());
		metaData.getClassifiedBy().add("http://www.example.org/regions.owl#Germany");
		client.updateArtifactMetaData(metaData);
		System.out.println("Artifact 1 successfully updated to add the #Germany classification.");

		// Let's make sure that classification really got added to the artifact.
		BaseArtifactType artifact = client.getArtifactMetaData(type, metaData.getUuid());
		if (artifact.getClassifiedBy().isEmpty()) {
			System.out.println("Re-fetched the artifact but didn't find any classifications!  Uh oh, this is embarrassing.");
			System.exit(1);
		} else {
			System.out.printf("Re-fetched the artifact and found %1$d classifications.\n", artifact.getClassifiedBy().size());
			System.out.println("  Classification: " + artifact.getClassifiedBy().get(0));
		}

		// Ok, so we're confident we can add classifications.  You've probably
		// figured out that you can also remove classifications the same way - just
		// manipulate that "classifiedBy" collection on the artifact's meta-data
		// object like we did above.

		// Now let's see how to find artifacts by their classifications.  But first
		// let's add another artifact, so we can be confident that the query is
		// working properly.
		content = ClassificationDemo.class.getResourceAsStream("sample-document-2.txt");
		client.uploadArtifact(type, content, "sample-document-2.txt");
		System.out.println("Artifact 2 successfully added.");
		// This time, we won't add classifications.


		// Now let's try to query and see what we get.  First let's query for all
		// documents - we should have at least 2 here (because we added two just now)
		System.out.println("-- Time to Query --");
		String q = "/s-ramp/core/Document";
		QueryResultSet resultSet = client.query(q);
		if (resultSet.size() >= 2) {
			System.out.println("Great, we found at least two Document artifacts!");
		} else {
			System.out.println("Uh oh, we didn't find enough Document artifacts.  Something is terribly wrong.");
			System.exit(1);
		}
		// We know that worked, so let's query for just Artifact 1 by its classification.  We
		// should find Artifact 1 in the result set.  Classifications are queried using S-RAMP
		// specific x-path functions.  Refer to the S-RAMP specification for details.
		q = "/s-ramp/core/Document[s-ramp:exactlyClassifiedByAnyOf(., 'http://www.example.org/regions.owl#Germany')]";
		resultSet = client.query(q);
		boolean found = false;
		for (ArtifactSummary artifactSummary : resultSet) {
			if (artifactSummary.getUuid().equals(metaData.getUuid())) {
				found = true;
				break;
			}
		}
		if (found) {
			System.out.println("Artifact 1 was successfully found in the query result set!");
		} else {
			System.out.println("Aw crap, we didn't find Artifact 1...");
			System.exit(1);
		}

		// All done.
		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

	/**
	 * Installs (or ensures that it already exists) the Regions S-RAMP ontology in
	 * the S-RAMP repository.
	 * @param client
	 * @throws Exception
	 */
	private static void installRegionsOntology(SrampAtomApiClient client) throws Exception {
		boolean alreadyExists = false;
		List<OntologySummary> ontologies = client.getOntologies();
		for (OntologySummary ontology : ontologies) {
			if ("http://www.example.org/regions.owl".equals(ontology.getBase())) {
				alreadyExists = true;
				break;
			}
		}

		if (!alreadyExists) {
			InputStream resourceAsStream = ClassificationDemo.class.getResourceAsStream("regions.owl.xml");
			client.uploadOntology(resourceAsStream);
			IOUtils.closeQuietly(resourceAsStream);
			System.out.println("The 'regions.owl' ontology has been installed.");
		} else {
			System.out.println("The 'regions.owl' ontology was already installed (ok!).");
		}
	}

}
