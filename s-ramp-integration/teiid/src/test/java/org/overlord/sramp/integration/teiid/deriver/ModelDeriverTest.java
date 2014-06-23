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
package org.overlord.sramp.integration.teiid.deriver;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.integration.teiid.model.TeiidModel;
import org.overlord.sramp.integration.teiid.model.VdbManifest;

/**
 * A test class for a {@link ModelDeriver}.
 */
@SuppressWarnings( {"javadoc", "nls"} )
public final class ModelDeriverTest extends TeiidIntegrationTest {
    private AbstractXmlDeriver deriver = null;
    private ExtendedDocument modelArtifact = null;

    @Before
    public void constructDeriver() {
        this.deriver = new ModelDeriver();

        this.modelArtifact = new ExtendedDocument();
        this.modelArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        this.modelArtifact.setName(VdbManifest.FILE_NAME);
        this.modelArtifact.setExtendedType(TeiidModel.ARTIFACT_TYPE.extendedType());
    }

    @Test
    public void shouldDeriveBooksOracleModel() throws Exception {
        final InputStream modelStream = getResourceAsStream("Books_Oracle.xmi");
        assertThat(modelStream, is(not(nullValue())));

        // deriver framework will call derive and link methods
        final Collection<BaseArtifactType> derivedArtifacts = this.deriver.derive(this.modelArtifact, modelStream);
        this.deriver.link(null, this.modelArtifact, derivedArtifacts);

        // check model artifact properties
        assertPropertyValue(this.modelArtifact, TeiidModel.PropertyId.MMUID, "mmuuid:18b6c0ff-aa2a-409b-87f2-c3f1cd742b41");
        assertPropertyValue(this.modelArtifact, TeiidModel.PropertyId.DESCRIPTION, "This is a source model");
        assertPropertyValue(this.modelArtifact,
                            TeiidModel.PropertyId.PRIMARY_METAMODEL_URI,
                            "http://www.metamatrix.com/metamodels/Relational");
        assertPropertyValue(this.modelArtifact, TeiidModel.PropertyId.MODEL_TYPE, "PHYSICAL");
        assertPropertyValue(this.modelArtifact, TeiidModel.PropertyId.MAX_SET_SIZE, "1000");
        assertPropertyValue(this.modelArtifact, TeiidModel.PropertyId.PRODUCER_NAME, "Teiid Designer");
        assertPropertyValue(this.modelArtifact, TeiidModel.PropertyId.PRODUCER_VERSION, "8.0.0");
    }

    @Test( expected = IOException.class )
    public void shouldNotDeriveNonModels() throws Exception {
        final InputStream notAModelStream = getResourceAsStream("twitterVdb.xml");
        assertThat(notAModelStream, is(not(nullValue())));

        // deriver framework will call derive
        this.deriver.derive(this.modelArtifact, notAModelStream);
    }

}
