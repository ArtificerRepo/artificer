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
package org.overlord.sramp.client.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.overlord.sramp.atom.archive.ArchiveUtils;

/**
 * Class that converts a JAR into an S-RAMP archive.
 *
 * This class performs the following tasks:
 *
 * <ul>
 *   <li>Crack open the JAR (or EAR, WAR, etc)</li>
 *   <li>Locate and index all S-RAMP artifacts</li>
 *   <li>Generate meta-data for all located artifacts</li>
 *   <li>Discover and record relationships between artifacts</li>
 *   <li>Create a new S-RAMP archive that includes the located artifacts</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class JarToSrampArchive {

	private File originalJar;
	private boolean shouldDeleteOriginalJar;
	private File jarWorkDir;

	/**
	 * Constructor.
	 * @param jar
	 * @throws JarToSrampArchiveException
	 */
	public JarToSrampArchive(File jar) throws JarToSrampArchiveException {
		this.originalJar = jar;
		this.shouldDeleteOriginalJar = false;
		this.jarWorkDir = null;

		try {
			jarWorkDir = createJarWorkDir();
			ArchiveUtils.unpackToWorkDir(this.originalJar, this.jarWorkDir);
		} catch (IOException e) {
			if (this.jarWorkDir != null) {
				try { FileUtils.deleteDirectory(this.jarWorkDir); } catch (IOException e1) { }
			}
			throw new JarToSrampArchiveException(e);
		}
	}

	/**
	 * Constructor from JAR input stream.  Note, this will consume and close the given {@link InputStream}.
	 * @param jarStream
	 * @throws JarToSrampArchiveException
	 */
	public JarToSrampArchive(InputStream jarStream) throws JarToSrampArchiveException {
		this.originalJar = null;
		this.shouldDeleteOriginalJar = true;
		this.jarWorkDir = null;

		try {
			this.originalJar = File.createTempFile("j2sramp", ".jar");
			copyJarStream(jarStream, this.originalJar);
			jarWorkDir = createJarWorkDir();
			ArchiveUtils.unpackToWorkDir(this.originalJar, this.jarWorkDir);
		} catch (IOException e) {
			if (this.jarWorkDir != null) {
				try { FileUtils.deleteDirectory(this.jarWorkDir); } catch (IOException e1) { }
			}
			if (this.originalJar != null && this.originalJar.exists()) {
				this.originalJar.delete();
			}
			throw new JarToSrampArchiveException(e);
		}
	}

	/**
	 * Copies the JAR content from the input stream to the given output file.
	 * @param jarStream
	 * @param jarOutputFile
	 * @throws IOException
	 */
	private static void copyJarStream(InputStream jarStream, File jarOutputFile) throws IOException {
		OutputStream oStream = null;
		try {
			oStream = FileUtils.openOutputStream(jarOutputFile);
			IOUtils.copy(jarStream, oStream);
		} finally {
			IOUtils.closeQuietly(jarStream);
			IOUtils.closeQuietly(oStream);
		}
	}

	/**
	 * Create the working directory for the JAR.
	 * @throws IOException
	 */
	private static File createJarWorkDir() throws IOException {
		File tempFile = File.createTempFile("j2sramp", ".work");
		tempFile.delete();
		tempFile.mkdir();
		return tempFile;
	}

	/**
	 * This object should be closed once we're done with it.  This allows us to clean up
	 * some resources.
	 * @throws IOException
	 */
	public void close() throws IOException {
		FileUtils.deleteDirectory(jarWorkDir);
		if (shouldDeleteOriginalJar) {
			this.originalJar.delete();
		}
	}

}
