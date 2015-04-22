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
package org.artificer.repository.test.jcr;

import org.artificer.repository.jcr.JCRRepositoryFactory;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

/**
 * Cleans the JCR repository to get it ready for a unit test.  This class
 * is used for each unit test so that they don't interfere with each other.
 *
 * @author eric.wittmann@redhat.com
 */
public class JCRRepositoryCleaner {

	/**
	 * Constructor.
	 */
	public JCRRepositoryCleaner() {
	}

	/**
	 * Called to clean the repository.
	 * @throws Exception
	 */
	public void clean() {
		Session session = null;
		try {
            session = JCRRepositoryFactory.getSession();
            Node artifactContentRoot = getNode(session, "/artifact");
            if (artifactContentRoot != null) {
	        	artifactContentRoot.remove();
            }
            Node srampRoot = getNode(session, "/s-ramp");
            if (srampRoot != null) {
	        	srampRoot.remove();
            }
        	session.save();
		} catch (PathNotFoundException e) {
			// The node doesn't exist - so no worries.
		} catch (Throwable t) {
			throw new RuntimeException(t);
		} finally {
			JCRRepositoryFactory.logoutQuietly(session);
		}
	}

	/**
	 * Gets a JCR node by path.  Returns null if the path doesn't exist.
	 * @param session
	 * @param path
	 * @throws javax.jcr.RepositoryException
	 */
	private Node getNode(Session session, String path) throws RepositoryException {
		try {
			return session.getNode(path);
		} catch (PathNotFoundException e) {
			return null;
		} catch (RepositoryException e) {
			throw e;
		}
	}

}
