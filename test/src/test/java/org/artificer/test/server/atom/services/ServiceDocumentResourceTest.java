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
package org.artificer.test.server.atom.services;

import org.junit.Test;

/**
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class ServiceDocumentResourceTest extends AbstractResourceTest {

    @Test
    public void testServiceDocument() throws Exception {
        // TODO: This test needs re-thought.
//        ClientRequest request = clientRequest("/s-ramp/servicedocument");
//
//        ClientResponse<AppService> response = request.get(AppService.class);
//        AppService appService = response.getEntity();
//
//        String actual = AbstractWorkspaceTest.marshall(appService);
//        String expected = AbstractWorkspaceTest.getExpectedWorkspaceXML("servicedocument");
//        expected = expected.replace("RE_PORT", String.valueOf(getPort()));
//        expected = expected.replace("RE_HOST", getHost());
//        try {
//            XMLAssert.assertXMLEqual(expected, actual);
//        } catch (Error e) {
//            System.out.println(actual);
//            throw e;
//        }
    }

}
