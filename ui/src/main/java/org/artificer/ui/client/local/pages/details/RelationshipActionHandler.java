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
package org.artificer.ui.client.local.pages.details;

import org.artificer.ui.client.local.services.callback.IServiceInvocationHandler;

/**
 * @author Brett Meyer.
 */
public interface RelationshipActionHandler {

    /**
     * Edit the identified relationship.  Note that the 'uuid' may be the source or the destination, depending
     * on the context.  It's up to the impls
     * @param oldRelationshipType
     * @param newRelationshipType
     * @param uuid
     */
    public void editRelationship(String oldRelationshipType, String newRelationshipType, String uuid,
            IServiceInvocationHandler<Void> editArtifactHandler);

    /**
     * Delete the identified relationship.  Note that the 'uuid' may be the source or the destination, depending
     * on the context.  It's up to the impls
     * @param relationshipType
     * @param uuid
     */
    public void deleteRelationship(String relationshipType, String uuid,
            IServiceInvocationHandler<Void> deleteArtifactHandler);
}
