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

/**
 * An artifact that *may* get included in the S-RAMP archive being created from a
 * source JAR archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class CandidateArtifact {

	private File file;

	/**
	 * Constructor.
	 * @param file
	 */
	public CandidateArtifact(File file) {
		this.file = file;
	}

	/**
	 * Gets the name of the candidate artifact.
	 */
	public String getName() {
		return file.getName();
	}

}
