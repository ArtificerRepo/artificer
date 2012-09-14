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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Models the archive format defined in the S-RAMP Atom Binding document.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampArchive {

	private File workDir;

	/**
	 * Creates a new, empty S-RAMP archive.
	 */
	public SrampArchive() throws SrampArchiveException {
		try {
			workDir = createWorkDir();
		} catch (IOException e) {
			throw new SrampArchiveException("Failed to create archive work directory", e);
		}
	}

	/**
	 * Creates an S-RAMP archive from an existing archive file.
	 * @param file
	 */
	public SrampArchive(File file) throws SrampArchiveException {
		this();
		try {
			unpackToWorkDir(file);
		} catch (IOException e) {
			throw new SrampArchiveException("Failed to unpack S-RAMP archive into work directory", e);
		}
	}

	/**
	 * Create the working directory for this archive.
	 * @throws IOException
	 */
	private static File createWorkDir() throws IOException {
		File tempFile = File.createTempFile("s-ramp-archive", ".work");
		tempFile.delete();
		tempFile.mkdir();
		return tempFile;
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	private void unpackToWorkDir(File file) throws IOException {
		ZipFile zipFile = new ZipFile(file);
		Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntriesInPhysicalOrder();
		while (zipEntries.hasMoreElements()) {
			ZipArchiveEntry entry = zipEntries.nextElement();
			String entryName = entry.getName();
			File outFile = new File(this.workDir, entryName);
			outFile.getParentFile().mkdirs();

			InputStream zipStream = null;
			OutputStream outFileStream = null;

			zipStream = zipFile.getInputStream(entry);
			outFileStream = new FileOutputStream(outFile);
			try {
				IOUtils.copy(zipStream, outFileStream);
			} finally {
				IOUtils.closeQuietly(zipStream);
				IOUtils.closeQuietly(outFileStream);
			}
		}
	}

	/**
	 * The S-RAMP archive should always be closed when the client is done with it.  This will
	 * clean up all temporary resources created by the archive.
	 * @throws IOException
	 */
	public void close() throws IOException {
		FileUtils.deleteDirectory(workDir);
	}

}
