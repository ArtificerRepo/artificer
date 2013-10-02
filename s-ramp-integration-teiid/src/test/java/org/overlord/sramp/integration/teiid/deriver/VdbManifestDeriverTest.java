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
import static org.junit.Assert.fail;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.integration.teiid.model.VdbDataPolicy;
import org.overlord.sramp.integration.teiid.model.VdbEntry;
import org.overlord.sramp.integration.teiid.model.VdbImport;
import org.overlord.sramp.integration.teiid.model.VdbManifest;
import org.overlord.sramp.integration.teiid.model.VdbPermission;
import org.overlord.sramp.integration.teiid.model.VdbSchema;
import org.overlord.sramp.integration.teiid.model.VdbSchemaSource;
import org.overlord.sramp.integration.teiid.model.VdbTranslator;
import org.overlord.sramp.integration.teiid.model.VdbValidationError;

/**
 * A test class for a {@link VdbManifestDeriver}.
 */
@SuppressWarnings( {"javadoc", "nls"} )
public final class VdbManifestDeriverTest extends TeiidIntegrationTest {

    private AbstractXmlDeriver deriver = null;
    private ExtendedDocument vdbManifestArtifact = null;

    @Before
    public void constructDeriver() {
        this.deriver = new VdbManifestDeriver();

        this.vdbManifestArtifact = new ExtendedDocument();
        this.vdbManifestArtifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        this.vdbManifestArtifact.setName(VdbManifest.FILE_NAME);
        this.vdbManifestArtifact.setExtendedType(VdbManifest.ARTIFACT_TYPE.extendedType());
    }

    @Test
    public void shouldDeriveParserParserTestVdbArtifacts() throws Exception {
        final InputStream vdbStream = getResourceAsStream("parser-test-vdb.xml");
        assertThat(vdbStream, is(not(nullValue())));

        // deriver framework will call derive and link methods
        final Collection<BaseArtifactType> derivedArtifacts = this.deriver.derive(this.vdbManifestArtifact, vdbStream);
        this.deriver.link(null, this.vdbManifestArtifact, derivedArtifacts);

        assertThat(this.vdbManifestArtifact.getName(), is("myVDB"));
        assertThat(this.vdbManifestArtifact.getDescription(), is("vdb description"));
        assertThat(this.vdbManifestArtifact.getVersion(), is("3"));
        assertPropertyValue(this.vdbManifestArtifact, VdbManifest.PropertyId.VERSION, "3");
        assertPropertyValue(this.vdbManifestArtifact, "vdb-property2", "vdb-value2");
        assertPropertyValue(this.vdbManifestArtifact, "vdb-property", "vdb-value");
        assertThat(derivedArtifacts.size(), is(14)); // 2 models, 1 vdb import, 3 sources, 1 validation error, 1 translator,
                                                     // 1 data role, 3 permission, 2 entry

        // verify derived artifacts
        boolean foundImportVdb = false;
        boolean foundPhysicalModel = false;
        boolean foundSource1 = false;
        boolean foundError = false;
        boolean foundVirtualModel = false;
        boolean foundSource2 = false;
        boolean foundSource3 = false;
        boolean foundEntry1 = false;
        boolean foundEntry2 = false;
        boolean foundTranslator = false;
        boolean foundDataRole = false;
        boolean foundPermission1 = false;
        boolean foundPermission2 = false;
        boolean foundPermission3 = false;

        // hold on to artifacts to test relationships
        BaseArtifactType importVdb = null;
        BaseArtifactType physicalModel = null;
        BaseArtifactType virtualModel = null;
        BaseArtifactType source1 = null;
        BaseArtifactType source2 = null;
        BaseArtifactType source3 = null;
        BaseArtifactType dataPolicy = null;
        BaseArtifactType permission1 = null;
        BaseArtifactType permission2 = null;
        BaseArtifactType permission3 = null;
        BaseArtifactType validationError = null;

        for (final BaseArtifactType derivedArtifact : derivedArtifacts) {
            final String artifactName = derivedArtifact.getName();

            if (!foundImportVdb && isExtendedType(derivedArtifact, VdbImport.ARTIFACT_TYPE)) {
                foundImportVdb = true;
                importVdb = derivedArtifact;
                assertThat(importVdb.getName(), is("x"));
                assertThat(importVdb.getVersion(), is("2"));
                assertPropertyValue(importVdb, VdbImport.PropertyId.VERSION, "2");
                assertPropertyValue(importVdb, VdbImport.PropertyId.IMPORT_DATA_POLICIES, "true");
                assertThat(importVdb.getProperty().size(), is(2)); // version, import data policies

                // related documents relationship
                final List<Relationship> relationships = derivedArtifact.getRelationship();
                assertThat(relationships.size(), is(1));

                // make sure VDB contains import VDB
                assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                             VdbManifest.CONTAINS_RELATIONSHIP,
                                             derivedArtifact.getUuid());
            } else if (!foundTranslator && isExtendedType(derivedArtifact, VdbTranslator.ARTIFACT_TYPE)) {
                foundTranslator = true;
                assertThat(artifactName, is("oracleOverride"));
                assertThat(derivedArtifact.getDescription(), is("hello world"));
                assertThat(derivedArtifact.getProperty().size(), is(2));
                assertPropertyValue(derivedArtifact, VdbTranslator.PropertyId.TYPE, "oracle");
                assertPropertyValue(derivedArtifact, "my-property", "my-value");

                // related documents relationship
                final List<Relationship> relationships = derivedArtifact.getRelationship();
                assertThat(relationships.size(), is(1));

                // make sure VDB contains translator
                assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                             VdbManifest.CONTAINS_RELATIONSHIP,
                                             derivedArtifact.getUuid());
            } else if (isExtendedType(derivedArtifact, VdbEntry.ARTIFACT_TYPE)) {
                if (!foundEntry1 && "/path-one".equals(artifactName)) {
                    foundEntry1 = true;
                    assertThat(derivedArtifact.getDescription(), is("path one description"));
                    assertPropertyValue(derivedArtifact, "entryone", "1");
                    assertThat(derivedArtifact.getProperty().size(), is(1));

                    // related documents relationship
                    final List<Relationship> relationships = derivedArtifact.getRelationship();
                    assertThat(relationships.size(), is(1));

                    // make sure VDB contains entry
                    assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                                 VdbManifest.CONTAINS_RELATIONSHIP,
                                                 derivedArtifact.getUuid());
                } else if (!foundEntry2 && "/path-two".equals(artifactName)) {
                    foundEntry2 = true;
                    assertThat(derivedArtifact.getProperty().isEmpty(), is(true));

                    // related documents relationship
                    final List<Relationship> relationships = derivedArtifact.getRelationship();
                    assertThat(relationships.size(), is(1));

                    // make sure VDB contains entry
                    assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                                 VdbManifest.CONTAINS_RELATIONSHIP,
                                                 derivedArtifact.getUuid());
                } else {
                    fail("unexpected entry: " + artifactName);
                }
            } else if (isExtendedType(derivedArtifact, VdbSchema.ARTIFACT_TYPE)) {
                if (!foundPhysicalModel && "model-one".equals(artifactName)) {
                    foundPhysicalModel = true;
                    physicalModel = derivedArtifact;

                    assertPropertyValue(physicalModel, VdbSchema.PropertyId.TYPE, VdbSchema.Type.PHYSICAL.name());
                    assertPropertyValue(physicalModel, VdbSchema.PropertyId.VISIBLE, "false");
                    assertThat(physicalModel.getDescription(), is("model description"));
                    assertPropertyValue(physicalModel, "model-prop", "model-value-override");

                    // relationships
                    final List<Relationship> relationships = physicalModel.getRelationship();
                    assertThat(relationships.size(), is(3)); // source, validation error, relatedDocument

                    // make sure VDB contains physical model
                    assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                                 VdbManifest.CONTAINS_RELATIONSHIP,
                                                 physicalModel.getUuid());
                } else if (!foundVirtualModel && "model-two".equals(artifactName)) {
                    foundVirtualModel = true;
                    virtualModel = derivedArtifact;

                    assertPropertyValue(virtualModel, VdbSchema.PropertyId.TYPE, VdbSchema.Type.VIRTUAL.name());
                    assertPropertyValue(virtualModel, VdbSchema.PropertyId.VISIBLE, "true");
                    assertPropertyValue(virtualModel, "model-prop", "model-value");
                    assertPropertyValue(virtualModel, VdbSchema.PropertyId.METADATA_TYPE, VdbSchema.DEFAULT_METADATA_TYPE.name());
                    assertPropertyValue(virtualModel, VdbSchema.PropertyId.METADATA, "DDL Here");
                    // assertPropertyValue(derivedArtifact, VdbSchema.PropertyId.VALIDATION_ERRORS, Boolean.FALSE.toString());

                    // sources and related document relationships
                    final List<Relationship> relationships = virtualModel.getRelationship();
                    assertThat(relationships.size(), is(2));

                    // make sure VDB contains virtual model
                    assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                                 VdbManifest.CONTAINS_RELATIONSHIP,
                                                 virtualModel.getUuid());
                } else {
                    fail("unexpected model: " + artifactName);
                }
            } else if (isExtendedType(derivedArtifact, VdbSchemaSource.ARTIFACT_TYPE)) {
                if (!foundSource1 && "s1".equals(artifactName)) {
                    foundSource1 = true;
                    source1 = derivedArtifact;

                    assertPropertyValue(source1, VdbSchemaSource.PropertyId.TRANSLATOR_NAME, "translator");
                    assertPropertyValue(source1, VdbSchemaSource.PropertyId.JNDI_NAME, "java:binding-one");
                    assertThat(source1.getProperty().size(), is(2));

                    // schema and related document relationships
                    final List<Relationship> relationships = source1.getRelationship();
                    assertThat(relationships.size(), is(2));
                } else if (!foundSource2 && "s2".equals(artifactName)) {
                    foundSource2 = true;
                    source2 = derivedArtifact;

                    assertPropertyValue(source2, VdbSchemaSource.PropertyId.TRANSLATOR_NAME, "translator");
                    assertPropertyValue(source2, VdbSchemaSource.PropertyId.JNDI_NAME, "java:binding-two");
                    assertThat(source2.getProperty().size(), is(2));

                    // schema and related document relationships
                    final List<Relationship> relationships = source2.getRelationship();
                    assertThat(relationships.size(), is(2));
                } else if (!foundSource3 && "s3".equals(artifactName)) {
                    foundSource3 = true;
                    source3 = derivedArtifact;

                    assertPropertyValue(source3, VdbSchemaSource.PropertyId.TRANSLATOR_NAME, "translator");
                    assertPropertyValue(source3, VdbSchemaSource.PropertyId.JNDI_NAME, "java:mybinding");
                    assertThat(source3.getProperty().size(), is(2));

                    // schema and related document relationships
                    final List<Relationship> relationships = source3.getRelationship();
                    assertThat(relationships.size(), is(2));
                } else {
                    fail("unexpected source: " + artifactName);
                }
            } else if (!foundDataRole && isExtendedType(derivedArtifact, VdbDataPolicy.ARTIFACT_TYPE)) {
                foundDataRole = true;
                dataPolicy = derivedArtifact;

                assertThat(artifactName, is("roleOne"));
                assertThat(dataPolicy.getDescription(), is("roleOne described"));
                assertPropertyValue(dataPolicy, VdbDataPolicy.PropertyId.ANY_AUTHENTICATED, "false");
                assertPropertyValue(dataPolicy, VdbDataPolicy.PropertyId.TEMP_TABLE_CREATABLE, "true");
                assertPropertyValue(dataPolicy, VdbDataPolicy.PropertyId.ROLE_NAMES, "ROLE1,ROLE2");
                assertThat(dataPolicy.getProperty().size(), is(3));

                // permissions and related document relationships
                final List<Relationship> relationships = dataPolicy.getRelationship();
                assertThat(relationships.size(), is(2));

                // make sure VDB contains virtual data policy
                assertRelationshipTargetUuid(this.vdbManifestArtifact, VdbManifest.CONTAINS_RELATIONSHIP, dataPolicy.getUuid());
            } else if (isExtendedType(derivedArtifact, VdbPermission.ARTIFACT_TYPE)) {
                if (!foundPermission1 && "myTable.T1".equals(artifactName)) {
                    foundPermission1 = true;
                    permission1 = derivedArtifact;

                    assertPropertyValue(permission1, VdbPermission.PropertyId.READABLE, "true");
                    assertThat(permission1.getProperty().size(), is(1));

                    // data policy and related document relationships
                    final List<Relationship> relationships = permission1.getRelationship();
                    assertThat(relationships.size(), is(2));
                } else if (!foundPermission2 && "myTable.T2".equals(artifactName)) {
                    foundPermission2 = true;
                    permission2 = derivedArtifact;

                    assertPropertyValue(permission2, VdbPermission.PropertyId.CREATABLE, "true");
                    assertPropertyValue(permission2, VdbPermission.PropertyId.READABLE, "false");
                    assertPropertyValue(permission2, VdbPermission.PropertyId.UPDATABLE, "true");
                    assertPropertyValue(permission2, VdbPermission.PropertyId.DELETABLE, "true");
                    assertPropertyValue(permission2, VdbPermission.PropertyId.EXECUTABLE, "true");
                    assertPropertyValue(permission2, VdbPermission.PropertyId.ALTERABLE, "true");
                    assertPropertyValue(permission2, VdbPermission.PropertyId.CONDITION, "col1 = user()");
                    assertThat(permission2.getProperty().size(), is(7));

                    // data policy and related document relationships
                    final List<Relationship> relationships = permission2.getRelationship();
                    assertThat(relationships.size(), is(2));
                } else if (!foundPermission3 && "javascript".equals(artifactName)) {
                    foundPermission3 = true;
                    permission3 = derivedArtifact;

                    assertPropertyValue(permission3, VdbPermission.PropertyId.LANGUAGABLE, "true");
                    assertThat(derivedArtifact.getProperty().size(), is(1));

                    // data policy and related document relationships
                    final List<Relationship> relationships = permission3.getRelationship();
                    assertThat(relationships.size(), is(2));
                } else {
                    fail("unexpected permission: " + artifactName);
                }
            } else if (isExtendedType(derivedArtifact, VdbValidationError.ARTIFACT_TYPE)) {
                foundError = true;
                validationError = derivedArtifact;
                assertPropertyValue(validationError, VdbValidationError.PropertyId.SEVERITY, "ERROR");
                assertPropertyValue(validationError, VdbValidationError.PropertyId.MESSAGE, "There is an error in VDB");
            } else {
                fail("unexpected artifact: " + artifactName);
            }
        }

        assertThat("foundImportVdb=" + foundImportVdb + ", foundPhysicalModel=" + foundPhysicalModel + ", foundSource1="
                   + foundSource1 + ", foundVirtualModel=" + foundVirtualModel + ", foundSource2=" + foundSource2
                   + ", foundSource3=" + foundSource3 + ", foundEntry1" + foundEntry1 + ", foundEntry2=" + foundEntry2
                   + ", foundError=" + foundError + ", foundTranslator=" + foundTranslator + ", foundDataRole=" + foundDataRole
                   + ", foundPermission1=" + foundPermission1 + ", foundPermission2=" + foundPermission2 + ", foundPermission3="
                   + foundPermission3, (foundImportVdb && foundPhysicalModel && foundSource1 && foundVirtualModel && foundSource2
                                        && foundSource3 && foundEntry1 && foundEntry2 && foundError && foundTranslator
                                        && foundDataRole && foundPermission1 && foundPermission2 && foundPermission3), is(true));

        // relationships
        assertRelationshipTargetUuid(physicalModel, VdbSchema.SOURCES_RELATIONSHIP, source3.getUuid());
        assertRelationshipTargetUuid(source3, VdbSchemaSource.SCHEMA_RELATIONSHIP, physicalModel.getUuid());

        assertRelationshipTargetUuid(physicalModel, VdbSchema.VALIDATION_ERRORS_RELATIONSHIP, validationError.getUuid());
        assertRelationshipTargetUuid(validationError, VdbValidationError.SOURCE_RELATIONSHIP, physicalModel.getUuid());

        assertRelationshipTargetUuid(virtualModel, VdbSchema.SOURCES_RELATIONSHIP, source1.getUuid());
        assertRelationshipTargetUuid(source1, VdbSchemaSource.SCHEMA_RELATIONSHIP, virtualModel.getUuid());

        assertRelationshipTargetUuid(virtualModel, VdbSchema.SOURCES_RELATIONSHIP, source2.getUuid());
        assertRelationshipTargetUuid(source2, VdbSchemaSource.SCHEMA_RELATIONSHIP, virtualModel.getUuid());

        assertRelationshipTargetUuid(permission1, VdbPermission.DATA_POLICY_RELATIONSHIP, dataPolicy.getUuid());
        assertRelationshipTargetUuid(dataPolicy, VdbDataPolicy.PERMISSIONS_RELATIONSHIP, permission1.getUuid());

        assertRelationshipTargetUuid(permission2, VdbPermission.DATA_POLICY_RELATIONSHIP, dataPolicy.getUuid());
        assertRelationshipTargetUuid(dataPolicy, VdbDataPolicy.PERMISSIONS_RELATIONSHIP, permission2.getUuid());

        assertRelationshipTargetUuid(permission3, VdbPermission.DATA_POLICY_RELATIONSHIP, dataPolicy.getUuid());
        assertRelationshipTargetUuid(dataPolicy, VdbDataPolicy.PERMISSIONS_RELATIONSHIP, permission3.getUuid());
    }

    @Test
    public void shouldDeriveProductsVdbArtifacts() throws Exception {
        final InputStream vdbStream = getResourceAsStream("ProductsSS_VDB.xml");
        assertThat(vdbStream, is(not(nullValue())));

        // deriver framework will call derive and link methods
        final Collection<BaseArtifactType> derivedArtifacts = this.deriver.derive(this.vdbManifestArtifact, vdbStream);
        this.deriver.link(null, this.vdbManifestArtifact, derivedArtifacts);

        assertThat(this.vdbManifestArtifact.getName(), is("ProductsSS_VDB"));
        assertThat(this.vdbManifestArtifact.getVersion(), is("1"));
        assertPropertyValue(this.vdbManifestArtifact, VdbManifest.PropertyId.VERSION, "1");
        assertPropertyValue(this.vdbManifestArtifact, VdbManifest.PropertyId.PREVIEW, Boolean.FALSE.toString());
        assertThat(derivedArtifacts.size(), is(6)); // model, source, 4 validation errors

        boolean foundError1 = false;
        boolean foundError2 = false;
        boolean foundError3 = false;
        boolean foundError4 = false;

        BaseArtifactType physicalModel = null;
        BaseArtifactType source = null;
        BaseArtifactType validationError1 = null;
        BaseArtifactType validationError2 = null;
        BaseArtifactType validationError3 = null;
        BaseArtifactType validationError4 = null;

        for (final BaseArtifactType derivedArtifact : derivedArtifacts) {
            if (isExtendedType(derivedArtifact, VdbSchema.ARTIFACT_TYPE)) {
                physicalModel = derivedArtifact;
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.VISIBLE, Boolean.TRUE.toString());
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.TYPE, VdbSchema.Type.PHYSICAL.name());
                assertThat(physicalModel.getName(), is("ProductsSS"));
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.PATH_IN_VDB, "/TestProducts/ProductsSS.xmi");
                assertThat(physicalModel.getDescription(), is("SQL Server products relational model"));
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.CHECKSUM, "3463730926");
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.MODEL_UUID, "mmuuid:99038f39-aaf6-4be5-98e7-cfb2e604424f");
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.MODEL_CLASS, "Relational");
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.BUILT_IN, Boolean.FALSE.toString());
                assertPropertyValue(physicalModel, VdbSchema.PropertyId.INDEX_NAME, "4286983813.INDEX");
            } else if (isExtendedType(derivedArtifact, VdbValidationError.ARTIFACT_TYPE)) {
                final String path = derivedArtifact.getName();

                if (!foundError1 && VdbValidationError.ROOT_PATH.equals(path)) {
                    foundError1 = true;
                    validationError1 = derivedArtifact;
                    assertPropertyValue(validationError1, VdbValidationError.PropertyId.SEVERITY, "WARNING");
                    assertPropertyValue(validationError1, VdbValidationError.PropertyId.MESSAGE, "This is a warning message");
                } else if ((!foundError2 || !foundError3) && "EMPLOYEE/ID".equals(path)) {
                    if (!foundError2) {
                        foundError2 = true;
                        validationError2 = derivedArtifact;
                        assertPropertyValue(validationError2, VdbValidationError.PropertyId.SEVERITY, "ERROR");
                        assertPropertyValue(validationError2, VdbValidationError.PropertyId.MESSAGE, "Error 1");
                    } else if (!foundError3) {
                        foundError3 = true;
                        validationError3 = derivedArtifact;
                        assertPropertyValue(validationError3, VdbValidationError.PropertyId.SEVERITY, "ERROR");
                        assertPropertyValue(validationError3, VdbValidationError.PropertyId.MESSAGE, "Error 2");
                    }
                } else if (!foundError4 && "DEPARTMENT/NAME".equals(path)) {
                    foundError4 = true;
                    validationError4 = derivedArtifact;
                    assertPropertyValue(validationError4, VdbValidationError.PropertyId.SEVERITY, "ERROR");
                    assertPropertyValue(validationError4, VdbValidationError.PropertyId.MESSAGE, "This is an error message");
                } else {
                    fail("unexpected validation error artifact '" + derivedArtifact.getName() + '\'');
                }
            } else if (isExtendedType(derivedArtifact, VdbSchemaSource.ARTIFACT_TYPE)) {
                source = derivedArtifact;
                assertThat(source.getName(), is("ProductsSS"));
                assertPropertyValue(source, VdbSchemaSource.PropertyId.TRANSLATOR_NAME, "sqlserver");
                assertPropertyValue(source, VdbSchemaSource.PropertyId.JNDI_NAME, "ProductsSS");
            } else {
                fail("unexpected artifact '" + derivedArtifact.getName() + '\'');
            }
        }

        assertThat(("foundError1=" + foundError1 + ", foundError2=" + foundError2 + ", foundError3=" + foundError3
                    + ", foundError4=" + foundError4), (foundError1 && foundError2 && foundError3 && foundError4), is(true));

        // relationships
        assertRelationshipTargetUuid(validationError1, VdbValidationError.SOURCE_RELATIONSHIP, physicalModel.getUuid());
        assertRelationshipTargetUuid(physicalModel, VdbSchema.VALIDATION_ERRORS_RELATIONSHIP, validationError1.getUuid());

        assertRelationshipTargetUuid(validationError2, VdbValidationError.SOURCE_RELATIONSHIP, physicalModel.getUuid());
        assertRelationshipTargetUuid(physicalModel, VdbSchema.VALIDATION_ERRORS_RELATIONSHIP, validationError2.getUuid());

        assertRelationshipTargetUuid(validationError3, VdbValidationError.SOURCE_RELATIONSHIP, physicalModel.getUuid());
        assertRelationshipTargetUuid(physicalModel, VdbSchema.VALIDATION_ERRORS_RELATIONSHIP, validationError3.getUuid());

        assertRelationshipTargetUuid(validationError4, VdbValidationError.SOURCE_RELATIONSHIP, physicalModel.getUuid());
        assertRelationshipTargetUuid(physicalModel, VdbSchema.VALIDATION_ERRORS_RELATIONSHIP, validationError4.getUuid());

        assertRelationshipTargetUuid(physicalModel, VdbSchema.SOURCES_RELATIONSHIP, source.getUuid());
        assertRelationshipTargetUuid(source, VdbSchemaSource.SCHEMA_RELATIONSHIP, physicalModel.getUuid());
    }

    @Test
    public void shouldDeriveTwitterVdbArtifacts() throws Exception {
        final InputStream vdbStream = getResourceAsStream("twitterVdb.xml");
        assertThat(vdbStream, is(not(nullValue())));

        // deriver framework will call derive and link methods
        final Collection<BaseArtifactType> derivedArtifacts = this.deriver.derive(this.vdbManifestArtifact, vdbStream);
        this.deriver.link(null, this.vdbManifestArtifact, derivedArtifacts);

        assertThat(this.vdbManifestArtifact.getName(), is("twitter"));
        assertThat(this.vdbManifestArtifact.getDescription(), is("Shows how to call Web Services"));
        assertThat(this.vdbManifestArtifact.getVersion(), is("1"));
        assertPropertyValue(this.vdbManifestArtifact, VdbManifest.PropertyId.VERSION, "1");
        assertPropertyValue(this.vdbManifestArtifact, VdbManifest.PropertyId.USE_CONNECTOR_METADATA, "cached");
        assertThat(derivedArtifacts.size(), is(4));

        // verify derived artifacts
        boolean foundTranslator = false;
        boolean foundPhysicalModel = false;
        boolean foundViewModel = false;
        boolean foundDataSource = false;

        BaseArtifactType sourceArtifact = null;
        BaseArtifactType translatorArtifact = null;

        for (final BaseArtifactType derivedArtifact : derivedArtifacts) {
            final String artifactName = derivedArtifact.getName();

            if (!foundTranslator && isExtendedType(derivedArtifact, VdbTranslator.ARTIFACT_TYPE)) {
                foundTranslator = true;
                translatorArtifact = derivedArtifact;

                assertThat(artifactName, is("rest"));
                assertPropertyValue(derivedArtifact, VdbTranslator.PropertyId.TYPE, "ws");
                assertPropertyValue(derivedArtifact, "DefaultBinding", "HTTP");
                assertPropertyValue(derivedArtifact, "DefaultServiceMode", "MESSAGE");
                assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                             VdbManifest.CONTAINS_RELATIONSHIP,
                                             derivedArtifact.getUuid());
            } else if (isExtendedType(derivedArtifact, VdbSchema.ARTIFACT_TYPE)) {
                if (!foundPhysicalModel && "twitter".equals(artifactName)) {
                    foundPhysicalModel = true;
                    assertPropertyValue(derivedArtifact, VdbSchema.PropertyId.TYPE, VdbSchema.Type.PHYSICAL.name());
                    // assertPropertyValue(derivedArtifact, VdbSchema.PropertyId.VALIDATION_ERRORS, Boolean.FALSE.toString());
                    assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                                 VdbManifest.CONTAINS_RELATIONSHIP,
                                                 derivedArtifact.getUuid());
                } else if (!foundViewModel && "twitterview".equals(artifactName)) {
                    foundViewModel = true;
                    assertPropertyValue(derivedArtifact, VdbSchema.PropertyId.TYPE, VdbSchema.Type.VIRTUAL.name());
                    assertPropertyValue(derivedArtifact,
                                        VdbSchema.PropertyId.METADATA_TYPE,
                                        VdbSchema.DEFAULT_METADATA_TYPE.name());
                    // assertPropertyValue(derivedArtifact, VdbSchema.PropertyId.VALIDATION_ERRORS, Boolean.FALSE.toString());
                    final String expected = "\n             CREATE VIRTUAL PROCEDURE getTweets(query varchar) RETURNS (created_on varchar(25), from_user varchar(25), to_user varchar(25),\n"
                                            + "                 profile_image_url varchar(25), source varchar(25), text varchar(140)) AS\n"
                                            + "                select tweet.* from\n"
                                            + "                    (call twitter.invokeHTTP(action => 'GET', endpoint =>querystring('',query as \"q\"))) w,\n"
                                            + "                    XMLTABLE('results' passing JSONTOXML('myxml', w.result) columns\n"
                                            + "                    created_on string PATH 'created_at',\n"
                                            + "                    from_user string PATH 'from_user',\n"
                                            + "                    to_user string PATH 'to_user',\n"
                                            + "                    profile_image_url string PATH 'profile_image_url',\n"
                                            + "                    source string PATH 'source',\n"
                                            + "                    text string PATH 'text') tweet;\n"
                                            + "                CREATE VIEW Tweet AS select * FROM twitterview.getTweets;\n"
                                            + "         ";
                    assertPropertyValue(derivedArtifact, VdbSchema.PropertyId.METADATA, expected);
                    assertRelationshipTargetUuid(this.vdbManifestArtifact,
                                                 VdbManifest.CONTAINS_RELATIONSHIP,
                                                 derivedArtifact.getUuid());
                } else {
                    fail("unexpected schema artifact '" + artifactName + '\'');
                }
            } else if (!foundDataSource && isExtendedType(derivedArtifact, VdbSchemaSource.ARTIFACT_TYPE)) {
                foundDataSource = true;
                sourceArtifact = derivedArtifact;
                assertThat(artifactName, is("twitter"));
                assertPropertyValue(derivedArtifact, VdbSchemaSource.PropertyId.TRANSLATOR_NAME, "rest");
                assertPropertyValue(derivedArtifact, VdbSchemaSource.PropertyId.JNDI_NAME, "java:/twitterDS");
            } else {
                fail("unexpected artifact '" + artifactName + +'\'');
            }
        }

        assertThat((foundTranslator && foundPhysicalModel && foundViewModel && foundDataSource), is(true));
        assertRelationshipTargetUuid(sourceArtifact, VdbSchemaSource.TRANSLATOR_RELATIONSHIP, translatorArtifact.getUuid());
        assertRelationshipTargetUuid(translatorArtifact, VdbTranslator.SOURCES_RELATIONSHIP, sourceArtifact.getUuid());
    }

    @Test( expected = IOException.class )
    public void shouldNotDeriveNonVdbManifests() throws Exception {
        final InputStream notAVdbManifestStream = getResourceAsStream("Books_Oracle.xmi");
        assertThat(notAVdbManifestStream, is(not(nullValue())));

        // deriver framework will call derive
        this.deriver.derive(this.vdbManifestArtifact, notAVdbManifestStream);
    }

}
