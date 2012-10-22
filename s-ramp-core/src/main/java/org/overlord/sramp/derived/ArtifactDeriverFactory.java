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
package org.overlord.sramp.derived;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.ArtifactTypeEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * Factory used to create an {@link ArtifactDeriver} for a particular type of artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtifactDeriverFactory {

	private static Map<ArtifactTypeEnum, ArtifactDeriver> derivers = new HashMap<ArtifactTypeEnum, ArtifactDeriver>();
	static {
		derivers.put(ArtifactTypeEnum.XsdDocument, new XsdDeriver());
		derivers.put(ArtifactTypeEnum.WsdlDocument, new WsdlDeriver());
		derivers.put(ArtifactTypeEnum.PolicyDocument, new PolicyDeriver());
	}
	private static ArtifactDeriver nullDeriver = new ArtifactDeriver() {
		@SuppressWarnings("unchecked")
		@Override
		public Collection<DerivedArtifactType> derive(BaseArtifactType artifact, InputStream content) {
			return Collections.EMPTY_SET;
		}
	};

	/**
	 * Creates an artifact deriver for a specific type of artifact.
	 * @param artifactType type of s-ramp artifact
	 * @return an artifact deriver
	 */
	public final static ArtifactDeriver createArtifactDeriver(ArtifactType artifactType) {
		ArtifactDeriver deriver = derivers.get(artifactType.getArtifactType());
		if (deriver == null) {
			deriver = nullDeriver;
		}
		return deriver;
	}

}
