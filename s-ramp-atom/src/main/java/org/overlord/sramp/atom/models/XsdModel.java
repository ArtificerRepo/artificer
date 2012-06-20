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
package org.overlord.sramp.atom.models;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.overlord.sramp.atom.MediaType;
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
        
        Content content = new Content();
        content.setType(MediaType.APPLICATION_ATOM_XML_UTF8_TYPE);
        //TODO create URL Helper, obtain base URL from server
        content.setSrc(new URI("http://localhost:8080/changeit/s-ramp/xsd/XsdDocument/" 
                + xsdDocument.getUuid() + "/media"));
        entry.setContent(content);
        
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
        entry.setAnyOtherJAXBObject(artifact);
        
        return entry;
    }
}
