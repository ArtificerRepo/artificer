/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.repository.jcr.modeshape;

import org.modeshape.jcr.api.ServletCredentials;
import org.overlord.sramp.repository.jcr.JCRExtensions;
import org.overlord.sramp.repository.jcr.JCRRepositoryFactory;

import javax.jcr.Binary;
import javax.servlet.http.HttpServletRequest;

/**
 * The ModeShape-specific implementation of the JCR Extensions, allowing ModeShape
 * specific enhancements to the JCR persistence layer.
 * 
 * @author Brett Meyer
 */
public class ModeshapeJCRExtensions extends JCRExtensions {
    
    /**
     * Constructor.
     */
    public ModeshapeJCRExtensions() {
    }

    /**
     * @see org.overlord.sramp.repository.jcr.JCRExtensions#getSha1Hash(javax.jcr.Binary)
     */
    @Override
    public String getSha1Hash(Binary binary) throws Exception {
        if (binary instanceof org.modeshape.jcr.api.Binary) {
            return ((org.modeshape.jcr.api.Binary) binary).getHexHash();
        } else {
            return super.getSha1Hash(binary);
        }
    }

    /**
     * @see org.overlord.sramp.repository.jcr.JCRExtensions#startup()
     */
    @Override
    public void startup() {
        // Set credentials (manufactured for full privileges)
        HttpServletRequest request = new ModeshapeStartupHttpServletRequest();
        ServletCredentials credentials = new ServletCredentials((HttpServletRequest) request);
        JCRRepositoryFactory.setLoginCredentials(credentials);

        try {
            JCRRepositoryFactory.getSession();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        } finally {
            JCRRepositoryFactory.setLoginCredentials(null);
        }
    }
}
