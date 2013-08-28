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
package org.overlord.sramp.integration.teiid;

import java.util.Collection;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.teiid.model.TeiidRelationshipType;

/**
 * Utilities used by the Teiid S-RAMP integration module.
 */
public final class Utils {

    private static final String ELEMENT_QUERY_PATTERN = "./%s"; //$NON-NLS-1$

    /**
     * Creates a one way relationship. Creates the relationship instance if necessary.
     * 
     * @param sourceArtifact the artifact where the relationship starts (cannot be <code>null</code>)
     * @param targetArtifact the artifact where the relationship ends (cannot be <code>null</code>)
     * @param relationshipType the relationship type (cannot be <code>null</code> or empty)
     */
    public static void addRelationship( final BaseArtifactType sourceArtifact,
                                        final BaseArtifactType targetArtifact,
                                        final TeiidRelationshipType relationshipType ) {
        SrampModelUtils.addGenericRelationship(sourceArtifact, relationshipType.relationshipType(), targetArtifact.getUuid());
    }

    /**
     * Creates a two way relationship. Creates the relationship instances if necessary.
     * 
     * @param sourceArtifact the artifact where the relationship starts (cannot be <code>null</code>)
     * @param targetArtifact the artifact where the relationship ends (cannot be <code>null</code>)
     * @param relationshipType the relationship type (cannot be <code>null</code> or empty)
     * @param inverseRelationshipType the relationship type of the inverse relationship (cannot be <code>null</code> or empty)
     */
    public static void addTwoWayRelationship( final BaseArtifactType sourceArtifact,
                                              final BaseArtifactType targetArtifact,
                                              final TeiidRelationshipType relationshipType,
                                              final TeiidRelationshipType inverseRelationshipType ) {
        SrampModelUtils.addGenericRelationship(sourceArtifact, relationshipType.relationshipType(), targetArtifact.getUuid());
        SrampModelUtils.addGenericRelationship(targetArtifact,
                                               inverseRelationshipType.relationshipType(),
                                               sourceArtifact.getUuid());
    }

    /**
     * @param qualifiedName the qualified name of the element whose query string is being requested (cannot be <code>null</code>
     *        or empty)
     * @return the query string (never <code>null</code>)
     */
    public static String getElementQueryString( final String qualifiedName ) {
        return String.format(ELEMENT_QUERY_PATTERN, qualifiedName);
    }

    /**
     * @param collection the collection being checked (can be <code>null</code> or empty)
     * @return <code>true</code> if <code>null</code> or empty
     */
    public static boolean isEmpty( final Collection<?> collection ) {
        return ((collection == null) || collection.isEmpty());
    }

    /**
     * @param text the string being checked (can be <code>null</code> or empty)
     * @return <code>true</code> if <code>null</code> or empty
     */
    public static boolean isEmpty( final String text ) {
        return ((text == null) || text.isEmpty());
    }

    /**
     * Don't allow construction outside of this class.
     */
    private Utils() {
        // nothing to do
    }

}
