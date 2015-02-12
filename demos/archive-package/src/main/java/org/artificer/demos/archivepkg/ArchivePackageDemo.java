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
package org.artificer.demos.archivepkg;

import java.io.InputStream;
import java.util.Map;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtificerModelUtils;

/**
 * Demonstrates the S-RAMP package archive feature.  The S-RAMP specification defines
 * how to upload multiple artifacts all at once, using a ZIP formatted package file.
 * The S-RAMP client supports this mechanism, and this demo shows how.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArchivePackageDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/s-ramp-server";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "overlord";

	/**
	 * Main.
	 *
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running S-RAMP Archive Package Demo ***\n");

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
                .parameter(ArchivePackageDemo.class.getSimpleName()).count(1).query();
        if (rs.size() > 0) {
            System.out.println("It looks like you already ran this demo!");
            System.out.println("I'm going to quit, because I don't want to clutter up");
            System.out.println("your repository with duplicate stuff.");
            System.exit(1);
        }

		// Let's create the S-RAMP archive and populate it with some artifacts.
		System.out.println("Creating the S-RAMP package...");
		ArtificerArchive archive = new ArtificerArchive();
		// The archive creates temporary files, which must be deleted by calling
		// close on the archive.  So wrap everything in a try/catch/finally so we
		// can ensure that the archive will be properly closed.
		try {
			// This will add the ws-security utility.xsd to the archive.
			// First get the artifact content
			InputStream contentStream = ArchivePackageDemo.class.getResourceAsStream("wss-wssecurity-utility-1.0.xsd");
			// Create a meta-data jaxb object and populate it.
			BaseArtifactType metaData = new XsdDocument();
			metaData.setName("wss-wssecurity-utility-1.0.xsd");
			metaData.setDescription("WS-Security: utility.xsd");
			metaData.setVersion("1.0");
	        // Tag this artifact as coming from this demo.
			ArtificerModelUtils.setCustomProperty(metaData, "from-demo", ArchivePackageDemo.class.getSimpleName());
			// Add the artifact (with its meta-data) to the archive.
			System.out.print("\tAdding " + metaData.getName() + " to the archive...");
			archive.addEntry("ws-security/schemas/wss-wssecurity-utility-1.0.xsd", metaData, contentStream);
			contentStream.close();
			System.out.println("added.");

			// This will add the ws-security secext.xsd to the archive.
			contentStream = ArchivePackageDemo.class.getResourceAsStream("wss-wssecurity-secext-1.0.xsd");
			metaData = new XsdDocument();
			metaData.setName("wss-wssecurity-secext-1.0.xsd");
			metaData.setDescription("WS-Security: secext.xsd");
			metaData.setVersion("1.0");
            ArtificerModelUtils.setCustomProperty(metaData, "from-demo", ArchivePackageDemo.class.getSimpleName());
			System.out.print("\tAdding " + metaData.getName() + " to the archive...");
			archive.addEntry("ws-security/schemas/wss-wssecurity-secext-1.0.xsd", metaData, contentStream);
			contentStream.close();
			System.out.println("added.");

			// This will add the ws-tx schema to the archive.
			contentStream = ArchivePackageDemo.class.getResourceAsStream("wstx-wsba-1.1-schema-200701.xsd");
			metaData = new XsdDocument();
			metaData.setName("wstx-wsba-1.1-schema-200701.xsd");
			metaData.setDescription("WS-Transaction: ws-tx-schema.xsd");
			metaData.setVersion("1.1");
            ArtificerModelUtils.setCustomProperty(metaData, "from-demo", ArchivePackageDemo.class.getSimpleName());
			System.out.print("\tAdding " + metaData.getName() + " to the archive...");
			archive.addEntry("ws-tx/schemas/wstx-wsba-1.1-schema-200701.xsd", metaData, contentStream);
			contentStream.close();
			System.out.println("added.");

			// This will add the ws-tx wsdl to the archive.
			contentStream = ArchivePackageDemo.class.getResourceAsStream("wstx-wsba-1.1-wsdl-200702.wsdl");
			// Note - this time we'll create a WsdlDocument as the meta-data object
			metaData = new WsdlDocument();
			metaData.setName("wstx-wsba-1.1-wsdl-200702.wsdl");
			metaData.setDescription("WS-Transaction: ws-tx-wsdl.wsdl");
			metaData.setVersion("1.1");
            ArtificerModelUtils.setCustomProperty(metaData, "from-demo", ArchivePackageDemo.class.getSimpleName());
			System.out.print("\tAdding " + metaData.getName() + " to the archive...");
			archive.addEntry("ws-tx/wsdl/wstx-wsba-1.1-wsdl-200702.wsdl", metaData, contentStream);
			contentStream.close();
			System.out.println("added.");

			// Now we can simply use the client to upload the entire package.
			System.out.print("Uploading the S-RAMP package...");
			Map<String, ?> batchResponse = client.uploadBatch(archive);
			System.out.println("uploaded:");
			// What we get back is a Map of results.  The key to the map is a
			// path within the s-ramp archive.  The value is either a new BaseArtifactType
			// or an SrampServerException.
			for (Map.Entry<String, ?> entry : batchResponse.entrySet()) {
				String path = entry.getKey();
				Object resp = entry.getValue();
				if (resp instanceof BaseArtifactType) {
					BaseArtifactType arty = (BaseArtifactType) resp;
					System.out.println("\t" + path + " (" + arty.getUuid() + ")");
				}
			}
		} finally {
			// Close the archive to cleanup any temporary resources.
			ArtificerArchive.closeQuietly(archive);
		}

		// Now query the S-RAMP repository (for the Schemas only)
		System.out.print("Querying the S-RAMP repository for Schemas...");
		QueryResultSet rset = client.query("/s-ramp/xsd/XsdDocument");
		System.out.println("success: " + rset.size() + " Schemas found:");
		for (ArtifactSummary summary : rset) {
			System.out.println("\t * " + summary.getName() + " (" + summary.getUuid() + ")");
		}

		System.out.println("\n*** Demo Completed Successfully ***\n\n");
		Thread.sleep(3000);
	}

}
