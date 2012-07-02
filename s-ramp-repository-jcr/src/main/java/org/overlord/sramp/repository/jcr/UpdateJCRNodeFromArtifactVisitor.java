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
package org.overlord.sramp.repository.jcr;

import javax.jcr.Node;

import org.overlord.sramp.ArtifactVisitorAdapter;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * An artifact visitor used to update a JCR node.  This class is responsible
 * for modifying a JCR node using information found in the supplied s-ramp
 * artifact.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateJCRNodeFromArtifactVisitor extends ArtifactVisitorAdapter {

	@SuppressWarnings("unused")
	private Node jcrNode;
	
	/**
	 * Constructor.
	 * @param jcrNode the JCR node this visitor will be updating
	 */
	public UpdateJCRNodeFromArtifactVisitor(Node jcrNode) {
		this.jcrNode = jcrNode;
	}
	
	/**
	 * Called to perform all common base artifact updates (e.g. classifiedBy and properties).
	 * @param artifact the artifact being visited
	 */
	protected void visitBaseArtifact(BaseArtifactType artifact) {
		
	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitorAdapter#visit(org.s_ramp.xmlns._2010.s_ramp.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
		visitBaseArtifact(artifact);
	}

}
