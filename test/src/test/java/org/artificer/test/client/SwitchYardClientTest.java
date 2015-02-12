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
package org.artificer.test.client;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.artificer.atom.err.ArtificerAtomException;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ArtificerClientException;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.integration.switchyard.model.SwitchYardModel;

import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Unit test for uploading SwitchYard applications.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardClientTest extends AbstractClientTest {

    @Test
    public void testUploadArtifact() throws Exception {
        ArtificerAtomApiClient client = client();

        // Upload the artifacts jar
        // This requires proper maven profile to be active as maven handles generation of this jar file
        InputStream artifactsIS = new FileInputStream("target/sample-files/switchyard/artifacts.jar");//$NON-NLS-1$
        try {
            ArtifactType artifactType = ArtifactType.valueOf(SwitchYardModel.SwitchYardApplication, true);
            client.uploadArtifact(artifactType, artifactsIS, "artifacts.jar");
        } finally {
            IOUtils.closeQuietly(artifactsIS);
        }

		doArtifactsJarAssertions(client);

        // Upload the order consumer jar
        // This requires proper maven profile to be active as maven handles generation of this jar file
        InputStream orderConsumerIS = new FileInputStream("target/sample-files/switchyard/order-consumer.jar"); //$NON-NLS-1$
        try {
            client.uploadArtifact(orderConsumerIS, "order-consumer.jar");
        } finally {
            IOUtils.closeQuietly(artifactsIS);
        }

        doOrderConsumerAssertions(client);

        // Upload the order service jar
        // This requires proper maven profile to be active as maven handles generation of this jar file
        InputStream orderServiceIS = new FileInputStream("target/sample-files/switchyard/order-service.jar"); //$NON-NLS-1$
        try {
            client.uploadArtifact(orderServiceIS, "order-service.jar");
        } finally {
            IOUtils.closeQuietly(artifactsIS);
        }

        doOrderServiceAssertions(client);
    }

    /**
     * Do some assertions to make sure that the content we expected to be extracted
     * from the switchyard app jar really was.
     * @param client
     * @throws org.artificer.atom.err.ArtificerAtomException
     * @throws org.artificer.client.ArtificerClientException
     */
    private void doArtifactsJarAssertions(ArtificerAtomApiClient client) throws ArtificerClientException, ArtificerAtomException {
        QueryResultSet result = client.buildQuery("/s-ramp/wsdl/WsdlDocument[@name = ?]").parameter("OrderService.wsdl").query(); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/wsdl/WsdlDocument[@name = ?]").parameter("OrderService.wsdl").query(); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/xsd/XsdDocument[@name = ?]").parameter("orderTypes.xsd").query(); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/JavaClass[@packageName = ? and @className = ?]") //$NON-NLS-1$
                .parameter("org.switchyard.quickstarts.demo.multiapp") //$NON-NLS-1$
                .parameter("Order") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/JavaInterface[@packageName = ? and @className = ?]") //$NON-NLS-1$
                .parameter("org.switchyard.quickstarts.demo.multiapp") //$NON-NLS-1$
                .parameter("InventoryService") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/xsd/ElementDeclaration[@namespace = ? and @ncName = ?]") //$NON-NLS-1$
                .parameter("urn:switchyard-quickstart-demo:multiapp:1.0") //$NON-NLS-1$
                .parameter("submitOrder") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
//        result = client.buildQuery("/s-ramp/wsdl/Part[@name = ? and element[@namespace = ? and @ncName = ?]]") //$NON-NLS-1$
//                .parameter("parameters") //$NON-NLS-1$
//                .parameter("urn:switchyard-quickstart-demo:multiapp:1.0") //$NON-NLS-1$
//                .parameter("submitOrder") //$NON-NLS-1$
//                .query();
//        Assert.assertEquals(1, result.size());
    }

    /**
     * Do some assertions to make sure the content we expect has been added to the
     * repository and all appropriate relationships have been created (by the SwitchYard
     * deriver/linker).
     * @param client
     * @throws org.artificer.atom.err.ArtificerAtomException
     * @throws org.artificer.client.ArtificerClientException
     */
    private void doOrderConsumerAssertions(ArtificerAtomApiClient client) throws ArtificerClientException, ArtificerAtomException {
        QueryResultSet result = client.buildQuery("/s-ramp/ext/SwitchYardXmlDocument").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardService").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[references]").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
    }

    /**
     * Do some assertions to make sure the content we expect has been added to the
     * repository and all appropriate relationships have been created (by the SwitchYard
     * deriver/linker).
     * @param client
     * @throws org.artificer.atom.err.ArtificerAtomException
     * @throws org.artificer.client.ArtificerClientException
     */
    private void doOrderServiceAssertions(ArtificerAtomApiClient client) throws ArtificerClientException, ArtificerAtomException {
        QueryResultSet result = client.buildQuery("/s-ramp/ext/SwitchYardXmlDocument").query(); //$NON-NLS-1$
        Assert.assertEquals(2, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardXmlDocument[@name = ?]").parameter("orders").query(); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer").query(); //$NON-NLS-1$
        Assert.assertEquals(2, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsFrom[@name = ?]]") //$NON-NLS-1$
                .parameter("org.switchyard.quickstarts.demo.multiapp.OrderAck") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsTo[@ncName = ?]]") //$NON-NLS-1$
                .parameter("submitOrderResponse") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsFrom[@ncName = ?]]") //$NON-NLS-1$
                .parameter("submitOrder") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[transformsTo[@name = ?]]") //$NON-NLS-1$
                .parameter("org.switchyard.quickstarts.demo.multiapp.Order") //$NON-NLS-1$
                .query();
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer[implementedBy[@name = ?]]") //$NON-NLS-1$
                .parameter("org.switchyard.quickstarts.demo.multiapp.service.Transformers") //$NON-NLS-1$
                .query();
        Assert.assertEquals(2, result.size());

        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent").query(); //$NON-NLS-1$
        Assert.assertEquals(3, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[@name = ?]").parameter("OrderService").query(); //$NON-NLS-1$ //$NON-NLS-2$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[@name = ?]/implementedBy") //$NON-NLS-1$
                .parameter("OrderService").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent/implementedBy").query(); //$NON-NLS-1$
        Assert.assertEquals(2, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[implementedBy[@name = ?]]") //$NON-NLS-1$
                .parameter("org.switchyard.quickstarts.demo.multiapp.service.InventoryServiceBean").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardComponent[@name = ?]/references") //$NON-NLS-1$
                .parameter("OrderService").query(); //$NON-NLS-1$
        Assert.assertEquals(1, result.size());
        result = client.buildQuery("/s-ramp/ext/SwitchYardTransformer/transformsFrom").query(); //$NON-NLS-1$
        Assert.assertEquals(2, result.size());
    }
}