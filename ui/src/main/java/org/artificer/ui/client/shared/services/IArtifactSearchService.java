/*
 * Copyright 2013 JBoss Inc
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
package org.artificer.ui.client.shared.services;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.artificer.ui.client.shared.beans.ArtifactFilterBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;
import org.artificer.ui.client.shared.beans.ArtifactResultSetBean;
import org.artificer.ui.client.shared.beans.ArtifactSearchBean;

/**
 * Provides a way to search for artifacts.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("artifacts")
public interface IArtifactSearchService {

    /**
     * Search for artifacts using the given query and pagination.
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @POST
    @Path("search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ArtifactResultSetBean search(ArtifactSearchBean searchBean) throws ArtificerUiException;

    /**
     * Generate a query string using the given filters.
     *
     * Note: Theoretically, query generation could be client-side.  However, numerous conveniences are used (Calendar, etc.)
     * that are not supported by GWT emulation.  So, for now, offloading to the server-side services.
     *
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @POST
    @Path("query")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String query(ArtifactFilterBean filterBean) throws ArtificerUiException;

}
