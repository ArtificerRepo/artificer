/*
 * Copyright 2012 JBoss Inc
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
package org.artificer.server.atom.services;

import org.apache.commons.io.IOUtils;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.beans.HttpResponseBean;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.atom.visitors.ArtifactToFullAtomEntryVisitor;
import org.artificer.common.ArtificerConfig;
import org.artificer.common.ArtificerException;
import org.artificer.common.MediaType;
import org.artificer.common.error.ArtificerServerException;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.server.BatchServiceImpl;
import org.artificer.server.core.api.BatchResult;
import org.artificer.server.i18n.Messages;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import java.io.InputStream;

/**
 * The JAX-RS resource that handles pushing artifacts into the repository in batches.  The
 * S-RAMP specification defines two mechanisms for this.  The first is via an archive file
 * and the second is via a multipart/related http POST.
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp")
public class BatchResource extends AbstractResource {

	private static Logger logger = LoggerFactory.getLogger(BatchResource.class);

	private final BatchServiceImpl batchService = new BatchServiceImpl();

    /**
     * S-RAMP atom POST of a package file (.zip) containing the artifacts and meta data that
     * should be published in the repository.
     * @param fileName the name of the .zip file (optional)
     * @param content the zip content
     * @return a multipart/mixed response as defined in the S-RAMP Atom binding document
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @POST
    @Consumes(MediaType.APPLICATION_ZIP)
    @Produces(MediaType.MULTIPART_MIXED)
    @PartType("message/http")
	public MultipartOutput zipPackagePost(@Context HttpServletRequest request,
	        @HeaderParam("Slug") String fileName, InputStream content) throws ArtificerAtomException, ArtificerException {
        return doZipPackage(request, content);
    }

    /**
     * S-RAMP atom PUT of a package file (.zip) containing the artifacts and meta data that
     * should be published in the repository.
     * @param fileName the name of the .zip file (optional)
     * @param content the zip content
     * @return a multipart/mixed response as defined in the S-RAMP Atom binding document
     * @throws org.artificer.atom.err.ArtificerAtomException
     */
    @PUT
    @Consumes(MediaType.APPLICATION_ZIP)
    @Produces(MediaType.MULTIPART_MIXED)
    @PartType("message/http")
    public MultipartOutput zipPackagePut(@Context HttpServletRequest request,
            @HeaderParam("Slug") String fileName, InputStream content) throws ArtificerServerException {
        return doZipPackage(request, content);
    }

    private MultipartOutput doZipPackage(HttpServletRequest request, InputStream content)
            throws ArtificerServerException {
        ArtificerArchive archive = null;
        String baseUrl = ArtificerConfig.getBaseUrl(request.getRequestURL().toString());
        try {
            archive = new ArtificerArchive(content);

            MultipartOutput output = new MultipartOutput();
            output.setBoundary("package"); //$NON-NLS-1$

            BatchResult batchResult = batchService.upload(archive);

            for (String batchItemId : batchResult.getCreates().keySet()) {
                ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
                ArtifactVisitorHelper.visitArtifact(visitor, batchResult.getCreates().get(batchItemId));
                Entry atomEntry = visitor.getAtomEntry();
                addCreatedPart(output, batchItemId, atomEntry);
            }

            for (String batchItemId : batchResult.getUpdates().keySet()) {
                ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
                ArtifactVisitorHelper.visitArtifact(visitor, batchResult.getUpdates().get(batchItemId));
                Entry atomEntry = visitor.getAtomEntry();
                addUpdatedPart(output, batchItemId, atomEntry);
            }
            
            for (String batchItemId : batchResult.getErrors().keySet()) {
                addErrorPart(output, batchItemId, batchResult.getErrors().get(batchItemId));
            }

            return output;
        } catch (ArtificerServerException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtificerServerException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Exception e) {
            logError(logger, Messages.i18n.format("ERROR_CONSUMING_ZIP"), e); //$NON-NLS-1$
            throw new ArtificerAtomException(e);
        } finally {
            IOUtils.closeQuietly(content);
            if (archive != null)
                ArtificerArchive.closeQuietly(archive);
        }
    }

	/**
	 * Adds an appropriate part to the batch response.  This takes the form of an HTTP
	 * response bean with the appropriate headers and data.
	 * @param output
	 * @param contentId
	 * @param atomEntry
	 */
	private void addCreatedPart(MultipartOutput output, String contentId, Entry atomEntry) {
		HttpResponseBean createdResponse = new HttpResponseBean(201, "Created"); //$NON-NLS-1$
		createdResponse.setBody(atomEntry, MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
		output.addPart(createdResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId); //$NON-NLS-1$
	}

    /**
     * Adds an appropriate part to the batch response.  This takes the form of an HTTP
     * response bean with the appropriate headers and data.
     * @param output
     * @param contentId
     * @param atomEntry
     */
    private void addUpdatedPart(MultipartOutput output, String contentId, Entry atomEntry) {
        HttpResponseBean createdResponse = new HttpResponseBean(200, "OK"); //$NON-NLS-1$
        createdResponse.setBody(atomEntry, MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        output.addPart(createdResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId); //$NON-NLS-1$
    }

	/**
	 * Writes an error part to the batch response.  This takes the form of an HTTP response
	 * bean with the appropriate headers and the error stack trace as the data.
	 * @param output
	 * @param contentId
	 * @param error
	 */
	private void addErrorPart(MultipartOutput output, String contentId, Exception error) {
        HttpResponseBean errorResponse = new HttpResponseBean(409, "Conflict"); //$NON-NLS-1$
        ArtificerAtomException e = new ArtificerAtomException(error);
        errorResponse.setBody(e, MediaType.APPLICATION_ARTIFICER_SERVER_EXCEPTION_TYPE);
        output.addPart(errorResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId); //$NON-NLS-1$
	}

}
