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
package org.overlord.sramp.repository.jcr.util;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;
import javax.jcr.lock.LockException;
import javax.jcr.nodetype.ConstraintViolationException;
import javax.jcr.version.VersionException;

import org.modeshape.jcr.api.JcrConstants;

/**
 * Some common utils for working with JCR.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRUtils {
	
	/**
	 * Called to set the jcr:mimeType property on the given artifact.  The mime type property
	 * must be set on the nt:resource node, which is a child of the given artifact node.
	 * @param artifactNode
	 * @param mimeType
	 * @throws RepositoryException 
	 * @throws ConstraintViolationException 
	 * @throws LockException 
	 * @throws VersionException 
	 * @throws ValueFormatException 
	 */
	public static void setArtifactContentMimeType(Node artifactNode, String mimeType) throws ValueFormatException,
			VersionException, LockException, ConstraintViolationException, RepositoryException {
		Node resourceNode = artifactNode.getNode(JcrConstants.JCR_CONTENT);
		resourceNode.setProperty(JcrConstants.JCR_MIME_TYPE, mimeType);
	}

}
