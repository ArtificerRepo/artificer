/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.test.client;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.test.AbstractIntegrationTest;

/**
 * Base class for client tests.
 *
 * @author eric.wittmann@redhat.com
 * @author Brett Meyer
 */
public abstract class AbstractClientTest extends AbstractIntegrationTest {

	@AfterClass
	public static void resetAuditing() {
        System.clearProperty(SrampConstants.SRAMP_CONFIG_AUDITING);
	}

    /**
     * Adds an XML document.
     * @throws Exception
     */
    protected BaseArtifactType addXmlDoc() throws Exception {
        String artifactFileName = "PO.xml"; //$NON-NLS-1$
        InputStream is = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName); //$NON-NLS-1$
        try {
            SrampAtomApiClient client = client();
            return client.uploadArtifact(ArtifactType.XmlDocument(), is, artifactFileName);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }
}
