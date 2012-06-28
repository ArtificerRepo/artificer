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
package org.overlord.sramp.atom.models;

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
import org.junit.Test;
import org.overlord.sramp.atom.models.CoreModel;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class CoreModelTest {
    /**
     * Taken from the S-RAMP Atom Binding Document Appendix E.
     */
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
    "<service xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns=\"http://www.w3.org/2007/app\">\n" + 
    "  <workspace>\n" + 
    "    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Core Model</atom:title>\n" + 
    "    <collection href=\"http://example.org/s-ramp/core/XmlDocument\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">XML Documents</atom:title>\n" + 
    "      <accept>application/xml</accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"XmlDocument\"\n" + 
    "                  label=\"XML Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/core/document\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Documents</atom:title>\n" + 
    "      <accept>application/octet-stream</accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Document\"\n" + 
    "                  label=\"Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "      </categories>\n" + 
    "    </collection>\n" + 
    "    <collection href=\"http://example.org/s-ramp/core\">\n" + 
    "      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Core Model Objects\n" + 
    "      </atom:title>\n" + 
    "      <accept>application/zip</accept>\n" + 
    "      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Document\"\n" + 
    "                  label=\"Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    "        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"XmlDocument\"\n" + 
    "          label=\"XML Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
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
        Assert.assertTrue(workspace.getCollection().size()==3);
    }
    
    @Test
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
        /*String expectedContent = */expectedCoreModelWorkspace.getCollection().get(0).getAccept().get(0).getContent();
        //String acceptContent   = coreModelWorkspace.getCollection().get(0).getAccept().get(0).getContent();
        //Assert.assertEquals(expectedContent, acceptContent);
    }
    
    @Test
    public void marshallCoreModel() throws Exception {
        
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
