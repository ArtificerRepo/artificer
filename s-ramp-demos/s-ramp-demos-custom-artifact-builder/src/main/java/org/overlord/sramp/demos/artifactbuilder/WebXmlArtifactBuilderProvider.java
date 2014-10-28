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
package org.overlord.sramp.demos.artifactbuilder;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.common.artifactbuilder.ArtifactBuilderProvider;

/**
 * @author Brett Meyer
 */
public class WebXmlArtifactBuilderProvider implements ArtifactBuilderProvider {

    @Override
    public List<ArtifactBuilder> createArtifactBuilders(BaseArtifactType primaryArtifact, byte[] content) {
        List<ArtifactBuilder> builders = new ArrayList<ArtifactBuilder>();
        ArtifactType artifactType = ArtifactType.valueOf(primaryArtifact);
        if ("WebXmlDocument".equals(artifactType.getExtendedType())) {
            builders.add(new WebXmlArtifactBuilder());
        }
        return builders;
    }

}
