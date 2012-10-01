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
package org.overlord.sramp.atom.services;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.jboss.resteasy.util.GenericType;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.ArtifactTypeEnum;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.atom.visitors.ArtifactContentTypeVisitor;
import org.overlord.sramp.atom.visitors.ArtifactToFullAtomEntryVisitor;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.visitors.ArtifactVisitorHelper;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * The JAX-RS resource that handles artifact specific tasks, including:
 *
 * <ul>
 *   <li>Add an artifact (upload)</li>
 *   <li>Get an artifact (full Atom {@link Entry})</li>
 *   <li>Get artifact content (binary content)</li>
 *   <li>Update artifact meta data</li>
 *   <li>Update artifact content</li>
 *   <li>Delete an artifact</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp")
public class ArtifactResource {

	private static final MimetypesFileTypeMap mimeTypes = new MimetypesFileTypeMap();

	/**
	 * Constructor.
	 */
	public ArtifactResource() {
	}

    /**
     * S-RAMP atom POST to upload an artifact to the repository.  The artifact content should
     * be POSTed raw.
     * @param fileName
     * @param model
     * @param type
     * @param content
     * @throws SrampAtomException
     */
    @POST
    @Path("{model}/{type}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(@HeaderParam("Content-Type") String contentType,
			@HeaderParam("Slug") String fileName, @PathParam("model") String model,
			@PathParam("type") String type, InputStream content) throws SrampAtomException {
        InputStream is = content;
        try {
            ArtifactType artifactType = ArtifactType.valueOf(model, type);
        	if (artifactType.getArtifactType().isDerived()) {
				throw new Exception("Failed to create artifact because '" + artifactType.getArtifactType()
						+ "' is a derived type.");
        	}
        	
        	String mimeType = determineMimeType(contentType, fileName, artifactType);
        	artifactType.setMimeType(mimeType);

            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            //store the content
            BaseArtifactType artifact = persistenceManager.persistArtifact(fileName, artifactType, is);

            //create the derivedArtifacts
            Collection<? extends DerivedArtifactType> dartifacts = DerivedArtifactsFactory.newInstance().createDerivedArtifacts(artifactType, artifact);

            //persist the derivedArtifacts
            for (DerivedArtifactType dartifact : dartifacts)
                persistenceManager.persistDerivedArtifact(dartifact);

            //return the entry containing the s-ramp artifact
            ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor();
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            return visitor.getAtomEntry();
        } catch (Exception e) {
			throw new SrampAtomException(e);
        } finally {
        	IOUtils.closeQuietly(is);
        }
    }

	/**
     * S-RAMP atom POST to upload an artifact to the repository.  This method handles the
     * multipart/related style POST.
     * @param model
     * @param type
     * @param input
     * @throws SrampAtomException
     */
    @POST
    @Path("{model}/{type}")
    @Consumes(MultipartConstants.MULTIPART_RELATED)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry create(@PathParam("model") String model, @PathParam("type") String type,
			MultipartRelatedInput input) throws SrampAtomException {
        try {
            List<InputPart> list = input.getParts();
            // Expecting 1 part.
            if (list.size()!=1)
            	throw new SrampAtomException("Artifact content expected in (multipart/related) POST but not found.");
            InputPart firstPart = list.get(0);

            // First part being the artifact
            MultivaluedMap<String, String> headers = firstPart.getHeaders();
            String fileName = headers.getFirst("Slug");
            String ct = headers.getFirst("Content-Type");
            InputStream is = firstPart.getBody(new GenericType<InputStream>() { });

            return create(ct, fileName, model, type, is);
        } catch (SrampAtomException e) {
        	throw e;
        } catch (Throwable e) {
        	throw new SrampAtomException(e);
        }
    }

    /**
     * Figures out the mime type of the new artifact given the POSTed Content-Type, the name
     * of the uploaded file, and the S-RAMP arifact type.  If the artifact type is Document
     * then the other two pieces of information are used to determine an appropriate mime type.
     * If no appropriate mime type can be determined for core/Document, then binary is returned.
	 * @param contentType the content type request header
	 * @param fileName the slug request header
	 * @param artifactType the artifact type (based on the endpoint POSTed to)
	 */
	private static String determineMimeType(String contentType, String fileName, ArtifactType artifactType) {
		if (artifactType.getArtifactType() == ArtifactTypeEnum.Document || artifactType.getArtifactType() == ArtifactTypeEnum.UserDefined) {
			if (contentType != null && contentType.trim().length() > 0)
				return contentType;
			if (fileName != null && fileName.trim().length() > 0 && fileName.contains(".")) {
				String ct = mimeTypes.getContentType(fileName);
				if (ct != null)
					return ct;
			}
			return "application/octet-stream";
		} else {
			// Everything else is an XML file
			return "application/xml";
		}
	}

    /**
     * Called to update the meta data for an artifact.  Note that this does *not* update
     * the content of the artifact, just the meta data.
     * @param model
     * @param type
     * @param uuid
     * @param atomEntry
     * @throws SrampAtomException
     */
    @PUT
    @Path("{model}/{type}/{uuid}")
    @Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public void updateMetaData(@PathParam("model") String model, @PathParam("type") String type,
    		@PathParam("uuid") String uuid, Entry atomEntry) throws SrampAtomException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(model, type);
        	Artifact artifactWrapper = atomEntry.getAnyOtherJAXBObject(Artifact.class);
        	Method method = artifactWrapper.getClass().getMethod("get" + artifactType.getArtifactType().getType());
        	BaseArtifactType artifact = (BaseArtifactType) method.invoke(artifactWrapper);
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			persistenceManager.updateArtifact(artifact, artifactType);
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
    }

    /**
     * S-RAMP atom PUT to upload a new version of the artifact into the repository.
     * @param model
     * @param type
     * @param uuid
     * @param content
     * @throws SrampAtomException
     */
    @PUT
    @Path("{model}/{type}/{uuid}/media")
    @Consumes(MediaType.APPLICATION_XML)
	public void updateContent(@HeaderParam("Content-Type") String contentType,
			@HeaderParam("Slug") String fileName, @PathParam("model") String model,
			@PathParam("type") String type, @PathParam("uuid") String uuid, InputStream content)
			throws SrampAtomException {
        ArtifactType artifactType = ArtifactType.valueOf(model, type);
    	if (artifactType.getArtifactType().isDerived()) {
			throw new SrampAtomException("Failed to create artifact because '" + artifactType.getArtifactType()
					+ "' is a derived type.");
    	}
    	String mimeType = determineMimeType(contentType, fileName, artifactType);
    	artifactType.setMimeType(mimeType);

        InputStream is = content;
        try {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            //store the content
            persistenceManager.updateArtifactContent(uuid, artifactType, is);
        } catch (Exception e) {
			throw new SrampAtomException(e);
        } finally {
        	IOUtils.closeQuietly(is);
        }
    }

    /**
     * Called to get the meta data for an s-ramp artifact.  This will return an Atom {@link Entry}
     * with the full information about the artifact.
     * @param model
     * @param type
     * @param uuid
     * @throws SrampAtomException
     */
    @GET
    @Path("{model}/{type}/{uuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
	public Entry getMetaData(@PathParam("model") String model, @PathParam("type") String type,
			@PathParam("uuid") String uuid) throws SrampAtomException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(model, type);
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();

			// Get the artifact by UUID
			BaseArtifactType artifact = persistenceManager.getArtifact(uuid, artifactType);
			if (artifact == null)
				return null;

			// Return the entry containing the s-ramp artifact
			ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			return visitor.getAtomEntry();
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
    }

    /**
     * Returns the content of an artifact in the s-ramp repository.
     * @param model
     * @param type
     * @param uuid
     * @throws SrampAtomException
     */
    @GET
    @Path("{model}/{type}/{uuid}/media")
	public Response getContent(@PathParam("model") String model, @PathParam("type") String type,
			@PathParam("uuid") String uuid) throws SrampAtomException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(model, type);
            //artifactType.setMimeType("application/pdf");
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			BaseArtifactType artifact = persistenceManager.getArtifact(uuid, artifactType);
            ArtifactContentTypeVisitor ctVizzy = new ArtifactContentTypeVisitor();
            ArtifactVisitorHelper.visitArtifact(ctVizzy, artifact);
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
	    	return Response.ok(output, artifactType.getMimeType()).build();
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
    }

    /**
     * Called to delete an s-ramp artifact from the repository.
     * @param model
     * @param type
     * @param uuid
     * @throws SrampAtomException
     */
    @DELETE
    @Path("{model}/{type}/{uuid}")
	public void deleteDocument(@PathParam("model") String model, @PathParam("type") String type,
			@PathParam("uuid") String uuid) throws SrampAtomException {
        try {
            ArtifactType artifactType = ArtifactType.valueOf(model, type);
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();

			// Delete the artifact by UUID
			persistenceManager.deleteArtifact(uuid, artifactType);
		} catch (Throwable e) {
			throw new SrampAtomException(e);
		}
    }

}
