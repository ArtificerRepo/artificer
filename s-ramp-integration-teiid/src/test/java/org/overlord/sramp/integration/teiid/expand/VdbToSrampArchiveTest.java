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

package org.overlord.sramp.integration.teiid.expand;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.atom.archive.expand.ZipToSrampArchive;
import org.overlord.sramp.integration.teiid.deriver.TeiidIntegrationTest;
import org.overlord.sramp.integration.teiid.model.TeiidArtifactType;
import org.overlord.sramp.integration.teiid.model.VdbManifest;

/**
 * A test class for {@link VdbToSrampArchive}.
 */
@SuppressWarnings( {"javadoc", "nls"} )
public final class VdbToSrampArchiveTest extends TeiidIntegrationTest {

    private void assertManifest( final SrampArchive srampArchive ) {
        final SrampArchiveEntry manifestEntry = srampArchive.getEntry(VdbManifest.PATH);
        assertThat(manifestEntry, is(not(nullValue())));

        final BaseArtifactType manifestArtifact = manifestEntry.getMetaData();
        assertThat(manifestArtifact, is(instanceOf(ExtendedDocument.class)));
        assertThat(((ExtendedDocument)manifestArtifact).getExtendedType(), is(VdbManifest.ARTIFACT_TYPE.extendedType()));
    }

    private void assertModel( final SrampArchive srampArchive,
                              final String modelEntryPath ) {
        final SrampArchiveEntry manifestEntry = srampArchive.getEntry(modelEntryPath);
        assertThat(manifestEntry, is(not(nullValue())));

        final BaseArtifactType manifestArtifact = manifestEntry.getMetaData();
        assertThat(manifestArtifact, is(instanceOf(ExtendedDocument.class)));
        assertThat(((ExtendedDocument)manifestArtifact).getExtendedType(), is(TeiidArtifactType.MODEL.extendedType()));
    }

    private void assertXsd( final SrampArchive srampArchive,
                            final String fileEntryPath ) {
        final SrampArchiveEntry manifestEntry = srampArchive.getEntry(fileEntryPath);
        assertThat(manifestEntry, is(not(nullValue())));

        final BaseArtifactType manifestArtifact = manifestEntry.getMetaData();
        assertThat(manifestArtifact, is(instanceOf(XsdDocument.class)));
    }

    @Test
    public void shouldExpandBooksVdb() throws Exception {
        final InputStream stream = getResourceAsStream("BooksVdb.vdb");
        VdbToSrampArchive vdbArchive = null;
        SrampArchive srampArchive = null;

        try {
            vdbArchive = new VdbToSrampArchive(stream);
            srampArchive = vdbArchive.createSrampArchive();
            assertThat(srampArchive, is(not(nullValue())));
            assertThat(srampArchive.getEntries().size(), is(7)); // manifest + 6 xmi models

            // check the manifest
            assertManifest(srampArchive);

            // check the models
            assertModel(srampArchive, "BooksProject/Books_Oracle.xmi");
            assertModel(srampArchive, "BooksProject/BooksView_Output_View.xmi");
            assertModel(srampArchive, "BooksProject/BooksView_WS.xmi");
            assertModel(srampArchive, "BooksProject/BooksView.xmi");

            // check other files
            assertXsd(srampArchive, "BooksProject/BooksView_Input.xsd");
            assertXsd(srampArchive, "BooksProject/BooksView_Output.xsd");
        } finally {
            IOUtils.closeQuietly(stream);
            SrampArchive.closeQuietly(srampArchive);
            ZipToSrampArchive.closeQuietly(vdbArchive);
        }
    }

    @Test
    public void shouldExpandProductsVdb() throws Exception {
        final InputStream stream = getResourceAsStream("ProductsSS_VDB.vdb");
        VdbToSrampArchive vdbArchive = null;
        SrampArchive srampArchive = null;

        try {
            vdbArchive = new VdbToSrampArchive(stream);
            srampArchive = vdbArchive.createSrampArchive();
            assertThat(srampArchive, is(not(nullValue())));
            assertThat(srampArchive.getEntries().size(), is(2)); // manifest + 1 xmi models

            // check the manifest
            assertManifest(srampArchive);

            // check the model
            assertModel(srampArchive, "TestProducts/ProductsSS.xmi");
        } finally {
            IOUtils.closeQuietly(stream);
            SrampArchive.closeQuietly(srampArchive);
            ZipToSrampArchive.closeQuietly(vdbArchive);
        }
    }

}
