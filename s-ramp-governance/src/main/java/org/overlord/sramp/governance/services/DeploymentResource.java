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
package org.overlord.sramp.governance.services;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.governance.Governance;
import org.overlord.sramp.governance.SrampAtomApiClientFactory;
import org.overlord.sramp.governance.Target;
import org.overlord.sramp.governance.SlashDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The JAX-RS resource that handles deployment specific tasks.
 *
 *
 */
@Path("/deploy")
public class DeploymentResource {

    private static Logger logger = LoggerFactory.getLogger(DeploymentResource.class);
    private Governance governance = new Governance();

    /**
     * Constructor.
     */
    public DeploymentResource() {
    }

    /**
     * Governance POST to deploy an artifact by copying it onto the file system.
     *
     * @param environment
     * @param uuid
     *
     * @throws SrampAtomException
     */
    @POST
    @Path("copy/{target}/{uuid}")
    @Produces(MediaType.APPLICATION_XML)
    public Response copy(@Context HttpServletRequest request,
            @PathParam("target") String targetRef,
            @PathParam("uuid") String uuid) throws Exception {
        InputStream is = null;
        OutputStream os = null;
        try {
            // 0. run the decoder on the arguments
            targetRef = SlashDecoder.decode(targetRef);
            uuid = SlashDecoder.decode(uuid);

            // 1. get the artifact from the repo
            SrampAtomApiClient client = SrampAtomApiClientFactory.createAtomApiClient();
            String query = String.format("/s-ramp[@uuid='%s']", uuid);
            QueryResultSet queryResultSet = client.query(query);
            if (queryResultSet.size() == 0) {
                return Response.serverError().status(0).build();
            }
            ArtifactSummary artifactSummary = queryResultSet.iterator().next();
            is = client.getArtifactContent(artifactSummary.getType(), uuid);

            // 2. get the deployment environment settings
            Target target = governance.getTargets().get(targetRef);
            if (target==null) {
                logger.error("No target could be found for target '"+ targetRef + "'");
                throw new SrampAtomException("No target could be found for target '"+ targetRef + "'");
            }
            File deployDir = new File(target.getDeployDir());
            if (!deployDir.exists()) {
                logger.info("creating " + deployDir);
                deployDir.mkdirs();
            }

            // 3. deploy the artifact
            File file = new File(deployDir + "/" + artifactSummary.getName());
            if (file.exists())
                file.delete();
            file.createNewFile();
            os = new FileOutputStream(file);
            IOUtils.copy(is, os);

            InputStream reply = IOUtils.toInputStream("success");
            return Response.ok(reply, MediaType.APPLICATION_OCTET_STREAM).build();
        } catch (Exception e) {
            logger.error("Error deploying artifact. " + e.getMessage(), e);
            throw new SrampAtomException(e);
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }
    }

}
