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

import org.apache.commons.io.FileUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * An artifact that will be incuded in the final S-RAMP archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class DiscoveredArtifact {

	private File file;
	private String archivePath;
	private BaseArtifactType metaData;

	/**
	 * Constructor.
	 * @param file
	 */
	public DiscoveredArtifact(File file) {
		this.setFile(file);
	}

	/**
	 * @return the file
	 */
	public File getFile() {
		return file;
	}

	/**
	 * @param file the file to set
	 */
	public void setFile(File file) {
		this.file = file;
	}

	/**
	 * @return the archivePath
	 */
	public String getArchivePath() {
		return archivePath;
	}

	/**
	 * @param archivePath the archivePath to set
	 */
	public void setArchivePath(String archivePath) {
		this.archivePath = archivePath;
	}

	/**
	 * Returns an {@link InputStream} over the content of the artifact.
	 * @throws IOException
	 */
	public InputStream getContent() throws IOException {
		return FileUtils.openInputStream(file);
	}

	/**
	 * @return the metaData
	 */
	public BaseArtifactType getMetaData() {
		return metaData;
	}

	/**
	 * @param metaData the metaData to set
	 */
	public void setMetaData(BaseArtifactType metaData) {
		this.metaData = metaData;
	}

	/**
	 * Gets the short name of the artifact.
	 */
	public String getName() {
		return file.getName();
	}

	/**
	 * @return the artifact's file extension, or null if none found
	 */
	public String getExtension() {
	    String name = getName();
	    int idx = name.lastIndexOf('.');
	    if (idx > 0) {
	        return name.substring(idx+1);
	    } else {
	        return null;
	    }
	}

}
