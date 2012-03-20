/*
 * Copyright 2001-2009 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.s_ramp.xmlns._2010.s_ramp;

import static junit.framework.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Testing marshalling functionality, making sure UTF-8 is handled correctly.
 * 
 * @author <a href="mailto:kurt.stam@redhat.com">Kurt T Stam</a>
 */
public class XsdDocumentTest {

	/**
	 * Testing going from object to XML using JAXB using a XML Fragment.
	 */
	@Test 
	public void marshall()
	{
		try {
			JAXBContext jaxbContext=JAXBContext.newInstance("org.s_ramp.xmlns._2010.s_ramp");
			Marshaller marshaller = jaxbContext.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
			marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "http://s-ramp.org/xmlns/2010/s-ramp/xsdmodel.xsd");
			ObjectFactory factory = new ObjectFactory();
			
			Artifact artifact = factory.createArtifact();
			XsdDocument xsdDocument = new XsdDocument();
			xsdDocument.setName("accountingTypes.xsd");
			xsdDocument.setDescription("accountingTypes.xsd");
			xsdDocument.setCreatedBy("Bellwood");
			xsdDocument.setVersion("1.0");
			xsdDocument.setUuid("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6a");
			XMLGregorianCalendar createdTS = DatatypeFactory.newInstance().newXMLGregorianCalendar("2009-05-26T13:13:55.013+02:00");
			xsdDocument.setCreatedTimestamp(createdTS);
			XMLGregorianCalendar lastModifiedTS = DatatypeFactory.newInstance().newXMLGregorianCalendar("2009-06-26T13:13:55.013+02:00");
			xsdDocument.setLastModifiedTimestamp(lastModifiedTS);
			xsdDocument.setLastModifiedBy("Pospisil");
			xsdDocument.setContentType("application/xml");
			xsdDocument.setContentEncoding("UTF-8");
			xsdDocument.setContentSize(4096l);
			
			xsdDocument.getClassifiedBy().add("http://example.org/ontologies/accounting.owl/accounts");
            
            Target importedXsdTarget = new Target();
                importedXsdTarget.setValue("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6b");
                importedXsdTarget.getOtherAttributes().put(
                        new QName("xlink:href"), 
                        "http://example.org/s-ramp/xsd/XsdDocument/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6b");
            xsdDocument.getImportedXsds().add(importedXsdTarget);
           
            Relationship relationship = new Relationship();
                relationship.setRelationshipType("similarXsds");
                Target relationshipTarget = new Target();
                relationshipTarget.setValue("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6b");
                relationshipTarget.getOtherAttributes().put(
                        new QName("xlink:href"), 
                        "http://example.org/s-ramp/xsd/XsdDocument/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6b");
                relationship.getRelationshipTarget().add(relationshipTarget);
                
                Target relationshipTarget2 = new Target();
                relationshipTarget2.setValue("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6c");
                relationshipTarget2.getOtherAttributes().put(
                        new QName("xlink:href"), 
                        "http://example.org/s-ramp/xsd/XsdDocument/aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaa6c");
                relationship.getRelationshipTarget().add(relationshipTarget2);
            xsdDocument.getRelationship().add(relationship);
            
            Property property = new Property();
                property.setPropertyName("myPropertyName");
                property.setPropertyValue("myPropertyValue");
            xsdDocument.getProperty().add(property);
            
            artifact.setXsdDocument(xsdDocument);
			
			StringWriter writer = new StringWriter();
			JAXBElement<Artifact> element = new JAXBElement<Artifact>(new QName("","artifact","s-ramp"),Artifact.class,artifact);
			
			marshaller.marshal(element,writer);
			String actualXml=writer.toString();
			java.lang.System.out.println(actualXml);
			
			Assert.assertNotNull("Cannot locate file XsdDocument-entry.xml", 
			        this.getClass().getResourceAsStream("XsdDocument-entry.xml"));
			
			String expectedXML = new Scanner(this.getClass().getResourceAsStream("XsdDocument-entry.xml")).useDelimiter("\\Z").next();
			java.lang.System.out.println(expectedXML);
			Diff diff = new Diff(expectedXML, actualXml);
			boolean isSimilar = diff.similar();
			XMLAssert.assertTrue(isSimilar);
			
		} catch (Exception e) {
			e.printStackTrace();
			fail("No exception should be thrown");
		}
	}
	/**
	 * Unmarshall an xml fragment.
	 */
	@Test
	public void unmarshall()
	{
		try {
			JAXBContext jaxbContext=JAXBContext.newInstance("org.s_ramp.xmlns._2010.s_ramp");
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			Assert.assertNotNull("Cannot locate file XsdDocument-entry.xml", 
			        this.getClass().getResourceAsStream("XsdDocument-entry.xml"));
			
			String expectedXML = new Scanner(this.getClass().getResourceAsStream("XsdDocument-entry.xml")).useDelimiter("\\Z").next();
			StringReader reader = new StringReader(expectedXML);
			JAXBElement<Artifact> element = unMarshaller.unmarshal(new StreamSource(reader),Artifact.class);
			Artifact artifact = element.getValue();
			Assert.assertEquals("accountingTypes.xsd", artifact.getXsdDocument().getName());
		} catch (JAXBException jaxbe) {
		    jaxbe.printStackTrace();
			fail("No exception should be thrown");
		}
	}
	
	/**
     * Unmarshall an xml fragment.
     */
    @Test
    public void unmarshall2()
    {
        try {
            JAXBContext jaxbContext=JAXBContext.newInstance("org.s_ramp.xmlns._2010.s_ramp");
            Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
            Assert.assertNotNull("Cannot locate file XsdDocument-entry2.xml", 
                    this.getClass().getResourceAsStream("XsdDocument-entry2.xml"));
            
            String expectedXML = new Scanner(this.getClass().getResourceAsStream("XsdDocument-entry2.xml")).useDelimiter("\\Z").next();
            StringReader reader = new StringReader(expectedXML);
            JAXBElement<Artifact> element = unMarshaller.unmarshal(new StreamSource(reader),Artifact.class);
            Artifact artifact = element.getValue();
            Assert.assertEquals("accountingTypes.xsd", artifact.getXsdDocument().getName());
        } catch (JAXBException jaxbe) {
            jaxbe.printStackTrace();
            fail("No exception should be thrown");
        }
    }

}
