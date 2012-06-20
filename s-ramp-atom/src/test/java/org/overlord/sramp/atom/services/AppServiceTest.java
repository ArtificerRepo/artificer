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
package org.overlord.sramp.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.app.AppCollection;
import org.jboss.resteasy.plugins.providers.atom.app.AppService;
import org.jboss.resteasy.test.BaseResourceTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.overlord.sramp.atom.services.ServiceDocumentResource;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class AppServiceTest extends BaseResourceTest {
    
    @Before
    public void setUp() throws Exception
    {
        //bring up the embedded container with the serviceDocument resource deployed.
       dispatcher.getRegistry().addPerRequestResource(ServiceDocumentResource.class);
    }
    
    @Test
    public void unmarshallAppService() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(AppService.class);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        AppService service = (AppService) unmarshaller
                .unmarshal(new StringReader(XML));
        Assert.assertTrue(service.getWorkspace().size()==8);
    }
    
    //Making a client call to the actual serviceDocument implementation running in
    //an embedded container.
    @Test
    public void testAppService() throws Exception
    {
       ClientRequest request = new ClientRequest(generateURL("/s-ramp/servicedocument"));
       
       ClientResponse<AppService> response = request.get(AppService.class);
       AppService appService = response.getEntity();
       
       //Assertions on the service and the workspace
       Assert.assertTrue(appService.getWorkspace().size()==1);
       Assert.assertEquals("Core Model", appService.getWorkspace().get(0).getTitle());
       
       //Assertions on the collections, 
       //at the moment we only implemented the xmlDocument Collection
       AppCollection appCollection = appService.getWorkspace().get(0).getCollection().get(0);
       Assert.assertEquals("/s-ramp/core/XmlDocument",appCollection.getHref());
       Assert.assertEquals("XML Documents",appCollection.getTitle());
       Assert.assertEquals("application/atom+xml;type=entry", appCollection.getAccept().get(0).getContent());
       //TODO Add more assertions as we implement more workspaces
       System.out.println(appService);
    }
    
    /**
     * Taken from the S-RAMP Atom Binding Document Appendix E.
     */
    private static final String XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
    		"<service xmlns:atom=\"http://www.w3.org/2005/Atom\" xmlns=\"http://www.w3.org/2007/app\">\n" + 
    		"  <workspace>\n" + 
    		"    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Query Model</atom:title>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/query\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Query Model Objects   \n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"query\"\n" + 
    		"                  label=\"Query\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"  </workspace>\n" + 
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
    		"  <workspace>\n" + 
    		"    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">WSDL Model</atom:title>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/BindingOperationOutput\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Binding Operation\n" + 
    		"                  Outputs</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperationOutput\" label=\"Binding\n" + 
    		"                  Operation Output\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/BindingOperation\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Binding \n" + 
    		"                  Operations</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperation\"\n" + 
    		"                  label=\"Binding Operation\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/WsdlDocument\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">WSDL Documents\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/xml</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"WsdlDocument\"\n" + 
    		"                  label=\"WSDL Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Binding\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Bindings</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Binding\"\n" + 
    		"                  label=\"Binding\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/OperationInput\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Operation Inputs\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"OperationInput\"\n" + 
    		"                  label=\"Operation Input\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Message\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Messages</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Message\"\n" + 
    		"                  label=\"Message\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Fault\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Faults\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Fault\"\n" + 
    		"                  label=\"Fault\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Operation\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Operations</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Operation\"\n" + 
    		"                  label=\"Operation\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">WSDL Model Objects\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/zip</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"WsdlDocument\"\n" + 
    		"                  label=\"WSDL Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"WsdlDerivedArtifactType\"\n" + 
    		"                  label=\"WSDL Derived Artifact\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"NamedWsdlDerivedArtifactType\"\n" + 
    		"                  label=\"Named WSDL Derived Artifact\"\n" + 
    		"                  xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Service\"\n" + 
    		"                  label=\"Service\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Port\"\n" + 
    		"                  label=\"Port\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"WsdlExtension\"\n" + 
    		"                  label=\"WSDL Extension\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Part\"\n" + 
    		"                  label=\"Part\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Message\"\n" + 
    		"                  label=\"Message\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Fault\"\n" + 
    		"                  label=\"Fault\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PortType\"\n" + 
    		"                  label=\"Port Type\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Operation\"\n" + 
    		"                  label=\"Operation\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"OperationInput\"\n" + 
    		"                  label=\"Operation Input\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"OperationOutput\"\n" + 
    		"                  label=\"Operation Output\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Binding\"\n" + 
    		"                  label=\"Binding\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperation\"\n" + 
    		"                  label=\"Binding Operation\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperationInput\"\n" + 
    		"                  label=\"Binding Operation Input\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperationOutput\"\n" + 
    		"                  label=\"Binding Operation Output\"\n" + 
    		"                  xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperationFault\"\n" + 
    		"                  label=\"Binding Operation Fault\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/WsdlExtension\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">WSDL Extensions\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"WsdlExtension\"\n" + 
    		"                  label=\"WSDL Extension\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/WsdlDerivedArtifactType\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">WSDL Derived Artifacts\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"WsdlDerivedArtifactType\"\n" + 
    		"                  label=\"WSDL Derived Artifact\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/OperationOutput\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Operation Outputs\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"OperationOutput\"\n" + 
    		"                  label=\"Operation Output\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/NamedWSDLDerivedArtifactType\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Named WSDL Derived\n" + 
    		"      Artifacts</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"NamedWsdlDerivedArtifactType\"\n" + 
    		"                  label=\"Named WSDL Derived Artifact\"\n" + 
    		"                  xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Port\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Ports</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Port\"\n" + 
    		"                  label=\"Port\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Part\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Parts</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Part\"\n" + 
    		"                  label=\"Part\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/PortType\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Port Types</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PortType\"\n" + 
    		"                  label=\"Port Type\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/BindingOperationFault\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Binding Operation\n" + 
    		"      Faults</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperationFault\"\n" + 
    		"                  label=\"Binding Operation Fault\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/BindingOperationInput\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Binding Operation\n" + 
    		"      Inputs</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"BindingOperationInput\"\n" + 
    		"                  label=\"Binding Operation Input\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/wsdl/Service\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service</atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Service\"\n" + 
    		"                  label=\"Service\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"  </workspace>\n" + 
    		"  <workspace>\n" + 
    		"    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service Implementation\n" + 
    		"    Model</atom:title>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/serviceImplementation/ServiceOperation\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service \n" + 
    		"      Operations</atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceOperation\"\n" + 
    		"                  label=\"Service Operation\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/serviceImplementation/ServiceInstance\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service\n" + 
    		"      Instances</atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceInstance\"\n" + 
    		"                  label=\"Service Instance\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/serviceImplementation/ServiceEndpoint\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service \n" + 
    		"      Endpoints</atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceEndpoint\"\n" + 
    		"                  label=\"Service Endpoint\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/serviceImplementation\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service Implementation\n" + 
    		"      Objects</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceEndpoint\"\n" + 
    		"                  label=\"Service Endpoint\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceInstance\"\n" + 
    		"                  label=\"Service Instance\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceOperation\"\n" + 
    		"                  label=\"Service Operation\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Organization\"\n" + 
    		"                  label=\"Organization\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"  </workspace>\n" + 
    		"  <workspace>\n" + 
    		"    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">SOAP WSDL Model\n" + 
    		"    </atom:title>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soapWsdl/SoapBinding\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">SOAP Bindings</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"SoapBinding\"\n" + 
    		"                  label=\"SOAP Binding\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soapWsdl/SoapAddress\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">SOAP Addresses\n" + 
    		"       </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"SoapAddress\"\n" + 
    		"                  label=\"SOAP Address\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soapWsdl\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">SOAP WSDL Model \n" + 
    		"      Objects</atom:title>\n" + 
    		"      <accept>application/zip</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"SoapAddress\"\n" + 
    		"                  label=\"SOAP Address\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"SoapBinding\"\n" + 
    		"                  label=\"SOAP Binding\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"  </workspace>\n" + 
    		"  <workspace>\n" + 
    		"    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">SOA Model</atom:title>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/ServiceInterface\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service \n" + 
    		"      Interface</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceInterface\"\n" + 
    		"                  label=\"Service Interface\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">SOA Model Objects\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/zip</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"HumanActor\"\n" + 
    		"                  label=\"HumanActor\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Choreography\"\n" + 
    		"                  label=\"Choreography\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ChoreographyProcess\"\n" + 
    		"                  label=\"Choreography Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Collaboration\"\n" + 
    		"                  label=\"Collaboration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"CollaborationProcess\"\n" + 
    		"                  label=\"Collaboration Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Composition\"\n" + 
    		"                  label=\"Composition\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Effect\"\n" + 
    		"                  label=\"Effect\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Element\"\n" + 
    		"                  label=\"Element\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Event\"\n" + 
    		"                  label=\"Event\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"InformationType\"\n" + 
    		"                  label=\"Information Type\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Orchestration\"\n" + 
    		"                  label=\"Orchestration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"OrchestrationProcess\"\n" + 
    		"                  label=\"Orchestration Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Policy\"\n" + 
    		"                  label=\"Policy\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicySubject\"\n" + 
    		"                  label=\"Policy Subject\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Process\"\n" + 
    		"                  label=\"Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Service\"\n" + 
    		"                  label=\"Service\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceContract\"\n" + 
    		"                  label=\"Service Contract\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceComposition\"\n" + 
    		"                   label=\"Service Composition\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceInterface\"\n" + 
    		"                  label=\"Service Interface\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"System\"\n" + 
    		"                  label=\"System\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Task\"\n" + 
    		"                  label=\"Task\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/CollaborationProcess\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Collaboration \n" + 
    		"       Process</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"CollaborationProcess\"\n" + 
    		"                  label=\"Collaboration Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Process\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Process</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Process\"\n" + 
    		"                  label=\"Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/serviceImplementation/HumanActor\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">HumanActor</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"HumanActor\"\n" + 
    		"                  label=\"HumanActor\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Collaboration\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Collaboration</atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Collaboration\"\n" + 
    		"                  label=\"Collaboration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Composition\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Composition</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Composition\"\n" + 
    		"                  label=\"Composition\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Element\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Element</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Element\"\n" + 
    		"                  label=\"Element\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Event\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Event</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Event\"\n" + 
    		"                  label=\"Event\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Orchestration\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Orchestration</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Orchestration\"\n" + 
    		"                  label=\"Orchestration\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/PolicySubject\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy Subject\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicySubject\"\n" + 
    		"                  label=\"Policy Subject\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/InformationType\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Information Type\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"InformationType\"\n" + 
    		"                  label=\"Information Type\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Task\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Task</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Task\"\n" + 
    		"                  label=\"Task\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/System\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">System</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"System\"\n" + 
    		"                  label=\"System\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Policy\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Policy\"\n" + 
    		"                  label=\"Policy\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Choreography\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Choreography</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Choreography\"\n" + 
    		"                  label=\"Choreography\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Effect\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Effect</atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Effect\"\n" + 
    		"                  label=\"Effect\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/ServiceContract\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service Contract\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ServiceContract\"\n" + 
    		"                  label=\"Service Contract\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/OrchestrationProcess\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Orchestration Process\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"OrchestrationProcess\"\n" + 
    		"                  label=\"Orchestration Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/serviceImplemenation/Organization\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Organization</atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Organization\"\n" + 
    		"                  label=\"Organization\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/Service\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Service</atom:title>\n" + 
    		"      <accept>application/atom+xml; type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"Service\"\n" + 
    		"                  label=\"Service\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/soa/ChoreographyProcess\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Choreography Process\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/atom+xml;type=entry</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"ChoreographyProcess\"\n" + 
    		"                  label=\"Choreography Process\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"  </workspace>\n" + 
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
    		"  <workspace>\n" + 
    		"    <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy Model</atom:title>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/policy/PolicyDocument\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy Documents \n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/xml</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicyDocument\"\n" + 
    		"                  label=\"Policy Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/policy\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy Model Objects\n" + 
    		"      </atom:title>\n" + 
    		"      <accept>application/zip</accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicyDocument\"\n" + 
    		"                  label=\"Policy Document\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicyExpression\"\n" + 
    		"                  label=\"Policy Expression\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicyAttachment\"\n" + 
    		"                  label=\"Policy Attachment\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/policy/PolicyAttachment\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy Attachments\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicyAttachment\"\n" + 
    		"                  label=\"Policy Attachment\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"    <collection href=\"http://example.org/s-ramp/policy/PolicyExpression\">\n" + 
    		"      <atom:title type=\"text\" xmlns:atom=\"http://www.w3.org/2005/Atom\">Policy Expressions\n" + 
    		"      </atom:title>\n" + 
    		"      <accept></accept>\n" + 
    		"      <categories fixed=\"yes\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n" + 
    		"        <category scheme=\"urn:x-s-ramp:2010:type\" term=\"PolicyExpression\"\n" + 
    		"                  label=\"Policy Expression\" xmlns=\"http://www.w3.org/2005/Atom\"></category>\n" + 
    		"      </categories>\n" + 
    		"    </collection>\n" + 
    		"  </workspace>\n" + 
    		"</service>\n" + 
    		"";


}
