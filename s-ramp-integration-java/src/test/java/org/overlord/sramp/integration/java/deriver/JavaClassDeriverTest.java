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
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.java.model.JavaModel;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class JavaClassDeriverTest {

    /**
     * Test method for {@link org.overlord.sramp.integration.switchyard.deriver.SwitchYardXmlDeriver#derive(java.util.Collection, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)}.
     * @throws IOException
     */
    @Test
    public void testJavaDeriver() throws IOException {
        JavaClassDeriver deriver = new JavaClassDeriver();
        ExtendedDocument artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("MyClass.class");
        InputStream is = getClass().getResourceAsStream("MyClass.class");

        // Derive
        Collection<BaseArtifactType> derivedArtifacts = deriver.derive(artifact, is);
        is.close();
        // Assert
        Assert.assertNotNull(derivedArtifacts);
        Assert.assertEquals(0, derivedArtifacts.size());
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver.MyClass", artifact.getName());
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_PACKAGE_NAME));
        Assert.assertEquals("MyClass", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_CLASS_NAME));
        Assert.assertEquals(JavaModel.TYPE_JAVA_CLASS, artifact.getExtendedType());


        artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("MyInterface.class");
        is = getClass().getResourceAsStream("MyInterface.class");

        // Derive
        derivedArtifacts = deriver.derive(artifact, is);
        is.close();
        // Assert
        Assert.assertNotNull(derivedArtifacts);
        Assert.assertEquals(0, derivedArtifacts.size());
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver.MyInterface", artifact.getName());
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_PACKAGE_NAME));
        Assert.assertEquals("MyInterface", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_CLASS_NAME));
        Assert.assertEquals(JavaModel.TYPE_JAVA_INTERFACE, artifact.getExtendedType());
    }

}
