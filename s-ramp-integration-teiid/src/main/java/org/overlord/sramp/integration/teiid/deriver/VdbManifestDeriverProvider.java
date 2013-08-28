/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.integration.teiid.deriver;

import java.util.Collections;
import java.util.Map;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.DeriverProvider;
import org.overlord.sramp.integration.teiid.model.VdbManifest;

/**
 * This provider contributes an {@link ArtifactDeriver} to the S-RAMP repository for Teiid VDB manifest files.
 */
public final class VdbManifestDeriverProvider implements DeriverProvider {

    /**
     * @see org.overlord.sramp.common.derived.DeriverProvider#createArtifactDerivers()
     */
    @Override
    public Map<String, ArtifactDeriver> createArtifactDerivers() {
        return Collections.singletonMap(VdbManifest.ARTIFACT_TYPE.extendedType(), (ArtifactDeriver)new VdbManifestDeriver());
    }

}
