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
package org.artificer.test.artifactbuilder;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.artificer.common.ArtifactType;
import org.artificer.test.AbstractIntegrationTest;

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
    
    /**
     * Ensure that if a duplicate derived artifact exists, the one stemming from the current batch is used
     * during relationship resolution.  See SRAMP-466
     * 
     * @throws Exception
     */
    @Test
    public void testRelationshipTargetOrdering() throws Exception {
        InputStream baseContent1 = null;
        InputStream baseContent2 = null;
        InputStream coreContent1 = null;
        InputStream coreContent2 = null;
        try {
            baseContent1 = getClass().getResourceAsStream("/sample-files/xsd/xlink.xsd"); //$NON-NLS-1$
            XsdDocument uploadedBaseArtifact1 = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    baseContent1, "xlink.xsd");
            coreContent1 = getClass().getResourceAsStream("/sample-files/xsd/coremodel.xsd"); //$NON-NLS-1$
            XsdDocument uploadedCoreArtifact1 = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    coreContent1, "coremodel.xsd");
            
            // do it again
            baseContent2 = getClass().getResourceAsStream("/sample-files/xsd/xlink.xsd"); //$NON-NLS-1$
            XsdDocument uploadedBaseArtifact2 = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    baseContent2, "xlink.xsd");
            coreContent2 = getClass().getResourceAsStream("/sample-files/xsd/coremodel.xsd"); //$NON-NLS-1$
            XsdDocument uploadedCoreArtifact2 = (XsdDocument) client().uploadArtifact(ArtifactType.XsdDocument(),
                    coreContent2, "coremodel.xsd");

            assertEquals(1, uploadedCoreArtifact1.getImportedXsds().size());
            assertEquals(1, uploadedCoreArtifact2.getImportedXsds().size());
            // If ordering worked properly... (seems overly simple, but it covers the main issue)
            assertEquals(uploadedBaseArtifact1.getUuid(), uploadedCoreArtifact1.getImportedXsds().get(0).getValue());
            assertEquals(uploadedBaseArtifact2.getUuid(), uploadedCoreArtifact2.getImportedXsds().get(0).getValue());
        } finally {
            IOUtils.closeQuietly(coreContent1);
            IOUtils.closeQuietly(coreContent2);
        }
    }
}
