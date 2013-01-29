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
package org.overlord.sramp.server.atom.services;

import static org.jboss.resteasy.test.TestPortProvider.generateURL;
import static org.jboss.resteasy.test.TestPortProvider.getPort;

import org.custommonkey.xmlunit.XMLAssert;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.plugins.providers.atom.app.AppService;
import org.junit.Test;
import org.overlord.sramp.atom.client.ClientRequest;
import org.overlord.sramp.server.atom.workspaces.AbstractWorkspaceTest;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class ServiceDocumentResourceTest extends AbstractResourceTest {

    @Test
    public void testServiceDocument() throws Exception {
       ClientRequest request = new ClientRequest(generateURL("/s-ramp/servicedocument"));

       ClientResponse<AppService> response = request.get(AppService.class);
       AppService appService = response.getEntity();

       String actual = AbstractWorkspaceTest.marshall(appService);
       String expected = AbstractWorkspaceTest.getExpectedWorkspaceXML("servicedocument");
       expected = expected.replace("RE_PORT", String.valueOf(getPort()));
       XMLAssert.assertXMLEqual(expected, actual);
    }

}
