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
package org.overlord.sramp.atom.models;

import java.net.URI;
import java.net.URISyntaxException;

import org.jboss.resteasy.plugins.providers.atom.Category;
import org.jboss.resteasy.plugins.providers.atom.Content;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Link;
import org.jboss.resteasy.plugins.providers.atom.Person;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.ArtifactVisitorAdapter;
import org.overlord.sramp.atom.MediaType;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.XmlDocument;
import org.s_ramp.xmlns._2010.s_ramp.XsdDocument;

/**
 * Visitor used to convert an artifact to an Atom entry.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ArtifactToAtomEntryVisitor extends ArtifactVisitorAdapter {
	
	private Entry atomEntry;
	private Exception failure;
	
	/**
	 * Constructor.
	 */
	public ArtifactToAtomEntryVisitor() {
	}

	/**
	 * @return the atomEntry
	 */
	public Entry getAtomEntry() throws Exception {
		if (this.failure != null)
			throw this.failure;
		if (this.atomEntry == null)
			throw new Exception("Could not convert from artifact to Atom entry:  missing implementation in ArtifactToAtomEntryVisitor");
		return atomEntry;
	}

	/**
	 * Creates the base Atom Entry, doing the stuff that's common to all types of artifacts.
	 * @param artifact the artifact being converted to an {@link Entry}
	 * @return an Atom Entry
	 * @throws URISyntaxException 
	 */
	protected Entry createBaseArtifactEntry(BaseArtifactType artifact, ArtifactType artifactType) throws URISyntaxException {
        Entry entry = new Entry();
        entry.setId(new URI(artifact.getUuid()));
        entry.setUpdated(artifact.getLastModifiedTimestamp().toGregorianCalendar().getTime());
        entry.setTitle(artifact.getName());
        entry.setPublished(artifact.getCreatedTimestamp().toGregorianCalendar().getTime());
        entry.getAuthors().add(new Person(artifact.getCreatedBy()));
        entry.setSummary(artifact.getDescription());
        
        // Original content can be accessed at /s-ramp/{model}/{artifact-type}/{uid}/media
        Content content = new Content();
        content.setType(MediaType.APPLICATION_ATOM_XML_UTF8_TYPE);
        //TODO create URL Helper, obtain base URL from server
		content.setSrc(new URI("http://localhost:8080/changeit/s-ramp/"
				+ artifactType.getModel() + "/" + artifactType.name() + "/"
				+ artifact.getUuid() + "/media"));
        entry.setContent(content);
        
        // Self can be accessed at /s-ramp/{model}/{artifact-type}/{uid}
        Link linkToSelf = new Link();
        linkToSelf.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToSelf.setRel("self");
		linkToSelf.setHref(new URI("http://localhost:8080/changeit/s-ramp/"
				+ artifactType.getModel() + "/" + artifactType.name() + "/"
				+ artifact.getUuid()));
        entry.getLinks().add(linkToSelf);
        
        // Link to edit-media can be accessed at /s-ramp/{model}/{artifact-type}/{uid}/edit-media
        Link linkToEditMedia = new Link();
        linkToEditMedia.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToEditMedia.setRel("edit-media");
        linkToEditMedia.setHref(new URI("http://localhost:8080/changeit/s-ramp/"
				+ artifactType.getModel() + "/" + artifactType.name() + "/"
				+ artifact.getUuid() + "/edit-media"));
        entry.getLinks().add(linkToEditMedia);
        
        // Link to edit can be accessed at /s-ramp/{model}/{artifact-type}/{uid}
        Link linkToEdit = new Link();
        linkToEdit.setType(MediaType.APPLICATION_ATOM_XML_ENTRY_TYPE);
        linkToEdit.setRel("edit");
        linkToEdit.setHref(new URI("http://localhost:8080/changeit/s-ramp/"
				+ artifactType.getModel() + "/" + artifactType.name() + "/"
                + artifact.getUuid()));
        entry.getLinks().add(linkToEdit);
        
        //category
        Category category = new Category();
        category.setTerm(artifactType.name());
        category.setLabel(artifactType.getLabel());
        category.setScheme(new URI("x-s-ramp:2010:type"));
        entry.getCategories().add(category);
        
        return entry;
	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.XmlDocument)
	 */
	@Override
	public void visit(XmlDocument artifact) {
		try {
			Entry entry = createBaseArtifactEntry(artifact, ArtifactType.XmlDocument);
	        Artifact srampArty = new Artifact();
	        srampArty.setXmlDocument(artifact);
	        entry.setAnyOtherJAXBObject(srampArty);
	        this.atomEntry = entry;
		} catch (URISyntaxException e) {
			this.failure = e;
		}
	}

	/**
	 * @see org.overlord.sramp.ArtifactVisitor#visit(org.s_ramp.xmlns._2010.s_ramp.XsdDocument)
	 */
	@Override
	public void visit(XsdDocument artifact) {
		try {
			Entry entry = createBaseArtifactEntry(artifact, ArtifactType.XsdDocument);
	        Artifact srampArty = new Artifact();
	        srampArty.setXsdDocument(artifact);
	        entry.setAnyOtherJAXBObject(srampArty);
	        this.atomEntry = entry;
		} catch (URISyntaxException e) {
			this.failure = e;
		}
	}

}
