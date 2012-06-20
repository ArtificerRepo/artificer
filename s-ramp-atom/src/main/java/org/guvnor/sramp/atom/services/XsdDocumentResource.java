package org.guvnor.sramp.atom.services;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MultivaluedMap;

import static org.guvnor.sramp.atom.Constants.XSD;
import org.guvnor.sramp.atom.MediaType;
import org.guvnor.sramp.atom.models.XsdModel;
import org.guvnor.sramp.repository.DerivedArtifacts;
import org.guvnor.sramp.repository.DerivedArtifactsFactory;
import org.guvnor.sramp.repository.PersistenceFactory;
import org.guvnor.sramp.repository.PersistenceManager;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;
import org.jboss.resteasy.plugins.providers.multipart.MultipartConstants;
import org.jboss.resteasy.plugins.providers.multipart.MultipartRelatedInput;
import org.jboss.resteasy.util.GenericType;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

@Path("/s-ramp/xsd/XsdDocument")
public class XsdDocumentResource
{
    
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_ATOM_XML_ENTRY)
    public Entry saveXsdDocument(@HeaderParam("Slug") String fileName, String body) {
        Entry entry = new Entry();
        try {
            PersistenceManager persistanceManager = PersistenceFactory.newInstance();
            //store the content
            InputStream is = new ByteArrayInputStream(body.getBytes("UTF-8"));
            persistanceManager.persistArtifact(fileName, XSD, is);
            is.close();
            
            //create the derivedArtifacts
            DerivedArtifacts derivedArtifacts = DerivedArtifactsFactory.newInstance();
            XsdDocument xsdDocument = derivedArtifacts.createDerivedArtifact(XsdDocument.class, fileName);
            
            //persist the derivedArtifacts
            persistanceManager.persistDerivedArtifact(xsdDocument);
            
            //return the entry containing an artifact which contains the Derived XSD Document Model
            entry = XsdModel.toEntry(xsdDocument);
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
        return entry;
    }
    
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
    
    public XsdDocument getXsdDocument(@PathParam("id") int id) {
        //Entry entry = firstPart.getBody(Entry.class, null);
        return null;
    }
    
  
}
