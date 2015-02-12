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
package org.artificer.server.atom.workspaces;

import org.custommonkey.xmlunit.XMLAssert;
import org.jboss.resteasy.plugins.providers.atom.app.AppService;
import org.junit.Test;

/**
 * Unit test for the core workspace.
 * @author <a href="mailto:kurt.stam@gmail.com">Kurt Stam</a>
 * @version $Revision: 1 $
 */
public class AllWorkspaceTest extends AbstractWorkspaceTest {

    @Test
    public void testWorkspace() throws Exception {
		AppService appService = new AppService();
		String hrefBase = "http://example.org"; //$NON-NLS-1$
		appService.getWorkspace().add(new CoreWorkspace(hrefBase));
		appService.getWorkspace().add(new XsdWorkspace(hrefBase));
		appService.getWorkspace().add(new PolicyWorkspace(hrefBase));
		appService.getWorkspace().add(new WsdlWorkspace(hrefBase));
        appService.getWorkspace().add(new OntologyWorkspace(hrefBase));
        appService.getWorkspace().add(new AuditWorkspace(hrefBase));
        // Keep StoredQueryWorkspace out, for now -- requires JCR persistence to be available.
//        appService.getWorkspace().add(new StoredQueryWorkspace(hrefBase));

        String actual = marshall(appService);
        String expected = getExpectedWorkspaceXML("all"); //$NON-NLS-1$

        XMLAssert.assertXMLEqual(expected, actual);
    }
}
