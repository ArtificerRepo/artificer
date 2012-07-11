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
package org.overlord.sramp.maven.repo;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Represents a path into the Maven repository facade.  This is typically created when 
 * an inbound request is being handled.  The HTTP request path is parsed into an instance
 * of this class and then handled.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenRepositoryPath {

	private String artifactModel;
	private String artifactType;
	private String artifactUuid;
	private String artifactVersion;
	private String artifactFileName;
	private String artifactExtension;
	
	/**
	 * Default constructor.
	 */
	public MavenRepositoryPath() {
	}

	/**
	 * @return the artifactModel
	 */
	public String getArtifactModel() {
		return artifactModel;
	}

	/**
	 * @param artifactModel the artifactModel to set
	 */
	public void setArtifactModel(String artifactModel) {
		this.artifactModel = artifactModel;
	}

	/**
	 * @return the artifactType
	 */
	public String getArtifactType() {
		return artifactType;
	}

	/**
	 * @param artifactType the artifactType to set
	 */
	public void setArtifactType(String artifactType) {
		this.artifactType = artifactType;
	}

	/**
	 * @return the artifactUuid
	 */
	public String getArtifactUuid() {
		return artifactUuid;
	}

	/**
	 * @param artifactUuid the artifactUuid to set
	 */
	public void setArtifactUuid(String artifactUuid) {
		this.artifactUuid = artifactUuid;
	}

	/**
	 * @return the artifactVersion
	 */
	public String getArtifactVersion() {
		return artifactVersion;
	}

	/**
	 * @param artifactVersion the artifactVersion to set
	 */
	public void setArtifactVersion(String artifactVersion) {
		this.artifactVersion = artifactVersion;
	}

	/**
	 * @return the artifactFileName
	 */
	public String getArtifactFileName() {
		return artifactFileName;
	}

	/**
	 * @param artifactFileName the artifactFileName to set
	 */
	public void setArtifactFileName(String artifactFileName) {
		this.artifactFileName = artifactFileName;
	}

	/**
	 * @return the artifactExtension
	 */
	public String getArtifactExtension() {
		return artifactExtension;
	}

	/**
	 * @param artifactExtension the artifactExtension to set
	 */
	public void setArtifactExtension(String artifactExtension) {
		this.artifactExtension = artifactExtension;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		List<String> segments = new LinkedList<String>();
		segments.add("");
		if (getArtifactModel() != null)
			segments.add(getArtifactModel());
		if (getArtifactType() != null)
			segments.add(getArtifactType());
		if (getArtifactUuid() != null)
			segments.add(getArtifactUuid());
		if (getArtifactVersion() != null)
			segments.add(getArtifactVersion());
		if (getArtifactFileName() != null) {
			String fname = getArtifactFileName();
			if (getArtifactExtension() != null)
				fname += "." + getArtifactExtension();
			segments.add(fname);
		} else {
			segments.add("");
		}
		
		return StringUtils.join(segments, "/");
	}
}
