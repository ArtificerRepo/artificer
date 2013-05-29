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
package org.overlord.sramp.server.atom.services;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.annotations.providers.multipart.PartType;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.MultipartOutput;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.visitors.ArtifactToFullAtomEntryVisitor;
import org.overlord.sramp.common.ArtifactNotFoundException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.PersistenceManager.BatchItem;
import org.overlord.sramp.server.atom.services.errors.DerivedArtifactAccessException;
import org.overlord.sramp.server.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final Sramp sramp = new Sramp();

	/**
	 * Constructor.
	 */
	public BatchResource() {
	}

    /**
     * S-RAMP atom POST of a package file (.zip) containing the artifacts and meta data that
     * should be published in the repository.
     * @param fileName the name of the .zip file (optional)
     * @param content the zip content
     * @return a multipart/mixed response as defined in the S-RAMP Atom binding document
     * @throws SrampAtomException
     */
    @POST
    @Consumes(MediaType.APPLICATION_ZIP)
    @Produces(MediaType.MULTIPART_MIXED)
    @PartType("message/http")
	public MultipartOutput zipPackage(@Context HttpServletRequest request,
	        @HeaderParam("Slug") String fileName, InputStream content) throws SrampAtomException {
        PersistenceManager persistenceManager = PersistenceFactory.newInstance();

        InputStream is = content;
    	SrampArchive archive = null;
    	String baseUrl = sramp.getBaseUrl(request.getRequestURL().toString());
        try {
            archive = new SrampArchive(content);

            MultipartOutput output = new MultipartOutput();
            output.setBoundary("package");

            // Process all of the entries in the s-ramp package.  First, do all the create
            // entries.  Once the creates are done, do the updates.
            Collection<SrampArchiveEntry> entries = archive.getEntries();
            List<BatchItem> createItems = new ArrayList<BatchItem>();
            List<SrampArchiveEntry> updates = new ArrayList<SrampArchiveEntry>();
            for (SrampArchiveEntry entry : entries) {
                String path = entry.getPath();
                String contentId = String.format("<%1$s@package>", path);
                BaseArtifactType metaData = entry.getMetaData();
                if (isCreate(metaData)) {
                    InputStream contentStream = ensureSupportsMark(archive.getInputStream(entry));
                    // Figure out the mime type
                    ArtifactType artifactType = ArtifactType.valueOf(metaData);
                    if (artifactType.isDerived()) {
                        throw new DerivedArtifactAccessException(artifactType.getArtifactType());
                    }
                    String mimeType = MimeTypes.determineMimeType(metaData.getName(), contentStream, artifactType);
                    artifactType.setMimeType(mimeType);
                    if (metaData instanceof DocumentArtifactType) {
                        ((DocumentArtifactType) metaData).setContentType(mimeType);
                    }
                    BatchItem bi = new BatchItem(contentId, metaData, contentStream);
                    createItems.add(bi);
                } else {
                    updates.add(entry);
                }
            }

            // Now, send the creates to the persistence manager in a batch and process the responses.
            List<Object> batchResponses = persistenceManager.persistBatch(createItems);
            for (int i = 0; i < createItems.size(); i++) {
                BatchItem bi = createItems.get(i);
                Object response = batchResponses.get(i);
                if (response instanceof BaseArtifactType) {
                    BaseArtifactType artifact = (BaseArtifactType) response;
                    ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
                    ArtifactVisitorHelper.visitArtifact(visitor, artifact);
                    Entry atomEntry = visitor.getAtomEntry();
                    addCreatedPart(output, bi.batchItemId, atomEntry);
                } else if (response instanceof Exception) {
                    addErrorPart(output, bi.batchItemId, (Exception) response);
                }
            }

            // Finally, process all the updates.
            for (SrampArchiveEntry updateEntry : updates) {
                InputStream contentStream = ensureSupportsMark(archive.getInputStream(updateEntry));
                String path = updateEntry.getPath();
                String contentId = String.format("<%1$s@package>", path);
                BaseArtifactType metaData = updateEntry.getMetaData();
                ArtifactType artifactType = ArtifactType.valueOf(metaData);
                Entry atomEntry = processUpdate(artifactType, metaData, contentStream, baseUrl);
                addUpdatedPart(output, contentId, atomEntry);
            }

            return output;
        } catch (Exception e) {
        	logError(logger, "Error consuming S-RAMP batch zip package.", e);
			throw new SrampAtomException(e);
        } finally {
        	IOUtils.closeQuietly(is);
        	if (archive != null)
        		SrampArchive.closeQuietly(archive);
        }
    }

	/**
     * Returns true if the given entry represents an artifact create operation.  Creates can be
     * done either with or without content (document vs. non-document type artifacts).
     * @param metaData
     */
    private boolean isCreate(BaseArtifactType metaData) {
        if (metaData.getUuid() == null) {
            return true;
        } else {
            return !artifactExists(metaData);
        }
    }

    /**
     * Returns true if the given artifact already exists in the repository.
     * @param metaData
     */
    private boolean artifactExists(BaseArtifactType metaData) {
        try {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            ArtifactType artifactType = ArtifactType.valueOf(metaData);
            // TODO Bug: this would allow a re-used UUID as long as the artifact type was different.  Should change this to query via UUID instead.
            BaseArtifactType artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
            return artifact != null;
        } catch (SrampException e) {
            return false;
        }
    }

	/**
	 * Process the case where we want to update the artifact's meta-data.
	 * @param artifactType the artifact type
	 * @param metaData the artifact meta-data
	 * @param contentStream the artifact content
	 * @return the Atom entry created as a result of the creat operation
	 * @throws Exception
	 */
    private Entry processUpdate(ArtifactType artifactType, BaseArtifactType metaData,
            InputStream contentStream, String baseUrl) throws Exception {
		PersistenceManager persistenceManager = PersistenceFactory.newInstance();
		BaseArtifactType artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
		if (artifact == null)
			throw new ArtifactNotFoundException(metaData.getUuid());

		// update the meta data
		persistenceManager.updateArtifact(metaData, artifactType);

		if (contentStream != null) {
		    persistenceManager.updateArtifactContent(metaData.getUuid(), artifactType, contentStream);
		}

		// Refetch the data to make sure what we return is up-to-date
		artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);

		// Return the entry containing the s-ramp artifact
		ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
		ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		Entry atomEntry = visitor.getAtomEntry();
		return atomEntry;
	}

	/**
	 * Adds an appropriate part to the batch response.  This takes the form of an HTTP
	 * response bean with the appropriate headers and data.
	 * @param output
	 * @param contentId
	 * @param atomEntry
	 */
	private void addCreatedPart(MultipartOutput output, String contentId, Entry atomEntry) {
		HttpResponseBean createdResponse = new HttpResponseBean(201, "Created");
		createdResponse.setBody(atomEntry, MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
		output.addPart(createdResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId);
	}

    /**
     * Adds an appropriate part to the batch response.  This takes the form of an HTTP
     * response bean with the appropriate headers and data.
     * @param output
     * @param contentId
     * @param atomEntry
     */
    private void addUpdatedPart(MultipartOutput output, String contentId, Entry atomEntry) {
        HttpResponseBean createdResponse = new HttpResponseBean(200, "OK");
        createdResponse.setBody(atomEntry, MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        output.addPart(createdResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId);
    }

	/**
	 * Writes an error part to the batch response.  This takes the form of an HTTP response
	 * bean with the appropriate headers and the error stack trace as the data.
	 * @param output
	 * @param contentId
	 * @param error
	 */
	private void addErrorPart(MultipartOutput output, String contentId, Exception error) {
        HttpResponseBean errorResponse = new HttpResponseBean(409, "Conflict");
        SrampAtomException e = new SrampAtomException(error);
        errorResponse.setBody(e, MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION_TYPE);
        output.addPart(errorResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId);
	}

}
