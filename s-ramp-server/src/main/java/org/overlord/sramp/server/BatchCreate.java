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
package org.overlord.sramp.server;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.repository.PersistenceManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Both BatchResource and ArtifactResource (when an archive is expanded) need to batch upload.  This class
 * encapsulates the flow.
 *
 * @author Brett Meyer.
 */
public class BatchCreate {

    private final List<PersistenceManager.BatchItem> batchItems = new ArrayList<PersistenceManager.BatchItem>();

    public void add(BaseArtifactType artifact, ArtifactContent artifactContent, String path) throws Exception {
        String contentId = String.format("<%1$s@package>", path); //$NON-NLS-1$
        PersistenceManager.BatchItem bi = new PersistenceManager.BatchItem(contentId, artifact, artifactContent);
        batchItems.add(bi);
    }

    public List<Object> execute(PersistenceManager persistenceManager) throws Exception {
        return persistenceManager.persistBatch(batchItems);
    }

    public List<PersistenceManager.BatchItem> getBatchItems() {
        return batchItems;
    }
}
