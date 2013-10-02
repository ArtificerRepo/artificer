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

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import java.io.InputStream;
import java.util.List;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.teiid.Utils;
import org.overlord.sramp.integration.teiid.model.TeiidExtendedType;
import org.overlord.sramp.integration.teiid.model.TeiidRelationshipType;

/**
 * Utilities use for Teiid integration tests.
 */
@SuppressWarnings( {"nls"} )
public abstract class TeiidIntegrationTest {

    /**
     * @param artifact the artifact whose custom property is being checked (cannot be <code>null</code>)
     * @param customPropertyName the custom property name whose value is being requested (cannot be <code>null</code> or empty)
     * @param expected the expected property value (can be <code>null</code> or empty)
     * @throws Exception if the test fails or there is a problem obtaining the property value
     */
    protected void assertPropertyValue( final BaseArtifactType artifact,
                                        final String customPropertyName,
                                        final String expected ) throws Exception {
        assert (artifact != null);
        assert ((customPropertyName != null) && !customPropertyName.isEmpty());

        final String actual = SrampModelUtils.getCustomProperty(artifact, customPropertyName);
        assertThat(actual, is(expected));
    }

    protected void assertRelationshipTargetUuid( final BaseArtifactType artifact,
                                                 final TeiidRelationshipType relationshipType,
                                                 final String targetArtifactUuid ) {
        assert (artifact != null);
        assert (relationshipType != null);
        assert ((targetArtifactUuid != null) && !targetArtifactUuid.isEmpty());

        final List<Relationship> relationships = artifact.getRelationship();

        if (Utils.isEmpty(relationships)) {
            throw new AssertionError("No relationships found for artifact '" + artifact.getName() + '\'');
        }

        boolean relationshipFound = false;
        boolean targetFound = false;

        for (final Relationship relationship : relationships) {
            if (relationship.getRelationshipType().equals(relationshipType.relationshipType())) {
                relationshipFound = true;
                final List<Target> targets = relationship.getRelationshipTarget();

                if (Utils.isEmpty(targets)) {
                    throw new AssertionError("No targets found for relationship '" + relationshipType + "' and artifact '"
                                             + artifact.getName() + '\'');
                }

                for (final Target target : targets) {
                    if (targetArtifactUuid.equals(target.getValue())) {
                        targetFound = true;
                        break;
                    }
                }
            }
        }

        if (!relationshipFound) {
            throw new AssertionError("Relationship '" + relationshipType + "' was not found for artifact '" + artifact.getName()
                                     + '\'');
        } else if (!targetFound) {
            throw new AssertionError("Target '" + targetArtifactUuid + ", for relationship '" + relationshipType
                                     + "' and artifact '" + artifact.getName() + "' was not found");
        }
    }

    /**
     * Obtains the content of a file resource.
     * 
     * @param fileName the file name relative to the calling class (cannot be <code>null</code> or empty)
     * @return the input stream to the content; may be <code>null</code> if the resource does not exist
     */
    protected InputStream getResourceAsStream( final String fileName ) {
        return getClass().getResourceAsStream(fileName);
    }

    /**
     * @param artifact the artifact being checked (cannot be <code>null</code>)
     * @param extendedType the expected extended type (cannot be <code>null</code>)
     * @return <code>true</code> if the artifact is an {@link ExtendedArtifactType} with the specified extended type
     */
    protected boolean isExtendedType( final BaseArtifactType artifact,
                                      final TeiidExtendedType extendedType ) {
        assert (artifact != null);
        assert (extendedType != null);

        if (artifact instanceof ExtendedArtifactType) {
            return extendedType.extendedType().equals(((ExtendedArtifactType)artifact).getExtendedType());
        }

        return false;
    }

}
