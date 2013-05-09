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
package org.overlord.sramp.client;

import static org.overlord.sramp.common.test.resteasy.TestPortProvider.generateURL;

import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.providers.HttpResponseProvider;
import org.overlord.sramp.atom.providers.SrampAtomExceptionProvider;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampConstants;
import org.overlord.sramp.common.test.resteasy.BaseResourceTest;
import org.overlord.sramp.repository.PersistenceFactory;
import org.overlord.sramp.repository.jcr.modeshape.AbstractNoAuditingJCRPersistenceTest;
import org.overlord.sramp.repository.jcr.modeshape.JCRRepositoryCleaner;
import org.overlord.sramp.server.atom.services.ArtifactResource;
import org.overlord.sramp.server.atom.services.AuditResource;
import org.overlord.sramp.server.atom.services.BatchResource;
import org.overlord.sramp.server.atom.services.FeedResource;
import org.overlord.sramp.server.atom.services.OntologyResource;
import org.overlord.sramp.server.atom.services.QueryResource;

/**
 * Base class for client tests.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractClientTest extends BaseResourceTest {

	/**
	 * @throws Exception
	 */
	public static void registerServices() throws Exception {
        // use the in-memory config for unit tests
        System.setProperty("sramp.modeshape.config.url", "classpath://" + AbstractNoAuditingJCRPersistenceTest.class.getName()
                + "/META-INF/modeshape-configs/junit-sramp-config.json");
		deployment.getProviderFactory().registerProvider(SrampAtomExceptionProvider.class);
		deployment.getProviderFactory().registerProvider(HttpResponseProvider.class);
		dispatcher.getRegistry().addPerRequestResource(ArtifactResource.class);
		dispatcher.getRegistry().addPerRequestResource(FeedResource.class);
		dispatcher.getRegistry().addPerRequestResource(BatchResource.class);
        dispatcher.getRegistry().addPerRequestResource(QueryResource.class);
        dispatcher.getRegistry().addPerRequestResource(OntologyResource.class);
        dispatcher.getRegistry().addPerRequestResource(AuditResource.class);
	}

    @Before
    public void cleanRepository() {
        new JCRRepositoryCleaner().clean();
    }

	@AfterClass
	public static void cleanup() {
		PersistenceFactory.newInstance().shutdown();
        System.clearProperty(SrampConstants.SRAMP_CONFIG_AUDITING);
	}

    /**
     * Adds an XML document.
     * @throws Exception
     */
    protected BaseArtifactType addXmlDoc() throws Exception {
        String artifactFileName = "PO.xml";
        InputStream is = this.getClass().getResourceAsStream("/sample-files/core/" + artifactFileName);
        try {
            SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));
            return client.uploadArtifact(ArtifactType.XmlDocument(), is, artifactFileName);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

}
