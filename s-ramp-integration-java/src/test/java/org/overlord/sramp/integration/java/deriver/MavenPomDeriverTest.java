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
package org.overlord.sramp.integration.java.deriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;

/**
 *
 * @author kurt.stam@redhat.com
 */
public class MavenPomDeriverTest {

    /**
     * Test method for {@link org.overlord.sramp.integration.switchyard.deriver.SwitchYardXmlDeriver#derive(java.util.Collection, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)}.
     * @throws IOException
     */
    @Test
    public void testJavaDeriver() throws IOException {
        MavenPomDeriver deriver = new MavenPomDeriver();
        ExtendedDocument artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("pom.xml"); //$NON-NLS-1$
        InputStream is = getClass().getResourceAsStream("pom.xml"); //$NON-NLS-1$
        // Derive
        @SuppressWarnings("unused")
		Collection<BaseArtifactType> derivedArtifacts = deriver.derive(artifact, is);
        is.close();
        // Assert
        Assert.assertNotNull(artifact.getProperty());
        for (Property property : artifact.getProperty()) {
        	System.out.println(property.getPropertyName() + " - " + property.getPropertyValue()); //$NON-NLS-1$
        }
        System.out.println(artifact.getDescription());
    }

}
