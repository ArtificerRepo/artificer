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
package org.overlord.sramp.common.derived;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.artifactbuilder.AbstractArtifactBuilder;
import org.overlord.sramp.common.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.common.artifactbuilder.RelationshipContext;

/**
 * A temporary means to link the old ArtifactDeriver to the new ArtifactBuilder
 * 
 * @deprecated
 * 
 * @author Brett Meyer
 */
@Deprecated
public class ArtifactBuilderAdapter extends AbstractArtifactBuilder {
    
    private final ArtifactDeriver deriver;
    
    public ArtifactBuilderAdapter(ArtifactDeriver deriver) {
        this.deriver = deriver;
    }
    
    @Override
    public ArtifactBuilder buildArtifacts(BaseArtifactType primaryArtifact, byte[] contentBytes) throws IOException {
        super.buildArtifacts(primaryArtifact, contentBytes);
        getDerivedArtifacts().addAll(deriver.derive(getPrimaryArtifact(), getContentStream()));
        return this;
    }

    @Override
    public ArtifactBuilder buildRelationships(final RelationshipContext context) throws IOException {
        LinkerContext linkerContext = new LinkerContext() {
            @Override
            public Collection<BaseArtifactType> findArtifacts(String model, String type, Map<String, String> criteria) {
                return context.findArtifacts(model, type, criteria);
            }
        };
        deriver.link(linkerContext, getPrimaryArtifact(), getDerivedArtifacts());
        return this;
    }

}
