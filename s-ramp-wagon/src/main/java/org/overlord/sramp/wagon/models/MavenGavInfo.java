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
package org.overlord.sramp.wagon.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.wagon.resource.Resource;

/**
 * GAV info for maven.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenGavInfo {
	
	/**
	 * <p>Parses the resource name and returns the GAV information.  An example of
	 * a Resource that might be passed in is:</p>
	 * <br/>
	 * <b>Format 1:</b> <code>org/example/schema/my-schema/1.3/my-schema-1.3.xsd</code><br/>
	 * <b>Format 2:</b> <code>xsd/XsdDocument/29873-21983-2497822-1989/1.0/29873-21983-2497822-1989-1.0.pom</code>
	 * 
	 * @param resource the Wagon {@link Resource}
	 * @return the maven GAV info
	 */
	public static MavenGavInfo fromResource(Resource resource) {
		String resourceName = resource.getName();
		List<String> segments = new ArrayList<String>(Arrays.asList(resourceName.split("/")));
		String filename = segments.remove(segments.size() - 1);
		String type = filename.substring(filename.lastIndexOf('.') + 1);
		if (filename.endsWith(".sha1")) {
			type = filename.substring(0, filename.length() - 5);
			type = type.substring(type.lastIndexOf('.') + 1) + ".sha1";
		}
		String version = segments.remove(segments.size() - 1);
		String artifactId = segments.remove(segments.size() - 1);
		String groupId = StringUtils.join(segments, ".");
		
		MavenGavInfo gav = new MavenGavInfo();
		gav.setGroupId(groupId);
		gav.setArtifactId(artifactId);
		gav.setVersion(version);
		gav.setType(type);
		return gav;
	}

	private String groupId;
	private String artifactId;
	private String version;
	private String type;

	/**
	 * Default constructor.
	 */
	public MavenGavInfo() {
	}

	/**
	 * @return the groupId
	 */
	public String getGroupId() {
		return groupId;
	}

	/**
	 * @param groupId the groupId to set
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	/**
	 * @return the artifactId
	 */
	public String getArtifactId() {
		return artifactId;
	}

	/**
	 * @param artifactId the artifactId to set
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("groupId: ");
		builder.append(getGroupId());
		builder.append("  artifactId: ");
		builder.append(getArtifactId());
		builder.append("  version: ");
		builder.append(getVersion());
		builder.append("  type: ");
		builder.append(getType());
		return builder.toString();
	}
	
}
