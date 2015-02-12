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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.artificer.atom.i18n.Messages;

/**
 * Some general porpoise utils for working with archives.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArchiveUtils {

	/**
	 * Unpacks the given archive file into the output directory.
	 * @param archiveFile an archive file
	 * @param toDir where to unpack the archive to
	 * @throws IOException
	 */
	public static void unpackToWorkDir(File archiveFile, File toDir) throws IOException {
		ZipFile zipFile = null;
		try {
			zipFile = new ZipFile(archiveFile);
			Enumeration<ZipArchiveEntry> zipEntries = zipFile.getEntriesInPhysicalOrder();
			while (zipEntries.hasMoreElements()) {
				ZipArchiveEntry entry = zipEntries.nextElement();
				String entryName = entry.getName();
				File outFile = new File(toDir, entryName);
				if (!outFile.getParentFile().exists()) {
					if (!outFile.getParentFile().mkdirs()) {
						throw new IOException(Messages.i18n.format("FAILED_TO_CREATE_PARENT_DIR", outFile.getParentFile().getCanonicalPath())); //$NON-NLS-1$
					}
				}

				if (entry.isDirectory()) {
				    if (outFile.isDirectory()) {
				        // Do nothing - already created.
				    } else if (outFile.isFile()) {
                        throw new IOException(Messages.i18n.format("FAILED_TO_CREATE_DIR", outFile.getCanonicalPath())); //$NON-NLS-1$
				    } else if (!outFile.mkdir()) {
						throw new IOException(Messages.i18n.format("FAILED_TO_CREATE_DIR", outFile.getCanonicalPath())); //$NON-NLS-1$
					}
				} else {
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
		} finally {
			ZipFile.closeQuietly(zipFile);
		}
	}

}
