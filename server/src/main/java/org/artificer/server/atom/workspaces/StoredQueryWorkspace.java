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

import org.artificer.common.ArtificerException;
import org.artificer.common.MediaType;
import org.artificer.repository.PersistenceManager;
import org.artificer.repository.RepositoryProviderFactory;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
		super(hrefBase, "Query Model");
	}

	@Override
	protected void configureWorkspace() {
        AppCollection collection = addCollection("/s-ramp/query", "Query Model", MediaType.APPLICATION_ZIP);
        addCategory(collection, "query", "StoredQuery");
        
        // The spec requires that all queries in the system be listed as a collection.
        try {
            PersistenceManager persistenceManager = RepositoryProviderFactory.persistenceManager();
            List<StoredQuery> storedQueries = persistenceManager.getStoredQueries();
            for (StoredQuery storedQuery : storedQueries) {
                AppCollection queryCollection = addCollection("/s-ramp/query/" + storedQuery.getQueryName(),
                        storedQuery.getQueryName(), "");
                // I *think* this is necessary since /query/{name} can accept a PUT
                addCategory(queryCollection, "query", "StoredQuery");
            }
        } catch (ArtificerException e) {
            LOGGER.error(Messages.i18n.format("ERROR_GETTING_STOREDQUERIES"), e);
        }
	}
}
