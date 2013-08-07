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
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.i18n.Messages;

/**
 * Models the archive format defined in the S-RAMP Atom Binding document.
 *
 * @author eric.wittmann@redhat.com
 */
public class SrampArchive {

	private File originalFile;
	private boolean shouldDeleteOriginalFile;
	private File workDir;

	/**
	 * Creates a new, empty S-RAMP archive.
	 * @throws SrampArchiveException
	 */
	public SrampArchive() throws SrampArchiveException {
		workDir = null;
		this.originalFile = null;
		try {
			workDir = createWorkDir();
		} catch (IOException e) {
			if (workDir != null && workDir.exists()) {
				try { FileUtils.deleteDirectory(workDir); } catch (IOException e1) { }
			}
			throw new SrampArchiveException(Messages.i18n.format("FAILED_TO_CREATE_WORK_DIR"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Creates an S-RAMP archive from an existing archive file.
	 * @param file
	 * @throws SrampArchiveException
	 */
	public SrampArchive(File file) throws SrampArchiveException {
		this();
		this.originalFile = file;
		this.shouldDeleteOriginalFile = false;
		try {
			ArchiveUtils.unpackToWorkDir(this.originalFile, this.workDir);
		} catch (IOException e) {
			if (this.workDir != null) {
				try { FileUtils.deleteDirectory(this.workDir); } catch (IOException e1) { }
			}
			throw new SrampArchiveException(Messages.i18n.format("FAILED_TO_UNPACK_ARCHIVE_TO_WORK_DIR"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Creates an S-RAMP archive from an {@link InputStream}.  This will consume and close the
	 * {@link InputStream}, creating a temporary local file that will be used as the basis for
	 * the archive input.
	 * @param input
	 * @throws SrampArchiveException
	 */
	public SrampArchive(InputStream input) throws SrampArchiveException {
		this();
		this.originalFile = null;
		this.shouldDeleteOriginalFile = true;

		try {
			this.originalFile = File.createTempFile("s-ramp-archive", ".zip"); //$NON-NLS-1$ //$NON-NLS-2$
			copyZipStream(input, this.originalFile);
			ArchiveUtils.unpackToWorkDir(this.originalFile, this.workDir);
		} catch (IOException e) {
			if (this.workDir != null) {
				try { FileUtils.deleteDirectory(this.workDir); } catch (IOException e1) { }
			}
			if (this.originalFile != null && this.originalFile.exists()) {
				this.originalFile.delete();
			}
			throw new SrampArchiveException(Messages.i18n.format("FAILED_TO_UNPACK_ARCHIVE_TO_WORK_DIR"), e); //$NON-NLS-1$
		}
	}

	/**
	 * Create the working directory for this archive.
	 * @throws IOException
	 */
	private static File createWorkDir() throws IOException {
		File tempFile = File.createTempFile("s-ramp-archive", ".work"); //$NON-NLS-1$ //$NON-NLS-2$
		tempFile.delete();
		tempFile.mkdir();
		return tempFile;
	}

	/**
	 * Copies the ZIP content from the input stream to the given output file.
	 * @param zipStream
	 * @param zipOutputFile
	 * @throws IOException
	 */
	private static void copyZipStream(InputStream zipStream, File zipOutputFile) throws IOException {
		OutputStream oStream = null;
		try {
			oStream = FileUtils.openOutputStream(zipOutputFile);
			IOUtils.copy(zipStream, oStream);
		} finally {
			IOUtils.closeQuietly(zipStream);
			IOUtils.closeQuietly(oStream);
		}
	}

	/**
	 * The S-RAMP archive should always be closed when the client is done with it.  This will
	 * clean up all temporary resources created by the archive.
	 * @throws IOException
	 */
	public void close() throws IOException {
		FileUtils.deleteDirectory(workDir);
		if (this.shouldDeleteOriginalFile) {
			this.originalFile.delete();
		}
	}

	/**
	 * Close the archive quietly (eat any {@link IOException}).
	 * @param archive
	 */
	public static void closeQuietly(SrampArchive archive) {
		try {
			if (archive != null)
				archive.close();
		} catch (IOException e) {
		}
	}

	/**
	 * Gets all of the entries found in this S-RAMP archive.  It does this by scanning the
	 * archive looking for all *.atom files.  One entry will be returned for each *.atom
	 * file found in the archive (assuming it has associated content and the *.atom file is
	 * properly formatted).
	 * @throws SrampArchiveException
	 */
	public Collection<SrampArchiveEntry> getEntries() throws SrampArchiveException {
		Collection<File> files = FileUtils.listFiles(workDir, new String[] { "atom" }, true); //$NON-NLS-1$
		Collection<SrampArchiveEntry> entries = new ArrayList<SrampArchiveEntry>(files.size());
		for (File metaDataFile : files) {
			String metaDataAbsPath = metaDataFile.getAbsolutePath();
			File contentFile = new File(metaDataAbsPath.substring(0, metaDataAbsPath.length() - 5));
			String path = contentFile.getAbsolutePath();
			path = path.substring(this.workDir.getAbsolutePath().length() + 1);
			path = path.replace('\\', '/'); // just in case we're in Windows :(
			entries.add(new SrampArchiveEntry(path, metaDataFile, contentFile));
		}
		return entries;
	}

	/**
	 * Gets the content {@link InputStream} for the given S-RAMP archive entry.
	 * @param entry the s-ramp archive entry
	 * @return an {@link InputStream} over the artifact content or null if no content found (meta-data only)
	 * @throws IOException
	 */
	public InputStream getInputStream(SrampArchiveEntry entry) throws IOException {
		File artifactPath = new File(this.workDir, entry.getPath());
		if (artifactPath.exists())
			return FileUtils.openInputStream(artifactPath);
		else
			return null;
	}

	/**
	 * Adds an entry to the S-RAMP archive.  This method will close the content
	 * {@link InputStream}.
	 * @param path the path in the archive (usually just the name of the artifact)
	 * @param metaData the artifact meta-data
	 * @param content the entry content (or null if a meta-data only entry)
	 * @throws SrampArchiveException
	 */
	public void addEntry(String path, BaseArtifactType metaData, InputStream content) throws SrampArchiveException {
		if (path == null)
			throw new SrampArchiveException(Messages.i18n.format("INVALID_ENTRY_PATH")); //$NON-NLS-1$
		if (metaData == null)
			throw new SrampArchiveException(Messages.i18n.format("MISSING_META_DATA")); //$NON-NLS-1$
		File metaDataFile = new File(this.workDir, path + ".atom"); //$NON-NLS-1$
		File contentFile = new File(this.workDir, path);
		if (metaDataFile.exists())
			throw new SrampArchiveException(Messages.i18n.format("ARCHIVE_ALREADY_EXISTS")); //$NON-NLS-1$
		// Create any required parent directories
		metaDataFile.getParentFile().mkdirs();
		if (content != null)
			writeContent(contentFile, content);
		try {
			SrampArchiveJaxbUtils.writeMetaData(metaDataFile, metaData);
		} catch (JAXBException e) {
			throw new SrampArchiveException(e);
		}
	}

	/**
	 * Updates an existing entry in the S-RAMP archive.  This method will close the content
	 * {@link InputStream}.
	 * @param entry the archive entry (or null if just udpating the content)
	 * @param content the entry content (or null if just updating meta data)
	 * @throws SrampArchiveException
	 */
	public void updateEntry(SrampArchiveEntry entry, InputStream content) throws SrampArchiveException {
		if (entry.getPath() == null)
			throw new SrampArchiveException(Messages.i18n.format("INVALID_ENTRY_PATH")); //$NON-NLS-1$
		File contentFile = new File(this.workDir, entry.getPath());
		File metaDataFile = new File(this.workDir, entry.getPath() + ".atom"); //$NON-NLS-1$

		if (content != null)
			writeContent(contentFile, content);
		if (entry.getMetaData() != null) {
			try {
				SrampArchiveJaxbUtils.writeMetaData(metaDataFile, entry.getMetaData());
			} catch (JAXBException e) {
				throw new SrampArchiveException(e);
			}
		}
	}

	/**
	 * Writes the artifact content to the given working path.
	 * @param workPath
	 * @param content
	 * @throws SrampArchiveException
	 */
	private void writeContent(File workPath, InputStream content) throws SrampArchiveException {
		OutputStream outStream = null;
		try {
			outStream = new FileOutputStream(workPath);
			IOUtils.copy(content, outStream);
		} catch (Throwable t) {
			throw new SrampArchiveException(Messages.i18n.format("ERROR_WRITING_CONTENT"), t); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(content);
			IOUtils.closeQuietly(outStream);
		}
	}

	/**
	 * Packs up the current contents of the S-RAMP archive into a single (.zip) file and
	 * returns a reference to it.  This method is guaranteed to either throw an Exception
	 * or return a valid {@link File}.  It will never throw and leave a temporary file
	 * behind.
	 * @throws SrampArchiveException
	 */
	public File pack() throws SrampArchiveException {
		try {
			File archiveFile = null;
			try {
				archiveFile = File.createTempFile("s-ramp-archive", ".sramp"); //$NON-NLS-1$ //$NON-NLS-2$
				FileOutputStream outputStream = FileUtils.openOutputStream(archiveFile);
				ZipOutputStream zipOutputStream = null;
				try {
					zipOutputStream = new ZipOutputStream(outputStream);
					Collection<SrampArchiveEntry> entries = getEntries();
					for (SrampArchiveEntry entry : entries) {
						packEntry(entry, zipOutputStream);
					}
				} finally {
					IOUtils.closeQuietly(zipOutputStream);
				}
			} catch (Throwable t) {
				// If anything goes wrong, make sure the File is cleaned up, as
				// we won't have another chance to do so.
				if (archiveFile != null && archiveFile.isFile())
					archiveFile.delete();
				throw t;
			}
			return archiveFile;
		} catch (Throwable t) {
			throw new SrampArchiveException(Messages.i18n.format("ERROR_PACKING_ARCHIVE"), t); //$NON-NLS-1$
		}
	}

	/**
	 * Pack the given S-RAMP archive entry into the ZIP.
	 * @param entry an s-ramp archive entry
	 * @param zipOutputStream the zip file
	 * @throws IOException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalAccessException
	 * @throws URISyntaxException
	 * @throws SecurityException
	 * @throws IllegalArgumentException
	 * @throws JAXBException
	 */
	private void packEntry(SrampArchiveEntry entry, ZipOutputStream zipOutputStream) throws IOException, IllegalArgumentException, SecurityException, URISyntaxException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, JAXBException {
		// Store the artifact content in the ZIP
		InputStream contentStream = getInputStream(entry);
		if (contentStream != null) {
			zipOutputStream.putNextEntry(new ZipEntry(entry.getPath()));
			try {
				IOUtils.copy(contentStream, zipOutputStream);
			} finally {
				IOUtils.closeQuietly(contentStream);
			}
			zipOutputStream.closeEntry();
		}

		// Store the meta-data in the ZIP
		zipOutputStream.putNextEntry(new ZipEntry(entry.getPath() + ".atom")); //$NON-NLS-1$
		try {
			SrampArchiveJaxbUtils.writeMetaData(zipOutputStream, entry.getMetaData());
		} finally {
		}
		zipOutputStream.closeEntry();
	}

	/**
	 * Gets a single entry in the archive by path.
	 * @param archivePath the path of the entry within the archive
	 * @return the archive entry, or null if not found
	 */
	public SrampArchiveEntry getEntry(String archivePath) {
		File contentFile = new File(this.workDir, archivePath);
		File metaDataFile = new File(this.workDir, archivePath + ".atom"); //$NON-NLS-1$
		SrampArchiveEntry rval = null;
		if (metaDataFile.exists()) {
			rval = new SrampArchiveEntry(archivePath, metaDataFile, contentFile);
		}
		return rval;
	}

	/**
	 * Returns true if the s-ramp archive contains an entry at the given path.
	 * @param archivePath path to the entry within the archive
	 * @return true if an entry exists at the path
	 */
	public boolean containsEntry(String archivePath) {
		File metaDataFile = new File(this.workDir, archivePath + ".atom"); //$NON-NLS-1$
		return metaDataFile.exists();
	}

	/**
	 * Removes the s-ramp archive entry at the given path if it exists.
	 * @param archivePath path to the entry within the archive
	 * @return true if an entry existed and was removed
	 */
	public boolean removeEntry(String archivePath) {
		File metaDataFile = new File(this.workDir, archivePath + ".atom"); //$NON-NLS-1$
		File contentFile = new File(this.workDir, archivePath);
		if (metaDataFile.isFile()) {
			metaDataFile.delete();
			if (contentFile.isFile()) {
				contentFile.delete();
			}
			return true;
		}
		return false;
	}

}
