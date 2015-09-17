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
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.artificer.ui.client.shared.beans.OntologyBean;
import org.artificer.ui.client.shared.beans.OntologyResultSetBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;

/**
 * Provides a way to get/set ontology data.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("ontologies")
public interface IOntologyService {

    /**
     * Gets the list of all ontologies.
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public OntologyResultSetBean list() throws ArtificerUiException;

    /**
     * Gets the full meta data for an ontology, including its full tree of classes.
     * @param uuid
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @GET
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public OntologyBean get(@PathParam("uuid") String uuid) throws ArtificerUiException;

    /**
     * Called to update the given ontology.
     * @param ontology
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(OntologyBean ontology) throws ArtificerUiException;
    
    /**
     * Called to add a new ontology.
     * @param ontology
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void add(OntologyBean ontology) throws ArtificerUiException;

    /**
     * Called to delete the given ontology.
     * @param uuid
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @DELETE
    @Path("{uuid}")
    public void delete(@PathParam("uuid") String uuid) throws ArtificerUiException;

}
