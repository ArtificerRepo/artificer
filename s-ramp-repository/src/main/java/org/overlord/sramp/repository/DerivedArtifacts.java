/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository;

import java.io.InputStream;
import java.util.Collection;

import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * A service that can rip apart an artifact (previously persisted) and create all of its Derived Artifacts.
 */
public interface DerivedArtifacts {

	/**
	 * Create the derived artifacts from the original artifact.
	 *
	 * @param sourceArtifact the source artifact's meta-data
	 * @param sourceArtifactContent the source artifact's content
	 */
	public Collection<DerivedArtifactType> deriveArtifacts(BaseArtifactType sourceArtifact,
			InputStream sourceArtifactContent) throws DerivedArtifactsCreationException;

}
