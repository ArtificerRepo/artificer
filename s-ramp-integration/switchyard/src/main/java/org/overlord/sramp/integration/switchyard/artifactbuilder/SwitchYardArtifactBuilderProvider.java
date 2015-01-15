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
package org.overlord.sramp.integration.switchyard.artifactbuilder;

import java.util.ArrayList;
import java.util.List;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.integration.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.integration.artifactbuilder.ArtifactBuilderProvider;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;

/**
 * @author Brett Meyer
 */
public class SwitchYardArtifactBuilderProvider implements ArtifactBuilderProvider {

    @Override
    public List<ArtifactBuilder> createArtifactBuilders(BaseArtifactType primaryArtifact, ArtifactContent artifactContent) {
        List<ArtifactBuilder> builders = new ArrayList<ArtifactBuilder>();
        ArtifactType artifactType = ArtifactType.valueOf(primaryArtifact);
        if (SwitchYardModel.SwitchYardXmlDocument.equals(artifactType.getExtendedType())) {
            builders.add(new SwitchYardXmlArtifactBuilder());
        }
        return builders;
    }

}
