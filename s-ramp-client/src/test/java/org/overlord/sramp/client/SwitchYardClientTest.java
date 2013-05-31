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
import org.junit.Assert;
import org.junit.Test;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.jar.JarToSrampArchive;
import org.overlord.sramp.atom.err.SrampAtomException;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.integration.switchyard.jar.SwitchYardAppToSrampArchive;

/**
 * Unit test for uploading SwitchYard applications.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardClientTest extends AbstractNoAuditingClientTest {

	@Test
	public void testUploadArtifact() throws Exception {
        SrampAtomApiClient client = new SrampAtomApiClient(generateURL("/s-ramp"));

        // Upload the artifacts jar
        InputStream artifactsIS = this.getClass().getResourceAsStream("/sample-files/switchyard/artifacts.jar");
		SwitchYardAppToSrampArchive sy2archive = null;
        SrampArchive archive = null;
		try {
		    sy2archive = new SwitchYardAppToSrampArchive(artifactsIS);
		    archive = sy2archive.createSrampArchive();
		    client.uploadBatch(archive);
		} finally {
			IOUtils.closeQuietly(artifactsIS);
			JarToSrampArchive.closeQuietly(sy2archive);
			SrampArchive.closeQuietly(archive);
		}

		doArtifactsJarAssertions(client);

		// Upload the order consumer jar
        InputStream orderConsumerIS = this.getClass().getResourceAsStream("/sample-files/switchyard/order-consumer.jar");
        sy2archive = null;
        archive = null;
        try {
            sy2archive = new SwitchYardAppToSrampArchive(orderConsumerIS);
            archive = sy2archive.createSrampArchive();
            client.uploadBatch(archive);
        } finally {
            IOUtils.closeQuietly(artifactsIS);
            JarToSrampArchive.closeQuietly(sy2archive);
            SrampArchive.closeQuietly(archive);
        }

        doOrderConsumerAssertions(client);

        // Upload the order service jar
        InputStream orderServiceIS = this.getClass().getResourceAsStream("/sample-files/switchyard/order-service.jar");
        sy2archive = null;
        archive = null;
        try {
            sy2archive = new SwitchYardAppToSrampArchive(orderServiceIS);
            archive = sy2archive.createSrampArchive();
            client.uploadBatch(archive);
        } finally {
            IOUtils.closeQuietly(artifactsIS);
            JarToSrampArchive.closeQuietly(sy2archive);
            SrampArchive.closeQuietly(archive);
        }

        doOrderServiceAssertions(client);
	}

    /**
     * Do some assertions to make sure that the content we expected to be extracted
     * from the switchyard app jar really was.
     * @param client
     * @throws SrampAtomException
     * @throws SrampClientException
     */
    private void doArtifactsJarAssertions(SrampAtomApiClient client) throws SrampClientException, SrampAtomException {
        QueryResultSet result = client.buildQuery("/s-ramp/wsdl/WsdlDocument[@name = ?]").parameter("OrderService.wsdl").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/wsdl/WsdlDocument[@name = ?]").parameter("OrderService.wsdl").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/xsd/XsdDocument[@name = ?]").parameter("orderTypes.xsd").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/JavaClass[@packageName = ? and @className = ?]")
                .parameter("org.switchyard.quickstarts.demo.multiapp")
                .parameter("Order")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/JavaInterface[@packageName = ? and @className = ?]")
                .parameter("org.switchyard.quickstarts.demo.multiapp")
                .parameter("InventoryService")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/xsd/ElementDeclaration[@namespace = ? and @ncName = ?]")
                .parameter("urn:switchyard-quickstart-demo:multiapp:1.0")
                .parameter("submitOrder")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/wsdl/Part[@name = ? and element[@namespace = ? and @ncName = ?]]")
                .parameter("parameters")
                .parameter("urn:switchyard-quickstart-demo:multiapp:1.0")
                .parameter("submitOrder")
                .query();
        Assert.assertEquals(1, result.size());
    }

    /**
     * Do some assertions to make sure the content we expect has been added to the
     * repository and all appropriate relationships have been created (by the SwitchYard
     * deriver/linker).
     * @param client
     * @throws SrampAtomException
     * @throws SrampClientException
     */
    private void doOrderConsumerAssertions(SrampAtomApiClient client) throws SrampClientException, SrampAtomException {
        QueryResultSet result = client.buildQuery("/s-ramp/ext/SwitchYardXmlDocument").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardService").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[references]").query();
        Assert.assertEquals(1, result.size());
    }

    /**
     * Do some assertions to make sure the content we expect has been added to the
     * repository and all appropriate relationships have been created (by the SwitchYard
     * deriver/linker).
     * @param client
     * @throws SrampAtomException
     * @throws SrampClientException
     */
    private void doOrderServiceAssertions(SrampAtomApiClient client) throws SrampClientException, SrampAtomException {
        QueryResultSet result = client.buildQuery("/s-ramp/ext/SwitchYardXmlDocument").query();
        Assert.assertEquals(2, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardXmlDocument[@name = ?]").parameter("orders").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer").query();
        Assert.assertEquals(2, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsFrom[@name = ?]]")
                .parameter("org.switchyard.quickstarts.demo.multiapp.OrderAck")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsTo[@ncName = ?]]")
                .parameter("submitOrderResponse")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsFrom[@ncName = ?]]")
                .parameter("submitOrder")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsTo[@name = ?]]")
                .parameter("org.switchyard.quickstarts.demo.multiapp.Order")
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[implementedBy[@name = ?]]")
                .parameter("org.switchyard.quickstarts.demo.multiapp.service.Transformers")
                .query();
        Assert.assertEquals(2, result.size());

        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent").query();
        Assert.assertEquals(3, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[@name = ?]").parameter("OrderService").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[@name = ?]/implementedBy")
                .parameter("OrderService").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent/implementedBy").query();
        Assert.assertEquals(2, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[implementedBy[@name = ?]]")
                .parameter("org.switchyard.quickstarts.demo.multiapp.service.InventoryServiceBean").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[@name = ?]/references")
                .parameter("OrderService").query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer/transformsFrom").query();
        Assert.assertEquals(2, result.size());
    }
}
