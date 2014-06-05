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

import javax.jcr.Repository;
import javax.jcr.RepositoryException;

/**
 * Base class for the JCR repository.
 */
public abstract class JCRRepository {

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
    }

    /**
     * Starts up the repository.
     */
    protected abstract void doStartup() throws RepositoryException;

    /**
     * Method called when the JCR implementation is no longer needed.
     */
    public final void shutdown() {
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
     * @return the JCRExtensions
     */
    public abstract JCRExtensions getExtensions();

}
