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
package org.artificer.integration.switchyard.artifactbuilder;

import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.artificer.common.ArtifactContent;
import org.artificer.common.ArtifactType;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.integration.artifactbuilder.MockRelationshipContext;
import org.artificer.integration.switchyard.model.SwitchYardModel;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * JUnit test for {@link SwitchYardXmlArtifactBuilder}.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardXmlDeriverTest {

    /**
     * Test method for {@link SwitchYardXmlArtifactBuilder#derive(java.util.Collection, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)}.
     * @throws IOException
     */
    @Test
    public void testSwitchyardDeriver() throws Exception {
        SwitchYardXmlArtifactBuilder builder = new SwitchYardXmlArtifactBuilder();
        ExtendedDocument artifact = new ExtendedDocument();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setName("switchyard.xml"); //$NON-NLS-1$
        artifact.setExtendedType(SwitchYardModel.SwitchYardXmlDocument);
        InputStream is = getClass().getResourceAsStream("switchyard.xml"); //$NON-NLS-1$
        
        // Derive
        Collection<BaseArtifactType> derivedArtifacts = builder.buildArtifacts(artifact, new ArtifactContent("switchyard.xml", is))
                .getDerivedArtifacts();
        builder.buildRelationships(new MockRelationshipContext());

        // Asserts
        Assert.assertNotNull(derivedArtifacts);
        Assert.assertEquals(12, derivedArtifacts.size());
        Assert.assertEquals("orders", artifact.getName()); //$NON-NLS-1$
        Assert.assertEquals("urn:switchyard-quickstart:bean-service:0.1.0", ArtificerModelUtils.getCustomProperty(artifact, "targetNamespace")); //$NON-NLS-1$ //$NON-NLS-2$


        BaseArtifactType orderService = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardServiceType, "OrderService"); //$NON-NLS-1$
        Assert.assertNotNull(orderService);
        Assert.assertEquals("OrderService", orderService.getName()); //$NON-NLS-1$
        Relationship relationship = ArtificerModelUtils.getGenericRelationship(orderService, SwitchYardModel.REL_IMPLEMENTS);
        Assert.assertNotNull(relationship);
//        Assert.assertEquals("wsdl:wsdl/OrderService.wsdl#wsdl.porttype(OrderService)", relationship.getOtherAttributes().get(SwitchYardXmlArtifactBuilder.UNRESOLVED_REF)); //$NON-NLS-1$


        BaseArtifactType inventoryServiceComponent = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentType, "InventoryService"); //$NON-NLS-1$
        Assert.assertNotNull(inventoryServiceComponent);
        Assert.assertEquals("managedTransaction.Global", ArtificerModelUtils.getCustomProperty(inventoryServiceComponent, "requires")); //$NON-NLS-1$ //$NON-NLS-2$
        relationship = ArtificerModelUtils.getGenericRelationship(inventoryServiceComponent, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(inventoryServiceComponent, SwitchYardModel.REL_REFERENCES);
        Assert.assertNull(relationship);


        BaseArtifactType orderServiceComponent = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentType, "OrderService"); //$NON-NLS-1$
        Assert.assertNotNull(orderServiceComponent);
        relationship = ArtificerModelUtils.getGenericRelationship(orderServiceComponent, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(orderServiceComponent, SwitchYardModel.REL_REFERENCES);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(orderService, SwitchYardModel.REL_PROMOTES);
        Assert.assertNotNull(relationship);
        Assert.assertNotNull(relationship.getRelationshipTarget());
        Assert.assertEquals(orderServiceComponent.getUuid(), relationship.getRelationshipTarget().iterator().next().getValue());


        BaseArtifactType camelServiceComponent = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentType, "CamelService"); //$NON-NLS-1$
        Assert.assertNotNull(camelServiceComponent);
        Assert.assertEquals("noManagedTransaction", ArtificerModelUtils.getCustomProperty(camelServiceComponent, "requires")); //$NON-NLS-1$ //$NON-NLS-2$
        relationship = ArtificerModelUtils.getGenericRelationship(camelServiceComponent, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        Assert.assertTrue(relationship.getRelationshipTarget().isEmpty());
        relationship = ArtificerModelUtils.getGenericRelationship(camelServiceComponent, SwitchYardModel.REL_REFERENCES);
        Assert.assertNull(relationship);

        BaseArtifactType inventoryServiceComponentSvc = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentServiceType, "InventoryService"); //$NON-NLS-1$
        Assert.assertNotNull(camelServiceComponent);
        relationship = ArtificerModelUtils.getGenericRelationship(inventoryServiceComponentSvc, SwitchYardModel.REL_IMPLEMENTS);
        Assert.assertNotNull(relationship);
//        Assert.assertEquals("java:org.switchyard.quickstarts.bean.service.InventoryService", relationship.getOtherAttributes().get(SwitchYardXmlArtifactBuilder.UNRESOLVED_REF)); //$NON-NLS-1$
        Assert.assertEquals("propagatesTransaction", ArtificerModelUtils.getCustomProperty(inventoryServiceComponentSvc, "requires")); //$NON-NLS-1$ //$NON-NLS-2$


        BaseArtifactType transformJava = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardTransformerType, "OrderAck->submitOrderResponse"); //$NON-NLS-1$
        Assert.assertNotNull(transformJava);
        Assert.assertEquals("java", ArtificerModelUtils.getCustomProperty(transformJava, SwitchYardModel.PROP_TRANSFORMER_TYPE)); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_FROM);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_TO);
        Assert.assertNotNull(relationship);


        BaseArtifactType transformXslt = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardTransformerType, "CDM->S1"); //$NON-NLS-1$
        Assert.assertNotNull(transformXslt);
        Assert.assertEquals("xslt", ArtificerModelUtils.getCustomProperty(transformXslt, SwitchYardModel.PROP_TRANSFORMER_TYPE)); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(transformXslt, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_FROM);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_TO);
        Assert.assertNotNull(relationship);


        BaseArtifactType validateJava = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardValidatorType, "java:org.switchyard.quickstarts.bean.service.Order"); //$NON-NLS-1$
        Assert.assertNotNull(validateJava);
        Assert.assertEquals("java", ArtificerModelUtils.getCustomProperty(validateJava, SwitchYardModel.PROP_VALIDATE_TYPE)); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(validateJava, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        relationship = ArtificerModelUtils.getGenericRelationship(validateJava, SwitchYardModel.REL_VALIDATES);
        Assert.assertNotNull(relationship);

        inventoryServiceComponent = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentType, "InventoryService"); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(inventoryServiceComponent, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(inventoryServiceComponent, SwitchYardModel.REL_REFERENCES);
        Assert.assertNull(relationship);


        orderServiceComponent = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentType, "OrderService"); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(orderServiceComponent, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(orderServiceComponent, SwitchYardModel.REL_REFERENCES);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());


        camelServiceComponent = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardComponentType, "CamelService"); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(camelServiceComponent, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        // TODO Note - looking up a camel xml is not yet implemented
//        Assert.assertEquals(1, relationship.getRelationshipTarget().size());


        transformJava = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardTransformerType, "OrderAck->submitOrderResponse"); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_FROM);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_TO);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());


        transformXslt = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardTransformerType, "CDM->S1"); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(transformXslt, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        // TODO: Note - XSLT transforms not yet implemented (see SwitchYardLinker)
//        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_FROM);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(transformJava, SwitchYardModel.REL_TRANSFORMS_TO);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());

        validateJava = getDerivedArtifact(derivedArtifacts, SwitchYardModel.SwitchYardValidatorType, "java:org.switchyard.quickstarts.bean.service.Order"); //$NON-NLS-1$
        relationship = ArtificerModelUtils.getGenericRelationship(validateJava, SwitchYardModel.REL_IMPLEMENTED_BY);
        Assert.assertNotNull(relationship);
        // TODO: Note - CDI beans not yet implemented (see SwitchYardLinker)
//        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
        relationship = ArtificerModelUtils.getGenericRelationship(validateJava, SwitchYardModel.REL_VALIDATES);
        Assert.assertNotNull(relationship);
        Assert.assertEquals(1, relationship.getRelationshipTarget().size());
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
