/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.test.client;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.query.ArtifactSummary;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Brett Meyer
 */
public class StoredQueryClientTest extends AbstractClientTest {
    
    @Test
    public void testStoredQuery() throws Exception {
        ArtificerAtomApiClient client = client();
        
        // create an artifact to use
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType("TestArtifact");
        artifact.setName("My Test Artifact");
        artifact.setDescription("Description of my test artifact.");
        BaseArtifactType createdArtifact = client.createArtifact(artifact);
        
        StoredQuery storedQuery = new StoredQuery();
        storedQuery.setQueryName("fooQuery");
        storedQuery.setQueryExpression("/s-ramp/ext/TestArtifact");
        storedQuery.getPropertyName().add("fooProperty");
        
        // add
        StoredQuery returnedStoredQuery = client.createStoredQuery(storedQuery);
        assertNotNull(returnedStoredQuery);
        assertEquals(storedQuery.getQueryName(), returnedStoredQuery.getQueryName());
        assertEquals(storedQuery.getQueryExpression(), returnedStoredQuery.getQueryExpression());
        assertEquals(1, returnedStoredQuery.getPropertyName().size());
        assertEquals(storedQuery.getPropertyName().get(0), returnedStoredQuery.getPropertyName().get(0));
        
        // get
        returnedStoredQuery = client.getStoredQuery(storedQuery.getQueryName());
        assertNotNull(returnedStoredQuery);
        assertEquals(storedQuery.getQueryName(), returnedStoredQuery.getQueryName());
        assertEquals(storedQuery.getQueryExpression(), returnedStoredQuery.getQueryExpression());
        assertEquals(1, returnedStoredQuery.getPropertyName().size());
        assertEquals(storedQuery.getPropertyName().get(0), returnedStoredQuery.getPropertyName().get(0));
        
        // get all
        List<StoredQuery> returnedStoredQueries = client.getStoredQueries();
        assertNotNull(returnedStoredQueries);
        assertEquals(1, returnedStoredQueries.size());
        assertEquals(storedQuery.getQueryName(), returnedStoredQueries.get(0).getQueryName());
        assertEquals(storedQuery.getQueryExpression(), returnedStoredQueries.get(0).getQueryExpression());
        assertEquals(1, returnedStoredQueries.get(0).getPropertyName().size());
        assertEquals(storedQuery.getPropertyName().get(0), returnedStoredQueries.get(0).getPropertyName().get(0));
        
        // update
        storedQuery.setQueryExpression("/s-ramp/ext/TestArtifact[@uuid='" + createdArtifact.getUuid() + "']");
        client.updateStoredQuery(storedQuery.getQueryName(), storedQuery);
        returnedStoredQuery = client.getStoredQuery(storedQuery.getQueryName());
        assertNotNull(returnedStoredQuery);
        assertEquals(storedQuery.getQueryName(), returnedStoredQuery.getQueryName());
        assertEquals(storedQuery.getQueryExpression(), returnedStoredQuery.getQueryExpression());
        assertEquals(1, returnedStoredQuery.getPropertyName().size());
        assertEquals(storedQuery.getPropertyName().get(0), returnedStoredQuery.getPropertyName().get(0));
        
        // execute query
        QueryResultSet queryResults = client.queryWithStoredQuery(storedQuery.getQueryName());
        assertNotNull(queryResults);
        assertEquals(1, queryResults.getTotalResults());
        ArtifactSummary queryResult = queryResults.get(0);
        assertEquals(artifact.getName(), queryResult.getName());
        assertEquals(artifact.getDescription(), queryResult.getDescription());
        
        // delete
        client.deleteStoredQuery(storedQuery.getQueryName());
        returnedStoredQueries = client.getStoredQueries();
        assertNotNull(returnedStoredQueries);
        assertEquals(0, returnedStoredQueries.size());
    }

	@Test
	public void testStoredQueryParamReplacement() throws Exception {
		ArtificerAtomApiClient client = client();

		// create an artifact to use
		ExtendedArtifactType artifact = new ExtendedArtifactType();
		artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
		artifact.setExtendedType("FooType");
		artifact.setName("Test Artifact");
		BaseArtifactType createdArtifact = client.createArtifact(artifact);

		// create a decoy, just to make sure the params really work
		ExtendedArtifactType artifact2 = new ExtendedArtifactType();
		artifact2.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
		artifact2.setExtendedType("AnotherFooType");
		artifact2.setName("Another Test Artifact");
		client.createArtifact(artifact2);

		StoredQuery storedQuery = new StoredQuery();
		storedQuery.setQueryName("fooQuery");
		storedQuery.setQueryExpression("/s-ramp/ext/${type}[@uuid = '${uuid}']");

		// add
		storedQuery = client.createStoredQuery(storedQuery);

		// param values
		Map<String, String> params = new HashMap<>();
		params.put("type", "FooType");
		params.put("uuid", createdArtifact.getUuid());

		// execute query
		QueryResultSet queryResults = client.queryWithStoredQuery(storedQuery.getQueryName(), params);
		assertNotNull(queryResults);
		assertEquals(1, queryResults.getTotalResults());
		ArtifactSummary queryResult = queryResults.get(0);
		assertEquals(artifact.getName(), queryResult.getName());
	}
}
