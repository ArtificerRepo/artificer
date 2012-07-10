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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.atom.models.ArtifactToFullAtomEntryVisitor;
import org.overlord.sramp.atom.models.ArtifactToSummaryAtomEntryVisitor;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.overlord.sramp.visitors.ArtifactVisitorHelper;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for all document resources in the S-RAMP Atom API binding.
 * 
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractDocumentResource {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * Default constructor.
	 */
	public AbstractDocumentResource() {
	}
	
    /**
	 * Saves a document in the repository using the persistence manager.  Default content
	 * encoding of the artifact body is UTF-8.
     * @param artifactName the name of the artifact being saved
     * @param artifactBody the content of the artifact being saved
     * @param artifactType the type of the artifact being saved
     */
    protected Entry saveArtifact(String artifactName, String artifactBody, ArtifactType artifactType) {
    	return saveArtifact(artifactName, artifactBody, artifactType, "UTF-8");
    }

	/**
	 * Saves a document in the repository using the persistence manager.
     * @param artifactName the name of the artifact being saved
     * @param artifactBody the content of the artifact being saved
     * @param artifactType the type of the artifact being saved
     * @param contentEncoding the encoding of the artifact content
	 */
    protected Entry saveArtifact(String artifactName, String artifactBody, ArtifactType artifactType, String contentEncoding) {
        try {
			InputStream artifactInputStream = new ByteArrayInputStream(artifactBody.getBytes(contentEncoding));
			return saveArtifact(artifactName, artifactInputStream, artifactType);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
    }

	/**
	 * Saves a document in the repository using the persistence manager.
     * @param artifactName the name of the artifact being saved
     * @param artifactBody the content of the artifact being saved
     * @param artifactType the type of the artifact being saved
     * @return an Atom entry representing the persisted artifact
	 */
    protected Entry saveArtifact(String artifactName, InputStream artifactBody, ArtifactType artifactType) {
        InputStream is = artifactBody;
        try {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            //store the content
            BaseArtifactType artifact = persistenceManager.persistArtifact(artifactName, artifactType, is);
            
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
        	// TODO need better error handling
			throw new RuntimeException(e);
        } finally {
        	IOUtils.closeQuietly(is);
        }
    }

	/**
	 * Gets a single artifact (by UUID) as an Atom Entry.
	 * @param uuid the UUID of the s-ramp artifact
	 * @param artifactType the type of artifact we are expecting
	 * @return an s-ramp extended Atom entry
	 */
    protected Entry getArtifact(String uuid, ArtifactType artifactType) {
        try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			// Get the artifact by UUID
			BaseArtifactType artifact = persistenceManager.getArtifact(uuid, artifactType);
			if (artifact == null)
				return null;

			//return the entry containing the s-ramp artifact
			ArtifactToFullAtomEntryVisitor visitor = new ArtifactToFullAtomEntryVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, artifact);
			return visitor.getAtomEntry();
		} catch (Exception e) {
        	// TODO need better error handling
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets the content of the artifact.
	 * @param uuid the UUID of the s-ramp artifact
	 * @param artifactType the type of artifact we are expecting
	 * @return the artifact content
	 */
	protected InputStream getArtifactContent(String uuid, ArtifactType artifactType) {
        try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			return persistenceManager.getArtifactContent(uuid, artifactType);
		} catch (Exception e) {
        	// TODO need better error handling
			throw new RuntimeException(e);
		}
	}

	/**
	 * Updates an artifact in the s-ramp repository.
	 * @param artifact the s-ramp artifact to update
	 * @param type the type of the artifact
	 */
	protected void updateArtifact(BaseArtifactType artifact, ArtifactType type) {
        try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			persistenceManager.updateArtifact(artifact, type);
		} catch (Exception e) {
        	// TODO need better error handling
			throw new RuntimeException(e);
		}
	}

	/**
	 * Gets a {@link Feed} of {@link Entry}s for the given S-RAMP artifact type.
	 * @param type the S-RAMP artifact type
	 * @return an Atom {@link Feed}
	 */
	protected Feed getFeed(ArtifactType type) {
        try {
			PersistenceManager persistenceManager = PersistenceFactory.newInstance();
			List<BaseArtifactType> artifacts = persistenceManager.getArtifacts(type);

			Feed feed = new Feed();
			feed.setId(new URI("urn:sramp:feed:/" + type.getModel() + "/" + type.name()));
			feed.setTitle("S-RAMP Artifact Feed");
			feed.setSubtitle("A feed of S-RAMP artifacts of type '" + type + "'.");
			feed.setUpdated(new Date());
			// TODO implement pagination by providing next/previous Atom feed links
//			feed.getLinks().add(new Link("","http://example.com"));

            ArtifactToSummaryAtomEntryVisitor visitor = new ArtifactToSummaryAtomEntryVisitor();
			for (BaseArtifactType artifact : artifacts) {
	            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
	            Entry entry = visitor.getAtomEntry();
				feed.getEntries().add(entry);
			}

			return feed;
		} catch (Exception e) {
        	// TODO need better error handling
			throw new RuntimeException(e);
		}
	}
	
}
