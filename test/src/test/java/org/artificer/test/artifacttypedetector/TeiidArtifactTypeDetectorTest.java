/*
 * Copyright 2013 JBoss Inc
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

package org.artificer.test.artifacttypedetector;

import org.apache.commons.io.IOUtils;
import org.artificer.test.client.AbstractClientTest;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.integration.teiid.model.TeiidArtifactType;
import org.artificer.integration.teiid.model.Vdb;
import org.artificer.integration.teiid.model.VdbManifest;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Brett Meyer
 */
public final class TeiidArtifactTypeDetectorTest extends AbstractClientTest {

    private void assertManifest(ArtificerAtomApiClient client) throws Exception {
        QueryResultSet results = client.buildQuery("/s-ramp/ext/" + VdbManifest.ARTIFACT_TYPE.extendedType()).query();
        assertEquals(1, results.size());
        ArtifactSummary summary = results.get(0);
        assertNotNull(summary);
    }

    private void assertModel(ArtificerAtomApiClient client, String filename) throws Exception {
        QueryResultSet results = client.buildQuery("/s-ramp/ext/" + TeiidArtifactType.MODEL.extendedType()
                + "[@name='" + filename + "']").query();
        assertEquals(1, results.size());
        ArtifactSummary summary = results.get(0);
        assertNotNull(summary);
    }

    private void assertXsd(ArtificerAtomApiClient client, String filename) throws Exception {
        QueryResultSet results = client.buildQuery("/s-ramp/xsd/XsdDocument[@name='" + filename + "']").query();
        assertEquals(1, results.size());
        ArtifactSummary summary = results.get(0);
        assertNotNull(summary);
    }

    @Test
    public void testBooksVdb() throws Exception {
        ArtificerAtomApiClient client = client();
        InputStream stream = null;
        try {
            stream = this.getClass().getResourceAsStream("BooksVdb.vdb");
            BaseArtifactType artifact = client.uploadArtifact(stream, "BooksVdb.vdb");

            assertNotNull(artifact);
            assertEquals(ExtendedDocument.class, artifact.getClass());
            ExtendedDocument extendedDocument = (ExtendedDocument) artifact;
            assertEquals(Vdb.ARTIFACT_TYPE, extendedDocument.getExtendedType());

            // check the manifest
            assertManifest(client);

            // check the models
            assertModel(client, "Books_Oracle.xmi");
            assertModel(client, "BooksView_Output_View.xmi");
            assertModel(client, "BooksView_WS.xmi");
            assertModel(client, "BooksView.xmi");

            // check other files
            assertXsd(client, "BooksView_Input.xsd");
            assertXsd(client, "BooksView_Output.xsd");
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    @Test
    public void testProductsVdb() throws Exception {
        ArtificerAtomApiClient client = client();
        InputStream stream = null;
        try {
            stream = this.getClass().getResourceAsStream("ProductsSS_VDB.vdb");
            BaseArtifactType artifact = client.uploadArtifact(stream, "ProductsSS_VDB.vdb");

            assertNotNull(artifact);
            assertEquals(ExtendedDocument.class, artifact.getClass());
            ExtendedDocument extendedDocument = (ExtendedDocument) artifact;
            assertEquals(Vdb.ARTIFACT_TYPE, extendedDocument.getExtendedType());

            // check the manifest
            assertManifest(client);

            // check the model
            assertModel(client, "ProductsSS.xmi");
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

}
