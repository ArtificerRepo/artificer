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

import static org.junit.Assert.fail;

import java.util.List;

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
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.SrampClientException;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;

/**
 * @author Brett Meyer
 */
@RunWith(Arquillian.class)
public abstract class AbstractIntegrationTest {
    
    // Note: Running Arquillian in its as-client mode by leaving off the @Deployment.
    
    private static final String HOST = "localhost"; //$NON-NLS-1$
    
    private static final int PORT = 8080;
    
    private static final String BASE_URL = "http://" + HOST + ":" + PORT + "/s-ramp-server"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    
    private static final String USERNAME = "admin"; //$NON-NLS-1$
    
    private static final String PASSWORD = "overlord1!"; //$NON-NLS-1$
    
    @After
    public void cleanup() {
        
        // delete all artifacts
        try {
            SrampAtomApiClient client = client();
            // Rather than mess with pagination, just set the count to something sufficiently large.
            QueryResultSet results = client.query("/s-ramp", 0, 10000, "name", true); //$NON-NLS-1$ //$NON-NLS-2$
            for (ArtifactSummary summary : results) {
                if (!summary.isDerived()) {
                    client.deleteArtifact(summary.getUuid(), summary.getType());
                }
            }
        } catch (Exception e) {
            fail("Unable to cleanup test artifacts."); //$NON-NLS-1$
        }
        
        // delete all ontologies
        try {
            SrampAtomApiClient client = client();
            List<OntologySummary> ontologies = client.getOntologies();
            for (OntologySummary ontology : ontologies) {
                client.deleteOntology(ontology.getUuid());
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
