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
package org.artificer.server.core.api;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Brett Meyer.
 */
public class BatchResult implements Serializable {

    /**
     * Artifacts created as a result of the batch execution.  Key: "batchId", based off of the path in the batch.
     */
    private Map<String, BaseArtifactType> creates = new HashMap<String, BaseArtifactType>();

    /**
     * Artifacts updated as a result of the batch execution.  Key: "batchId", based off of the path in the batch.
     */
    private Map<String, BaseArtifactType> updates = new HashMap<String, BaseArtifactType>();

    public Map<String, BaseArtifactType> getCreates() {
        return creates;
    }

    public void setCreates(Map<String, BaseArtifactType> creates) {
        this.creates = creates;
    }

    public Map<String, BaseArtifactType> getUpdates() {
        return updates;
    }

    public void setUpdates(Map<String, BaseArtifactType> updates) {
        this.updates = updates;
    }
}
