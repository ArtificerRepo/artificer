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
package org.artificer.test.artifacttypedetector;

import org.apache.commons.io.IOUtils;
import org.artificer.test.client.AbstractClientTest;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.integration.java.model.JavaModel;
import org.artificer.integration.switchyard.model.SwitchYardModel;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Brett Meyer
 */
public class SwitchYardArtifactTypeDetectorTest extends AbstractClientTest {

	@Test
	public void testSwitchYardIntegration() throws Exception {
	    InputStream stream = getClass().getResourceAsStream("switchyard-quickstart-bean-service.jar"); //$NON-NLS-1$
        ArtificerAtomApiClient client = client();
	    try {
            BaseArtifactType artifact = client.uploadArtifact(stream, "switchyard-quickstart-bean-service.jar");

            assertNotNull(artifact);
            assertEquals(ExtendedDocument.class, artifact.getClass());
            ExtendedDocument extendedDocument = (ExtendedDocument) artifact;
            assertEquals(SwitchYardModel.SwitchYardApplication, extendedDocument.getExtendedType());

            QueryResultSet results = client.buildQuery("/s-ramp/ext/" + SwitchYardModel.SwitchYardXmlDocument
                    + "[@name='orders']").query();
            assertEquals(1, results.size());
            ArtifactSummary summary = results.get(0);
            assertNotNull(summary);

            results = client.buildQuery("/s-ramp/ext/" + JavaModel.TYPE_JAVA_CLASS
                    + "[@name='org.switchyard.quickstarts.bean.service.InventoryServiceBean']").query();
            assertEquals(1, results.size());
            summary = results.get(0);
            assertNotNull(summary);

            results = client.buildQuery("/s-ramp/wsdl/" + BaseArtifactEnum.WSDL_DOCUMENT.value()
                    + "[@name='OrderService.wsdl']").query();
            assertEquals(1, results.size());
            summary = results.get(0);
            assertNotNull(summary);
	    } finally {
	        IOUtils.closeQuietly(stream);
	    }
	}
}
