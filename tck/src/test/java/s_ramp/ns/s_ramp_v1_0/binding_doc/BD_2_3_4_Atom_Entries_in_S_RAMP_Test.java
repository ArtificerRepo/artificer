package s_ramp.ns.s_ramp_v1_0.binding_doc;

import static org.junit.Assert.fail;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.Assert;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.Entry;
import org.jboss.resteasy.plugins.providers.atom.Feed;
import org.junit.Test;
import org.overlord.sramp.atom.MediaType;
import org.overlord.sramp.atom.SrampAtomUtils;
import org.overlord.sramp.atom.client.ClientRequest;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Artifact;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ObjectFactory;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;

public class BD_2_3_4_Atom_Entries_in_S_RAMP_Test {

    /**
     * An example of an S-RAMP summary (media link) 
     * entry which corresponds to the accountingTypes.xsd resource.
     */
    @Test
    public void summaryArtifactEntry() {
        try {
            System.out.println("1. Marshalling the full entry which is given in Example 2");
            Assert.assertNotNull("Cannot locate file BD_2_3_4_Atom_Entries_in_S_RAMP_2.xml",
                    this.getClass().getClassLoader().getResourceAsStream("BD_2_3_4_Atom_Entries_in_S_RAMP_2.xml"));
    
            String entryXML = new Scanner(this.getClass().getClassLoader().getResourceAsStream("BD_2_3_4_Atom_Entries_in_S_RAMP_2.xml")).useDelimiter("\\Z").next();
            JAXBContext ctx = JAXBContext.newInstance(Entry.class);
            Entry entry = (Entry) ctx.createUnmarshaller().unmarshal(new StringReader(entryXML));
            
            Assert.assertEquals("accountingTypes.xsd", entry.getTitle());
            BaseArtifactType baseArtifact = SrampAtomUtils.unwrapSrampArtifact(entry);
            Assert.assertTrue(baseArtifact instanceof XsdDocument);
            XsdDocument xsdDocument = (XsdDocument) baseArtifact;
            Assert.assertEquals("accountingTypes.xsd", xsdDocument.getName());
            
            System.out.println("2. Now POST this as XsdDocument as new entry into the repo");
            
            JAXBContext jaxbContext=JAXBContext.newInstance("org.oasis_open.docs.s_ramp.ns.s_ramp_v1");
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            marshaller.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "org.oasis_open.docs.s_ramp.ns.s_ramp_v1/xsdmodel.xsd");
           
            ObjectFactory factory = new ObjectFactory();
            Artifact artifact = factory.createArtifact();
            artifact.setXsdDocument(xsdDocument);
            
            StringWriter writer = new StringWriter();
            JAXBElement<Artifact> element = new JAXBElement<Artifact>(new QName("","artifact","s-ramp"),Artifact.class,artifact);
            marshaller.marshal(element,writer);
            String xsdDocumentXml=writer.toString();
            java.lang.System.out.println(xsdDocumentXml);
            
            ClientRequest request = new ClientRequest("http://localhost:8080/s-ramp-server/s-ramp/xsd/XsdDocument");
            request.header("Slug", xsdDocument.getName());
            request.body(MediaType.APPLICATION_XML, xsdDocumentXml);
            
            ClientResponse<Entry> response = request.post(Entry.class);
            Entry responseEntry = response.getEntity();
            
            System.out.println(responseEntry);
            Assert.assertEquals(entry.getTitle(), responseEntry.getTitle());
            
            JAXBContext ctx2 = JAXBContext.newInstance(Entry.class);
            Marshaller marshaller2 = ctx2.createMarshaller();
            marshaller2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller2.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
            marshaller2.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            
            StringWriter writer2 = new StringWriter();
            JAXBElement<Entry> element2 = new JAXBElement<Entry>(new QName("","entry","atom"),Entry.class,responseEntry);

            marshaller2.marshal(element2,writer2);
            String responseEntryXML = writer2.toString();
            java.lang.System.out.println(responseEntryXML);
            
            //TODO if we upload the actual xsd then we may get the importedXsds link (which is
            //a derived relation)
            
            System.out.print("3. Now get the summary");
            ClientRequest request2 = new ClientRequest("http://localhost:8080/s-ramp-server/s-ramp/xsd/XsdDocument");
            ClientResponse<Feed> response2 = request2.get(Feed.class);
            Feed responseFeed = response2.getEntity();
            System.out.println(responseFeed);
            
            StringWriter writer3 = new StringWriter();
            JAXBElement<Feed> element3 = new JAXBElement<Feed>(new QName("","feed","atom"),Feed.class,responseFeed);

            JAXBContext ctx3 = JAXBContext.newInstance(Feed.class);
            Marshaller marshaller3 = ctx3.createMarshaller();
            marshaller3.marshal(element3,writer3);
            String responseFeedXML = writer3.toString();
            java.lang.System.out.println(responseFeedXML);
            
            
        } catch (Exception e) {
            e.printStackTrace();
            fail("No exception should be thrown");
        }
        
        
    }

}
