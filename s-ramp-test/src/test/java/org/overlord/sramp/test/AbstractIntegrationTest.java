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
package org.overlord.sramp.test;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.junit.After;
import org.junit.runner.RunWith;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.ClientRequest;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;

import java.util.List;

import static org.junit.Assert.fail;

/**
 * @author Brett Meyer
 */
@RunWith(Arquillian.class)
public abstract class AbstractIntegrationTest {

    // Note: Running Arquillian in its as-client mode by leaving off the @Deployment.

    private static final String HOST = System.getProperty("sramp.test.host", "localhost"); //$NON-NLS-1$ //$NON-NLS-2$

    private static final int PORT = Integer.parseInt(System.getProperty("sramp.test.port", "8080")); //$NON-NLS-1$ //$NON-NLS-2$

    private static final String BASE_URL = "http://" + HOST + ":" + PORT + "/s-ramp-server"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    protected static final String USERNAME = System.getProperty("sramp.test.username", "admin"); //$NON-NLS-1$ //$NON-NLS-2$

    protected static final String PASSWORD = System.getProperty("sramp.test.password", "artificer1!"); //$NON-NLS-1$ //$NON-NLS-2$

    @After
    public void cleanup() {

        // delete all artifacts
        try {
            SrampAtomApiClient client = client();
            // Rather than mess with pagination, just set the count to something sufficiently large.
            QueryResultSet results = client.query("/s-ramp", 0, 10000, "name", true); //$NON-NLS-1$ //$NON-NLS-2$
            for (ArtifactSummary summary : results) {
                String uuid = summary.getUuid().replace("urn:uuid:", "");
                // First, need to clear the relationships, custom properties, and classifiers to prevent
                // constraint Exceptions.  Note that modeled relationships must be manually cleared by tests!
                BaseArtifactType artifact = client.getArtifactMetaData(uuid);
                // This is expensive, so prevent it if possible.
                if (artifact.getRelationship().size() > 0 || artifact.getProperty().size() > 0 || artifact.getClassifiedBy().size() > 0) {
                    artifact.getRelationship().clear();
                    artifact.getProperty().clear();
                    artifact.getClassifiedBy().clear();
                    client.updateArtifactMetaData(artifact);
                }
            }
            for (ArtifactSummary summary : results) {
                String uuid = summary.getUuid().replace("urn:uuid:", "");
                if (!summary.isDerived()) {
                    client.deleteArtifact(uuid, summary.getType());
                }
            }
        } catch (Exception e) {
//            e.printStackTrace();
//            fail("Unable to cleanup test artifacts."); //$NON-NLS-1$
        }
        
        // delete all stored queries
        try {
            SrampAtomApiClient client = client();
            List<StoredQuery> storedQueries = client.getStoredQueries();
            for (StoredQuery storedQuery : storedQueries) {
                client.deleteStoredQuery(storedQuery.getQueryName());
            }
        } catch (Exception e) {
            fail("Unable to cleanup test artifacts."); //$NON-NLS-1$
        }

        // delete all ontologies
        try {
            SrampAtomApiClient client = client();
            List<OntologySummary> ontologies = client.getOntologies();
            for (OntologySummary ontology : ontologies) {
                String uuid = ontology.getUuid().replace("urn:uuid:", "");
                client.deleteOntology(uuid);
            }
        } catch (Exception e) {
            fail("Unable to cleanup test artifacts."); //$NON-NLS-1$
        }
    }

    protected String getHost() {
        return HOST;
    }

    protected int getPort() {
        return PORT;
    }

    protected String getUsername() {
        return USERNAME;
    }

    protected String getPassword() {
        return PASSWORD;
    }

    protected SrampAtomApiClient client() throws SrampClientException, SrampAtomException {
        return new SrampAtomApiClient(BASE_URL, USERNAME, PASSWORD, true);
    }

    protected ClientRequest clientRequest(String endpoint) {
        DefaultHttpClient client = new DefaultHttpClient();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(USERNAME, PASSWORD);
        client.getCredentialsProvider().setCredentials(new AuthScope(AuthScope.ANY), credentials);
        AuthCache authCache = new BasicAuthCache();
        BasicScheme basicAuth = new BasicScheme();
        HttpHost targetHost = new HttpHost(HOST, PORT);
        authCache.put(targetHost, basicAuth);
        BasicHttpContext localContext = new BasicHttpContext();
        localContext.setAttribute(ClientContext.AUTH_CACHE, authCache);
        ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client, localContext);

        ClientRequest clientRequest = new ClientRequest(BASE_URL + endpoint, executor);
        return clientRequest;
    }

    protected String generateURL(String endpoint) {
        return BASE_URL + endpoint;
    }
}
