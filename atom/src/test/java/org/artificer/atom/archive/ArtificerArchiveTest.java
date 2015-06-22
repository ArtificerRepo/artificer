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
package org.artificer.atom.archive;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artificer.atom.test.AtomTestUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * JUnit test for the {@link ArtificerArchive} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerArchiveTest {

	@Test
	public void testSrampArchiveFile() throws Exception {
		InputStream resourceAsStream = null;
		File tempFile = null;
		FileOutputStream tempFileStream = null;
		ArtificerArchive archive = null;

		try {
			resourceAsStream = ArtificerArchiveTest.class.getResourceAsStream("simple-sramp-archive.zip");
			tempFile = File.createTempFile("sramp_test", "ar");
			tempFileStream = new FileOutputStream(tempFile);
			IOUtils.copy(resourceAsStream, tempFileStream);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
			IOUtils.closeQuietly(tempFileStream);
		}

		try {
			archive = new ArtificerArchive(tempFile);

			File workDir = AtomTestUtils.getArchiveWorkDir(archive);
			Assert.assertNotNull(workDir);
			Assert.assertTrue(workDir.isDirectory());
			Collection<File> files = FileUtils.listFiles(workDir, new String[] {"xsd", "atom"}, true);
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("sample.xsd"));
			Assert.assertTrue(fnames.contains("sample.xsd.atom"));
		} finally {
			FileUtils.deleteQuietly(tempFile);
			ArtificerArchive.closeQuietly(archive);
		}
	}

	@Test
	public void testGetEntries() throws Exception {
		InputStream resourceAsStream = null;
		File tempFile = null;
		FileOutputStream tempFileStream = null;
		ArtificerArchive archive = null;

		try {
			resourceAsStream = ArtificerArchiveTest.class.getResourceAsStream("simple-sramp-archive.zip");
			tempFile = File.createTempFile("sramp_test", "ar");
			tempFileStream = new FileOutputStream(tempFile);
			IOUtils.copy(resourceAsStream, tempFileStream);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
			IOUtils.closeQuietly(tempFileStream);
		}

		try {
			archive = new ArtificerArchive(tempFile);
			Collection<ArtificerArchiveEntry> entries = archive.getEntries();
			Assert.assertEquals(1, entries.size());
			ArtificerArchiveEntry entry = entries.iterator().next();
			Assert.assertEquals("simple-sramp-archive/sample.xsd", entry.getPath());
			Assert.assertNotNull(entry.getMetaData());
			Assert.assertEquals("d658b181-975c-42c5-ad5c-dc65cb9aa4a1", entry.getMetaData().getUuid());
			Assert.assertEquals("sample.xsd", entry.getMetaData().getName());
			Assert.assertEquals("1.0", entry.getMetaData().getVersion());
		} finally {
			FileUtils.deleteQuietly(tempFile);
			ArtificerArchive.closeQuietly(archive);
		}
	}

	@Test
	public void testAddEntry() throws Exception {
		InputStream artifactContentStream = ArtificerArchiveTest.class.getResourceAsStream("sample.xsd");
		XsdDocument artifactMetaData = new XsdDocument();
		setMetaData(artifactMetaData, "sample.xsd", "1.0.3", "Just a sample XML Schema.");

		ArtificerArchive archive = null;
		try {
			archive = new ArtificerArchive();
			archive.addEntry("sample.xsd", artifactMetaData, artifactContentStream);

			File workDir = AtomTestUtils.getArchiveWorkDir(archive);
			Assert.assertNotNull(workDir);
			Assert.assertTrue(workDir.isDirectory());
			Collection<File> files = FileUtils.listFiles(workDir, new String[] {"xsd", "atom"}, true);
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("sample.xsd"));
			Assert.assertTrue(fnames.contains("sample.xsd.atom"));
		} finally {
			ArtificerArchive.closeQuietly(archive);
		}
	}

	/**
	 * Full test of creating an empty archive, loading it up with artifacts, then reading it
	 * back out again.  As a side effect, this is the only test of the pack() method on the
	 * archive.
	 * @throws Exception
	 */
	@Test
	public void testArchiveRoundtrip() throws Exception {
		ArtificerArchive archive = null;
		File archiveFile = null;
		try {
			InputStream artifact1ContentStream = ArtificerArchiveTest.class.getResourceAsStream("sample.xsd");
			InputStream artifact2ContentStream = ArtificerArchiveTest.class.getResourceAsStream("PO.xsd");
			InputStream artifact3ContentStream = ArtificerArchiveTest.class.getResourceAsStream("coremodel.xsd");
			InputStream artifact4ContentStream = ArtificerArchiveTest.class.getResourceAsStream("xlink.xsd");

			String path1 = "sample/sample.xsd";
			String path2 = "sample/PO.xsd";
			String path3 = "s-ramp/coremodel.xsd";
			String path4 = "s-ramp/xlink.xsd";

			XsdDocument artifact1MetaData = new XsdDocument();
			XsdDocument artifact2MetaData = new XsdDocument();
			XsdDocument artifact3MetaData = new XsdDocument();
			XsdDocument artifact4MetaData = new XsdDocument();

			setMetaData(artifact1MetaData, "sample.xsd", "1.0.3", "Just a sample XML Schema.");
			setMetaData(artifact2MetaData, "PO.xsd", "2.1.4", "The Purchase Order schema.");
			setMetaData(artifact3MetaData, "coremodel.xsd", "1.6.1", "S-RAMP core schema.");
			setMetaData(artifact4MetaData, "xlink.xsd", "1.6.1", "X-LINK schema.");

			archive = new ArtificerArchive();
			archive.addEntry(path1, artifact1MetaData, artifact1ContentStream);
			archive.addEntry(path2, artifact2MetaData, artifact2ContentStream);
			archive.addEntry(path3, artifact3MetaData, artifact3ContentStream);
			archive.addEntry(path4, artifact4MetaData, artifact4ContentStream);

			archiveFile = archive.pack();
		} finally {
			ArtificerArchive.closeQuietly(archive);
		}

		// Now use the archive we just created, rip it apart and do some assertions
		try {
			archive = new ArtificerArchive(archiveFile);
			Collection<ArtificerArchiveEntry> entries = archive.getEntries();
			Assert.assertEquals(4, entries.size());
			Map<String, ArtificerArchiveEntry> entryMap = new HashMap<String, ArtificerArchiveEntry>();
			for (ArtificerArchiveEntry entry : entries)
				entryMap.put(entry.getPath(), entry);

			// Assertions for sample.xsd
			XsdDocument sampleXsdMetaData = (XsdDocument) entryMap.get("sample/sample.xsd").getMetaData();
			Assert.assertNotNull(sampleXsdMetaData);
			Assert.assertEquals("sample.xsd", sampleXsdMetaData.getName());
			Assert.assertEquals("1.0.3", sampleXsdMetaData.getVersion());
			Assert.assertEquals("Just a sample XML Schema.", sampleXsdMetaData.getDescription());
			// Assertions for PO.xsd
			XsdDocument poXsdMetaData = (XsdDocument) entryMap.get("sample/PO.xsd").getMetaData();
			Assert.assertNotNull(poXsdMetaData);
			Assert.assertEquals("PO.xsd", poXsdMetaData.getName());
			Assert.assertEquals("2.1.4", poXsdMetaData.getVersion());
			Assert.assertEquals("The Purchase Order schema.", poXsdMetaData.getDescription());
			// Assertions for coremodel.xsd
			XsdDocument coremodelXsdMetaData = (XsdDocument) entryMap.get("s-ramp/coremodel.xsd").getMetaData();
			Assert.assertNotNull(coremodelXsdMetaData);
			Assert.assertEquals("coremodel.xsd", coremodelXsdMetaData.getName());
			Assert.assertEquals("1.6.1", coremodelXsdMetaData.getVersion());
			Assert.assertEquals("S-RAMP core schema.", coremodelXsdMetaData.getDescription());
			// Assertions for xlink.xsd
			XsdDocument xlinkXsdMetaData = (XsdDocument) entryMap.get("s-ramp/xlink.xsd").getMetaData();
			Assert.assertNotNull(xlinkXsdMetaData);
			Assert.assertEquals("xlink.xsd", xlinkXsdMetaData.getName());
			Assert.assertEquals("1.6.1", xlinkXsdMetaData.getVersion());
			Assert.assertEquals("X-LINK schema.", xlinkXsdMetaData.getDescription());
		} finally {
			ArtificerArchive.closeQuietly(archive);
			FileUtils.deleteQuietly(archiveFile);
		}
	}

	/**
	 * Sets some meta data on the artifact model.
	 * @param artifactMetaData
	 * @param name
	 * @param version
	 * @param desc
	 */
	private void setMetaData(XsdDocument artifactMetaData, String name, String version, String desc) {
		artifactMetaData.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
		artifactMetaData.setName(name);
		artifactMetaData.setVersion(version);
		artifactMetaData.setDescription(desc);
	}

}
