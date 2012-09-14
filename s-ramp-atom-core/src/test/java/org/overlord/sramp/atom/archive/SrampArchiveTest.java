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
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import test.org.overlord.sramp.atom.AtomTestUtils;

/**
 * JUnit test for the {@link SrampArchive} class.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampArchiveTest {

	/**
	 * Test method for {@link org.overlord.sramp.atom.archive.SrampArchive#SrampArchive(java.io.File)}.
	 */
	@Test
	public void testSrampArchiveFile() throws Exception {
		InputStream resourceAsStream = null;
		File tempFile = null;
		FileOutputStream tempFileStream = null;
		SrampArchive archive = null;

		try {
			resourceAsStream = SrampArchiveTest.class.getResourceAsStream("simple-sramp-archive.zip");
			tempFile = File.createTempFile("sramp_test", "ar");
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
			Collection<File> files = FileUtils.listFiles(workDir, new String[] {"xsd", "atom"}, true);
			Assert.assertEquals(2, files.size());
			Set<String> fnames = new HashSet<String>();
			for (File f : files) {
				fnames.add(f.getName());
			}
			Assert.assertTrue(fnames.contains("sample.xsd"));
			Assert.assertTrue(fnames.contains("sample.xsd.atom"));
		} finally {
			tempFile.delete();
			if (archive != null)
				archive.close();
		}
	}

}
