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
package org.overlord.sramp.repository;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampException;
import org.overlord.sramp.common.ontology.SrampOntology;

/**
 * Service used to persist artifacts to some (permanent?) storage.
 */
public interface PersistenceManager extends AbstractManager {

    /**
     * Persists a batch of artifacts.  For each item in the batch, either a {@link BaseArtifactType}
     * or {@link SrampException} is returned in the result list.  Note that any content streams
     * provided in the list of batch items will be closed.
     * @param items
     * @throws SrampException
     */
    public List<Object> persistBatch(List<BatchItem> items) throws SrampException;

    /**
     * Persists a single artifact.
     * @param baseArtifactType, the s-ramp artifact
     * @param content, the artifact content
     * @throws SrampException
     */
    public BaseArtifactType persistArtifact(BaseArtifactType baseArtifactType, ArtifactContent content) throws SrampException;

	/**
	 * Gets a previously persisted artifact by its UUID.
	 * @param uuid the UUID of the s-ramp artifact
	 * @param type the type of the artifact
	 * @return an instance of a {@link BaseArtifactType} or null if not found
	 * @throws SrampException
	 */
	public BaseArtifactType getArtifact(String uuid, ArtifactType type) throws SrampException;

	/**
	 * Gets the content (media) for a previously persisted artifact by its UUID.
	 * <br/><br/>
	 * <b><i>Note: it is the responsibility of the calling method to close the
	 * resulting {@link InputStream}.</i></b>
	 * @param uuid the S-RAMP uuid of the artifact.
	 * @param artifactType the type of the artifact
	 * @return an {@link InputStream} over the artifact content
	 * @throws SrampException
	 */
	public InputStream getArtifactContent(String uuid, ArtifactType artifactType) throws SrampException;

	/**
	 * Updates a previously persisted artifact.  Note that this method only updates the meta data
	 * of the artifact, not the content.  This will not create or delete any derived artifacts.
	 * @param artifact the s-ramp artifact being updated
	 * @param type the type of the artifact
	 * @throws SrampException
	 */
	public BaseArtifactType updateArtifact(BaseArtifactType artifact, ArtifactType type) throws SrampException;

	/**
	 * Called to update the content for an existing artifact.
	 * @param uuid a UUID of an existing artifact
	 * @param artifactType the artifact's type
	 * @param content the new artifact content
	 * @throws SrampException
	 */
	public BaseArtifactType updateArtifactContent(String uuid, ArtifactType artifactType, ArtifactContent content) throws SrampException;

	/**
	 * Deletes a previously persisted artifact from the S-RAMP repository.
	 * @param uuid
	 * @param artifactType
	 * @throws SrampException
	 */
	public BaseArtifactType deleteArtifact(String uuid, ArtifactType artifactType) throws SrampException;

    /**
     * Deletes a previously persisted artifact's content from the S-RAMP repository.
     * @param uuid
     * @param artifactType
     * @throws SrampException
     */
    public BaseArtifactType deleteArtifactContent(String uuid, ArtifactType artifactType) throws SrampException;

    /**
     * Persists a single ontology.
     * @param ontology
     * @throws SrampException
     */
    public SrampOntology persistOntology(SrampOntology ontology) throws SrampException;

	/**
	 * Gets a previously persisted ontology by its UUID.
	 * @param uuid the UUID of the s-ramp ontology
	 * @return an instance of a {@link SrampOntology} or null if not found
	 * @throws SrampException
	 */
	public SrampOntology getOntology(String uuid) throws SrampException;

	/**
	 * Gets all of the ontologies known to the S-RAMP repository.
	 * @throws SrampException
	 */
	public List<SrampOntology> getOntologies() throws SrampException;

	/**
	 * Updates a previously persisted ontology.
	 * @param ontology the s-ramp artifact being updated
	 * @throws SrampException
	 */
	public void updateOntology(SrampOntology ontology) throws SrampException;

	/**
	 * Deletes a previously persisted ontology from the S-RAMP repository.
	 * @param uuid
	 * @throws SrampException
	 */
	public void deleteOntology(String uuid) throws SrampException;

    /**
     * Persists a single StoredQuery.
     * @param storedQuery
     * @throws SrampException
     */
    public StoredQuery persistStoredQuery(StoredQuery storedQuery) throws SrampException;

    /**
     * Gets a previously persisted StoredQuery by its UUID.
     * @param queryName the queryName of the s-ramp StoredQuery
     * @return an instance of a {@link StoredQuery} or null if not found
     * @throws SrampException
     */
    public StoredQuery getStoredQuery(String queryName) throws SrampException;

    /**
     * Gets all of the StoredQueries known to the S-RAMP repository.
     * @throws SrampException
     */
    public List<StoredQuery> getStoredQueries() throws SrampException;

    /**
     * Updates a previously persisted StoredQuery.
     * @param queryName
     * @param storedQuery the StoredQuery being updated
     * @throws SrampException
     */
    public void updateStoredQuery(String queryName, StoredQuery storedQuery) throws SrampException;

    /**
     * Deletes a previously persisted StoredQuery from the S-RAMP repository.
     * @param queryName
     * @throws SrampException
     */
    public void deleteStoredQuery(String queryName) throws SrampException;

	/**
	 * TODO remove this
	 * @param uuid
	 * @param type
	 */
    public void printArtifactGraph(String uuid, ArtifactType type);

    /**
     * Called to startup the persistence manager, providing an opportunity
     * for the implementation to bootstrap.  This is optional - perhaps an
     * implementation will want to lazy-load itself on first use.  It is 
     * recommended to implement this if it makes sense, however.
     */
    public void startup();

    /**
     * Called to shutdown the persistence manager, cleaning up any resources
     * being held open.  This is typically called when the container hosting
     * the s-ramp repository is shut down.
     */
    public void shutdown();

    /**
     * An item in a batch of items to be processed by persistBatch().
     */
    public static class BatchItem {
        public String batchItemId;
        public BaseArtifactType baseArtifactType;
        public ArtifactContent content;
        public Map<String, Object> attributes = new HashMap<String, Object>();

        /**
         * Constructor.
         * @param batchItemId
         * @param baseArtifactType
         * @param content
         */
        public BatchItem(String batchItemId, BaseArtifactType baseArtifactType, ArtifactContent content) {
            this.batchItemId = batchItemId;
            this.baseArtifactType = baseArtifactType;
            this.content = content;
        }
    }
}
