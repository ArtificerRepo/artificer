package org.guvnor.sramp.atom.services;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

@Path("/s-ramp")
public class EntryResource
{
    @GET
    @Path("entry")
    @Produces("application/atom+xml")
    public Entry getEntry() {
 
        Entry entry = new Entry();
        entry.setTitle("Hello World");
        Content content = new Content();
        Artifact artifact = new Artifact();
        XsdDocument xsdDocument = new XsdDocument();
        xsdDocument.setName("my.xsd");
        artifact.setXsdDocument(xsdDocument);
        
        content.setJAXBObject(artifact);
        entry.setContent(content);

        return entry;
    }
    
  
}
