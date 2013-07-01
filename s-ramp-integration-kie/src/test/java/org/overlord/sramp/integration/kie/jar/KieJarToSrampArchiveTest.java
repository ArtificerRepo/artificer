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
package org.overlord.sramp.integration.kie.jar;

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
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.integration.kie.expand.KieJarToSrampArchive;
import org.overlord.sramp.integration.kie.model.KieJarModel;

/**
 * @author eric.wittmann@redhat.com
 */
public class KieJarToSrampArchiveTest {

	@Test
	public void testKieIntegration() throws Exception {
	    InputStream stream = getClass().getResourceAsStream("kie.jar");
	    SrampArchive archive = null;
	    KieJarToSrampArchive kie2archive = null;
	    try {
	    	kie2archive = new KieJarToSrampArchive(stream);
    		archive = kie2archive.createSrampArchive();
    		Assert.assertNotNull(archive);
    		doAllAssertions(archive);
	    } finally {
	        IOUtils.closeQuietly(stream);
            SrampArchive.closeQuietly(archive);
            ZipToSrampArchive.closeQuietly(kie2archive);
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

        // Check the kmodule.xml
        SrampArchiveEntry entry = archive.getEntry("META-INF/kmodule.xml");
        Assert.assertNotNull(entry);
        Assert.assertEquals("kmodule.xml", entry.getMetaData().getName());
        Assert.assertEquals(BaseArtifactEnum.EXTENDED_DOCUMENT, entry.getMetaData().getArtifactType());
        ExtendedDocument kieXmlDoc = (ExtendedDocument) entry.getMetaData();
        Assert.assertEquals(KieJarModel.KieXmlDocument, kieXmlDoc.getExtendedType());

    }

    private static final String EXPECTED_ENTRIES =
            "META-INF/kmodule.xml\n" +
            "SRAMPPackage/overlord.demo.CheckDeployment-taskform.xml\n" +
            "SRAMPPackage/overlord.demo.ProjectLifeCycle.bpmn2\n" +
            "SRAMPPackage/overlord.demo.SimpleReleaseProcess.bpmn\n";
}
