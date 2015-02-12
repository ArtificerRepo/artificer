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

import java.io.StringWriter;
import java.net.URL;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;

import org.junit.Assert;

import org.apache.commons.io.IOUtils;
import org.jboss.resteasy.plugins.providers.atom.app.AppService;

/**
 * Base class for workspace tests.
 *
 * @author eric.wittmann@redhat.com
 */
public class AbstractWorkspaceTest {

    /**
     * Returns the XML in the expected file for the given workspace.
     * @param workspace
     */
    public static String getExpectedWorkspaceXML(String workspace) throws Exception {
    	URL url = AbstractWorkspaceTest.class.getResource(String.format("/workspace-files/%1$s-workspace.xml", workspace)); //$NON-NLS-1$
    	Assert.assertNotNull("Failed to find expected workspace XML file in /workspace-files (src/test/resources).", url); //$NON-NLS-1$
    	return IOUtils.toString(url);
    }

    /**
     * Marshalls the app service to XML.
     * @param appService
     */
	public static String marshall(AppService appService) throws Exception {
		JAXBContext jaxbContext = JAXBContext.newInstance(AppService.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.FALSE);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8"); //$NON-NLS-1$
		StringWriter writer = new StringWriter();
		JAXBElement<AppService> element = new JAXBElement<AppService>(new QName("", "app:service", "app"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				AppService.class, appService);
		marshaller.marshal(element, writer);
		return writer.toString();
	}
}
