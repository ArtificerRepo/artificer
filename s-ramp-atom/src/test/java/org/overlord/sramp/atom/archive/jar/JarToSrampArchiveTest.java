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
package org.overlord.sramp.atom.archive.jar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.archive.jar.ArtifactFilter;
import org.overlord.sramp.atom.archive.jar.CandidateArtifact;
import org.overlord.sramp.atom.archive.jar.DiscoveredArtifact;
import org.overlord.sramp.atom.archive.jar.JarToSrampArchive;
import org.overlord.sramp.atom.archive.jar.MetaDataFactory;

/**
 * Unit test for the {@link JarToSrampArchive} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class JarToSrampArchiveTest {

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.jar.client.jar.JarToSrampArchive#JarToSrampArchive(java.io.File)}.
	 */
	@Test
	public void testJarToSrampArchiveFile() throws Exception {
		InputStream resourceAsStream = null;
		File tempFile = null;
		FileOutputStream tempFileStream = null;
		JarToSrampArchive j2sramp = null;

		try {
			resourceAsStream = JarToSrampArchiveTest.class.getResourceAsStream("sample-webservice-0.0.1.jar");
			tempFile = File.createTempFile("j2sramp_test", ".jar");
			tempFileStream = FileUtils.openOutputStream(tempFile);
			IOUtils.copy(resourceAsStream, tempFileStream);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
			IOUtils.closeQuietly(tempFileStream);
		}

		try {
			j2sramp = new JarToSrampArchive(tempFile);

			File jarWorkDir = getJarWorkDir(j2sramp);
			Assert.assertNotNull(jarWorkDir);
			Assert.assertTrue(jarWorkDir.isDirectory());
			Collection<File> files = FileUtils.listFiles(jarWorkDir, new String[] {"xsd", "wsdl"}, true);
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("teetime.xsd"));
			Assert.assertTrue(fnames.contains("teetime.wsdl"));
		} finally {
			FileUtils.deleteQuietly(tempFile);
			JarToSrampArchive.closeQuietly(j2sramp);
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.jar.client.jar.JarToSrampArchive#JarToSrampArchive(java.io.InputStream)}.
	 */
	@Test
	public void testJarToSrampArchiveInputStream() throws Exception {
		InputStream resourceAsStream = JarToSrampArchiveTest.class.getResourceAsStream("sample-webservice-0.0.1.jar");
		JarToSrampArchive j2sramp = null;

		try {
			j2sramp = new JarToSrampArchive(resourceAsStream);

			File jarWorkDir = getJarWorkDir(j2sramp);
			Assert.assertNotNull(jarWorkDir);
			Assert.assertTrue(jarWorkDir.isDirectory());
			Collection<File> files = FileUtils.listFiles(jarWorkDir, new String[] {"xsd", "wsdl"}, true);
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("teetime.xsd"));
			Assert.assertTrue(fnames.contains("teetime.wsdl"));
		} finally {
			if (j2sramp != null)
				j2sramp.close();
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.jar.client.jar.JarToSrampArchive#createSrampArchive()}.
	 */
	@Test
	public void testCreateSrampArchive() throws Exception {
		InputStream resourceAsStream = JarToSrampArchiveTest.class.getResourceAsStream("sample-webservice-0.0.1.jar");
		JarToSrampArchive j2sramp = null;
		SrampArchive archive = null;

		try {
			j2sramp = new JarToSrampArchive(resourceAsStream);
			archive = j2sramp.createSrampArchive();
			Assert.assertNotNull(archive);
			Collection<SrampArchiveEntry> entries = archive.getEntries();
			Assert.assertEquals(2, entries.size());
			Set<String> paths = new HashSet<String>();
			for (SrampArchiveEntry entry : entries) {
				paths.add(entry.getPath());
			}
			Assert.assertEquals(2, entries.size());
			Assert.assertTrue(paths.contains("schema/teetime.xsd"));
			Assert.assertTrue(paths.contains("wsdl/teetime.wsdl"));
		} finally {
			JarToSrampArchive.closeQuietly(j2sramp);
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.jar.client.jar.JarToSrampArchive#setArtifactFilter(ArtifactFilter)}.
	 */
	@Test
	public void testCustomArtifactFilter() throws Exception {
		InputStream resourceAsStream = JarToSrampArchiveTest.class.getResourceAsStream("sample-webservice-0.0.1.jar");
		JarToSrampArchive j2sramp = null;
		SrampArchive archive = null;

		try {
			j2sramp = new JarToSrampArchive(resourceAsStream);
			j2sramp.setArtifactFilter(new ArtifactFilter() {
			    @Override
			    public void setContext(JarToSrampArchiveContext context) {
			    }
				@Override
				public boolean accepts(CandidateArtifact artifact) {
					return true;
				}
			});
			archive = j2sramp.createSrampArchive();
			Assert.assertNotNull(archive);
			Collection<SrampArchiveEntry> entries = archive.getEntries();
			Assert.assertEquals(16, entries.size());
			Set<String> paths = new HashSet<String>();
			for (SrampArchiveEntry entry : entries) {
				paths.add(entry.getPath());
			}
			Assert.assertEquals(16, entries.size());
			Assert.assertTrue(paths.contains("schema/teetime.xsd"));
			Assert.assertTrue(paths.contains("wsdl/teetime.wsdl"));
			Assert.assertTrue(paths.contains("com/redhat/ewittman/teetime/_2012/_09/wsdl/teetime_wsdl/TeeTimePortType.class"));
			Assert.assertTrue(paths.contains("META-INF/maven/com.redhat.ewittman/sample-web-service/pom.properties"));
		} finally {
			JarToSrampArchive.closeQuietly(j2sramp);
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.jar.client.jar.JarToSrampArchive#createSrampArchive()}.
	 */
	@Test
	public void testMetaData() throws Exception {
		InputStream resourceAsStream = JarToSrampArchiveTest.class.getResourceAsStream("sample-webservice-0.0.1.jar");
		JarToSrampArchive j2sramp = null;
		SrampArchive archive = null;

		try {
			j2sramp = new JarToSrampArchive(resourceAsStream);
			archive = j2sramp.createSrampArchive();
			Assert.assertNotNull(archive);
			SrampArchiveEntry entry = archive.getEntry("schema/teetime.xsd");
			Assert.assertNotNull(entry);
			BaseArtifactType metaData = entry.getMetaData();
			Assert.assertNotNull(metaData);
			Assert.assertTrue(metaData instanceof XsdDocument);
			XsdDocument md = (XsdDocument) metaData;
			Assert.assertEquals("teetime.xsd", md.getName());
			Assert.assertNotNull(md.getUuid());
		} finally {
			JarToSrampArchive.closeQuietly(j2sramp);
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.jar.client.jar.JarToSrampArchive#createSrampArchive()}.
	 */
	@Test
	public void testMetaDataFactory() throws Exception {
		InputStream resourceAsStream = JarToSrampArchiveTest.class.getResourceAsStream("sample-webservice-0.0.1.jar");
		JarToSrampArchive j2sramp = null;
		SrampArchive archive = null;

		try {
			j2sramp = new JarToSrampArchive(resourceAsStream);
			j2sramp.setMetaDataFactory(new MetaDataFactory() {
			    @Override
			    public void setContext(JarToSrampArchiveContext context) {
			    }
				@Override
				public BaseArtifactType createMetaData(DiscoveredArtifact artifact) {
					BaseArtifactType md = new Document();
					md.setUuid(UUID.randomUUID().toString());
					md.setName("custom-" + artifact.getName());
					return md;
				}
			});
			archive = j2sramp.createSrampArchive();
			Assert.assertNotNull(archive);
			SrampArchiveEntry entry = archive.getEntry("schema/teetime.xsd");
			Assert.assertNotNull(entry);
			BaseArtifactType metaData = entry.getMetaData();
			Assert.assertNotNull(metaData);
			Assert.assertTrue(metaData instanceof Document);
			Document md = (Document) metaData;
			Assert.assertEquals("custom-teetime.xsd", md.getName());
			Assert.assertNotNull(md.getUuid());
		} finally {
			JarToSrampArchive.closeQuietly(j2sramp);
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Gets the JAR working directory.
	 * @param j2sramp
	 * @return the private JAR working directory
	 * @throws Exception
	 */
	public static File getJarWorkDir(JarToSrampArchive j2sramp) throws Exception {
		Field field = j2sramp.getClass().getDeclaredField("jarWorkDir");
		boolean oldAccessible = field.isAccessible();
		field.setAccessible(true);
		File workDir = (File) field.get(j2sramp);
		field.setAccessible(oldAccessible);
		return workDir;
	}

}
