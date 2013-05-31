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

import java.util.HashSet;
import java.util.Set;

/**
 * A default implementation of an {@link ArtifactFilter} that includes only those files
 * that are likely to be understood by the S-RAMP server (XML, Schema, WSDL, etc).  This
 * class can serve as either a reasonable default or as a good starting point for a more
 * nuanced implementation.
 *
 * @author eric.wittmann@redhat.com
 */
public class DefaultArtifactFilter implements ArtifactFilter {

	private static Set<String> validExtensions = new HashSet<String>();
	static {
		validExtensions.add("xml");
		validExtensions.add("xsd");
		validExtensions.add("wsdl");
		validExtensions.add("wspolicy");
	}
	private static Set<String> exclusions = new HashSet<String>();
	static {
		exclusions.add("pom.xml");
	}

	/**
	 * Constructor.
	 */
	public DefaultArtifactFilter() {
	}

	/**
	 * @see org.overlord.sramp.atom.archive.jar.ArtifactFilter#setContext(org.overlord.sramp.atom.archive.jar.JarToSrampArchiveContext)
	 */
	@Override
	public void setContext(JarToSrampArchiveContext context) {
	    // Don't need it
	}

	/**
	 * @see org.overlord.sramp.atom.archive.jar.client.jar.ArtifactFilter#accepts(org.overlord.sramp.atom.archive.jar.client.jar.CandidateArtifact)
	 */
	@Override
	public boolean accepts(CandidateArtifact artifact) {
		String name = artifact.getName();
		if (getExclusions().contains(name)) {
			return false;
		}
		String ext = null;
		if (name.contains(".")) {
			ext = name.substring(name.lastIndexOf('.') + 1);
		}
		if (ext != null) {
			return getValidExtensions().contains(ext);
		} else {
			return false;
		}
	}

	/**
	 * Returns the set of file names that should be excluded from the s-ramp archive.
	 */
	private Set<String> getExclusions() {
		return exclusions;
	}

	/**
	 * Returns the {@link Set} of valid extensions.
	 */
	private Set<String> getValidExtensions() {
		return validExtensions;
	}

}
