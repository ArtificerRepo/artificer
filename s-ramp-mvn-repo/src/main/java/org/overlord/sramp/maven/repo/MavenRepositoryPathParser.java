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

/**
 * Parses a requested path into its component pieces.
 *
 * @author eric.wittmann@redhat.com
 */
public class MavenRepositoryPathParser {

	/**
	 * Default constructor.
	 */
	public MavenRepositoryPathParser() {
	}
	
	/**
	 * Parses a string Maven repository path into an object ({@link MavenRepositoryPath}).
	 * @param path the repository path (typically from an inbound http request)
	 * @return a parsed version of the repository path
	 */
	public MavenRepositoryPath parse(String path) {
		MavenRepositoryPath repoPath = new MavenRepositoryPath();
		if (path != null) {
			String[] pathSegments = path.split("/");
			if (pathSegments.length >= 2)
				repoPath.setArtifactModel(pathSegments[1]);
			if (pathSegments.length >= 3)
				repoPath.setArtifactType(pathSegments[2]);
			if (pathSegments.length >= 4)
				repoPath.setArtifactUuid(pathSegments[3]);
			if (pathSegments.length >= 5)
				repoPath.setArtifactVersion(pathSegments[4]);
			if (pathSegments.length >= 6) {
				String file = pathSegments[5];
				String[] fileSegments = file.split("\\.");
				String fname = fileSegments[0];
				String fext = null;
				if (fileSegments.length >= 2)
					fext = fileSegments[1];
				repoPath.setArtifactFileName(fname);
				repoPath.setArtifactExtension(fext);
			}
		}
		return repoPath;
	}
	
}
