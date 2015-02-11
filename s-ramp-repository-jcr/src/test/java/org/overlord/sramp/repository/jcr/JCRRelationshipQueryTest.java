/*
 * Copyright 2011 JBoss Inc
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
package org.overlord.sramp.repository.jcr;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.WsdlDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.repository.query.ArtifactSet;
import org.overlord.sramp.repository.query.SrampQuery;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Tests that relationships can be queried.
 * @author eric.wittmann@redhat.com
 */
public class JCRRelationshipQueryTest extends AbstractNoAuditingJCRPersistenceTest {

    /**
     * Tests the query manager + derived relationships.
     * @throws Exception
     */
    @Test
    public void testDerivedRelationshipQueries() throws Exception {
        addWsdlDoc();

        // Get all the element style WSDL message parts
        SrampQuery query = queryManager.createQuery("/s-ramp/wsdl/Part[element]");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(3, artifactSet.size());

        // Get all the element style WSDL message parts that refer to the element with name 'findRequest'
        query = queryManager.createQuery("/s-ramp/wsdl/Part[element[@name = 'find']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        // Get all the messages that have at least one part
        query = queryManager.createQuery("/s-ramp/wsdl/Message[part]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(5, artifactSet.size());

        // Get all operations that have faults
        query = queryManager.createQuery("/s-ramp/wsdl/Operation[fault]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        // Get all operations that have faults named 'foo' (hint - there aren't any)
        query = queryManager.createQuery("/s-ramp/wsdl/Operation[fault[@name = 'foo']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        // Get all faults
        query = queryManager.createQuery("/s-ramp/wsdl/Fault");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        // Get all operations for the port type (sub-artifact-set query)
        query = queryManager.createQuery("/s-ramp/wsdl/PortType[@name = 'SamplePortType']/operation");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        // Get just one operation for the port type (sub-artifact-set query with predicate)
        query = queryManager.createQuery("/s-ramp/wsdl/PortType[@name = 'SamplePortType']/operation[@name = 'findSimple']");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
    }

    /**
     * Tests the query manager + custom/generic relationships
     * @throws Exception
     */
    @Test
    public void testGenericRelationshipQueries() throws Exception {
        XsdDocument xsdDoc = addXsdDoc();
        WsdlDocument wsdlDoc1 = addWsdlDoc();
        WsdlDocument wsdlDoc2 = addWsdlDoc();

        SrampModelUtils.addGenericRelationship(xsdDoc, "importedBy", wsdlDoc1.getUuid());
        SrampModelUtils.addGenericRelationship(xsdDoc, "importedBy", wsdlDoc2.getUuid());
        SrampModelUtils.addGenericRelationship(xsdDoc, "markerRel", null);

        persistenceManager.updateArtifact(xsdDoc, ArtifactType.XsdDocument());

        SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[markerRel]");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[importedBy]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[importedBy[@uuid = ?]]");
        query.setString(wsdlDoc1.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[noSuchRel]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/wsdl/WsdlDocument[importedBy]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());
    }

    @Test
    public void testGenericRelationshipAttributeQueries() throws Exception {
        XsdDocument xsdDoc = addXsdDoc();
        WsdlDocument wsdlDoc1 = addWsdlDoc();
        WsdlDocument wsdlDoc2 = addWsdlDoc();
        WsdlDocument wsdlDoc3 = addWsdlDoc();

        Map<QName, String> otherAttributes = new HashMap<QName, String>();
        otherAttributes.put(QName.valueOf("FooKey"), "FooValue");
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr", wsdlDoc1.getUuid(), otherAttributes, Collections.EMPTY_MAP);
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr", wsdlDoc2.getUuid(), otherAttributes, Collections.EMPTY_MAP);
        Map<QName, String> otherAttributes2 = new HashMap<QName, String>();
        otherAttributes2.put(QName.valueOf("FooKey2"), "FooValue2");
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr2", wsdlDoc3.getUuid(), otherAttributes2, Collections.EMPTY_MAP);

        xsdDoc = (XsdDocument) persistenceManager.updateArtifact(xsdDoc, ArtifactType.XsdDocument());

        // add custom properties only to one of the wsdls
        Property prop = new Property();
        prop.setPropertyName("FooProperty");
        prop.setPropertyValue("FooValue");
        wsdlDoc1.getProperty().add(prop);

        wsdlDoc1 = (WsdlDocument) persistenceManager.updateArtifact(wsdlDoc1, ArtifactType.WsdlDocument());

        SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey')]]");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'InvalidKey')]]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue' and @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue' and @InvalidProperty]]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue' or @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'InvalidValue' or @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'InvalidValue' or @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2[s-ramp:getRelationshipAttribute(., 'FooKey2')]");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
        Assert.assertEquals(wsdlDoc3.getUuid(), artifactSet.iterator().next().getUuid());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2[s-ramp:getRelationshipAttribute(., 'FooKey2') = 'FooValue2']");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
        Assert.assertEquals(wsdlDoc3.getUuid(), artifactSet.iterator().next().getUuid());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2[s-ramp:getRelationshipAttribute(., 'InvalidKey')]");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());
    }

    @Test
    public void testGenericTargetAttributeQueries() throws Exception {
        XsdDocument xsdDoc = addXsdDoc();
        WsdlDocument wsdlDoc1 = addWsdlDoc();
        WsdlDocument wsdlDoc2 = addWsdlDoc();
        WsdlDocument wsdlDoc3 = addWsdlDoc();

        Map<QName, String> otherAttributes = new HashMap<QName, String>();
        otherAttributes.put(QName.valueOf("FooKey"), "FooValue");
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr", wsdlDoc1.getUuid(), Collections.EMPTY_MAP, otherAttributes);
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr", wsdlDoc2.getUuid(), Collections.EMPTY_MAP, otherAttributes);
        Map<QName, String> otherAttributes2 = new HashMap<QName, String>();
        otherAttributes2.put(QName.valueOf("FooKey2"), "FooValue2");
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr2", wsdlDoc3.getUuid(), Collections.EMPTY_MAP, otherAttributes2);

        xsdDoc = (XsdDocument) persistenceManager.updateArtifact(xsdDoc, ArtifactType.XsdDocument());

        // add custom properties only to one of the wsdls
        Property prop = new Property();
        prop.setPropertyName("FooProperty");
        prop.setPropertyValue("FooValue");
        wsdlDoc1.getProperty().add(prop);

        wsdlDoc1 = (WsdlDocument) persistenceManager.updateArtifact(wsdlDoc1, ArtifactType.WsdlDocument());

        SrampQuery query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey')]]");
        ArtifactSet artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey') = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'InvalidKey')]]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey') = 'FooValue' and @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey') = 'FooValue' and @InvalidProperty]]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey') = 'FooValue' or @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(2, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey') = 'InvalidValue' or @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getTargetAttribute(., 'FooKey') = 'InvalidValue' or @FooProperty = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2[s-ramp:getTargetAttribute(., 'FooKey2')]");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
        Assert.assertEquals(wsdlDoc3.getUuid(), artifactSet.iterator().next().getUuid());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2[s-ramp:getTargetAttribute(., 'FooKey2') = 'FooValue2']");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());
        Assert.assertEquals(wsdlDoc3.getUuid(), artifactSet.iterator().next().getUuid());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[@uuid = ?]/relWithAttr2[s-ramp:getTargetAttribute(., 'InvalidKey')]");
        query.setString(xsdDoc.getUuid());
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());
    }

	private WsdlDocument addWsdlDoc() throws Exception {
		String artifactFileName = "jcr-sample.wsdl";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/wsdl/" + artifactFileName);

        try {
	        WsdlDocument wsdlDoc = new WsdlDocument();
	        wsdlDoc.setArtifactType(BaseArtifactEnum.WSDL_DOCUMENT);
	        wsdlDoc.setName(artifactFileName);
	        wsdlDoc.setContentEncoding("application/xml");
	        // Persist the artifact
	        BaseArtifactType artifact = persistenceManager.persistArtifact(wsdlDoc, new ArtifactContent(artifactFileName, contentStream));
	        Assert.assertNotNull(artifact);

            return (WsdlDocument) artifact;
        } finally {
        	IOUtils.closeQuietly(contentStream);
        }
	}

	private XsdDocument addXsdDoc() throws Exception {
		String artifactFileName = "PO.xsd";
        InputStream contentStream = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);

        try {
	        XsdDocument xsdDoc = new XsdDocument();
	        xsdDoc.setArtifactType(BaseArtifactEnum.XSD_DOCUMENT);
	        xsdDoc.setName(artifactFileName);
	        xsdDoc.setContentEncoding("application/xml");
	        // Persist the artifact
	        BaseArtifactType artifact = persistenceManager.persistArtifact(xsdDoc, new ArtifactContent(artifactFileName, contentStream));
	        Assert.assertNotNull(artifact);

            return (XsdDocument) artifact;
        } finally {
        	IOUtils.closeQuietly(contentStream);
        }
	}

}
