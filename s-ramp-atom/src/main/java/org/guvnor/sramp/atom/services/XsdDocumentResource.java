package org.guvnor.sramp.atom.services;


import java.net.URI;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.guvnor.sramp.atom.SRAMP_UUID;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

@Path("/s-ramp/xsd/XsdDocument")
public class XsdDocumentResource
{
    @POST
    @Consumes(MultipartConstants.MULTIPART_RELATED)
    @Produces(MediaType.APPLICATION_ATOM_XML)
    public Entry createXsdDocument(MultipartRelatedInput input) {

        try {
            
            List<InputPart> list = input.getParts();
            // Expecting 2 parts. 
            if (list.size()<2) ;// error
            InputPart firstPart = list.get(0);
            //InputPart secondPart = list.get(1);
            // First part being the Entry.
            MultivaluedMap<String, String> headers = firstPart.getHeaders();
            //String fileName = headers.get("Slug");
            Entry entry = firstPart.getBody(Entry.class, null);
            
            System.out.println(entry);
            // Second part being the file content (XML in this case)
            //String fileContent = secondPart.getBodyAsString();
            //store the file to JCR.
            
            
            //store XsdDocument
            
            //store fileContent
            
            //TODO get the xsd document, decompose and persist
            URI id = SRAMP_UUID.createRandomUUID();
            XsdDocument xsdDocument = new XsdDocument();
            xsdDocument.setUuid(null);
            
            //Formulate the response
            entry = new Entry();
            entry.setId(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        
        return null;
    }
    
    @GET
    @Path("{id}")
    
    public XsdDocument getXsdDocument(@PathParam("id") int id) {
        return null;
    }
    
  
}
