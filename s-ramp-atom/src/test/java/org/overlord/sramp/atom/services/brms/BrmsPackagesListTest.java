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
package org.overlord.sramp.atom.services.brms;

import static junit.framework.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Testing marshalling functionality, making sure UTF-8 is handled correctly.
 *
 * @author <a href="mailto:kurt.stam@redhat.com">Kurt T Stam</a>
 */
public class BrmsPackagesListTest {

    /**
     * Testing going from object to XML using JAXB using a XML Fragment.
     */
    @Test
    public void marshallPackageXml()
    {
        try {
            JAXBContext jaxbContext=JAXBContext.newInstance("org.overlord.sramp.atom.services.brms");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            Packages packages = new Packages();
            Packages.Package _package = new Packages.Package();
            _package.setAuthor("kurt");
            _package.getAssets().add("http://localhost:8080/drools-guvnor/rest/packages/defaultPackage/assets/Test");
            packages.getPackage().add(_package);
            StringWriter writer = new StringWriter();
            JAXBElement<Packages> element = new JAXBElement<Packages>(new QName("","packages",""),Packages.class,packages);

            marshaller.marshal(element,writer);
            String actualXml=writer.toString();
            java.lang.System.out.println(actualXml);
        } catch (Exception e) {
            e.printStackTrace();
            fail("No exception should be thrown");
        }
    }
	/**
	 * Unmarshall an xml fragment.
	 */
	@Test
	public void unmarshallPackageXml()
	{
		try {
			JAXBContext jaxbContext=JAXBContext.newInstance("org.overlord.sramp.atom.services.brms");
			Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
			Assert.assertNotNull("Cannot locate file packages.xml",
			        this.getClass().getResourceAsStream("packages.xml"));

			String packagesXML = new Scanner(this.getClass().getResourceAsStream("packages.xml")).useDelimiter("\\Z").next();
			StringReader reader = new StringReader(packagesXML);
			JAXBElement<Packages> element = unMarshaller.unmarshal(new StreamSource(reader),Packages.class);
			Packages packages = element.getValue();
			Assert.assertEquals(2, packages.getPackage().size());
		} catch (JAXBException jaxbe) {
		    jaxbe.printStackTrace();
			fail("No exception should be thrown");
		}
	}
	
    /**
     * Testing going from object to XML using JAXB using a XML Fragment.
     */
    @Test
    public void marshallAssetsXml()
    {
        try {
            JAXBContext jaxbContext=JAXBContext.newInstance("org.overlord.sramp.atom.services.brms");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");

            Assets assets = new Assets();
            Assets.Asset asset = new Assets.Asset();
            asset.setTitle("myAsset");
           assets.getAsset().add(asset);
            StringWriter writer = new StringWriter();
            JAXBElement<Assets> element = new JAXBElement<Assets>(new QName("","assets",""),Assets.class,assets);

            marshaller.marshal(element,writer);
            String actualXml=writer.toString();
            java.lang.System.out.println(actualXml);
        } catch (Exception e) {
            e.printStackTrace();
            fail("No exception should be thrown");
        }
    }
    /**
     * Unmarshall an xml fragment.
     */
    @Test
    public void unmarshallAssetsXml()
    {
        try {
            JAXBContext jaxbContext=JAXBContext.newInstance("org.overlord.sramp.atom.services.brms");
            Unmarshaller unMarshaller = jaxbContext.createUnmarshaller();
            Assert.assertNotNull("Cannot locate file packages.xml",
                    this.getClass().getResourceAsStream("assets.xml"));

            String assetsXML = new Scanner(this.getClass().getResourceAsStream("assets.xml")).useDelimiter("\\Z").next();
            StringReader reader = new StringReader(assetsXML);
            JAXBElement<Assets> element = unMarshaller.unmarshal(new StreamSource(reader),Assets.class);
            Assets assets = element.getValue();
            Assert.assertEquals(13, assets.getAsset().size());
        } catch (JAXBException jaxbe) {
            jaxbe.printStackTrace();
            fail("No exception should be thrown");
        }
    }


}
