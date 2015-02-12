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
package org.artificer.test.maven;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.junit.Assert;
import org.junit.Test;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.client.ClientRequest;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.auth.BasicAuthenticationProvider;
import org.artificer.client.query.QueryResultSet;
import org.artificer.test.AbstractIntegrationTest;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Unit test for the s-ramp maven facade.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
public class ArtificerMavenTest extends AbstractIntegrationTest {

	/**
	 * Unit test.
	 */
	@Test
	public void testPush() throws Exception {
		InputStream metaDataStream = null;
		InputStream artifactStream = null;
		InputStream pomStream = null;
		InputStream artifactSHA1Stream = null;
		InputStream pomSHA1Stream = null;
		try {
			metaDataStream = getClass().getResourceAsStream("maven-metadata.xml");
			artifactStream = getClass().getResourceAsStream("artifact-0.0.3.jar");
			pomStream = getClass().getResourceAsStream("artifact-0.0.3.pom");
			artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1");
			pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1");

			Assert.assertNotNull(metaDataStream);
			Assert.assertNotNull(artifactStream);
			Assert.assertNotNull(pomStream);
			Assert.assertNotNull(artifactSHA1Stream);
			Assert.assertNotNull(pomSHA1Stream);

            createClientRequest("org/overlord/sramp/test/archive/0.0.3/maven-metadata.xml?artifactType=JavaArchive")
                    .body("application/octet-stream", metaDataStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar?artifactType=JavaArchive")
                    .body("application/octet-stream", artifactStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar.sha1?artifactType=JavaArchive")
                    .body("application/octet-stream", artifactSHA1Stream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom?artifactType=JavaArchive")
                    .body("application/octet-stream", pomStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1?artifactType=JavaArchive")
                    .body("application/octet-stream", pomSHA1Stream).post();
		} finally {
			IOUtils.closeQuietly(metaDataStream);
			IOUtils.closeQuietly(artifactStream);
			IOUtils.closeQuietly(pomStream);
			IOUtils.closeQuietly(artifactSHA1Stream);
			IOUtils.closeQuietly(pomSHA1Stream);
		}

		// Now that we've deployed the artifacts, do some queries to make sure we put away
		// what we intended.
		ArtificerAtomApiClient client = client();
		QueryResultSet rset = client.query("/s-ramp/ext/JavaArchive");
		Assert.assertEquals(1, rset.size());
        rset = client.query("/s-ramp/ext/MavenPom");
        Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp/xsd/XsdDocument");
		Assert.assertEquals(3, rset.size());
		rset = client.query("/s-ramp/wsdl/WsdlDocument");
		Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp[expandedFromDocument]");
		Assert.assertEquals(4, rset.size());
	}
	
	/**
	 * Unit test.
	 */
	@Test
	public void testPushNoSpecifiedTypeUseHints() throws Exception {
		InputStream metaDataStream = null;
		InputStream artifactStream = null;
		InputStream pomStream = null;
		InputStream artifactSHA1Stream = null;
		InputStream pomSHA1Stream = null;
		try {
			metaDataStream = getClass().getResourceAsStream("maven-metadata.xml");
			artifactStream = getClass().getResourceAsStream("artifact-0.0.3.jar");
			pomStream = getClass().getResourceAsStream("artifact-0.0.3.pom");
			artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1");
			pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1");

			Assert.assertNotNull(metaDataStream);
			Assert.assertNotNull(artifactStream);
			Assert.assertNotNull(pomStream);
			Assert.assertNotNull(artifactSHA1Stream);
			Assert.assertNotNull(pomSHA1Stream);

            createClientRequest("org/overlord/sramp/test/archive/0.0.3/maven-metadata.xml")
                    .body("application/octet-stream", metaDataStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar")
                    .body("application/octet-stream", artifactStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar.sha1")
                    .body("application/octet-stream", artifactSHA1Stream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom")
                    .body("application/octet-stream", pomStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1")
                    .body("application/octet-stream", pomSHA1Stream).post();
		} finally {
			IOUtils.closeQuietly(metaDataStream);
			IOUtils.closeQuietly(artifactStream);
			IOUtils.closeQuietly(pomStream);
			IOUtils.closeQuietly(artifactSHA1Stream);
			IOUtils.closeQuietly(pomSHA1Stream);
		}

		// Now that we've deployed the artifacts, do some queries to make sure we put away
		// what we intended.
		ArtificerAtomApiClient client = client();
		QueryResultSet rset = client.query("/s-ramp/ext/JavaArchive");
		Assert.assertEquals(1, rset.size());
        rset = client.query("/s-ramp/ext/MavenPom");
        Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp/xsd/XsdDocument");
		Assert.assertEquals(3, rset.size());
		rset = client.query("/s-ramp/wsdl/WsdlDocument");
		Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp[expandedFromDocument]");
		Assert.assertEquals(4, rset.size());
	}

    /**
     * Unit test.
     */
    @Test
    public void testPushWithArtifactName() throws Exception {
        InputStream artifactStream = null;
        InputStream pomStream = null;
        InputStream artifactSHA1Stream = null;
        InputStream pomSHA1Stream = null;
        try {
            artifactStream = getClass().getResourceAsStream("artifact-0.0.3.jar");
            artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1");
            pomStream = getClass().getResourceAsStream("artifact-0.0.3.pom");
            pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1");

            Assert.assertNotNull(artifactStream);
            Assert.assertNotNull(pomStream);
            Assert.assertNotNull(artifactSHA1Stream);
            Assert.assertNotNull(pomSHA1Stream);

            createClientRequest("org/overlord/sramp/test/foo/0.0.3/artifact-0.0.3.jar?artifactType=FooApplication")
                    .body("application/octet-stream", artifactStream).post();
            createClientRequest("org/overlord/sramp/test/foo/0.0.3/artifact-0.0.3.jar.sha1?artifactType=FooApplication")
                    .body("application/octet-stream", artifactSHA1Stream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom?artifactType=FooApplication")
                    .body("application/octet-stream", pomStream).post();
            createClientRequest("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1?artifactType=FooApplication")
                    .body("application/octet-stream", pomSHA1Stream).post();
        } finally {
            IOUtils.closeQuietly(artifactStream);
            IOUtils.closeQuietly(pomStream);
            IOUtils.closeQuietly(artifactSHA1Stream);
            IOUtils.closeQuietly(pomSHA1Stream);
        }

        // Now that we've deployed the artifacts, do some queries to make sure we put away
        // what we intended.
        ArtificerAtomApiClient client = client();
        QueryResultSet rset = client.query("/s-ramp/core/Document");
        Assert.assertEquals(0, rset.size());
        rset = client.query("/s-ramp/ext/FooApplication");
        Assert.assertEquals(1, rset.size());
        rset = client.query("/s-ramp/ext/MavenPom");
        Assert.assertEquals(1, rset.size());

        rset = client.query("/s-ramp[@maven.groupId = 'org.overlord.sramp.test']");
        Assert.assertEquals(2, rset.size());
    }

	/**
	 * Unit test.
	 * @throws Exception
	 */
	@Test
	public void testPull() throws Exception {
		// First, deploy some maven annotated artifacts
		ArtificerAtomApiClient client = client();
		InputStream archiveStream = null;
		ArtificerArchive archive = null;
		try {
			archiveStream = getClass().getResourceAsStream("sramp-archive.zip");
			archive = new ArtificerArchive(archiveStream);
			client.uploadBatch(archive);
		} finally {
			ArtificerArchive.closeQuietly(archive);
		}

		String s = (String) createClientRequest(
                "org/overlord/sramp/test/archive/maven-metadata.xml").get().getEntity(String.class);
        Assert.assertTrue(StringUtils.isNotBlank(s));
        s = (String) createClientRequest(
                "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar").get().getEntity(String.class);
        Assert.assertTrue(StringUtils.isNotBlank(s));
        s = (String) createClientRequest(
                "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar.sha1").get().getEntity(String.class);
        Assert.assertTrue(StringUtils.isNotBlank(s));
        s = (String) createClientRequest(
                "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom").get().getEntity(String.class);
        Assert.assertTrue(StringUtils.isNotBlank(s));
        s = (String) createClientRequest(
                "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1").get().getEntity(String.class);
        Assert.assertTrue(StringUtils.isNotBlank(s));
	}

	/**
	 * Verifies that the correct file content was downloaded.
	 * @param expected
	 * @param actual
	 * @throws IOException
	 */
	private void assertContents(String expected, File actual) throws IOException {
		InputStream expectedStream = null;
		InputStream actualStream = null;
		try {
			expectedStream = getClass().getResourceAsStream(expected);
			actualStream = FileUtils.openInputStream(actual);
			Assert.assertTrue("File contents failed to match: " + actual.getName(),
					IOUtils.contentEquals(expectedStream, actualStream));
		} finally {
			IOUtils.closeQuietly(actualStream);
			IOUtils.closeQuietly(expectedStream);
		}
	}

    private ClientRequest createClientRequest(String urlAppend) {
        String url = "http://localhost:8080/artificer-server/maven/repository/" + urlAppend;
        ClientExecutor executor = createClientExecutor();
        ClientRequest request = new ClientRequest(url, executor);
        return request;
    }

    private ClientExecutor createClientExecutor() {
        final BasicAuthenticationProvider authProvider = new BasicAuthenticationProvider("admin", "artificer1!");

        DefaultHttpClient httpClient = new DefaultHttpClient();
        httpClient.addRequestInterceptor(new HttpRequestInterceptor() {
            @Override
            public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                authProvider.provideAuthentication(request);
            }
        });
        return new ApacheHttpClient4Executor(httpClient);
    }

}
