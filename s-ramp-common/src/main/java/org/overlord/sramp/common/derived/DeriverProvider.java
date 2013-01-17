/*
 * Copyright 2012 JBoss Inc
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

import java.util.Map;

/**
 * Implement this interface to provide a Deriver that will be used when an artifact
 * of a specific extended type is added to the repository.
 *
 * @author eric.wittmann@redhat.com
 */
public interface DeriverProvider {

    /**
     * Creates zero or more {@link ArtifactDeriver} instances to be used by the S-RAMP
     * repository when adding extended artifacts to the repository.
     *
     * @return a map of Extended type (extendedType) to {@link ArtifactDeriver} instance
     */
    public Map<String, ArtifactDeriver> createArtifactDerivers();

}
