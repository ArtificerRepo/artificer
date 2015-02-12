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
package org.artificer.server.atom.workspaces;

import java.util.List;

import org.artificer.repository.PersistenceManager;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.artificer.atom.MediaType;
import org.artificer.common.ArtificerException;
import org.artificer.repository.PersistenceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models the stored query workspace.
 *
 * @author Brett Meyer
 */
public class StoredQueryWorkspace extends AbstractWorkspace {

	private static final long serialVersionUID = 9119601241133543724L;
	
	private static Logger LOGGER = LoggerFactory.getLogger(StoredQueryWorkspace.class);

    /**
	 * Constructor.
	 * @param hrefBase
	 */
	public StoredQueryWorkspace(String hrefBase) {
		super(hrefBase, "Query Model"); //$NON-NLS-1$
	}

	@Override
	protected void configureWorkspace() {
        AppCollection collection = addCollection("/s-ramp/query", "Query Model", MediaType.APPLICATION_ZIP); //$NON-NLS-1$ //$NON-NLS-2$
        addCategory(collection, "query", "StoredQuery"); //$NON-NLS-1$ //$NON-NLS-2$
        
        // The spec requires that all queries in the system be listed as a collection.
        try {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            List<StoredQuery> storedQueries = persistenceManager.getStoredQueries();
            for (StoredQuery storedQuery : storedQueries) {
                AppCollection queryCollection = addCollection("/s-ramp/query/" + storedQuery.getQueryName(), //$NON-NLS-1$
                        storedQuery.getQueryName(), ""); //$NON-NLS-1$
                // I *think* this is necessary since /query/{name} can accept a PUT
                addCategory(queryCollection, "query", "StoredQuery"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        } catch (ArtificerException e) {
            LOGGER.error(Messages.i18n.format("ERROR_GETTING_STOREDQUERIES"), e); //$NON-NLS-1$
        }
	}
}
