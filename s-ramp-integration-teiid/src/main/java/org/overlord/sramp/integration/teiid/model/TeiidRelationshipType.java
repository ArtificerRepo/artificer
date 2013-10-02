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

package org.overlord.sramp.integration.teiid.model;

/**
 * The type of a Teiid artifact relationship.
 */
public interface TeiidRelationshipType {

    /**
     * @param proposedRelationshipType the type being checked (can be <code>null</code> or empty)
     * @return the relationship type (never <code>null</code>)
     * @throws IllegalArgumentException if the proposed type is not valid
     */
    TeiidRelationshipType convert( final String proposedRelationshipType );

    /**
     * @param proposedRelationshipType the type being checked (can be <code>null</code> or empty)
     * @return <code>true</code> if the parameter is a valid relationship type of a VDB manifest artifact
     */
    boolean isValid( final String proposedRelationshipType );

    /**
     * @return the relationship type (never <code>null</code> or empty)
     */
    String relationshipType();

}
