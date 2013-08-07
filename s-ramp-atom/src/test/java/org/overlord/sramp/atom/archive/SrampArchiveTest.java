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
package org.overlord.sramp.atom.archive;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

import test.org.overlord.sramp.atom.AtomTestUtils;

/**
 * JUnit test for the {@link SrampArchive} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampArchiveTest {

	/**
	 * Test method for {@link org.overlord.sramp.common.atom.archive.SrampArchive#SrampArchive(java.io.File)}.
	 */
	@Test
	public void testSrampArchiveFile() throws Exception {
		InputStream resourceAsStream = null;
		File tempFile = null;
		FileOutputStream tempFileStream = null;
		SrampArchive archive = null;

		try {
			resourceAsStream = SrampArchiveTest.class.getResourceAsStream("simple-sramp-archive.zip"); //$NON-NLS-1$
			tempFile = File.createTempFile("sramp_test", "ar"); //$NON-NLS-1$ //$NON-NLS-2$
			tempFileStream = new FileOutputStream(tempFile);
			IOUtils.copy(resourceAsStream, tempFileStream);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
			IOUtils.closeQuietly(tempFileStream);
		}

		try {
			archive = new SrampArchive(tempFile);

			File workDir = AtomTestUtils.getArchiveWorkDir(archive);
			Assert.assertNotNull(workDir);
			Assert.assertTrue(workDir.isDirectory());
			Collection<File> files = FileUtils.listFiles(workDir, new String[] {"xsd", "atom"}, true); //$NON-NLS-1$ //$NON-NLS-2$
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("sample.xsd")); //$NON-NLS-1$
			Assert.assertTrue(fnames.contains("sample.xsd.atom")); //$NON-NLS-1$
		} finally {
			FileUtils.deleteQuietly(tempFile);
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.common.atom.archive.SrampArchive#getEntries()}.
	 */
	@Test
	public void testGetEntries() throws Exception {
		InputStream resourceAsStream = null;
		File tempFile = null;
		FileOutputStream tempFileStream = null;
		SrampArchive archive = null;

		try {
			resourceAsStream = SrampArchiveTest.class.getResourceAsStream("simple-sramp-archive.zip"); //$NON-NLS-1$
			tempFile = File.createTempFile("sramp_test", "ar"); //$NON-NLS-1$ //$NON-NLS-2$
			tempFileStream = new FileOutputStream(tempFile);
			IOUtils.copy(resourceAsStream, tempFileStream);
		} finally {
			IOUtils.closeQuietly(resourceAsStream);
			IOUtils.closeQuietly(tempFileStream);
		}

		try {
			archive = new SrampArchive(tempFile);
			Collection<SrampArchiveEntry> entries = archive.getEntries();
			Assert.assertEquals(1, entries.size());
			SrampArchiveEntry entry = entries.iterator().next();
			Assert.assertEquals("simple-sramp-archive/sample.xsd", entry.getPath()); //$NON-NLS-1$
			Assert.assertNotNull(entry.getMetaData());
			Assert.assertEquals("d658b181-975c-42c5-ad5c-dc65cb9aa4a1", entry.getMetaData().getUuid()); //$NON-NLS-1$
			Assert.assertEquals("sample.xsd", entry.getMetaData().getName()); //$NON-NLS-1$
			Assert.assertEquals("1.0", entry.getMetaData().getVersion()); //$NON-NLS-1$
		} finally {
			FileUtils.deleteQuietly(tempFile);
			SrampArchive.closeQuietly(archive);
		}
	}

	/**
	 * Test method for {@link org.overlord.sramp.common.atom.archive.SrampArchive#addEntry(String, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, InputStream)}.
	 */
	@Test
	public void testAddEntry() throws Exception {
		InputStream artifactContentStream = SrampArchiveTest.class.getResourceAsStream("sample.xsd"); //$NON-NLS-1$
		XsdDocument artifactMetaData = new XsdDocument();
		setMetaData(artifactMetaData, "sample.xsd", "1.0.3", "Just a sample XML Schema."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

		SrampArchive archive = null;
		try {
			archive = new SrampArchive();
			archive.addEntry("sample.xsd", artifactMetaData, artifactContentStream); //$NON-NLS-1$

			File workDir = AtomTestUtils.getArchiveWorkDir(archive);
			Assert.assertNotNull(workDir);
			Assert.assertTrue(workDir.isDirectory());
			Collection<File> files = FileUtils.listFiles(workDir, new String[] {"xsd", "atom"}, true); //$NON-NLS-1$ //$NON-NLS-2$
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("sample.xsd")); //$NON-NLS-1$
			Assert.assertTrue(fnames.contains("sample.xsd.atom")); //$NON-NLS-1$
		} finally {
			SrampArchive.closeQuietly(archive);
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
		SrampArchive archive = null;
		File archiveFile = null;
		try {
			InputStream artifact1ContentStream = SrampArchiveTest.class.getResourceAsStream("sample.xsd"); //$NON-NLS-1$
			InputStream artifact2ContentStream = SrampArchiveTest.class.getResourceAsStream("PO.xsd"); //$NON-NLS-1$
			InputStream artifact3ContentStream = SrampArchiveTest.class.getResourceAsStream("coremodel.xsd"); //$NON-NLS-1$
			InputStream artifact4ContentStream = SrampArchiveTest.class.getResourceAsStream("xlink.xsd"); //$NON-NLS-1$

			String path1 = "sample/sample.xsd"; //$NON-NLS-1$
			String path2 = "sample/PO.xsd"; //$NON-NLS-1$
			String path3 = "s-ramp/coremodel.xsd"; //$NON-NLS-1$
			String path4 = "s-ramp/xlink.xsd"; //$NON-NLS-1$

			XsdDocument artifact1MetaData = new XsdDocument();
			XsdDocument artifact2MetaData = new XsdDocument();
			XsdDocument artifact3MetaData = new XsdDocument();
			XsdDocument artifact4MetaData = new XsdDocument();

			setMetaData(artifact1MetaData, "sample.xsd", "1.0.3", "Just a sample XML Schema."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			setMetaData(artifact2MetaData, "PO.xsd", "2.1.4", "The Purchase Order schema."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			setMetaData(artifact3MetaData, "coremodel.xsd", "1.6.1", "S-RAMP core schema."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			setMetaData(artifact4MetaData, "xlink.xsd", "1.6.1", "X-LINK schema."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			archive = new SrampArchive();
			archive.addEntry(path1, artifact1MetaData, artifact1ContentStream);
			archive.addEntry(path2, artifact2MetaData, artifact2ContentStream);
			archive.addEntry(path3, artifact3MetaData, artifact3ContentStream);
			archive.addEntry(path4, artifact4MetaData, artifact4ContentStream);

			archiveFile = archive.pack();
		} finally {
			SrampArchive.closeQuietly(archive);
		}

		// Now use the archive we just created, rip it apart and do some assertions
		try {
			archive = new SrampArchive(archiveFile);
			Collection<SrampArchiveEntry> entries = archive.getEntries();
			Assert.assertEquals(4, entries.size());
			Map<String, SrampArchiveEntry> entryMap = new HashMap<String, SrampArchiveEntry>();
			for (SrampArchiveEntry entry : entries)
				entryMap.put(entry.getPath(), entry);

			// Assertions for sample.xsd
			XsdDocument sampleXsdMetaData = (XsdDocument) entryMap.get("sample/sample.xsd").getMetaData(); //$NON-NLS-1$
			Assert.assertNotNull(sampleXsdMetaData);
			Assert.assertEquals("sample.xsd", sampleXsdMetaData.getName()); //$NON-NLS-1$
			Assert.assertEquals("1.0.3", sampleXsdMetaData.getVersion()); //$NON-NLS-1$
			Assert.assertEquals("Just a sample XML Schema.", sampleXsdMetaData.getDescription()); //$NON-NLS-1$
			// Assertions for PO.xsd
			XsdDocument poXsdMetaData = (XsdDocument) entryMap.get("sample/PO.xsd").getMetaData(); //$NON-NLS-1$
			Assert.assertNotNull(poXsdMetaData);
			Assert.assertEquals("PO.xsd", poXsdMetaData.getName()); //$NON-NLS-1$
			Assert.assertEquals("2.1.4", poXsdMetaData.getVersion()); //$NON-NLS-1$
			Assert.assertEquals("The Purchase Order schema.", poXsdMetaData.getDescription()); //$NON-NLS-1$
			// Assertions for coremodel.xsd
			XsdDocument coremodelXsdMetaData = (XsdDocument) entryMap.get("s-ramp/coremodel.xsd").getMetaData(); //$NON-NLS-1$
			Assert.assertNotNull(coremodelXsdMetaData);
			Assert.assertEquals("coremodel.xsd", coremodelXsdMetaData.getName()); //$NON-NLS-1$
			Assert.assertEquals("1.6.1", coremodelXsdMetaData.getVersion()); //$NON-NLS-1$
			Assert.assertEquals("S-RAMP core schema.", coremodelXsdMetaData.getDescription()); //$NON-NLS-1$
			// Assertions for xlink.xsd
			XsdDocument xlinkXsdMetaData = (XsdDocument) entryMap.get("s-ramp/xlink.xsd").getMetaData(); //$NON-NLS-1$
			Assert.assertNotNull(xlinkXsdMetaData);
			Assert.assertEquals("xlink.xsd", xlinkXsdMetaData.getName()); //$NON-NLS-1$
			Assert.assertEquals("1.6.1", xlinkXsdMetaData.getVersion()); //$NON-NLS-1$
			Assert.assertEquals("X-LINK schema.", xlinkXsdMetaData.getDescription()); //$NON-NLS-1$
		} finally {
			SrampArchive.closeQuietly(archive);
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
