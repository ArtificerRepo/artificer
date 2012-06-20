package org.guvnor.sramp.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.guvnor.sramp.atom.MediaType;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.s_ramp.xmlns._2010.s_ramp.Artifact;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class XsdDocumentTest extends BaseResourceTest {
    
    @Before
    public void setUp() throws Exception
    {
        //bring up the embedded container with the XsdDocument resource deployed.
       dispatcher.getRegistry().addPerRequestResource(XsdDocumentResource.class);
    }
    
    //Making a client call to the actual XsdDocument implementation running in
    //an embedded container.
    @Test
    public void testPostingPurchaseOrderXSD() throws Exception
    {
       ClientRequest request = new ClientRequest(generateURL("/s-ramp/xsd/XsdDocument"));
       
       //read the XsdDocument from file
       String artifactFileName = "PO.xsd";
       InputStream POXsd = this.getClass().getResourceAsStream("/sample-files/xsd/" + artifactFileName);
       String xmltext = convertStreamToString(POXsd);
       POXsd.close();
       
       request.header("Slug", artifactFileName);
       request.body( MediaType.APPLICATION_XML, xmltext);
       
       ClientResponse<Entry> response = request.post(Entry.class);
       
       Entry entry = response.getEntity();
       Artifact artifact = entry.getAnyOtherJAXBObject(Artifact.class);
       Assert.assertEquals(Long.valueOf(2376), artifact.getXsdDocument().getContentSize());
       
       //Serializing to XML so we can check that it looks good.
       JAXBContext jaxbContext=JAXBContext.newInstance(Entry.class);
       Marshaller marshaller = jaxbContext.createMarshaller();
       marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
       marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
       marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
       
       StringWriter writer = new StringWriter();
       JAXBElement<Entry> element = new JAXBElement<Entry>(new QName("http://www.w3.org/2005/Atom","atom:entry","atom"),Entry.class,entry);
       
       marshaller.marshal(element,writer);
       String actualXml=writer.toString();
       System.out.println(actualXml);
    }
    
    public String convertStreamToString(java.io.InputStream is) {
        try {
            return new java.util.Scanner(is).useDelimiter("\\A").next();
        } catch (java.util.NoSuchElementException e) {
            return "";
        }
    }

    
   

}
