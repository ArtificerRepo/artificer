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

import java.io.File;

import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import javax.jcr.observation.Event;
import javax.jcr.observation.ObservationManager;

import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.repository.jcr.audit.AuditEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for the JCR repository.
 */
public abstract class JCRRepository {

    private static Logger log = LoggerFactory.getLogger(JCRRepository.class);
    private static Sramp sramp = new Sramp();

    private Session auditingSession;
    private AuditEventListener auditingEventListener;

    /**
     * Constructor.
     */
    public JCRRepository() {
    }

    /**
     * Method called to start and initialize the JCR implementation.
     */
    public final void startup() throws RepositoryException {
        doStartup();
        if (sramp.isAuditingEnabled()) {
            enableAuditing();
        }
    }

    /**
     * Starts up the repository.
     */
    protected abstract void doStartup() throws RepositoryException;

    /**
     * Method called when the JCR implementation is no longer needed.
     */
    public final void shutdown() {
        if (sramp.isAuditingEnabled())
            disableAuditing();
        doShutdown();
    }

    /**
     * Shuts down the repository.
     */
    protected abstract void doShutdown();

    /**
     * @return the JCR repository
     */
    public abstract Repository getRepo();

    /**
     * Enables auditing for the JCR repository.
     * @throws RepositoryException
     */
    private void enableAuditing() throws RepositoryException {
        // TODO option to disable auditing?
        // TODO ensure that the auditor user can be found in overlord-idp.properties
        // TODO need configurable values for auditor user creds
        auditingSession = getRepo().login(new SimpleCredentials("auditor", "overlord-auditor".toCharArray()));
        ObservationManager observationManager = auditingSession.getWorkspace().getObservationManager();
        auditingEventListener = new AuditEventListener(sramp, auditingSession);
        observationManager.addEventListener(
                auditingEventListener,
                Event.NODE_ADDED | Event.PROPERTY_ADDED | Event.PROPERTY_CHANGED | Event.PROPERTY_REMOVED,
                "/s-ramp/", true, null, null, false);
        log.info("JCR Auditor installed successfully.");
    }

    /**
     * Turns off auditing.
     */
    private void disableAuditing() {
        try {
            if (auditingSession != null && auditingEventListener != null) {
                // Wait for a bit to let any async audit tasks finish up
                try { Thread.sleep(2000); } catch (InterruptedException e) { }
                auditingSession.getWorkspace().getObservationManager().removeEventListener(auditingEventListener);
                auditingEventListener = null;
            }
        } catch (Exception e) {
            log.error("Error turning off auditing.", e);
        }
        if (auditingSession != null) {
            auditingSession.logout();
            auditingSession = null;
        }
    }

	/**
	 * Figures out what the current data directory is.  The data directory will be
	 * different depending on where we're running.  In an application server this
	 * code should strive to detect the app server specific data dir.
	 */
	public File determineRuntimeDataDir() {
		// Our property takes precedent if present
		String rootDataDir = System.getProperty("s-ramp.jcr.data.dir");
		// Check for JBoss
		if (rootDataDir == null) {
			rootDataDir = System.getProperty("jboss.server.data.dir");
		}
		// Default to "data/"
		if (rootDataDir == null) {
			rootDataDir = "data";
		}

		File root = new File(rootDataDir);
		File srampDataDir = new File(root, "s-ramp");
		if (!srampDataDir.exists()) {
			srampDataDir.mkdirs();
		}

		return srampDataDir;
	}

}
