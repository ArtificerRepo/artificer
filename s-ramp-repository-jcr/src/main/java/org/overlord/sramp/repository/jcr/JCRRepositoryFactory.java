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

import javax.jcr.Credentials;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

<<<<<<< HEAD
import org.overlord.commons.services.ServiceRegistryUtil;
=======
import org.overlord.commons.config.services.ServiceRegistryUtil;
>>>>>>> 5b3b6bf6b5acf60e408c713328b4a8cf7e491105
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRRepositoryFactory {

    public static String WORKSPACE_NAME = "default"; //$NON-NLS-1$
	private static Logger log = LoggerFactory.getLogger(JCRRepositoryFactory.class);

	private static JCRRepository instance;
	private static ThreadLocal<Credentials> loginCredentials = new ThreadLocal<Credentials>();
	public static void setLoginCredentials(Credentials credentials) {
	    loginCredentials.set(credentials);
	}
	public static void clearLoginCredentials() {
	    loginCredentials.remove();
	}

    public synchronized static JCRRepository getInstance() throws RepositoryException {
        if (instance == null) {
            instance = ServiceRegistryUtil.getSingleService(JCRRepository.class);
            if (instance == null)
                throw new RuntimeException(Messages.i18n.format("Failed to find a JCR Repository provider.")); //$NON-NLS-1$
            log.info(Messages.i18n.format("JCR_PROVIDER", instance.getClass())); //$NON-NLS-1$
            instance.startup();
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
        // Login with credentials set by some external force.  This may be a ServletFilter
        // when running in a servlet container, or it may be null when running in a JAAS
        // compliant application server (e.g. JBoss).

        // Note: when passing 'null', it forces ModeShape to authenticate with either
        // the anonymous auth provider (if configured) or any other auth provider that might
        // be configured *and* can accept null creds.  Typically this means the JAAS provider,
        // which should use the current JAAS subject in the absence of credentials.
        return getInstance().getRepo().login(loginCredentials.get(), WORKSPACE_NAME);
    }

    /**
     * Quietly logs out of the JCR session.
     * @param session
     */
    public static void logoutQuietly(Session session) {
        if (session != null) {
            try { session.logout(); } catch (Throwable t) { }
        }
    }


}
