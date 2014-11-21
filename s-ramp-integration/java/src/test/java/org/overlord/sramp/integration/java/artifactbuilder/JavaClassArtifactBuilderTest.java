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
package org.overlord.sramp.integration.java.artifactbuilder;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.java.model.JavaModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 *
 * @author eric.wittmann@redhat.com
 */
public class JavaClassArtifactBuilderTest {

    @Test
    public void testJavaDeriver() throws IOException {
        JavaClassArtifactBuilder builder = new JavaClassArtifactBuilder();
        ExtendedDocument artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("MyClass.class"); //$NON-NLS-1$
        InputStream is = getClass().getResourceAsStream("MyClass.class"); //$NON-NLS-1$

        // Derive
        Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(artifact, new ArtifactContent("MyClass.class", is))
                .getDerivedArtifacts();
        is.close();
        // Assert
        Assert.assertNotNull(derivedArtifacts);
        Assert.assertEquals(0, derivedArtifacts.size());
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver.MyClass", artifact.getName()); //$NON-NLS-1$
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_PACKAGE_NAME)); //$NON-NLS-1$
        Assert.assertEquals("MyClass", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_CLASS_NAME)); //$NON-NLS-1$
        Assert.assertEquals(JavaModel.TYPE_JAVA_CLASS, artifact.getExtendedType());


        artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("MyInterface.class"); //$NON-NLS-1$
        is = getClass().getResourceAsStream("MyInterface.class"); //$NON-NLS-1$

        // Derive
        derivedArtifacts = builder.buildArtifacts(artifact, new ArtifactContent("MyInterface.class", is)).getDerivedArtifacts();
        is.close();
        // Assert
        Assert.assertNotNull(derivedArtifacts);
        Assert.assertEquals(0, derivedArtifacts.size());
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver.MyInterface", artifact.getName()); //$NON-NLS-1$
        Assert.assertEquals("org.overlord.sramp.integration.java.deriver", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_PACKAGE_NAME)); //$NON-NLS-1$
        Assert.assertEquals("MyInterface", SrampModelUtils.getCustomProperty(artifact, JavaModel.PROP_CLASS_NAME)); //$NON-NLS-1$
        Assert.assertEquals(JavaModel.TYPE_JAVA_INTERFACE, artifact.getExtendedType());
    }

}
