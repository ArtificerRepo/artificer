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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * The collection of artifacts found during the discovery process.
 *
 * @author eric.wittmann@redhat.com
 */
public class DiscoveredArtifacts implements Iterable<DiscoveredArtifact> {

	private Set<DiscoveredArtifact> artifacts = new HashSet<DiscoveredArtifact>();
	private Map<String, DiscoveredArtifact> index = new HashMap<String, DiscoveredArtifact>();

	/**
	 * Constructor.
	 */
	public DiscoveredArtifacts() {
	}

	/**
	 * Adds a candidate artifact to the set of collected (accepted/discovered) artifacts.
	 * @param candidate
	 */
	public void add(File file) {
		DiscoveredArtifact artifact = new DiscoveredArtifact(file);
		getArtifacts().add(artifact);
	}

	/**
	 * Indexes all of the discovered artifacts using the given directory as the base directory
	 * (for determining canonical archive paths).
	 * @param workDir
	 */
	public void index(File workDir) {
		for (DiscoveredArtifact artifact : getArtifacts()) {
			String archivePath = determineArchivePath(workDir, artifact.getFile());
			artifact.setArchivePath(archivePath);
			index.put(archivePath, artifact);
		}
	}

	/**
	 * Figures out the path of the given file relative to the given directory.
	 * @param workDir
	 * @param file
	 */
	private String determineArchivePath(File workDir, File file) {
		String absWorkDirPath = workDir.getAbsolutePath();
		String absFilePath = file.getAbsolutePath();
		if (!absFilePath.startsWith(absFilePath)) {
			throw new RuntimeException("Failed to determine archive path for: " + file.getName());
		}
		String relativeFilePath = absFilePath.substring(absWorkDirPath.length());
		return relativeFilePath.replace('\\', '/');
	}

	/**
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<DiscoveredArtifact> iterator() {
		return getArtifacts().iterator();
	}

	/**
	 * @return the artifacts
	 */
	protected Set<DiscoveredArtifact> getArtifacts() {
		return artifacts;
	}

	/**
	 * @param artifacts the artifacts to set
	 */
	protected void setArtifacts(Set<DiscoveredArtifact> artifacts) {
		this.artifacts = artifacts;
	}

}
