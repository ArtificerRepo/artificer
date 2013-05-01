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

/**
 * Base class for the JCR repository.
 */
public abstract class JCRRepository {

    /**
     * Method called when the JCR implementation is no longer needed.
     */
    public abstract void shutdown();

    /**
     * Method called to start and initialize the JCR implementation.
     */
    public abstract void startup() throws RepositoryException;

    /**
     * @return the JCR repository
     */
    public abstract Repository getRepo();

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
