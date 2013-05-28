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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.LinkerContext;

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
	public Collection<BaseArtifactType> deriveArtifacts(BaseArtifactType sourceArtifact,
			InputStream sourceArtifactContent) throws SrampException;

	/**
	 * Creates any relationships for the given artifacts.  This phase of the deriver provides
	 * an opportunity for the {@link ArtifactDeriver} to create relationships between the
	 * derived artifacts.
     * @param context
     * @param sourceArtifact
     * @param derivedArtifacts
     * @throws SrampException
     */
    public void linkArtifacts(LinkerContext context, BaseArtifactType sourceArtifact,
            Collection<BaseArtifactType> derivedArtifacts) throws SrampException;

}
