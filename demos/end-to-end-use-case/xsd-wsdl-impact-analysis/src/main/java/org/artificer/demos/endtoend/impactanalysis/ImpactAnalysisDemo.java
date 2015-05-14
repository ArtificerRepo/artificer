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
package org.artificer.demos.endtoend.impactanalysis;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactTypeEnum;
import org.artificer.common.ArtificerConstants;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Organization;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceEndpoint;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ServiceImplementationModelTarget;

import java.io.InputStream;
import java.util.Iterator;

/**
 * In this demo, we pretend that we're a part of a large software development company, focused on provided
 * SOAP-based services.  Assume a common schema is used across the board.  Reuse is a good thing, right?
 *
 * What if I want to update, replace, or create a new version of that schema?  What software and what teams will be
 * impacted?  More specifically, what will be impacted if I change a *specific element* of the schema?
 *
 * This demo shows the processes and specific queries used in traversing the bi-directional relationship tree,
 * providing both coarse and fine grained impact analysis for your organization's artifacts.
 *
 * @author Brett Meyer
 */
public class ImpactAnalysisDemo {

	private static final String DEFAULT_ENDPOINT = "http://localhost:8080/artificer-server";
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASSWORD = "artificer1!";

	public static void main(String[] args) throws Exception {
		System.out.println("\n*** Running Demo ***\n");

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
        System.out.println("Artificer Endpoint: " + endpoint);
        System.out.println("Artificer User: " + username);

        ArtificerAtomApiClient client = new ArtificerAtomApiClient(endpoint, username, password, true);

        System.out.println("\nUploading XSD artifact (sample.xsd)...");
        InputStream is = ImpactAnalysisDemo.class.getResourceAsStream("sample.xsd");
        BaseArtifactType xsdArtifact = client.uploadArtifact(is, "sample.xsd");
        is.close();

        System.out.println("Uploading WSDL artifact (sample.wsdl)...");
        is = ImpactAnalysisDemo.class.getResourceAsStream("sample.wsdl");
        BaseArtifactType wsdlArtifact = client.uploadArtifact(is, "sample.wsdl");
        is.close();

        System.out.println("\nTake a look at the primary artifacts, derived artifacts, and relationships through the UI: " +
                "http://[HOST]:[PORT]/artificer-ui/index.html");

        System.out.println("\nRetrieving a specific type declaration (needed later), which was derived from the XSD...");
        String query = String.format("/s-ramp/xsd/ComplexTypeDeclaration[@name='%s']", "outputType");
        System.out.println("*** Query: " + query);
        QueryResultSet resultSet = client.query(query);
        ArtifactSummary typeSummary = resultSet.iterator().next();
        System.out.println("ComplexTypeDeclaration: " + typeSummary.getName());

        System.out.println("\nRetrieving the Service artifact (needed later), which was derived from the WSDL...");
        query = String.format("/s-ramp/wsdl/WsdlService[relatedDocument[@uuid='%s']]", wsdlArtifact.getUuid());
        System.out.println("*** Query: " + query);
        resultSet = client.query(query);
        ArtifactSummary serviceSummary = resultSet.iterator().next();
        System.out.println("Service: " + serviceSummary.getName());

        System.out.println("\nShowing that the WSDL imported the XSD and a relationship is defined...");
        query = String.format("/s-ramp/wsdl/WsdlDocument[@uuid = '%s']/importedXsds", wsdlArtifact.getUuid());
        System.out.println("*** Query: " + query);
        resultSet = client.query(query);
        Iterator<ArtifactSummary> itr = resultSet.iterator();
        while (itr.hasNext()) {
            ArtifactSummary resultSummary = itr.next();
            System.out.println("*** Result: " + wsdlArtifact.getName() + " imports " + resultSummary.getName());
        }

        System.out.println("\nShowing all artifact relationships that target the given XSD (should include the 'importedXsds' from the WSDL)...");
        System.out.println("NOTE: This is an Artificer-specific endpoint, outside of the S-RAMP spec, allowing reverse/bi-directional traversal!");
        System.out.println(String.format("*** Endpoint: /artificer-server/artificer/reverseRelationships/%s", xsdArtifact.getUuid()));
        resultSet = client.reverseRelationships(xsdArtifact.getUuid());
        itr = resultSet.iterator();
        while (itr.hasNext()) {
            ArtifactSummary resultSummary = itr.next();
            // The /reverseRelationships endpoint includes the relationship type name as an extension attribute on the results!
            String relationshipType = (String) resultSummary.getExtensionAttribute(
                    ArtificerConstants.ARTIFICER_RELATIONSHIP_TYPE_QNAME);
            System.out.println("*** Result: " + resultSummary.getName() + " --> '" + relationshipType + "' --> " + xsdArtifact.getName());
        }

        System.out.println("\nCreating SOA & ServiceImplementation logical artifacts:");

        System.out.println("Creating ServiceEndpoint 'ABC', which has a 'definedBy' relationship to the WSDL service...");
        ServiceEndpoint serviceEndpointArtifact = new ServiceEndpoint();
        serviceEndpointArtifact.setArtifactType(BaseArtifactEnum.SERVICE_ENDPOINT);
        serviceEndpointArtifact.setName("Service Endpoint ABC");
        DerivedArtifactTarget serviceTarget = new DerivedArtifactTarget();
        serviceTarget.setArtifactType(DerivedArtifactEnum.WSDL_SERVICE);
        serviceTarget.setValue(serviceSummary.getUuid());
        serviceEndpointArtifact.setEndpointDefinedBy(serviceTarget);
        serviceEndpointArtifact = (ServiceEndpoint) client.createArtifact(serviceEndpointArtifact);

        System.out.println("Creating Organization 'Team XYZ', which has a 'provides' relationship to the ServiceEndpoint...");
        Organization organizationArtifact = new Organization();
        organizationArtifact.setArtifactType(BaseArtifactEnum.ORGANIZATION);
        organizationArtifact.setName("Team XYZ");
        ServiceImplementationModelTarget serviceEndpointTarget = new ServiceImplementationModelTarget();
        serviceEndpointTarget.setArtifactType(ServiceImplementationModelEnum.SERVICE_ENDPOINT);
        serviceEndpointTarget.setValue(serviceEndpointArtifact.getUuid());
        organizationArtifact.getProvides().add(serviceEndpointTarget);
        client.createArtifact(organizationArtifact);

        System.out.println("\nOk, I want to update the content of the schema.  But how do I know who/what that impacts?");
        System.out.println("Traverse the chain of relationships, starting at the XSD, to find out which organization to contact about the change!");
        System.out.println("NOTE: This is easily done in the UI (see link, above).  Click on the schema, then on its 'Relationships' tab, to start.");

        System.out.println("\nFind, once again, all artifacts targeting the XSD with a relationship...");
        String nextUuid = reverseRelationship(xsdArtifact.getUuid(), "importedXsds", client);

        System.out.println("\nOk, looks like a WSDL is importing it.  Find all relationships targeting that WSDL... ");
        resultSet = client.reverseRelationships(nextUuid);
        itr = resultSet.iterator();
        while (itr.hasNext()) {
            ArtifactSummary resultSummary = itr.next();
            String relationshipType = (String) resultSummary.getExtensionAttribute(
                    ArtificerConstants.ARTIFICER_RELATIONSHIP_TYPE_QNAME);
            System.out.println("*** Result: '" + relationshipType + "' <-- " + resultSummary.getName()
                    + " (" + resultSummary.getType().getType() + ")");
            if (resultSummary.getType().getArtifactType().equals(ArtifactTypeEnum.WsdlService)) {
                // Found the service
                nextUuid = resultSummary.getUuid();
            }
        }

        System.out.println("\nWow, there's a lot to that WSDL.  It looks like it defines a specific service, 'SampleService'.  Let's check that out next.");
        nextUuid = reverseRelationship(nextUuid, "endpointDefinedBy", client);

        System.out.println("\nLooks like 'Service Endpoint ABC' is defined by that WSDL service.  Who is responsible for the endpoint?");
        reverseRelationship(nextUuid, null, client);

        System.out.println("\nAlright, so 'Team XYZ' is responsible for 'Service Endpoint ABC'.");
        System.out.println("Better contact them and let them know why things are about to break...");

        System.out.println("\nMore specifically, I only want to change one single type declaration: 'outputType'.");
        System.out.println("Within *all* WSDLs in the repo, what exactly is using that type?  Again, look at the relationships targeting it.");
        nextUuid = reverseRelationship(typeSummary.getUuid(), "type", client);
        System.out.println("Ok, one Part.  Keep going.  Which Message?");
        nextUuid = reverseRelationship(nextUuid, "part", client);
        System.out.println("Found the Message.  Now where is it used?");
        nextUuid = reverseRelationship(nextUuid, "message", client);
        System.out.println("An OperationOutput.  Which Operation?");
        reverseRelationship(nextUuid, null, client);

        System.out.println("\nNow I know exactly which pieces of the WSDL will be affected!");

        System.out.println("\nOk, ok, I get it.  Artificer is pretty sweet...");

        System.out.println("\n*** Demo Completed ***\n\n");
	}

    private static String reverseRelationship(String uuid, String desiredRelationshipType, ArtificerAtomApiClient client)
            throws Exception {
        QueryResultSet resultSet = client.reverseRelationships(uuid);
        Iterator<ArtifactSummary> itr = resultSet.iterator();
        while (itr.hasNext()) {
            ArtifactSummary resultSummary = itr.next();
            String relationshipType = (String) resultSummary.getExtensionAttribute(
                    ArtificerConstants.ARTIFICER_RELATIONSHIP_TYPE_QNAME);
            System.out.println("*** Result: '" + relationshipType + "' <-- " + resultSummary.getName()
                    + " (" + resultSummary.getType().getType() + ")");
            if (desiredRelationshipType != null && desiredRelationshipType.equals(relationshipType)) {
                return resultSummary.getUuid();
            }
        }
        return null;
    }
}
