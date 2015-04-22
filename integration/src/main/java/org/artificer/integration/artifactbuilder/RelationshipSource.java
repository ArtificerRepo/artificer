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

/**
 * {@link ArtifactBuilder#buildRelationships(RelationshipContext)} needs to generate artifact relationships within
 * the primary artifact and the builder's generated derived artifacts.  For many integrations, this requires
 * specialized bookkeeping and logic.  One option is to build a collection of RelationshipSources during
 * {@link ArtifactBuilder#buildArtifacts(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, byte[])}.  These
 * sources provide enough context in order to find the *persisted* artifact after the buildArtifacts phase has
 * completed.
 * 
 * @see CriteriaQueryRelationshipSource
 * @see QNameRelationshipSource
 * @see NamespaceRelationshipSource
 * 
 * @author Brett Meyer
 */
public interface RelationshipSource {
    
    /**
     * Build this relationship using the given context.
     * 
     * @param context
     */
    public void build(RelationshipContext context) throws Exception;
}
