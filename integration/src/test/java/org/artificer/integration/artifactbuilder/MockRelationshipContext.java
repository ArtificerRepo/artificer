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
package org.artificer.integration.artifactbuilder;

import org.artificer.common.query.ArtifactSummary;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * @author Brett Meyer
 */
public class MockRelationshipContext implements RelationshipContext {
    
    @Override
    public Collection<ArtifactSummary> findArtifacts(String model, String type, Map<String, String> criteria)
            throws Exception {
        ArtifactSummary doc = new ArtifactSummary();
        doc.setType("Document");
        doc.setModel("core");
        doc.setName("Mock Artifact"); //$NON-NLS-1$
        doc.setUuid(UUID.randomUUID().toString());
        return Collections.singletonList(doc);
    }
}
