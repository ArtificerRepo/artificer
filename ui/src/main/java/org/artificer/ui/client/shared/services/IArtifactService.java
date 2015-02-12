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
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.artificer.ui.client.shared.beans.ArtifactBean;
import org.artificer.ui.client.shared.beans.ArtifactRelationshipsIndexBean;
import org.artificer.ui.client.shared.exceptions.ArtificerUiException;

/**
 * Provides a way to get and set Artifact meta data.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("artifacts")
public interface IArtifactService {

    /**
     * Gets the full meta data for an artifact (by UUID).
     * @param uuid
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @GET
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public ArtifactBean get(@PathParam("uuid") String uuid) throws ArtificerUiException;

    /**
     * Gets the full document content for an artifact (by UUID).
     * @param uuid
     * @param artifactType
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @GET
    @Path("content/{uuid}/{artifactType}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDocumentContent(@PathParam("uuid") String uuid, @PathParam("artifactType") String artifactType)
            throws ArtificerUiException;

    /**
     * Gets all of the relationships (resolved) for an artifact.
     * @param uuid
     * @param artifactType
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("relationships/{uuid}/{artifactType}")
    public ArtifactRelationshipsIndexBean getRelationships(@PathParam("uuid") String uuid,
            @PathParam("artifactType") String artifactType) throws ArtificerUiException;

    /**
     * Called to update the given artifact bean.
     * @param artifact
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void update(ArtifactBean artifact) throws ArtificerUiException;

    /**
     * Called to delete an artifact.
     * @param artifact
     * @throws org.artificer.ui.client.shared.exceptions.ArtificerUiException
     */
    @DELETE
    public void delete(ArtifactBean artifact) throws ArtificerUiException;

}
