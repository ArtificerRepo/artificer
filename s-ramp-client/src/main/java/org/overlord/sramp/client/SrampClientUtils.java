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
package org.overlord.sramp.client;

import org.overlord.sramp.ArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Some useful static utils for users of the s-ramp client.
 *
 * @author eric.wittmann@redhat.com
 */
public final class SrampClientUtils {

	/**
	 * Private constructor.
	 */
	private SrampClientUtils() {
	}
	
	/**
	 * Unwraps the specific {@link BaseArtifactType} from the S-RAMP Artifact wrapper
	 * element.  This method requires the artifact's type.
	 * @param artifactType the s-ramp artifact type
	 * @param artifact the s-ramp wrapper {@link Artifact}
	 * @return a {@link BaseArtifactType}
	 */
	public static BaseArtifactType unwrapSrampArtifact(String artifactType, Artifact artifact) {
		return unwrapSrampArtifact(ArtifactType.valueOf(artifactType), artifact);
	}

	/**
	 * Unwraps the specific {@link BaseArtifactType} from the S-RAMP Artifact wrapper
	 * element.  This method requires the artifact's type.
	 * @param artifactType the s-ramp artifact type
	 * @param artifact the s-ramp wrapper {@link Artifact}
	 * @return a {@link BaseArtifactType}
	 */
	public static BaseArtifactType unwrapSrampArtifact(ArtifactType artifactType, Artifact artifact) {
		return artifactType.unwrap(artifact);
	}

}
