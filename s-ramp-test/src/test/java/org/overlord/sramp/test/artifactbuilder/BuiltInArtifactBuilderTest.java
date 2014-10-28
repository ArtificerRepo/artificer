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
package org.overlord.sramp.test.artifactbuilder;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.test.AbstractIntegrationTest;

/**
 * @author Brett Meyer
 */
public class BuiltInArtifactBuilderTest extends AbstractIntegrationTest {
    
    @Test
    public void testXsdRelationships() throws Exception {
        InputStream baseContent = null;
        InputStream coreContent = null;
        InputStream xsdContent = null;
        try {
            baseContent = getClass().getResourceAsStream("/sample-files/xsd/xlink.xsd"); //$NON-NLS-1$
            coreContent = getClass().getResourceAsStream("/sample-files/xsd/coremodel.xsd"); //$NON-NLS-1$
            xsdContent = getClass().getResourceAsStream("/sample-files/xsd/xsdmodel.xsd"); //$NON-NLS-1$

            XsdDocument uploadedBaseArtifact = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    baseContent, "xlink.xsd");
            XsdDocument uploadedCoreArtifact = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    coreContent, "coremodel.xsd");
            XsdDocument uploadedXsdArtifact = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    xsdContent, "xsdmodel.xsd");

            assertEquals(1, uploadedCoreArtifact.getImportedXsds().size());
            assertEquals(uploadedBaseArtifact.getUuid(), uploadedCoreArtifact.getImportedXsds().get(0).getValue());
            assertEquals(1, uploadedXsdArtifact.getIncludedXsds().size());
            assertEquals(uploadedCoreArtifact.getUuid(), uploadedXsdArtifact.getIncludedXsds().get(0).getValue());
        } finally {
            IOUtils.closeQuietly(baseContent);
            IOUtils.closeQuietly(coreContent);
            IOUtils.closeQuietly(xsdContent);
        }
    }
}
