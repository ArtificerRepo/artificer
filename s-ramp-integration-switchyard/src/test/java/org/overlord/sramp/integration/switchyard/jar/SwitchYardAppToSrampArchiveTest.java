/*
 * Copyright 2012 JBoss Inc
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
package org.overlord.sramp.integration.switchyard.jar;

import java.io.InputStream;
import java.util.Collection;
import java.util.TreeSet;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.archive.SrampArchiveException;
import org.overlord.sramp.atom.archive.jar.JarToSrampArchive;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;


/**
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardAppToSrampArchiveTest {

	@Test
	public void testSwitchYardIntegration() throws Exception {
	    InputStream stream = getClass().getResourceAsStream("switchyard-quickstart-bean-service.jar");
	    SrampArchive archive = null;
	    SwitchYardAppToSrampArchive sy2archive = null;
	    try {
	        sy2archive = new SwitchYardAppToSrampArchive(stream);
    		archive = sy2archive.createSrampArchive();
    		Assert.assertNotNull(archive);
    		doAllAssertions(archive);
	    } finally {
	        IOUtils.closeQuietly(stream);
            SrampArchive.closeQuietly(archive);
            JarToSrampArchive.closeQuietly(sy2archive);
	    }
	}

    /**
     * Asserts that the s-ramp archive was properly created.
     * @param archive
     * @throws SrampArchiveException
     */
    private void doAllAssertions(SrampArchive archive) throws SrampArchiveException {
        Collection<SrampArchiveEntry> entries = archive.getEntries();

        // Make sure all the entries we expect to be there are there.
        TreeSet<String> entryNames = new TreeSet<String>();
        for (SrampArchiveEntry srampArchiveEntry : entries) {
            entryNames.add(srampArchiveEntry.getPath());
        }
        StringBuilder buff = new StringBuilder();
        for (String entryName : entryNames) {
            buff.append(entryName).append("\n");
        }
        Assert.assertEquals(EXPECTED_ENTRIES, buff.toString());

        // Now spot-check some representative entries.

        // Check the switchyard.xml
        SrampArchiveEntry entry = archive.getEntry("META-INF/switchyard.xml");
        Assert.assertNotNull(entry);
        Assert.assertEquals("switchyard.xml", entry.getMetaData().getName());
        Assert.assertEquals(BaseArtifactEnum.EXTENDED_DOCUMENT, entry.getMetaData().getArtifactType());
        ExtendedDocument switchyardXmlDoc = (ExtendedDocument) entry.getMetaData();
        Assert.assertEquals(SwitchYardModel.SwitchYardXmlDocument, switchyardXmlDoc.getExtendedType());

        // Check InventoryService.class
        entry = archive.getEntry("org/switchyard/quickstarts/bean/service/InventoryService.class");
        Assert.assertNotNull(entry);
        Assert.assertEquals("InventoryService.class", entry.getMetaData().getName());
        Assert.assertEquals(BaseArtifactEnum.EXTENDED_DOCUMENT, entry.getMetaData().getArtifactType());
        ExtendedDocument inventoryServiceClass = (ExtendedDocument) entry.getMetaData();
        Assert.assertEquals("JavaClass", inventoryServiceClass.getExtendedType());

        // Check OrderService.wsdl
        entry = archive.getEntry("wsdl/OrderService.wsdl");
        Assert.assertNotNull(entry);
        Assert.assertEquals("OrderService.wsdl", entry.getMetaData().getName());
        Assert.assertEquals(BaseArtifactEnum.WSDL_DOCUMENT, entry.getMetaData().getArtifactType());
    }

    private static final String EXPECTED_ENTRIES =
            "META-INF/beans.xml\n" +
            "META-INF/switchyard.xml\n" +
            "org/switchyard/quickstarts/bean/service/InventoryService.class\n" +
            "org/switchyard/quickstarts/bean/service/InventoryServiceBean.class\n" +
//            "org/switchyard/quickstarts/bean/service/Item.class\n" +
//            "org/switchyard/quickstarts/bean/service/ItemNotFoundException.class\n" +
            "org/switchyard/quickstarts/bean/service/Order.class\n" +
            "org/switchyard/quickstarts/bean/service/OrderAck.class\n" +
            "org/switchyard/quickstarts/bean/service/OrderService.class\n" +
            "org/switchyard/quickstarts/bean/service/OrderServiceBean.class\n" +
//            "org/switchyard/quickstarts/bean/service/Transformers.class\n" +
//            "org/switchyard/quickstarts/bean/service/Validators.class\n" +
            "wsdl/OrderService.wsdl\n";
}
