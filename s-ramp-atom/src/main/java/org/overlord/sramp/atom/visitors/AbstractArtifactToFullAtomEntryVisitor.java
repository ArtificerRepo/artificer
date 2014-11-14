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
package org.overlord.sramp.atom.visitors;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Visitor used to convert an artifact to an Atom entry.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractArtifactToFullAtomEntryVisitor extends AbstractArtifactToSummaryAtomEntryVisitor {

	/**
	 * Constructor.
	 */
	public AbstractArtifactToFullAtomEntryVisitor(String baseUrl) {
	    super(baseUrl);
	}

	/**
	 * @see org.overlord.sramp.common.atom.models.ArtifactToSummaryAtomEntryVisitor#includeArtifact()
	 */
	@Override
	protected boolean includeArtifact() {
		return true;
	}

	/**
	 * @see org.overlord.sramp.common.atom.models.ArtifactToSummaryAtomEntryVisitor#createIncludedArtifact(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType)
	 */
	@Override
	protected BaseArtifactType createIncludedArtifact(BaseArtifactType artifact)
			throws InstantiationException, IllegalAccessException {
		return artifact;
	}

}
