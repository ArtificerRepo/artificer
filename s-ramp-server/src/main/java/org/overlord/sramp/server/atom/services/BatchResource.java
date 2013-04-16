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
import java.util.Collection;

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
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.beans.HttpResponseBean;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.visitors.ArtifactToFullAtomEntryVisitor;
import org.overlord.sramp.common.ArtifactNotFoundException;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.Sramp;
import org.overlord.sramp.common.SrampServerException;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
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
        InputStream is = content;
    	SrampArchive archive = null;
    	String baseUrl = sramp.getBaseUrl(request.getRequestURL().toString());
        try {
            archive = new SrampArchive(content);

            MultipartOutput output = new MultipartOutput();
            output.setBoundary("package");

            // Process all of the entries in the s-ramp package.
            Collection<SrampArchiveEntry> entries = archive.getEntries();
            for (SrampArchiveEntry entry: entries) {
                BaseArtifactType metaData = entry.getMetaData();
                InputStream contentStream = ensureSupportsMark(archive.getInputStream(entry));
                try {
                    processBatchEntry(output, entry.getPath(), metaData, contentStream, baseUrl);
                } finally {
                    IOUtils.closeQuietly(contentStream);
                }
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
	 * Process a single entry from the s-ramp archive.
	 * @param output where to write the result of processing the entry
	 * @param path path to the entry in the s-ramp package
	 * @param metaData the entry meta-data (s-ramp artifact)
	 * @param contentStream the artifact content (or null if a meta-data only entry)
	 */
	private void processBatchEntry(MultipartOutput output, String path, BaseArtifactType metaData,
			InputStream contentStream, String baseUrl) {
		String contentId = String.format("<%1$s@package>", path);
    	try {
    	    ArtifactType artifactType = ArtifactType.valueOf(metaData);
    	    if (artifactType.getArtifactType().isDerived()) {
    	        throw new DerivedArtifactAccessException(artifactType.getArtifactType());
    	    }
    	    // Figure out the mime type
    	    String mimeType = MimeTypes.determineMimeType(metaData.getName(), contentStream, artifactType);
    	    artifactType.setMimeType(mimeType);

    	    if (metaData.getUuid() == null) {
    	        // The "create" case - no UUID specified
    	        Entry atomEntry = processCreate(artifactType, metaData, contentStream, baseUrl);
    	        addCreatedPart(output, contentId, atomEntry);
    	    } else if (metaData.getUuid() != null && contentStream != null) {
    	        // Either an "update" case or a "create" case - depends on if we find an existing
    	        // artifact with the supplied UUID.  Content has been supplied, so it *may* be
    	        // a create.
    	        Entry atomEntry = processUpdateOrCreate(artifactType, metaData, contentStream, baseUrl);
    	        addCreatedPart(output, contentId, atomEntry);
    	    } else if (metaData.getUuid() != null && contentStream == null) {
    	        // This is the "update" only case - metadata has been supplied but
    	        // no content is included.  Thus, this cannot be a create.
    	        Entry atomEntry = processUpdate(artifactType, metaData, baseUrl);
    	        addCreatedPart(output, contentId, atomEntry);
    	    } else {
    	        // This is a "create" of a non-Document artifact.
    	        throw new SrampServerException("Unsupported path (TBD).");
    	    }
		} catch (Exception e) {
	        HttpResponseBean errorResponse = new HttpResponseBean(409, "Conflict");
	        SrampAtomException error = new SrampAtomException(e);
	        errorResponse.setBody(error, MediaType.APPLICATION_SRAMP_ATOM_EXCEPTION_TYPE);
	        output.addPart(errorResponse, MediaType.MESSAGE_HTTP_TYPE).getHeaders().putSingle("Content-ID", contentId);
		}
	}

	/**
	 * Processes a batch create.  This will create the artifact in the s-ramp
	 * repository and update the resulting repository entry with the meta data
	 * included.
	 * @param artifactType the artifact type
	 * @param metaData the artifact meta-data
	 * @param contentStream the artifact content
	 * @return the Atom entry created as a result of the creat operation
	 * @throws Exception
	 */
	private Entry processCreate(ArtifactType artifactType, BaseArtifactType metaData,
			InputStream contentStream, String baseUrl) throws Exception {
		PersistenceManager persistenceManager = PersistenceFactory.newInstance();

		BaseArtifactType artifact = persistenceManager.persistArtifact(metaData, contentStream);

		// Now get the latest copy and return it
		artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
		// Return the entry containing the s-ramp artifact
		ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
		ArtifactVisitorHelper.visitArtifact(visitor, artifact);
		Entry atomEntry = visitor.getAtomEntry();
		return atomEntry;
	}

	/**
	 * Process the case where we've either got an update or a create.  It could be either
	 * because we have both meta-data and a valid content stream.  We need to query for an
	 * existing artifact (by uuid) to figure out which one we're doing.
	 * @param artifactType the artifact type
	 * @param metaData the artifact meta-data
	 * @param contentStream the artifact content
	 * @return the Atom entry created as a result of the creat operation
	 * @throws Exception
	 */
	private Entry processUpdateOrCreate(ArtifactType artifactType, BaseArtifactType metaData,
			InputStream contentStream, String baseUrl) throws Exception {
		PersistenceManager persistenceManager = PersistenceFactory.newInstance();
		BaseArtifactType artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
		if (artifact == null) {
			return processCreate(artifactType, metaData, contentStream, baseUrl);
		} else {
			// Update the artifact metadata
			persistenceManager.updateArtifact(metaData, artifactType);
			// Update the artifact content
			persistenceManager.updateArtifactContent(metaData.getUuid(), artifactType, contentStream);

			// Refetch the data to make sure what we return is up-to-date
			artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
			// Return the entry containing the s-ramp artifact
			ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			Entry atomEntry = visitor.getAtomEntry();
			return atomEntry;
		}
	}

	/**
	 * Process the case where we want to update the meta-data only.  This is being done
	 * because meta-data was supplied without accompanying content.
	 * @param artifactType the artifact type
	 * @param metaData the artifact meta-data
	 * @param contentStream the artifact content
	 * @return the Atom entry created as a result of the creat operation
	 * @throws Exception
	 */
	private Entry processUpdate(ArtifactType artifactType, BaseArtifactType metaData, String baseUrl) throws Exception {
		PersistenceManager persistenceManager = PersistenceFactory.newInstance();
		BaseArtifactType artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
		if (artifact == null)
			throw new ArtifactNotFoundException(metaData.getUuid());

		// update the meta data
		persistenceManager.updateArtifact(metaData, artifactType);

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

}
