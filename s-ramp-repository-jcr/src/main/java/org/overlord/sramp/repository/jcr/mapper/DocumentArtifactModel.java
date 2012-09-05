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
package org.overlord.sramp.repository.jcr.mapper;

import javax.jcr.Node;

import org.s_ramp.xmlns._2010.s_ramp.DocumentArtifactType;

/**
 * Handles mapping of document artifact meta data.
 *
 * @author eric.wittmann@redhat.com
 */
public class DocumentArtifactModel extends BaseArtifactModel {

	/**
	 * Maps the document artifact model meta data (from the JCR node to the s-ramp artifact).  This
	 * method does not map the base artifact meta data.
	 * @param jcrNode
	 * @param artifact
	 */
	protected static void mapDocumentArtifactMetaData(Node jcrNode, DocumentArtifactType artifact) {
        artifact.setContentSize(Long.valueOf(getProperty(jcrNode, "sramp:contentSize")));
        artifact.setContentType(getProperty(jcrNode, "sramp:contentType"));
	}

}
