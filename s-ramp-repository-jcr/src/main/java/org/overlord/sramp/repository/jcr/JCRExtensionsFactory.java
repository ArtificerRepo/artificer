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

import javax.jcr.RepositoryException;

import org.overlord.commons.services.ServiceRegistryUtil;
import org.overlord.sramp.repository.jcr.i18n.Messages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JCRExtensionsFactory {

	private static Logger log = LoggerFactory.getLogger(JCRExtensionsFactory.class);
	private static JCRExtensions instance;

    /**
     * @return the discovered jcr extensions
     * @throws RepositoryException
     */
    public synchronized static JCRExtensions getInstance() throws RepositoryException {
        if (instance == null) {
            instance = ServiceRegistryUtil.getSingleService(JCRExtensions.class);
            if (instance == null)
                throw new RuntimeException(Messages.i18n.format("JCR_EXTENSIONS_NOT_FOUND"));
            log.info(Messages.i18n.format("JCR_EXTENSIONS", instance.getClass()));
        }
        return instance;
    }

    /**
     * Destroys the factory.  This causes the instance to be shut down.
     */
    public static synchronized void destroy() {
        instance = null;
    }

}
