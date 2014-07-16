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
package org.overlord.sramp.test.simple;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.test.AbstractIntegrationTest;


/**
 * @author Brett Meyer
 *
 */
@RunWith(Arquillian.class)
public class SimpleClientTest extends AbstractIntegrationTest {
    
    @Test
    public void testSimple() {
        try {
            SrampAtomApiClient client = client();
            ExtendedArtifactType artifact = new ExtendedArtifactType();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            artifact.setExtendedType("FooArtifactType"); //$NON-NLS-1$
            artifact.setName("Foo"); //$NON-NLS-1$
            artifact.setDescription("I'm a Foo"); //$NON-NLS-1$
            BaseArtifactType createdArtifact = client.createArtifact(artifact);
            
            assertNotNull(createdArtifact);
            assertEquals(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE, createdArtifact.getArtifactType());
            assertEquals("Foo", createdArtifact.getName()); //$NON-NLS-1$
            assertEquals("I'm a Foo", createdArtifact.getDescription()); //$NON-NLS-1$
            assertNotNull(createdArtifact.getUuid());
            assertTrue(createdArtifact.getUuid().length() > 0);
            
            BaseArtifactType queriedArtifact = client.getArtifactMetaData(createdArtifact.getUuid());
            
            assertNotNull(queriedArtifact);
            assertEquals(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE, queriedArtifact.getArtifactType());
            assertEquals("Foo", queriedArtifact.getName()); //$NON-NLS-1$
            assertEquals("I'm a Foo", queriedArtifact.getDescription()); //$NON-NLS-1$
            assertEquals(createdArtifact.getUuid(), queriedArtifact.getUuid());
            
            QueryResultSet results = client.buildQuery("/s-ramp/ext/FooArtifactType[@name = ?]") //$NON-NLS-1$
                    .parameter("Foo") //$NON-NLS-1$
                    .query();
            assertEquals(1, results.size());
            ArtifactSummary summary = results.get(0);
            
            assertNotNull(summary);
            assertEquals("Foo", summary.getName()); //$NON-NLS-1$
            assertEquals("I'm a Foo", summary.getDescription()); //$NON-NLS-1$
            assertEquals(createdArtifact.getUuid(), summary.getUuid());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
