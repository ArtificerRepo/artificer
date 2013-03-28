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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.ArchiveUtils;
import org.overlord.sramp.atom.archive.SrampArchive;

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

    private static final ArtifactFilter DEFAULT_ARTIFACT_FILTER = new DefaultArtifactFilter();
    private static final MetaDataFactory DEFAULT_META_DATA_FACTORY = new DefaultMetaDataFactory();

	private File originalJar;
	private boolean shouldDeleteOriginalJar;
	private File jarWorkDir;

	/* Customizable handlers for various phases of the create. */

	private ArtifactFilter artifactFilter = DEFAULT_ARTIFACT_FILTER;
	private MetaDataFactory metaDataFactory = DEFAULT_META_DATA_FACTORY;
	private JarToSrampArchiveContext context;

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
			context = new JarToSrampArchiveContext(this.jarWorkDir);
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
            context = new JarToSrampArchiveContext(this.jarWorkDir);
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
	 * Creates an S-RAMP archive from this JAR.
	 * @return an S-RAMP archive
	 * @throws JarToSrampArchiveException
	 */
	public SrampArchive createSrampArchive() throws JarToSrampArchiveException {
	    this.artifactFilter.setContext(this.context);
	    this.metaDataFactory.setContext(this.context);
		DiscoveredArtifacts discoveredArtifacts = discoverArtifacts();
		discoveredArtifacts.index(jarWorkDir);
		generateMetaData(discoveredArtifacts);
		try {
			SrampArchive archive = new SrampArchive();
			for (DiscoveredArtifact artifact : discoveredArtifacts) {
				String path = artifact.getArchivePath();
				archive.addEntry(path, artifact.getMetaData(), artifact.getContent());
			}
			return archive;
		} catch (Exception e) {
			throw new JarToSrampArchiveException(e);
		}
	}

	/**
	 * Generates the meta data for all of the artifacts found during discovery.
	 * @param discoveredArtifacts
	 */
	private void generateMetaData(DiscoveredArtifacts discoveredArtifacts) {
		for (DiscoveredArtifact artifact : discoveredArtifacts) {
			BaseArtifactType metaData = this.metaDataFactory.createMetaData(artifact);
			artifact.setMetaData(metaData);
		}
	}

	/**
	 * Iterates through all of the entries in the JAR and determines which of them will
	 * be included in the S-RAMP archive.  Returns the collection of artifacts that should be
	 * included.
	 */
	private DiscoveredArtifacts discoverArtifacts() {
		DiscoveredArtifacts artifacts = new DiscoveredArtifacts();
		Collection<File> files = FileUtils.listFiles(jarWorkDir, null, true);
		for (File file : files) {
			CandidateArtifact candidate = new CandidateArtifact(file, jarWorkDir);
			if (this.artifactFilter.accepts(candidate)) {
				artifacts.add(file);
			}
		}

		return artifacts;
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

	/**
	 * @param artifactFilter the artifactFilter to set
	 */
	public void setArtifactFilter(ArtifactFilter artifactFilter) {
		this.artifactFilter = artifactFilter;
	}

	/**
	 * @return the configured artifact filter
	 */
	public ArtifactFilter getArtifactFilter() {
	    return this.artifactFilter;
	}

	/**
	 * @param metaDataFactory the metaDataFactory to set
	 */
	public void setMetaDataFactory(MetaDataFactory metaDataFactory) {
		this.metaDataFactory = metaDataFactory;
	}

	/**
	 * @return the configured meta-data factory
	 */
	public MetaDataFactory getMetaDataFactory() {
	    return this.metaDataFactory;
	}

	/**
	 * @param j2sramp
	 */
	public static void closeQuietly(JarToSrampArchive j2sramp) {
		try {
			if (j2sramp != null)
				j2sramp.close();
		} catch (IOException e) {
		}
	}

}
