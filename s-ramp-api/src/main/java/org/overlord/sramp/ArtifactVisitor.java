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
package org.overlord.sramp;

import org.s_ramp.xmlns._2010.s_ramp.AttributeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.ComplexTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ElementDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.SimpleTypeDeclaration;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * An interface for visiting an S-RAMP artifact.  This interface contains a visit method
 * for each type of artifact defined by S-RAMP.
 * 
 * @author eric.wittmann@redhat.com
 */
public interface ArtifactVisitor {

	public void visit(DocumentArtifactType artifact);

	public void visit(XmlDocument artifact);

	public void visit(XsdDocument artifact);

	public void visit(AttributeDeclaration artifact);

	public void visit(ElementDeclaration artifact);

	public void visit(SimpleTypeDeclaration artifact);

	public void visit(ComplexTypeDeclaration artifact);

}
