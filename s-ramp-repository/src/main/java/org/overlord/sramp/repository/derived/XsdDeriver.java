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
package org.overlord.sramp.repository.derived;

import java.util.Collection;
import java.util.LinkedList;

import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * Creates derived content from an XSD document.  This will create the derived content as
 * defined in the XML Schema model found in the s-ramp specification.  The following derived
 * artifact types will (potentially) be created:
 *
 * <ul>
 *   <li>AttributeDeclaration</li>
 *   <li>ElementDeclaration</li>
 *   <li>ComplexTypeDeclaration</li>
 *   <li>SimpleTypeDeclaration</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class XsdDeriver implements ArtifactDeriver {

	/**
	 * Constructor.
	 */
	public XsdDeriver() {
	}

	/**
	 * @see org.overlord.sramp.repository.derived.ArtifactDeriver#derive(org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType)
	 */
	@Override
	public Collection<? extends DerivedArtifactType> derive(BaseArtifactType artifact) {
		Collection<? extends DerivedArtifactType> derivedArtifacts = new LinkedList<DerivedArtifactType>();
		return derivedArtifacts;
	}

}
