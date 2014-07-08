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
package org.overlord.sramp.test.wagon;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.wagon.authentication.AuthenticationInfo;
import org.apache.maven.wagon.repository.Repository;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.test.AbstractIntegrationTest;
import org.overlord.sramp.wagon.SrampWagon;

/**
 * Unit test for the s-ramp wagon class.
 *
 * @author eric.wittmann@redhat.com
 */
@RunWith(Arquillian.class)
public class SrampWagonTest extends AbstractIntegrationTest {

	/**
	 * Unit test.
	 */
	@Test
	public void testWagonPush() throws Exception {
		SrampWagon wagon = new SrampWagon();
		setLogger(wagon);
		Repository repo = new Repository("sramp.repo", generateURL("/s-ramp/?artifactType=JavaArchive").replaceAll("http", "sramp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		connect(wagon, repo);
		InputStream metaDataStream = null;
		InputStream artifactStream = null;
		InputStream pomStream = null;
		InputStream artifactSHA1Stream = null;
		InputStream pomSHA1Stream = null;
		try {
			metaDataStream = getClass().getResourceAsStream("maven-metadata.xml"); //$NON-NLS-1$
			artifactStream = getClass().getResourceAsStream("artifact-0.0.3.jar"); //$NON-NLS-1$
			pomStream = getClass().getResourceAsStream("artifact-0.0.3.pom"); //$NON-NLS-1$
			artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
			pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1"); //$NON-NLS-1$

			Assert.assertNotNull(metaDataStream);
			Assert.assertNotNull(artifactStream);
			Assert.assertNotNull(pomStream);
			Assert.assertNotNull(artifactSHA1Stream);
			Assert.assertNotNull(pomSHA1Stream);

			wagon.putFromStream(metaDataStream, "org/overlord/sramp/test/archive/0.0.3/maven-metadata.xml"); //$NON-NLS-1$
			wagon.putFromStream(artifactStream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar"); //$NON-NLS-1$
			wagon.putFromStream(artifactSHA1Stream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
			wagon.putFromStream(pomStream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom"); //$NON-NLS-1$
			wagon.putFromStream(pomSHA1Stream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1"); //$NON-NLS-1$
		} finally {
			wagon.disconnect();
			IOUtils.closeQuietly(metaDataStream);
			IOUtils.closeQuietly(artifactStream);
			IOUtils.closeQuietly(pomStream);
			IOUtils.closeQuietly(artifactSHA1Stream);
			IOUtils.closeQuietly(pomSHA1Stream);
		}

		// Now that we've deployed the artifacts, do some queries to make sure we put away
		// what we intended.
		SrampAtomApiClient client = client("/s-ramp/"); //$NON-NLS-1$
		QueryResultSet rset = client.query("/s-ramp/ext/JavaArchive"); //$NON-NLS-1$
		Assert.assertEquals(1, rset.size());
        rset = client.query("/s-ramp/ext/MavenPom"); //$NON-NLS-1$
        Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp/xsd/XsdDocument"); //$NON-NLS-1$
		Assert.assertEquals(3, rset.size());
		rset = client.query("/s-ramp/wsdl/WsdlDocument"); //$NON-NLS-1$
		Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp[expandedFromDocument]"); //$NON-NLS-1$
		Assert.assertEquals(4, rset.size());

		// Upload the content again (to make sure the expanded artifacts get deleted and re-added)
		// TODO re-enable this once I figure out why I am getting a referential integrity error
		/*
		connect(wagon, repo);
		try {
			artifactStream = getClass().getResourceAsStream("artifact-0.0.3.jar");
			Assert.assertNotNull(artifactStream);
			wagon.putFromStream(artifactStream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar");
		} finally {
			wagon.disconnect();
			IOUtils.closeQuietly(artifactStream);
		}
		*/

		// Now all the same assertions.
		/*
		rset = client.query("/s-ramp/core/Document");
		Assert.assertEquals(2, rset.size());
		rset = client.query("/s-ramp/xsd/XsdDocument");
		Assert.assertEquals(3, rset.size());
		rset = client.query("/s-ramp/wsdl/WsdlDocument");
		Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp[mavenParent]");
		Assert.assertEquals(4, rset.size());
		*/
	}
	
	/**
	 * Unit test.
	 */
	@Test
	public void testWagonPushNoSpecifiedTypeUseHints() throws Exception {
		SrampWagon wagon = new SrampWagon();
		setLogger(wagon);
		Repository repo = new Repository("sramp.repo", generateURL("/s-ramp/").replaceAll("http", "sramp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		connect(wagon, repo);
		InputStream metaDataStream = null;
		InputStream artifactStream = null;
		InputStream pomStream = null;
		InputStream artifactSHA1Stream = null;
		InputStream pomSHA1Stream = null;
		try {
			metaDataStream = getClass().getResourceAsStream("maven-metadata.xml"); //$NON-NLS-1$
			artifactStream = getClass().getResourceAsStream("artifact-0.0.3.jar"); //$NON-NLS-1$
			pomStream = getClass().getResourceAsStream("artifact-0.0.3.pom"); //$NON-NLS-1$
			artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
			pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1"); //$NON-NLS-1$

			Assert.assertNotNull(metaDataStream);
			Assert.assertNotNull(artifactStream);
			Assert.assertNotNull(pomStream);
			Assert.assertNotNull(artifactSHA1Stream);
			Assert.assertNotNull(pomSHA1Stream);

			wagon.putFromStream(metaDataStream, "org/overlord/sramp/test/archive/0.0.3/maven-metadata.xml"); //$NON-NLS-1$
			wagon.putFromStream(artifactStream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar"); //$NON-NLS-1$
			wagon.putFromStream(artifactSHA1Stream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
			wagon.putFromStream(pomStream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom"); //$NON-NLS-1$
			wagon.putFromStream(pomSHA1Stream, "org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1"); //$NON-NLS-1$
		} finally {
			wagon.disconnect();
			IOUtils.closeQuietly(metaDataStream);
			IOUtils.closeQuietly(artifactStream);
			IOUtils.closeQuietly(pomStream);
			IOUtils.closeQuietly(artifactSHA1Stream);
			IOUtils.closeQuietly(pomSHA1Stream);
		}

		// Now that we've deployed the artifacts, do some queries to make sure we put away
		// what we intended.
		SrampAtomApiClient client = client("/s-ramp/"); //$NON-NLS-1$
		QueryResultSet rset = client.query("/s-ramp/ext/JavaArchive"); //$NON-NLS-1$
		Assert.assertEquals(1, rset.size());
        rset = client.query("/s-ramp/ext/MavenPom"); //$NON-NLS-1$
        Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp/xsd/XsdDocument"); //$NON-NLS-1$
		Assert.assertEquals(3, rset.size());
		rset = client.query("/s-ramp/wsdl/WsdlDocument"); //$NON-NLS-1$
		Assert.assertEquals(1, rset.size());
		rset = client.query("/s-ramp[expandedFromDocument]"); //$NON-NLS-1$
		Assert.assertEquals(4, rset.size());
	}

    /**
     * Unit test.
     */
    @Test
    public void testWagonPushWithArtifactName() throws Exception {
        SrampWagon wagon = new SrampWagon();
        setLogger(wagon);
        Repository repo = new Repository("sramp.repo", generateURL("/s-ramp/?artifactType=FooApplication").replaceAll("http", "sramp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        connect(wagon, repo);
        InputStream artifactStream = null;
        InputStream pomStream = null;
        InputStream artifactSHA1Stream = null;
        InputStream pomSHA1Stream = null;
        try {
            artifactStream = getClass().getResourceAsStream("foo-artifact-0.0.3.txt"); //$NON-NLS-1$
            pomStream = getClass().getResourceAsStream("artifact-0.0.3.pom"); //$NON-NLS-1$
            artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
            pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1"); //$NON-NLS-1$

            Assert.assertNotNull(artifactStream);
            Assert.assertNotNull(pomStream);
            Assert.assertNotNull(artifactSHA1Stream);
            Assert.assertNotNull(pomSHA1Stream);

            wagon.putFromStream(artifactStream, "org/overlord/sramp/test/foo/0.0.3/foo-artifact-0.0.3.txt"); //$NON-NLS-1$
            wagon.putFromStream(artifactSHA1Stream, "org/overlord/sramp/test/foo/0.0.3/foo-artifact-0.0.3.txt.sha1"); //$NON-NLS-1$
            wagon.putFromStream(pomStream, "org/overlord/sramp/test/foo/0.0.3/foo-artifact-0.0.3.pom"); //$NON-NLS-1$
            wagon.putFromStream(pomSHA1Stream, "org/overlord/sramp/test/foo/0.0.3/foo-artifact-0.0.3.pom.sha1"); //$NON-NLS-1$
        } finally {
            wagon.disconnect();
            IOUtils.closeQuietly(artifactStream);
            IOUtils.closeQuietly(pomStream);
            IOUtils.closeQuietly(artifactSHA1Stream);
            IOUtils.closeQuietly(pomSHA1Stream);
        }

        // Now that we've deployed the artifacts, do some queries to make sure we put away
        // what we intended.
        SrampAtomApiClient client = client("/s-ramp/"); //$NON-NLS-1$
        QueryResultSet rset = client.query("/s-ramp/core/Document"); //$NON-NLS-1$
        Assert.assertEquals(0, rset.size());
        rset = client.query("/s-ramp/ext/FooApplication"); //$NON-NLS-1$
        Assert.assertEquals(1, rset.size());
        rset = client.query("/s-ramp/ext/MavenPom"); //$NON-NLS-1$
        Assert.assertEquals(1, rset.size());

        rset = client.query("/s-ramp[@maven.groupId = 'org.overlord.sramp.test']"); //$NON-NLS-1$
        Assert.assertEquals(2, rset.size());
    }

    /**
     * Unit test.
     */
    @Test
    public void testWagonPushWithArtifactGrouping() throws Exception {
        SrampWagon wagon = new SrampWagon();
        setLogger(wagon);
        Repository repo = new Repository("sramp.repo", generateURL("/s-ramp/?artifactGrouping=MyArtifactGrouping").replaceAll("http", "sramp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        InputStream artifact1Stream = null;
        InputStream artifact2Stream = null;
        InputStream pom1Stream = null;
        InputStream pom2Stream = null;
        InputStream artifactSHA1Stream = null;
        InputStream artifactSHA2Stream = null;
        InputStream pomSHA1Stream = null;
        InputStream pomSHA2Stream = null;
        try {
            artifact1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar"); //$NON-NLS-1$
            artifact2Stream = getClass().getResourceAsStream("foo-artifact-0.0.3.txt"); //$NON-NLS-1$
            pom1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom"); //$NON-NLS-1$
            pom2Stream = getClass().getResourceAsStream("artifact-0.0.3.pom"); //$NON-NLS-1$
            artifactSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
            artifactSHA2Stream = getClass().getResourceAsStream("artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
            pomSHA1Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1"); //$NON-NLS-1$
            pomSHA2Stream = getClass().getResourceAsStream("artifact-0.0.3.pom.sha1"); //$NON-NLS-1$

            Assert.assertNotNull(artifact1Stream);
            Assert.assertNotNull(artifact2Stream);
            Assert.assertNotNull(pom1Stream);
            Assert.assertNotNull(pom2Stream);
            Assert.assertNotNull(artifactSHA1Stream);
            Assert.assertNotNull(artifactSHA2Stream);
            Assert.assertNotNull(pomSHA1Stream);
            Assert.assertNotNull(pomSHA2Stream);

            // Maven module/project 1
            connect(wagon, repo);
            wagon.putFromStream(artifact1Stream, "org/overlord/sramp/test/artifact/0.0.3/artifact-0.0.3.jar"); //$NON-NLS-1$
            wagon.putFromStream(artifactSHA1Stream, "org/overlord/sramp/test/artifact/0.0.3/artifact-0.0.3.jar.sha1"); //$NON-NLS-1$
            wagon.putFromStream(pom1Stream, "org/overlord/sramp/test/artifact/0.0.3/artifact-0.0.3.pom"); //$NON-NLS-1$
            wagon.putFromStream(pomSHA1Stream, "org/overlord/sramp/test/artifact/0.0.3/artifact-0.0.3.pom.sha1"); //$NON-NLS-1$
            wagon.disconnect();

            // Maven module/project 2
            connect(wagon, repo);
            wagon.putFromStream(artifact2Stream, "org/overlord/sramp/test/bar/0.0.3/bar-0.0.3.txt"); //$NON-NLS-1$
            wagon.putFromStream(artifactSHA2Stream, "org/overlord/sramp/test/bar/0.0.3/bar-0.0.3.txt.sha1"); //$NON-NLS-1$
            wagon.putFromStream(pom2Stream, "org/overlord/sramp/test/bar/0.0.3/bar-0.0.3.pom"); //$NON-NLS-1$
            wagon.putFromStream(pomSHA2Stream, "org/overlord/sramp/test/bar/0.0.3/bar-0.0.3.pom.sha1"); //$NON-NLS-1$
        } finally {
            wagon.disconnect();
            IOUtils.closeQuietly(artifact1Stream);
            IOUtils.closeQuietly(artifact2Stream);
            IOUtils.closeQuietly(pom1Stream);
            IOUtils.closeQuietly(pom2Stream);
            IOUtils.closeQuietly(artifactSHA1Stream);
            IOUtils.closeQuietly(artifactSHA2Stream);
            IOUtils.closeQuietly(pomSHA1Stream);
            IOUtils.closeQuietly(pomSHA2Stream);
        }

        // Now that we've deployed the artifacts, do some queries to make sure we put away
        // what we intended.
        SrampAtomApiClient client = client("/s-ramp/"); //$NON-NLS-1$
        QueryResultSet rset = client.query("/s-ramp[@maven.groupId = 'org.overlord.sramp.test']"); //$NON-NLS-1$
        Assert.assertEquals(4, rset.size());

        rset = client.query("/s-ramp/ext/ArtifactGrouping"); //$NON-NLS-1$
        Assert.assertEquals(1, rset.size());

        rset = client.query("/s-ramp/ext/ArtifactGrouping[@name = 'MyArtifactGrouping']"); //$NON-NLS-1$
        Assert.assertEquals(1, rset.size());
        ArtifactSummary artifactSummary = rset.get(0);
        Assert.assertEquals("MyArtifactGrouping", artifactSummary.getName()); //$NON-NLS-1$
        BaseArtifactType groupingArtifact = client.getArtifactMetaData(artifactSummary.getUuid());
        Assert.assertNotNull(groupingArtifact);
        Relationship relationship = SrampModelUtils.getGenericRelationship(groupingArtifact, "groups"); //$NON-NLS-1$
        Assert.assertNotNull(relationship);
        Assert.assertEquals(4, relationship.getRelationshipTarget().size());

        rset = client.query("/s-ramp[groupedBy[@name = 'MyArtifactGrouping']]"); //$NON-NLS-1$
        Assert.assertEquals(4, rset.size());
    }

	/**
	 * Unit test.
	 * @throws Exception
	 */
	@Test
	public void testWagonPull() throws Exception {
		// First, deploy some maven annotated artifacts
		SrampAtomApiClient client = client("/s-ramp/"); //$NON-NLS-1$
		InputStream archiveStream = null;
		SrampArchive archive = null;
		try {
			archiveStream = getClass().getResourceAsStream("sramp-archive.zip"); //$NON-NLS-1$
			archive = new SrampArchive(archiveStream);
			client.uploadBatch(archive);
		} finally {
			SrampArchive.closeQuietly(archive);
		}

		// Now test that the wagon can pull down the artifacts
		SrampWagon wagon = new SrampWagon();
		setLogger(wagon);
		Repository repo = new Repository("sramp.repo", generateURL("/s-ramp/").replaceAll("http", "sramp")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		connect(wagon, repo);

		File tempFile = File.createTempFile("s-ramp-wagon-test", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
            wagon.get("org/overlord/sramp/test/archive/maven-metadata.xml", tempFile); //$NON-NLS-1$
            Assert.assertTrue(tempFile.exists());
            Assert.assertTrue(tempFile.isFile());
			wagon.get("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar", tempFile); //$NON-NLS-1$
			assertContents("artifact-0.0.3.jar", tempFile); //$NON-NLS-1$
			tempFile.delete();
			wagon.get("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.jar.sha1", tempFile); //$NON-NLS-1$
			assertContents("artifact-0.0.3.jar.sha1", tempFile); //$NON-NLS-1$
			tempFile.delete();
            wagon.get("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom", tempFile); //$NON-NLS-1$
            tempFile.delete();
			wagon.get("org/overlord/sramp/test/archive/0.0.3/artifact-0.0.3.pom.sha1", tempFile); //$NON-NLS-1$
			assertContents("artifact-0.0.3.pom.sha1", tempFile); //$NON-NLS-1$
			tempFile.delete();
		} finally {
			if (tempFile.exists())
				tempFile.delete();
		}
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
			Assert.assertTrue("File contents failed to match: " + actual.getName(), //$NON-NLS-1$
					IOUtils.contentEquals(expectedStream, actualStream));
		} finally {
			IOUtils.closeQuietly(actualStream);
			IOUtils.closeQuietly(expectedStream);
		}
	}

	/**
	 * Sets the plexus logger on the wagon.
	 * @param wagon
	 */
	private void setLogger(SrampWagon wagon) throws Exception {
		Field field = wagon.getClass().getDeclaredField("logger"); //$NON-NLS-1$
		field.setAccessible(true);
		field.set(wagon, new ConsoleLogger(0, "logger")); //$NON-NLS-1$
	}
	
	private void connect(SrampWagon wagon, Repository repo) throws Exception {
	    AuthenticationInfo auth = new AuthenticationInfo();
        auth.setUserName(getUsername());
        auth.setPassword(getPassword());
        wagon.connect(repo, auth);
	}

}
