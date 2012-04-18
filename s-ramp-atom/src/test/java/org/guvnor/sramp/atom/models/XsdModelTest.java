package org.guvnor.sramp.atom.models;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import org.jboss.resteasy.plugins.providers.atom.app.AppService;
import org.jboss.resteasy.plugins.providers.atom.app.AppWorkspace;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class XsdModelTest {
    /**
     * Taken from the S-RAMP Atom Binding Document Appendix E.
     */
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
    "<service xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns=\"http://www.w3.org/2007/app\">\n" + 
    "  <workspace>\n" + 
    "    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">XSD Model</atom:title>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd/XsdType\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">XSD Types</atom:title>\n" + 
    "      <accept></accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"XsdType\"\n" + 
    "                  label=\"XSD Type\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd/ElementDeclaration\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Element Declarations\n" + 
    "      </atom:title>\n" + 
    "      <accept></accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ElementDeclaration\"\n" + 
    "                  label=\"Element Declaration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd/AttributeDeclaration\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Attribute Declarations\n" + 
    "      </atom:title>\n" + 
    "      <accept></accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"AttributeDeclaration\"\n" + 
    "                  label=\"Attribute Declaration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd/ComplexTypeDeclaration\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Complex Type Declarations\n" + 
    "      </atom:title>\n" + 
    "      <accept></accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ComplexTypeDeclaration\"\n" + 
    "                  label=\"Complex Type Declaration\"\n" + 
    "                  xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd/SimpleTypeDeclaration\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Simple Type Declarations\n" + 
    "      </atom:title>\n" + 
    "      <accept></accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"SimpleTypeDeclaration\"\n" + 
    "                  label=\"Simple Type Declaration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd/XsdDocument\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">XSD Documents</atom:title>\n" + 
    "      <accept>application/xml</accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"XsdDocument\"\n" + 
    "                  label=\"XSD Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/xsd\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">XSD Model Objects\n" + 
    "      </atom:title>\n" + 
    "      <accept>application/zip</accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"XsdDocument\"\n" + 
    "                  label=\"XSD Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"AttributeDeclaration\"\n" + 
    "                  label=\"Attribute Declaration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"XsdType\"\n" + 
    "                  label=\"XSD Type\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ElementDeclaration\"\n" + 
    "                  label=\"Element Declaration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"SimpleTypeDeclaration\"\n" + 
    "                  label=\"Simple Type Declaration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ComplexTypeDeclaration\"\n" + 
    "                  label=\"Complex Type Declaration\"\n" + 
    "                  xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "  </workspace>\n" + 
    "</service>";

    public AppWorkspace getWorkspaceFromXML() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(AppService.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        AppService service = (AppService) unmarshaller
            .unmarshal(new StringReader(XML));
        AppWorkspace workspace = service.getWorkspace().get(0);
        return workspace;
    }
    @Test
    public void unmarshallCoreModelWorkspace() throws Exception {
        AppWorkspace workspace = getWorkspaceFromXML();
        Assert.assertTrue(workspace.getCollection().size()==7);
    }
    
    @Test @Ignore
    public void compareCoreModelToSpecification() throws Exception {
        AppWorkspace coreModelWorkspace = CoreModel.getWorkSpace("http://example.org");
        AppWorkspace expectedCoreModelWorkspace = getWorkspaceFromXML();
        
        Assert.assertEquals(expectedCoreModelWorkspace.getTitle(), 
                                    coreModelWorkspace.getTitle());
        Assert.assertEquals(expectedCoreModelWorkspace.getCollection().get(0).getHref(), 
                                    coreModelWorkspace.getCollection().get(0).getHref());
        long expectedSize = expectedCoreModelWorkspace.getCollection().get(0).getAccept().size();
        long acceptSize   = coreModelWorkspace.getCollection().get(0).getAccept().size();
        Assert.assertTrue(expectedSize==acceptSize);
        String expectedContent = expectedCoreModelWorkspace.getCollection().get(0).getAccept().get(0).getContent();
        String acceptContent   = coreModelWorkspace.getCollection().get(0).getAccept().get(0).getContent();
        Assert.assertEquals(expectedContent, acceptContent);
    }
    
    @Test
    public void marshallXsdModel() throws Exception {
        
        JAXBContext jaxbContext=JAXBContext.newInstance(AppService.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        
        AppWorkspace coreModelWorkspace = CoreModel.getWorkSpace("http://example.org");
        AppService appService = new AppService();
        appService.getWorkspace().add(coreModelWorkspace);
        
        StringWriter writer = new StringWriter();
        JAXBElement<AppService> element = new JAXBElement<AppService>(new QName("","app:service","app"),AppService.class,appService);
        
        marshaller.marshal(element,writer);
        String actualXml=writer.toString();
        System.out.println(actualXml);
        Assert.assertTrue(actualXml.contains("atom:category"));
    }
    
}
