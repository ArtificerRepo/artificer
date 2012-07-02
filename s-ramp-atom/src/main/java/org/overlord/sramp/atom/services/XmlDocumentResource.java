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


import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.MediaType;

/**
 * Implements the XmlDocument S-RAMP Atom API resource.  This provides the mechanism for adding, deleting,
 * and publishing XML documents in the S-RAMP repository.
 * 
 * @author eric.wittmann@redhat.com
 */
@Path("/s-ramp/core/XmlDocument")
public class XmlDocumentResource extends AbstractDocumentResource
{
	/**
	 * Default constructor.
	 */
	public XmlDocumentResource() {
	}
    
	/**
	 * Called for standard (non-multipart) http POST invocations.
	 * @param fileName the name of the file being POST'd - should be sent via HTTP header "Slug"
	 * @param body the body of the file being POST'd
	 */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry saveXmlDocument(@HeaderParam("Slug") String fileName, String body) {
    	return super.saveArtifact(fileName, body, ArtifactType.XmlDocument);
    }

    /*
    @POST
    @Consumes(MultipartConstants.MULTIPART_RELATED)
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Entry saveXsdDocument(MultipartRelatedInput input) {
        Entry entry = new Entry();
        try {
            
            List<InputPart> list = input.getParts();
            // Expecting 1 part. 
            if (list.size()!=1) ; //throw error
            InputPart firstPart = list.get(0);
           
            // First part being the Entry.
            MultivaluedMap<String, String> headers = firstPart.getHeaders();
            String fileName = headers.getFirst("Slug");
            InputStream is = firstPart.getBody(new GenericType<InputStream>() { });
            
            PersistenceManager persistanceManager = PersistenceFactory.newInstance();
            //store the content
            persistanceManager.persistArtifact(fileName, XSD,is);
            is.close();
            
            //create the derivedArtifacts
            DerivedArtifacts derivedArtifacts = DerivedArtifactsFactory.newInstance();
            XsdDocument xsdDocument = derivedArtifacts.createDerivedArtifact(XsdDocument.class, fileName);
            
            //persist the derivedArtifacts
            persistanceManager.persistDerivedArtifact(xsdDocument);
            
            entry = XsdModel.toEntry(xsdDocument);
            
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
        
        return entry;
    }

    @GET
    @Path("{id}")
    public XmlDocument getXmlDocument(@PathParam("id") int id) {
        //Entry entry = firstPart.getBody(Entry.class, null);
        return null;
    }
    */
    
  
}
