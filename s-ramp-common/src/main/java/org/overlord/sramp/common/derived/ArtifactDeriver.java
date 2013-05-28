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
package org.overlord.sramp.common.derived;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Provides a way to derive artifacts.  Classes that implement this interface must
 * be able to parse a particular type of artifact and produce all of the derived
 * artifacts for it.  Examples include an XSD deriver, WSDL deriver, etc.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ArtifactDeriver {

	/**
	 * Given an artifact, this method will return a collection of derived content
	 * for it.
	 * @param artifact the artifact to derive
	 * @param contentStream the artifact content
	 * @return derived content
	 * @throws IOException
	 */
	public Collection<BaseArtifactType> derive(BaseArtifactType artifact, InputStream contentStream)
			throws IOException;

	/**
	 * This method represents the "linker" phase of deriving artifact content.  The
	 * linker phase is responsible for creating any relationships between and among
	 * the derived artifacts.
	 *
	 * @param context
	 * @param sourceArtifact
	 * @param derivedArtifacts
	 */
	public void link(LinkerContext context, BaseArtifactType sourceArtifact, Collection<BaseArtifactType> derivedArtifacts);

}
