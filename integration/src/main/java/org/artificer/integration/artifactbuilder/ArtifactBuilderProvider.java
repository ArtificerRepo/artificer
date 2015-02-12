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

import java.util.List;
import java.util.ServiceLoader;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.common.ArtifactContent;

/**
 * One or more {@link ArtifactBuilder}s are associated with one ArtifactBuilderProvider.  {@link ArtifactBuilder} logic
 * tends to be much cleaner when the builder is able to be *stateful*.  Therefore, this service contract creates
 * builders on demand, rather than singleton builder use.
 * 
 * Register this service through a simple {@link ServiceLoader} registration.  The following file should contain
 * the FQN of your module's ArtifactBuilderProvider implementations, one per line:
 * 
 * src/main/resources/META-INF/services/ArtifactBuilderProvider
 * 
 * @author Brett Meyer
 */
public interface ArtifactBuilderProvider {
    
    /**
     * Provide a list of providers, applicable to the given {@link BaseArtifactType}.  The whole artifact is provided
     * in order to allow contextual logic.
     * 
     * @param primaryArtifact
     * @param artifactContent
     * @return List<ArtifactBuilder>
     */
    public List<ArtifactBuilder> createArtifactBuilders(BaseArtifactType primaryArtifact, ArtifactContent artifactContent);
}
