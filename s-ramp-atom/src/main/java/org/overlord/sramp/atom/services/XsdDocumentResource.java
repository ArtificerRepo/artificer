/*
 * Copyright 2011 JBoss Inc
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


import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.jboss.resteasy.util.GenericType;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.MediaType;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

@Path("/s-ramp/xsd/XsdDocument")
public class XsdDocumentResource extends AbstractDocumentResource
{
	/**
	 * Default constructor.
	 */
	public XsdDocumentResource() {
	}

    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry saveXsdDocument(@HeaderParam("Slug") String fileName, String body) {
    	return super.saveArtifact(fileName, body, ArtifactType.XsdDocument);
    }

    @POST
    @Consumes(MultipartConstants.MULTIPART_RELATED)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry saveXsdDocument(MultipartRelatedInput input) {
        try {
            List<InputPart> list = input.getParts();
            // Expecting 1 part. 
            if (list.size()!=1) ; //throw error
            InputPart firstPart = list.get(0);

            // First part being the artifact
            MultivaluedMap<String, String> headers = firstPart.getHeaders();
            String fileName = headers.getFirst("Slug");
            InputStream is = firstPart.getBody(new GenericType<InputStream>() { });
            
            return super.saveArtifact(fileName, is, ArtifactType.XsdDocument);
        } catch (Exception e) {
        	// TODO need better error handling here
        	throw new RuntimeException(e);
        }
    }

    /**
     * Called to update the meta data for a XSD document.  Note that this does *not* update
     * the content of the XSD, just the meta data.
     * @param uuid the UUID of the s-ramp artifact
     * @param atomEntryBody an s-ramp extended Atom entry
     */
    @PUT
    @Path("{uuid}")
    @Consumes(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public void updateXsdDocument(@PathParam("uuid") String uuid, Entry atomEntry) {
    	try {
			Artifact srampArtifactWrapper = atomEntry.getAnyOtherJAXBObject(Artifact.class);
			XsdDocument xsdDocument = srampArtifactWrapper.getXsdDocument();
			super.updateArtifact(xsdDocument, ArtifactType.XsdDocument);
		} catch (JAXBException e) {
        	// TODO need better error handling here
        	throw new RuntimeException(e);
		}
    }

    /**
     * Called to get the {@link XsdDocument} with the supplied uuid.
     * @param uuid the UUID of the s-ramp artifact
     * @return an s-ramp extended Atom entry
     */
    @GET
    @Path("{uuid}")
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry getXsdDocument(@PathParam("uuid") String uuid) {
    	return super.getArtifact(uuid, ArtifactType.XsdDocument);
    }

    /**
     * Called to delete an {@link XsdDocument} with the supplied uuid.
     * @param uuid the UUID of the s-ramp artifact
     */
    @DELETE
    @Path("{uuid}")
    public void deleteXsdDocument(@PathParam("uuid") String uuid) {
    	// TODO implement DELETE
    }

    /**
     * Returns a feed of summary documents for all XsdDocument artifacts.
     * @return an Atom {@link Feed}
     */
    @GET
    @Produces(MediaType.APPLICATION_ATOM_XML_FEED)
    public Feed getFeed() {
    	return super.getFeed(ArtifactType.XsdDocument);
    }
}
