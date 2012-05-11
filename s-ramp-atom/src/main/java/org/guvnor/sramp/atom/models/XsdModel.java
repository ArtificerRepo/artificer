package org.guvnor.sramp.atom.models;

import java.net.URI;
import java.net.URISyntaxException;

import org.guvnor.sramp.atom.MediaType;
import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

public class XsdModel {

    
    public static Entry toEntry(XsdDocument xsdDocument) throws URISyntaxException {
        Entry entry = new Entry();
        entry.setId(new URI(xsdDocument.getUuid()));
        entry.setUpdated(xsdDocument.getLastModifiedTimestamp().toGregorianCalendar().getTime());
        entry.setTitle(xsdDocument.getName());
        entry.setPublished(xsdDocument.getCreatedTimestamp().toGregorianCalendar().getTime());
        entry.getAuthors().add(new Person(xsdDocument.getCreatedBy()));
        entry.setSummary(xsdDocument.getDescription());
        
        Link contentLink = new Link();
        contentLink.setType(MediaType.APPLICATION_ATOM_XML_UTF8_TYPE);
        //TODO create URL Helper, obtain base URL from server
        contentLink.setHref(new URI("http://localhost:8080/changeit/s-ramp/xsd/XsdDocument/" 
                + xsdDocument.getUuid() + "/media"));
        entry.getLinks().add(contentLink);
        
        //link to self
        Link linkToSelf = new Link();
        linkToSelf.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToSelf.setRel("self");
        linkToSelf.setHref(new URI("http://localhost:8080/changeit/s-ramp/xsd/XsdDocument/" 
                + xsdDocument.getUuid()));
        entry.getLinks().add(linkToSelf);
        
        //link to edit-media
        Link linkToEditMedia = new Link();
        linkToEditMedia.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToEditMedia.setRel("edit-media");
        linkToEditMedia.setHref(new URI("http://localhost:8080/changeit/s-ramp/xsd/XsdDocument/" 
                + xsdDocument.getUuid() + "/edit-media"));
        entry.getLinks().add(linkToEditMedia);
        
         //link to edit
        Link linkToEdit = new Link();
        linkToEdit.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToEdit.setRel("edit");
        linkToEdit.setHref(new URI("http://localhost:8080/changeit/s-ramp/xsd/XsdDocument/" 
                + xsdDocument.getUuid() + "/edit"));
        entry.getLinks().add(linkToEdit);
        
        //category
        Category category = new Category();
        category.setTerm("XsdDocument");
        category.setLabel("XML Schema");
        category.setScheme(new URI("x-s-ramp:2010:type"));
        entry.getCategories().add(category);
        
        //artifact
        Artifact artifact = new Artifact();
        artifact.setXsdDocument(xsdDocument);
        Content content = new Content();
        content.setJAXBObject(artifact);
        entry.setContent(content);
        
        return entry;
    }
}
