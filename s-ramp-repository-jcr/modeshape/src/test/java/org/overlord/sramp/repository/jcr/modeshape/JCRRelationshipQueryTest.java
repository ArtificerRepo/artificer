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
package org.overlord.sramp.repository.jcr.modeshape;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.*;
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

        // TODO: The spec is incorrect here!  ElementTarget is for the SOA Model's Element.  Instead,
        // we need an *ElementDeclaration* target!  Until that's fixed, skip.
        // See WsdlDocumentArtifactBuilder#processParts
//        // Get all the element style WSDL message parts
//        SrampQuery query = queryManager.createQuery("/s-ramp/wsdl/Part[element]");
//        ArtifactSet artifactSet = query.executeQuery();
//        Assert.assertNotNull(artifactSet);
//        Assert.assertEquals(3, artifactSet.size());
//
//        // Get all the element style WSDL message parts that refer to the element with name 'findRequest'
//        query = queryManager.createQuery("/s-ramp/wsdl/Part[element[@name = 'find']]");
//        artifactSet = query.executeQuery();
//        Assert.assertNotNull(artifactSet);
//        Assert.assertEquals(1, artifactSet.size());

        // Get all the messages that have at least one part
        SrampQuery query = queryManager.createQuery("/s-ramp/wsdl/Message[part]");
        ArtifactSet artifactSet = query.executeQuery();
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
    	WsdlDocument wsdlDoc = addWsdlDoc();

    	SrampModelUtils.addGenericRelationship(xsdDoc, "importedBy", wsdlDoc.getUuid());
    	SrampModelUtils.addGenericRelationship(xsdDoc, "markerRel", null);

        Map<QName, String> otherAttributes = new HashMap<QName, String>();
        otherAttributes.put(QName.valueOf("FooKey"), "FooValue");
        Map<QName, String> otherAttributes2 = new HashMap<QName, String>();
        otherAttributes2.put(QName.valueOf("FooKey2"), "FooValue2");
        SrampModelUtils.addGenericRelationship(xsdDoc, "relWithAttr", wsdlDoc.getUuid(), otherAttributes, otherAttributes2);
        Property prop = new Property();
        prop.setPropertyName("FooProperty");
        prop.setPropertyValue("FooValue");
        xsdDoc.getProperty().add(prop);

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
        query.setString(wsdlDoc.getUuid());
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

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey')]]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue']]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(1, artifactSet.size());

        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'InvalidKey')]]");
        artifactSet = query.executeQuery();
        Assert.assertNotNull(artifactSet);
        Assert.assertEquals(0, artifactSet.size());

        // TODO: These won't be supported until after the query visitor uses a builder pattern.  See SRAMP-627
//        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue' and @FooProperty = 'FooValue']]");
//        artifactSet = query.executeQuery();
//        Assert.assertNotNull(artifactSet);
//        Assert.assertEquals(1, artifactSet.size());
//
//        query = queryManager.createQuery("/s-ramp/xsd/XsdDocument[relWithAttr[s-ramp:getRelationshipAttribute(., 'FooKey') = 'FooValue' and @InvalidProperty]]");
//        artifactSet = query.executeQuery();
//        Assert.assertNotNull(artifactSet);
//        Assert.assertEquals(0, artifactSet.size());

        // TODO: After SRAMP-625, test getTargetAttribute using
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
