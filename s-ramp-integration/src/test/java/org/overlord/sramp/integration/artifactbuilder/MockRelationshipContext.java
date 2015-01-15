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
package org.overlord.sramp.integration.artifactbuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Document;

/**
 * @author Brett Meyer
 */
public class MockRelationshipContext implements RelationshipContext {
    
    @Override
    public Collection<BaseArtifactType> findArtifacts(String model, String type, Map<String, String> criteria) {
        BaseArtifactType doc = new Document();
        doc.setArtifactType(BaseArtifactEnum.DOCUMENT);
        doc.setName("Mock Artifact"); //$NON-NLS-1$
        doc.setUuid(UUID.randomUUID().toString());
        return Collections.singletonList(doc);
    }
}
