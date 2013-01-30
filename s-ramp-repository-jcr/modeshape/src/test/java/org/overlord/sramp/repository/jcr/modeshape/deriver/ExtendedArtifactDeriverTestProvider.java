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
package org.overlord.sramp.repository.jcr.modeshape.deriver;

import java.util.HashMap;
import java.util.Map;

import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.derived.DeriverProvider;

/**
 * @author eric.wittmann@redhat.com
 */
public class ExtendedArtifactDeriverTestProvider implements DeriverProvider {

    /**
     * Constructor.
     */
    public ExtendedArtifactDeriverTestProvider() {
    }

    /**
     * @see org.overlord.sramp.common.derived.DeriverProvider#createArtifactDerivers()
     */
    @Override
    public Map<String, ArtifactDeriver> createArtifactDerivers() {
        Map<String, ArtifactDeriver> derivers = new HashMap<String, ArtifactDeriver>();
        derivers.put("ExtendedArtifactDeriverTestDocument", new ExtendedArtifactDeriverTestDeriver());
        return derivers;
    }

}
