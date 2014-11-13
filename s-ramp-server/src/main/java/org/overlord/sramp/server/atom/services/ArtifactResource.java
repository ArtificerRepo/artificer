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

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.jboss.resteasy.util.GenericType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.visitors.ArtifactContentTypeVisitor;
import org.overlord.sramp.atom.visitors.ArtifactToFullAtomEntryVisitor;
import org.overlord.sramp.common.*;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.events.EventProducer;
import org.overlord.sramp.events.EventProducerFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.repository.errors.DerivedArtifactCreateException;
import org.overlord.sramp.repository.errors.DerivedArtifactDeleteException;
import org.overlord.sramp.server.i18n.Messages;
import org.overlord.sramp.server.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

/**
 * The JAX-RS resource that handles artifact specific tasks, including:
 *
 * <ul>
 * <li>Add an artifact (upload)</li>
 * <li>Get an artifact (full Atom {@link Entry})</li>
 * <li>Get artifact content (binary content)</li>
 * <li>Update artifact meta data</li>
 * <li>Update artifact content</li>
 * <li>Delete an artifact</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp")
public class ArtifactResource extends AbstractResource {

	private static Logger logger = LoggerFactory.getLogger(ArtifactResource.class);

	// Sadly, date formats are not thread safe.
	private static final ThreadLocal<SimpleDateFormat> dateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat(SrampConstants.DATE_FORMAT);
		}
	};

	/**
	 * Constructor.
	 */
	public ArtifactResource() {
	}

    @POST
    @Path("{model}/{type}")
    @Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry create(@Context HttpServletRequest request,
        @PathParam("model") String model, @PathParam("type") String type, Entry entry)
        throws SrampAtomException, SrampException {
        try {
            String baseUrl = SrampConfig.getBaseUrl(request.getRequestURL().toString());
            ArtifactType artifactType = ArtifactType.valueOf(model, type, false);
            BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(entry);

			ArtifactVerifier verifier = new ArtifactVerifier(artifactType);
			ArtifactVisitorHelper.visitArtifact(verifier, artifact);
			verifier.throwError();
            
            if (artifactType.isDerived()) {
                throw new DerivedArtifactCreateException(artifactType.getArtifactType());
            }
            if (artifactType.isDocument()) {
                throw new InvalidArtifactCreationException(Messages.i18n.format("INVALID_DOCARTY_CREATE")); //$NON-NLS-1$
            }
            
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            // store the content
            BaseArtifactType persistedArtifact = persistenceManager.persistArtifact(artifact, null);
            
            Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.artifactCreated(persistedArtifact);
            }

            // return the entry containing the s-ramp artifact
            ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
            ArtifactVisitorHelper.visitArtifact(visitor, persistedArtifact);
            return visitor.getAtomEntry();
        } catch (WrongModelException e) {
            // Simply re-throw.  Don't allow the following catch it -- WrongModelException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (SrampAlreadyExistsException e) {
            // Simply re-throw.  Don't allow the following catch it -- SrampAlreadyExistsException is mapped to a
            // unique HTTP response type.
            throw e;
        } catch (Exception e) {
            logError(logger, Messages.i18n.format("ERROR_CREATING_ARTY"), e); //$NON-NLS-1$
            throw new SrampAtomException(e);
        }
    }



	/**
	 * S-RAMP atom POST to upload an artifact to the repository. The artifact content should be POSTed raw.
	 *
	 * @param fileName
	 * @param model
	 * @param type
	 * @param is
	 * @throws SrampAtomException
	 */
	@POST
	@Path("{model}/{type}")
	@Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(@Context HttpServletRequest request, @HeaderParam("Slug") String fileName,
		@PathParam("model") String model, @PathParam("type") String type, InputStream is)
		throws SrampAtomException {
		try {
			String baseUrl = SrampConfig.getBaseUrl(request.getRequestURL().toString());
			ArtifactType artifactType = ArtifactType.valueOf(model, type, true);
			if (artifactType.isDerived()) {
				throw new DerivedArtifactCreateException(artifactType.getArtifactType());
			}

			is = ensureSupportsMark(is);

			// Figure out the mime type (from the http header, filename, or default by artifact type)
			String mimeType = MimeTypes.determineMimeType(fileName, is, artifactType);
			artifactType.setMimeType(mimeType);

			// Pick a reasonable file name if Slug is not present
			if (fileName == null) {
				if (artifactType.getArtifactType() == ArtifactTypeEnum.Document) {
					fileName = "newartifact.bin"; //$NON-NLS-1$
				} else if (artifactType.getArtifactType() == ArtifactTypeEnum.XmlDocument) {
					fileName = "newartifact.xml"; //$NON-NLS-1$
				} else {
					fileName = "newartifact." + artifactType.getArtifactType().getModel(); //$NON-NLS-1$
				}
			}

			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			// store the content
			BaseArtifactType baseArtifactType = artifactType.newArtifactInstance();
            if (!SrampModelUtils.isDocumentArtifact(baseArtifactType)) {
                throw new InvalidArtifactCreationException(Messages.i18n.format("INVALID_DOCARTY_CREATE")); //$NON-NLS-1$
            }
			baseArtifactType.setName(fileName);
			BaseArtifactType artifact = persistenceManager.persistArtifact(baseArtifactType, is);
			
			Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
			for (EventProducer eventProducer : eventProducers) {
			    eventProducer.artifactCreated(artifact);
			}

			// return the entry containing the s-ramp artifact
			ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			return visitor.getAtomEntry();
		} catch (Exception e) {
		    logError(logger, Messages.i18n.format("ERROR_CREATING_ARTY"), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

    /**
	 * Handles multi-part creates. In S-RAMP, an HTTP multi-part request can be POST'd to the endpoint, which
	 * allows Atom Entry formatted meta-data to be included in the same request as the artifact content.
	 *
	 * @param model
	 * @param type
	 * @param input
	 * @return the newly created artifact as an Atom {@link Entry}
	 * @throws SrampAtomException
     * @throws WrongModelException 
	 */
	@POST
	@Path("{model}/{type}")
	@Consumes(MultipartConstants.MULTIPART_RELATED)
	@Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry createMultiPart(@Context HttpServletRequest request, @PathParam("model") String model,
	        @PathParam("type") String type, MultipartRelatedInput input) throws SrampAtomException, SrampException {
		InputStream contentStream = null;
		try {
			String baseUrl = SrampConfig.getBaseUrl(request.getRequestURL().toString());
			ArtifactType artifactType = ArtifactType.valueOf(model, type, false);
			if (artifactType.isDerived()) {
				throw new DerivedArtifactCreateException(artifactType.getArtifactType());
			}
			if (artifactType.isExtendedType()) {
			    artifactType = ArtifactType.ExtendedDocument(artifactType.getExtendedType());
			}

			List<InputPart> list = input.getParts();
			// Expecting 2 parts
			if (list.size() != 2) {
				throw new SrampAtomException(Messages.i18n.format("INVALID_MULTIPART_POST", list.size())); //$NON-NLS-1$
			}
			InputPart firstPart = list.get(0);
			InputPart secondpart = list.get(1);

			// Getting the S-RAMP Artifact
			Entry atomEntry = firstPart.getBody(new GenericType<Entry>() {});
			BaseArtifactType artifactMetaData = SrampAtomUtils.unwrapSrampArtifact(atomEntry);

			ArtifactVerifier verifier = new ArtifactVerifier(artifactType);
			ArtifactVisitorHelper.visitArtifact(verifier, artifactMetaData);
			verifier.throwError();
            
			String fileName = null;
			if (artifactMetaData.getName() != null)
				fileName = artifactMetaData.getName();

			contentStream = ensureSupportsMark(secondpart.getBody(new GenericType<InputStream>() {
			}));
			String mimeType = MimeTypes.determineMimeType(fileName, contentStream, artifactType);
			artifactType.setMimeType(mimeType);

			// Processing the content itself first
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			// store the content
			BaseArtifactType artifactRval = persistenceManager.persistArtifact(artifactMetaData,
			        contentStream);
			
			Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.artifactCreated(artifactRval);
            }

			// Convert to a full Atom Entry and return it
			ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
			ArtifactVisitorHelper.visitArtifact(visitor, artifactRval);
			return visitor.getAtomEntry();
		} catch (WrongModelException e) {
            // Simply re-throw.  Don't allow the following catch it -- WrongModelException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (SrampAlreadyExistsException e) {
            // Simply re-throw.  Don't allow the following catch it -- SrampAlreadyExistsException is mapped to a
            // unique HTTP response type.
            throw e;
        } catch (Exception e) {
			logError(logger, Messages.i18n.format("ERROR_CREATING_ARTY"), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	/**
	 * Called to update the meta data for an artifact. Note that this does *not* update the content of the
	 * artifact, just the meta data.
	 *
	 * @param model
	 * @param type
	 * @param uuid
	 * @param atomEntry
	 * @throws SrampAtomException
	 * @throws WrongModelException 
	 */
	@PUT
	@Path("{model}/{type}/{uuid}")
	@Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public void updateMetaData(@PathParam("model") String model, @PathParam("type") String type,
	        @PathParam("uuid") String uuid, Entry atomEntry) throws SrampAtomException, SrampException {
		try {
			ArtifactType artifactType = ArtifactType.valueOf(model, type, null);
			if (artifactType.isExtendedType()) {
			    artifactType = SrampAtomUtils.getArtifactType(atomEntry);
			}
			BaseArtifactType artifact = SrampAtomUtils.unwrapSrampArtifact(atomEntry);

			ArtifactVerifier verifier = new ArtifactVerifier(artifactType);
			ArtifactVisitorHelper.visitArtifact(verifier, artifact);
			verifier.throwError();
            
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			BaseArtifactType oldArtifact = persistenceManager.getArtifact(uuid, artifactType);
            if (oldArtifact == null) {
                throw new ArtifactNotFoundException(uuid);
            }
			BaseArtifactType updatedArtifact = persistenceManager.updateArtifact(artifact, artifactType);
			
			Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.artifactUpdated(updatedArtifact, oldArtifact);
            }
		} catch (WrongModelException e) {
            // Simply re-throw.  Don't allow the following catch it -- WrongModelException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (ArtifactNotFoundException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtifactNotFoundException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_UPDATING_META_DATA", uuid), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		}
	}

	/**
	 * S-RAMP atom PUT to upload a new version of the artifact into the repository.
	 *
	 * @param model
	 * @param type
	 * @param uuid
	 * @param content
	 * @throws SrampAtomException
	 */
	@PUT
	@Path("{model}/{type}/{uuid}/media")
	public void updateContent(@HeaderParam("Slug") String fileName, @PathParam("model") String model,
	        @PathParam("type") String type, @PathParam("uuid") String uuid, InputStream content)
	        throws SrampAtomException, SrampException {
		InputStream is = ensureSupportsMark(content);
		try {
	        ArtifactType artifactType = ArtifactType.valueOf(model, type, true);
	        if (artifactType.isDerived()) {
	            throw new DerivedArtifactCreateException(artifactType.getArtifactType());
	        }
	        String mimeType = MimeTypes.determineMimeType(fileName, is, artifactType);
	        artifactType.setMimeType(mimeType);

	        // TODO we need to update the S-RAMP metadata too (new updateDate, size, etc)?

	        PersistenceManager persistenceManager = PersistenceFactory.newInstance();
	        BaseArtifactType oldArtifact = persistenceManager.getArtifact(uuid, artifactType);
            if (oldArtifact == null) {
                throw new ArtifactNotFoundException(uuid);
            }
	        BaseArtifactType updatedArtifact = persistenceManager.updateArtifactContent(uuid, artifactType, is);
			
			Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.artifactUpdated(updatedArtifact, oldArtifact);
            }
		} catch (ArtifactNotFoundException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtifactNotFoundException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Exception e) {
			logError(logger, Messages.i18n.format("ERROR_UPDATING_CONTENT", uuid), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	/**
	 * Called to get the meta data for an s-ramp artifact. This will return an Atom {@link Entry} with the
	 * full information about the artifact.
	 *
	 * @param model
	 * @param type
	 * @param uuid
	 * @throws SrampAtomException
	 */
	@GET
	@Path("{model}/{type}/{uuid}")
	@Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry getMetaData(@Context HttpServletRequest request, @PathParam("model") String model,
	        @PathParam("type") String type, @PathParam("uuid") String uuid) throws SrampAtomException, SrampException {
		try {
			String baseUrl = SrampConfig.getBaseUrl(request.getRequestURL().toString());
			ArtifactType artifactType = ArtifactType.valueOf(model, type, false);
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();

			// Get the artifact by UUID
			// TODO: The last extendedDocFix check should not be necessary.  However, since we
			// don't know whether or not the artifact has content prior to calling ArtifactType.valueOf, this is
			// necessary.  It would be better if we could somehow get the artifact without knowing the artifact type
			// ahead of time (ie, purely use the JCR property).
			BaseArtifactType artifact = persistenceManager.getArtifact(uuid, artifactType);
			if (artifact == null || (!artifactType.getArtifactType().getApiType().equals(artifact.getArtifactType())
			        && !(artifactType.getArtifactType().equals(ArtifactTypeEnum.ExtendedArtifactType) && artifact.getArtifactType().equals(BaseArtifactEnum.EXTENDED_DOCUMENT)))) {
				throw new ArtifactNotFoundException(uuid);
			}

			// Return the entry containing the s-ramp artifact
			ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor(baseUrl);
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			return visitor.getAtomEntry();
		} catch (ArtifactNotFoundException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtifactNotFoundException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_GETTING_META_DATA", uuid), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		}
	}

	/**
	 * Returns the content of an artifact in the s-ramp repository.
	 *
	 * @param model
	 * @param type
	 * @param uuid
	 * @throws SrampAtomException
	 */
	@GET
	@Path("{model}/{type}/{uuid}/media")
	public Response getContent(@PathParam("model") String model, @PathParam("type") String type,
	        @PathParam("uuid") String uuid) throws SrampAtomException, SrampException {
		try {
			ArtifactType artifactType = ArtifactType.valueOf(model, type, true);
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			BaseArtifactType baseArtifact = persistenceManager.getArtifact(uuid, artifactType);
			if (baseArtifact == null || ! artifactType.getArtifactType().getApiType().equals(baseArtifact.getArtifactType())) {
                throw new ArtifactNotFoundException(uuid);
            }

			ArtifactContentTypeVisitor ctVizzy = new ArtifactContentTypeVisitor();
			ArtifactVisitorHelper.visitArtifact(ctVizzy, baseArtifact);
			javax.ws.rs.core.MediaType mediaType = ctVizzy.getContentType();
			artifactType.setMimeType(mediaType.toString());
			final InputStream artifactContent = persistenceManager.getArtifactContent(uuid, artifactType);
			Object output = new StreamingOutput() {
				@Override
				public void write(OutputStream output) throws IOException, WebApplicationException {
					try {
						IOUtils.copy(artifactContent, output);
					} finally {
						IOUtils.closeQuietly(artifactContent);
					}
				}
			};
			String lastModifiedDate = dateFormat.get().format(
			        baseArtifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
			return Response
			        .ok(output, artifactType.getMimeType())
			        .header("Content-Disposition", "attachment; filename=" + baseArtifact.getName()) //$NON-NLS-1$ //$NON-NLS-2$
			        .header("Content-Length", //$NON-NLS-1$
			                baseArtifact.getOtherAttributes().get(SrampConstants.SRAMP_CONTENT_SIZE_QNAME))
			        .header("Last-Modified", lastModifiedDate).build(); //$NON-NLS-1$
		} catch (ArtifactNotFoundException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtifactNotFoundException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_GETTING_CONTENT", uuid), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		}
	}

	/**
	 * Called to delete an s-ramp artifact from the repository.
	 *
	 * @param model
	 * @param type
	 * @param uuid
	 * @throws SrampAtomException
	 */
	@DELETE
	@Path("{model}/{type}/{uuid}")
	public void delete(@PathParam("model") String model, @PathParam("type") String type,
	        @PathParam("uuid") String uuid) throws SrampAtomException, SrampException {
		try {
			ArtifactType artifactType = ArtifactType.valueOf(model, type, null);
            if (artifactType.isDerived()) {
                throw new DerivedArtifactDeleteException(artifactType.getArtifactType());
            }
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();

			// Delete the artifact by UUID
			BaseArtifactType artifact = persistenceManager.deleteArtifact(uuid, artifactType);
			
			Set<EventProducer> eventProducers = EventProducerFactory.getEventProducers();
            for (EventProducer eventProducer : eventProducers) {
                eventProducer.artifactDeleted(artifact);
            }
		} catch (ArtifactNotFoundException e) {
            // Simply re-throw.  Don't allow the following catch it -- ArtifactNotFoundException is mapped to a unique
            // HTTP response type.
            throw e;
        } catch (Throwable e) {
			logError(logger, Messages.i18n.format("ERROR_DELETING_ARTY", uuid), e); //$NON-NLS-1$
			throw new SrampAtomException(e);
		}
	}

}
