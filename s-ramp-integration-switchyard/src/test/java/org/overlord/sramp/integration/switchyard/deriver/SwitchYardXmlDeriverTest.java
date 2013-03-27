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
package org.overlord.sramp.integration.switchyard.deriver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactEnum;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;
import org.s_ramp.xmlns._2010.s_ramp.ExtendedArtifactType;

/**
 * JUnit test for {@link SwitchYardXmlDeriver}.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardXmlDeriverTest {

    /**
     * Test method for {@link org.overlord.sramp.integration.switchyard.deriver.SwitchYardXmlDeriver#derive(java.util.Collection, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)}.
     * @throws IOException
     */
    @Test
    public void testDeriveCollectionOfBaseArtifactTypeBaseArtifactTypeElementXPath() throws IOException {
        SwitchYardXmlDeriver deriver = new SwitchYardXmlDeriver();
        BaseArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("switchyard.xml");
        InputStream is = getClass().getResourceAsStream("switchyard.xml");
        // Derive
        Collection<BaseArtifactType> derivedArtifacts = deriver.derive(artifact, is);

        // Assert
        Assert.assertNotNull(derivedArtifacts);
        Assert.assertEquals(1, derivedArtifacts.size());
        Assert.assertEquals("orders", artifact.getName());
        Assert.assertEquals("urn:switchyard-quickstart:bean-service:0.1.0", SrampModelUtils.getCustomProperty(artifact, "targetNamespace"));

        BaseArtifactType orderService = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardServiceType, "OrderService");
        Assert.assertNotNull(orderService);
        Assert.assertEquals("OrderService", orderService.getName());

    }

    /**
     * Gets a single derived artifact from the collection of derived artifacts.  Narrows
     * it down by type and name.
     * @param derivedArtifacts
     * @param artifactType
     * @param name
     */
    private BaseArtifactType getDerivedArtifact(Collection<BaseArtifactType> derivedArtifacts,
            ArtifactType artifactType, String name) {
        for (BaseArtifactType artifact : derivedArtifacts) {
            ArtifactType at = ArtifactType.valueOf(artifact);
            if (at.equals(artifactType) && artifact.getName().equals(name)) {
                return artifact;
            }
        }
        return null;
    }

}
