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
import java.util.Collection;

import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.ArtifactVisitorHelper;
import org.overlord.sramp.atom.models.ArtifactToAtomEntryVisitor;
import org.overlord.sramp.repository.DerivedArtifactsFactory;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.PersistenceManager;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.DerivedArtifactType;

/**
 * Base class for all document resources in the S-RAMP Atom API binding.
 * 
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractDocumentResource {
	
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
        Entry entry = new Entry();
        try {
            PersistenceManager persistenceManager = PersistenceFactory.newInstance();
            //store the content
            InputStream is = new ByteArrayInputStream(artifactBody.getBytes(contentEncoding));
            BaseArtifactType artifact = persistenceManager.persistArtifact(artifactName, artifactType, is);
            is.close();
            
            //create the derivedArtifacts
            Collection<? extends DerivedArtifactType> dartifacts = DerivedArtifactsFactory.newInstance().createDerivedArtifacts(artifactType, artifact);
            
            //persist the derivedArtifacts
            for (DerivedArtifactType dartifact : dartifacts)
                persistenceManager.persistDerivedArtifact(dartifact);
            
            //return the entry containing the s-ramp artifact
            ArtifactToAtomEntryVisitor visitor = new ArtifactToAtomEntryVisitor();
            ArtifactVisitorHelper.visitArtifact(visitor, artifact);
            entry = visitor.getAtomEntry();
        } catch (Exception e) {
            //TODO
            e.printStackTrace();
        }
        return entry;
    }

}
