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
package org.artificer.integration.java.artifactbuilder;

import org.artificer.common.ArtifactContent;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;

import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author kurt.stam@redhat.com
 */
public class MavenPomArtifactBuilderTest {

    @Test
    public void testJavaDeriver() throws Exception {
        MavenPomArtifactBuilder builder = new MavenPomArtifactBuilder();
        ExtendedDocument artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("pom.xml"); //$NON-NLS-1$
        InputStream is = getClass().getResourceAsStream("pom.xml"); //$NON-NLS-1$
        // Derive
        @SuppressWarnings("unused")
		Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(artifact, new ArtifactContent("pom.xml", is))
		        .getDerivedArtifacts();
        is.close();
        // Assert
        Assert.assertNotNull(artifact.getProperty());
        for (Property property : artifact.getProperty()) {
        	System.out.println(property.getPropertyName() + " - " + property.getPropertyValue()); //$NON-NLS-1$
        }
        System.out.println(artifact.getDescription());
    }

}
