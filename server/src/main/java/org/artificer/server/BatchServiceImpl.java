/*
 * Copyright 2014 JBoss Inc
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
package org.artificer.server;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.archive.ArtificerArchiveEntry;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerException;
import org.artificer.common.error.ArtificerNotFoundException;
import org.artificer.common.error.ArtificerUserException;
import org.artificer.repository.PersistenceManager;
import org.artificer.repository.RepositoryProviderFactory;
import org.artificer.server.core.api.BatchResult;
import org.artificer.server.core.api.BatchService;
import org.artificer.server.mime.MimeTypes;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactType;

import javax.ejb.Remote;
import javax.ejb.Stateful;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Brett Meyer.
 */
@Stateful(name = "BatchService")
@Remote(BatchService.class)
public class BatchServiceImpl extends AbstractServiceImpl implements BatchService {

    @Override
    public BatchResult upload(ArtificerArchive archive)
            throws Exception {
        BatchResult batchResult = new BatchResult();
        PersistenceManager persistenceManager = persistenceManager();

        // Process all of the entries in the s-ramp package.  First, do all the create
        // entries.  Once the creates are done, do the updates.
        Collection<ArtificerArchiveEntry> entries = archive.getEntries();
        BatchCreate batchCreates = new BatchCreate();
        List<ArtificerArchiveEntry> updates = new ArrayList<ArtificerArchiveEntry>();
        for (ArtificerArchiveEntry entry : entries) {
            String path = entry.getPath();
            BaseArtifactType metaData = entry.getMetaData();
            if (isCreate(metaData)) {
                ArtifactType artifactType = ArtifactType.valueOf(metaData);
                String mimeType;
                InputStream entryIs = archive.getInputStream(entry);
                ArtifactContent entryContent = null;
                if (entryIs != null) {
                    entryContent = new ArtifactContent(path, archive.getInputStream(entry));
                    mimeType = MimeTypes.determineMimeType(metaData.getName(),
                            entryContent.getInputStream(), artifactType);
                } else {
                    mimeType = MimeTypes.determineMimeType(metaData.getName(), null, artifactType);
                }

                if (artifactType.isDerived()) {
                    throw ArtificerUserException.derivedArtifactCreate(artifactType.getArtifactType());
                }
                artifactType.setMimeType(mimeType);
                if (metaData instanceof DocumentArtifactType) {
                    ((DocumentArtifactType) metaData).setContentType(mimeType);
                }

                batchCreates.add(metaData, entryContent, entry.getPath());
            } else {
                updates.add(entry);
            }
        }

        // Now, send the creates to the persistence manager in a batch and process the responses.
        List<PersistenceManager.BatchItem> createItems = batchCreates.getBatchItems();
        List<Object> batchResponses = batchCreates.execute(persistenceManager);
        for (int i = 0; i < createItems.size(); i++) {
            PersistenceManager.BatchItem bi = createItems.get(i);
            Object response = batchResponses.get(i);
            if (response instanceof BaseArtifactType) {
                BaseArtifactType artifact = (BaseArtifactType) response;
                batchResult.getCreates().put(bi.batchItemId, artifact);
            } else if (response instanceof Exception) {
                batchResult.getErrors().put(bi.batchItemId, (Exception) response);
            }
        }

        // Finally, process all the updates.
        for (ArtificerArchiveEntry updateEntry : updates) {
            String path = updateEntry.getPath();
            InputStream updateIs = archive.getInputStream(updateEntry);
            ArtifactContent entryContent = null;
            if (updateIs != null) {
                entryContent = new ArtifactContent(path, updateIs);
            }
            String contentId = String.format("<%1$s@package>", path); //$NON-NLS-1$
            BaseArtifactType metaData = updateEntry.getMetaData();
            ArtifactType artifactType = ArtifactType.valueOf(metaData);
            metaData = processUpdate(artifactType, metaData, entryContent);
            batchResult.getUpdates().put(contentId, metaData);
        }

        return batchResult;
    }

    /**
     * Returns true if the given entry represents an artifact create operation.  Creates can be
     * done either with or without content (document vs. non-document type artifacts).
     * @param metaData
     */
    private boolean isCreate(BaseArtifactType metaData) {
        if (metaData.getUuid() == null) {
            return true;
        } else {
            return !artifactExists(metaData);
        }
    }

    /**
     * Returns true if the given artifact already exists in the repository.
     * @param metaData
     */
    private boolean artifactExists(BaseArtifactType metaData) {
        try {
            PersistenceManager persistenceManager = RepositoryProviderFactory.persistenceManager();
            ArtifactType artifactType = ArtifactType.valueOf(metaData);
            // TODO Bug: this would allow a re-used UUID as long as the artifact type was different.  Should change this to query via UUID instead.
            BaseArtifactType artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
            return artifact != null;
        } catch (ArtificerException e) {
            return false;
        }
    }

    /**
     * Process the case where we want to update the artifact's meta-data.
     * @param artifactType the artifact type
     * @param metaData the artifact meta-data
     * @param content the artifact content
     * @return BaseArtifactType
     * @throws Exception
     */
    private BaseArtifactType processUpdate(ArtifactType artifactType, BaseArtifactType metaData,
            ArtifactContent content) throws Exception {
        PersistenceManager persistenceManager = RepositoryProviderFactory.persistenceManager();
        BaseArtifactType artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);
        if (artifact == null)
            throw ArtificerNotFoundException.artifactNotFound(metaData.getUuid());

        // update the meta data
        persistenceManager.updateArtifact(metaData, artifactType);

        if (content != null) {
            persistenceManager.updateArtifactContent(metaData.getUuid(), artifactType, content);
        }

        // Refetch the data to make sure what we return is up-to-date
        artifact = persistenceManager.getArtifact(metaData.getUuid(), artifactType);

        return artifact;
    }
}
