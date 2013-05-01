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
package org.overlord.sramp.repository.jcr;

import java.util.ServiceLoader;

import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRRepositoryFactory {

    public static String WORKSPACE_NAME = "default";
	private static Logger log = LoggerFactory.getLogger(JCRRepositoryFactory.class);

	private static JCRRepository instance;

    public synchronized static JCRRepository getInstance() throws RepositoryException {
        if (instance == null) {
            for (JCRRepository jcrRepository : ServiceLoader.load(JCRRepository.class)) {
                log.info("Found JCR Provider " + jcrRepository.getClass());
                instance = jcrRepository;
                instance.startup();
                return instance;
            }
            throw new RuntimeException("Failed to find a JCR Repository provider.");
        }
        return instance;
    }

    /**
     * Destroys the factory.  This causes the instance to be shut down.
     */
    public static synchronized void destroy() {
        if (instance != null) {
            instance.shutdown();
        }
        instance = null;
    }

    /**
     * Convenience method for getting a JCR session from the repo singleton.
     * @throws LoginException
     * @throws NoSuchWorkspaceException
     * @throws RepositoryException
     */
    public static Session getSession() throws RepositoryException {
        // Login with null credentials.  This forces ModeShape to authenticate with either
        // the anonymous auth provider (if configured) or any other auth provider that might
        // be configured *and* can accept null creds.  Typically this means the JAAS provider,
        // which should use the current JAAS subject in the absence of credentials.
        return getInstance().getRepo().login(null, WORKSPACE_NAME);
    }

    /**
     * Quietly logs out of the JCR session.
     * @param session
     */
    public static void logoutQuietly(Session session) {
        if (session != null) {
            try {
                session.logout();
            } catch (Throwable t) {
                // eat it
            }
        }
    }


}
