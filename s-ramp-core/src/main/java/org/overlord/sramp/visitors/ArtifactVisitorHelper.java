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
package org.overlord.sramp.visitors;

import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * Helper class used to visit S-RAMP artifacts.  This should be replaced by "accept" methods implemented
 * on each of the generated S-RAMP artifact classes.  I think there might be a jax-b plugin that will do
 * that, but I haven't checked into yet.  This is important because currently the order of the instanceof
 * checks below is vital to the proper visiting of the artifact, which is not ideal.
 * 
 * @author eric.wittmann@redhat.com
 */
public final class ArtifactVisitorHelper {

	/**
	 * Called to help the given visitor visit the provided artifact.
	 * @param visitor
	 * @param artifact
	 */
	public static void visitArtifact(ArtifactVisitor visitor, BaseArtifactType artifact) {
		// XSD Artifact Types
		if (artifact instanceof XsdDocument)
			visitor.visit((XsdDocument) artifact);
		else if (artifact instanceof AttributeDeclaration)
			visitor.visit((AttributeDeclaration) artifact);
		else if (artifact instanceof ElementDeclaration)
			visitor.visit((ElementDeclaration) artifact);
		else if (artifact instanceof SimpleTypeDeclaration)
			visitor.visit((SimpleTypeDeclaration) artifact);
		else if (artifact instanceof ComplexTypeDeclaration)
			visitor.visit((ComplexTypeDeclaration) artifact);

		// Core Artifact Types
		// WARNING:  the core types must be last due to artifact type hierarchy
		else if (artifact instanceof XmlDocument)
			visitor.visit((XmlDocument) artifact);
	}
	
}
