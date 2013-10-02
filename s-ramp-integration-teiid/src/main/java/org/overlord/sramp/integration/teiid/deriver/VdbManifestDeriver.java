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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Property;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.integration.teiid.Messages;
import org.overlord.sramp.integration.teiid.Utils;
import org.overlord.sramp.integration.teiid.model.Describable.XmlId;
import org.overlord.sramp.integration.teiid.model.Propertied;
import org.overlord.sramp.integration.teiid.model.TeiidExtendedType;
import org.overlord.sramp.integration.teiid.model.Vdb;
import org.overlord.sramp.integration.teiid.model.VdbDataPolicy;
import org.overlord.sramp.integration.teiid.model.VdbEntry;
import org.overlord.sramp.integration.teiid.model.VdbImport;
import org.overlord.sramp.integration.teiid.model.VdbManifest;
import org.overlord.sramp.integration.teiid.model.VdbPermission;
import org.overlord.sramp.integration.teiid.model.VdbSchema;
import org.overlord.sramp.integration.teiid.model.VdbSchemaSource;
import org.overlord.sramp.integration.teiid.model.VdbTranslator;
import org.overlord.sramp.integration.teiid.model.VdbValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A deriver that creates VDB artifacts.
 */
public final class VdbManifestDeriver extends AbstractXmlDeriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(VdbManifestDeriver.class);

    /**
     * The delimeter that separates mapped role names in the data policy property value. Value is {@value} .
     */
    public static final char ROLE_NAME_DELIMETER = ',';

    /**
     * @param extendedType the extended artifact type of the artifact to create (cannot be <code>null</code> or empty)
     * @return the created artifact (never <code>null</code>)
     */
    private static BaseArtifactType create( final TeiidExtendedType extendedType ) {
        final ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType(extendedType.extendedType());
        artifact.setUuid(UUID.randomUUID().toString());

        return artifact;
    }

    private final Collection<BaseArtifactType> sources = new ArrayList<BaseArtifactType>();
    private final Collection<BaseArtifactType> translators = new ArrayList<BaseArtifactType>();

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#derive(java.util.Collection,
     *      org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)
     */
    @Override
    protected void derive( final Collection<BaseArtifactType> derivedArtifacts,
                           final BaseArtifactType artifact,
                           final Element rootElement,
                           final XPath xpath ) throws IOException {
        LOGGER.debug("VdbManifestDeriver:root element='{}' of artifact '{}'", rootElement.getLocalName(), artifact.getName()); //$NON-NLS-1$

        this.sources.clear();
        this.translators.clear();

        // make sure Teiid VDB manifest
        if (!(artifact instanceof ExtendedDocument)
            || !VdbManifest.ARTIFACT_TYPE.extendedType().equals(((ExtendedDocument)artifact).getExtendedType())) {
            throw new IllegalArgumentException(Messages.I18N.format("notVdbArtifact", artifact.getName())); //$NON-NLS-1$
        }

        final ExtendedDocument vdbArtifact = (ExtendedDocument)artifact;

        try {
            // root element should be the VDB element
            if (!VdbManifest.ManifestId.VDB_ELEMENT.equals(rootElement.getLocalName())) {
                throw new IllegalArgumentException(Messages.I18N.format("missingVdbRootElement", artifact.getName())); //$NON-NLS-1$
            }

            processVdb(derivedArtifacts, vdbArtifact, rootElement, xpath);
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.common.derived.ArtifactDeriver#link(org.overlord.sramp.common.derived.LinkerContext,
     *      org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
     */
    @Override
    public void link( final LinkerContext context,
                      final BaseArtifactType vdbManifestArtifact,
                      final Collection<BaseArtifactType> derivedArtifacts ) {
        // setup relationships between sources and translators
        if (!Utils.isEmpty(this.sources) && !Utils.isEmpty(this.translators)) {
            for (final BaseArtifactType sourceArtifact : this.sources) {
                final String translatorName = SrampModelUtils.getCustomProperty(sourceArtifact,
                                                                                VdbSchemaSource.PropertyId.TRANSLATOR_NAME);

                if (!Utils.isEmpty(translatorName)) {
                    for (final BaseArtifactType translatorArtifact : this.translators) {
                        if (translatorName.equals(translatorArtifact.getName())) {
                            LOGGER.debug("VdbManifestDeriver:adding relationships between source '{}' and translator '{}'", //$NON-NLS-1$
                                         sourceArtifact.getName(),
                                         translatorArtifact.getName());
                            Utils.addTwoWayRelationship(sourceArtifact,
                                                        translatorArtifact,
                                                        VdbSchemaSource.TRANSLATOR_RELATIONSHIP,
                                                        VdbTranslator.SOURCES_RELATIONSHIP);
                        }
                    }
                }
            }
        }
    }

    private void processDataRoles( final Collection<BaseArtifactType> derivedArtifacts,
                                   final ExtendedDocument vdbArtifact,
                                   final Element vdb,
                                   final XPath xpath ) throws Exception {
        final NodeList dataPolicies = (NodeList)query(xpath,
                                                      vdb,
                                                      Utils.getElementQueryString(VdbManifest.ManifestId.DATA_POLICY),
                                                      XPathConstants.NODESET);

        if (dataPolicies.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' data policies", dataPolicies.getLength()); //$NON-NLS-1$

            for (int dataPolicyIndex = 0, numDataPolicies = dataPolicies.getLength(); dataPolicyIndex < numDataPolicies; ++dataPolicyIndex) {
                final Element dataPolicy = (Element)dataPolicies.item(dataPolicyIndex);
                final BaseArtifactType dataPolicyArtifact = VdbManifestDeriver.create(VdbDataPolicy.ARTIFACT_TYPE);
                derivedArtifacts.add(dataPolicyArtifact);

                { // name
                    final String name = dataPolicy.getAttribute(VdbDataPolicy.ManifestId.NAME);
                    dataPolicyArtifact.setName(name);
                }

                { // any authenticated
                    final String anyAuthenticated = dataPolicy.getAttribute(VdbDataPolicy.ManifestId.ANY_AUTHENTICATED);
                    SrampModelUtils.setCustomProperty(dataPolicyArtifact,
                                                      VdbDataPolicy.PropertyId.ANY_AUTHENTICATED,
                                                      anyAuthenticated);
                }

                { // create temp tables
                    final String creatable = dataPolicy.getAttribute(VdbDataPolicy.ManifestId.TEMP_TABLE_CREATABLE);
                    SrampModelUtils.setCustomProperty(dataPolicyArtifact,
                                                      VdbDataPolicy.PropertyId.TEMP_TABLE_CREATABLE,
                                                      creatable);
                }

                // description
                setDescriptionFromElementValue(dataPolicy, XmlId.DESCRIPTION, dataPolicyArtifact, xpath);

                { // mapped role names
                    final NodeList roleNames = (NodeList)query(xpath,
                                                               dataPolicy,
                                                               Utils.getElementQueryString(VdbDataPolicy.ManifestId.ROLE_NAME),
                                                               XPathConstants.NODESET);

                    if (roleNames.getLength() != 0) {
                        LOGGER.debug("VdbManifestDeriver:processing '{}' mapped role names for data policy '{}'", //$NON-NLS-1$
                                     roleNames.getLength(),
                                     dataPolicyArtifact.getName());

                        // combine role names into one string
                        final StringBuilder mappedNames = new StringBuilder();

                        for (int roleNameIndex = 0, numRoleNames = roleNames.getLength(); roleNameIndex < numRoleNames; ++roleNameIndex) {
                            if (roleNameIndex != 0) {
                                mappedNames.append(ROLE_NAME_DELIMETER);
                            }

                            final Element roleName = (Element)roleNames.item(roleNameIndex);
                            final String name = roleName.getTextContent();
                            mappedNames.append(name);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("VdbManifestDeriver:found mapped role name '{}' for data policy '{}'", dataPolicyArtifact.getName()); //$NON-NLS-1$
                            }
                        }

                        SrampModelUtils.setCustomProperty(dataPolicyArtifact,
                                                          VdbDataPolicy.PropertyId.ROLE_NAMES,
                                                          mappedNames.toString());
                    }
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:data policy name '{}'", dataPolicyArtifact.getName()); //$NON-NLS-1$
                    LOGGER.debug("VdbManifestDeriver:data policy description '{}'", dataPolicyArtifact.getDescription()); //$NON-NLS-1$                  

                    for (final Property prop : dataPolicyArtifact.getProperty()) {
                        LOGGER.debug("VdbManifestDeriver:data policy property '{}' with value '{}'", //$NON-NLS-1$
                                     prop.getPropertyName(),
                                     prop.getPropertyValue());
                    }
                }

                { // permissions
                    final NodeList permissions = (NodeList)query(xpath,
                                                                 dataPolicy,
                                                                 Utils.getElementQueryString(VdbDataPolicy.ManifestId.PERMISSION),
                                                                 XPathConstants.NODESET);

                    if (permissions.getLength() != 0) {
                        LOGGER.debug("VdbManifestDeriver:processing '{}' data permissions for data policy '{}'", //$NON-NLS-1$
                                     permissions.getLength(),
                                     dataPolicyArtifact.getName());

                        for (int permissionIndex = 0, numPermissions = permissions.getLength(); permissionIndex < numPermissions; ++permissionIndex) {
                            final Element permission = (Element)permissions.item(permissionIndex);
                            final BaseArtifactType permissionArtifact = VdbManifestDeriver.create(VdbPermission.ARTIFACT_TYPE);
                            derivedArtifacts.add(permissionArtifact);

                            { // resource name
                                final Element element = (Element)query(xpath,
                                                                       permission,
                                                                       Utils.getElementQueryString(VdbPermission.ManifestId.RESOURCE_NAME),
                                                                       XPathConstants.NODE);
                                final String resourceName = element.getTextContent();
                                permissionArtifact.setName(resourceName);
                            }

                            // alterable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.ALTERABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.ALTERABLE,
                                                        xpath);

                            // condition
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.CONDITION,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.CONDITION,
                                                        xpath);

                            // creatable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.CREATABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.CREATABLE,
                                                        xpath);

                            // deletable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.DELETABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.DELETABLE,
                                                        xpath);

                            // executable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.EXECUTABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.EXECUTABLE,
                                                        xpath);

                            // languagable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.LANGUAGABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.LANGUAGABLE,
                                                        xpath);

                            // mask
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.MASK,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.MASK,
                                                        xpath);

                            // readable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.READABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.READABLE,
                                                        xpath);

                            // updatable
                            setPropertyFromElementValue(permission,
                                                        VdbPermission.ManifestId.UPDATABLE,
                                                        permissionArtifact,
                                                        VdbPermission.PropertyId.UPDATABLE,
                                                        xpath);

                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("VdbManifestDeriver:permission resource name '{}'", permissionArtifact.getName()); //$NON-NLS-1$

                                // properties
                                for (final Property prop : permissionArtifact.getProperty()) {
                                    LOGGER.debug("VdbManifestDeriver:Source property '{}' with value '{}'", //$NON-NLS-1$
                                                 prop.getPropertyName(),
                                                 prop.getPropertyValue());
                                }
                            }

                            // add the relationships
                            Utils.addTwoWayRelationship(dataPolicyArtifact,
                                                        permissionArtifact,
                                                        VdbDataPolicy.PERMISSIONS_RELATIONSHIP,
                                                        VdbPermission.DATA_POLICY_RELATIONSHIP);
                        }
                    }
                }

                // add the relationship from VDB to data policy (inverse is created by deriver framework)
                Utils.addRelationship(vdbArtifact, dataPolicyArtifact, VdbManifest.CONTAINS_RELATIONSHIP);
            }
        }
    }

    private void processEntries( final Collection<BaseArtifactType> derivedArtifacts,
                                 final ExtendedDocument vdbArtifact,
                                 final Element vdb,
                                 final XPath xpath ) throws Exception {
        final NodeList entries = (NodeList)query(xpath,
                                                 vdb,
                                                 Utils.getElementQueryString(VdbManifest.ManifestId.ENTRY),
                                                 XPathConstants.NODESET);

        if (entries.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' entries", entries.getLength()); //$NON-NLS-1$

            for (int entryIndex = 0, numEntries = entries.getLength(); entryIndex < numEntries; ++entryIndex) {
                final Element entry = (Element)entries.item(entryIndex);
                final BaseArtifactType entryArtifact = VdbManifestDeriver.create(VdbEntry.ARTIFACT_TYPE);
                derivedArtifacts.add(entryArtifact);

                { // name
                    final String path = entry.getAttribute(VdbEntry.ManifestId.PATH);
                    entryArtifact.setName(path);
                }

                // description
                setDescriptionFromElementValue(entry, XmlId.DESCRIPTION, entryArtifact, xpath);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:entry path '{}'", entryArtifact.getName()); //$NON-NLS-1$
                    LOGGER.debug("VdbManifestDeriver:entry description '{}'", entryArtifact.getDescription()); //$NON-NLS-1$
                }

                // properties
                processProperties(entryArtifact, entry, xpath);

                // add the relationship from VDB to entry (inverse is created by deriver framework)
                Utils.addRelationship(vdbArtifact, entryArtifact, VdbManifest.CONTAINS_RELATIONSHIP);
            }
        }
    }

    private void processProperties( final BaseArtifactType artifact,
                                    final Element element,
                                    final XPath xpath ) throws Exception {
        final NodeList props = (NodeList)query(xpath,
                                               element,
                                               Utils.getElementQueryString(Propertied.XmlId.PROPERTY),
                                               XPathConstants.NODESET);

        if (props.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' properties", props.getLength()); //$NON-NLS-1$

            for (int propIndex = 0, numProps = props.getLength(); propIndex < numProps; ++propIndex) {
                final Element prop = (Element)props.item(propIndex);
                final String name = prop.getAttribute(Propertied.XmlId.NAME);
                final String value = prop.getAttribute(Propertied.XmlId.VALUE);
                SrampModelUtils.setCustomProperty(artifact, name, value);
            }

            if (LOGGER.isDebugEnabled()) {
                for (final Property prop : artifact.getProperty()) {
                    LOGGER.debug("VdbManifestDeriver:artifact '{}' has property '{}' with value '{}'", //$NON-NLS-1$
                                 new Object[] {artifact.getName(), prop.getPropertyName(), prop.getPropertyValue()});
                }
            }
        }
    }

    private void processSchemas( final Collection<BaseArtifactType> derivedArtifacts,
                                 final ExtendedDocument vdbArtifact,
                                 final Element vdb,
                                 final XPath xpath ) throws Exception {
        final NodeList schemas = (NodeList)query(xpath,
                                                 vdb,
                                                 Utils.getElementQueryString(VdbManifest.ManifestId.SCHEMA),
                                                 XPathConstants.NODESET);

        if (schemas.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' schemas", schemas.getLength()); //$NON-NLS-1$

            for (int schemaIndex = 0, numSchemas = schemas.getLength(); schemaIndex < numSchemas; ++schemaIndex) {
                final Element schema = (Element)schemas.item(schemaIndex);
                final BaseArtifactType schemaArtifact = VdbManifestDeriver.create(VdbSchema.ARTIFACT_TYPE);
                derivedArtifacts.add(schemaArtifact);

                { // name
                    final String name = schema.getAttribute(VdbSchema.ManifestId.NAME);
                    schemaArtifact.setName(name);
                }

                // description
                setDescriptionFromElementValue(schema, XmlId.DESCRIPTION, schemaArtifact, xpath);

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:schema name '{}'", schemaArtifact.getName()); //$NON-NLS-1$
                    LOGGER.debug("VdbManifestDeriver:schema description '{}'", schemaArtifact.getDescription()); //$NON-NLS-1$
                }

                { // visible
                    final String visible = schema.getAttribute(VdbSchema.ManifestId.VISIBLE);

                    if (!Utils.isEmpty(visible)) {
                        SrampModelUtils.setCustomProperty(schemaArtifact, VdbSchema.PropertyId.VISIBLE, visible);
                    }
                }

                { // path in VDB
                    final String path = schema.getAttribute(VdbSchema.ManifestId.PATH);

                    if (!Utils.isEmpty(path)) {
                        SrampModelUtils.setCustomProperty(schemaArtifact, VdbSchema.PropertyId.PATH_IN_VDB, path);
                    }
                }

                { // type
                    String type = schema.getAttribute(VdbSchema.ManifestId.TYPE);

                    if (Utils.isEmpty(type)) {
                        type = VdbSchema.DEFAULT_TYPE.name();
                    }

                    SrampModelUtils.setCustomProperty(schemaArtifact, VdbSchema.PropertyId.TYPE, type);
                }

                { // metadata
                    final Element element = setPropertyFromElementValue(schema,
                                                                        VdbSchema.ManifestId.METADATA,
                                                                        schemaArtifact,
                                                                        VdbSchema.PropertyId.METADATA,
                                                                        xpath);

                    if (element != null) {
                        final String metadataType = element.getAttribute(VdbSchema.ManifestId.METADATA_TYPE);
                        SrampModelUtils.setCustomProperty(schemaArtifact, VdbSchema.PropertyId.METADATA_TYPE, metadataType);
                    }
                }

                // properties
                processProperties(schemaArtifact, schema, xpath);

                // validation errors
                processSchemaValidationErrors(derivedArtifacts, schemaArtifact, schema, xpath);

                // sources
                processSchemaSources(derivedArtifacts, schemaArtifact, schema, xpath);

                // add the relationship from VDB to schema (inverse is created by deriver framework)
                Utils.addRelationship(vdbArtifact, schemaArtifact, VdbManifest.CONTAINS_RELATIONSHIP);
            }
        }
    }

    /**
     * @param derivedArtifacts
     * @param schemaArtifact
     * @param schema
     * @param xpath
     */
    private void processSchemaSources( final Collection<BaseArtifactType> derivedArtifacts,
                                       final BaseArtifactType schemaArtifact,
                                       final Element schema,
                                       final XPath xpath ) throws Exception {
        final NodeList sources = (NodeList)query(xpath,
                                                 schema,
                                                 Utils.getElementQueryString(VdbSchema.ManifestId.SOURCE),
                                                 XPathConstants.NODESET);

        if (sources.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' sources for schema '{}'", sources.getLength(), schemaArtifact.getName()); //$NON-NLS-1$

            for (int sourceIndex = 0, numSources = sources.getLength(); sourceIndex < numSources; ++sourceIndex) {
                final Element source = (Element)sources.item(sourceIndex);
                final BaseArtifactType sourceArtifact = VdbManifestDeriver.create(VdbSchemaSource.ARTIFACT_TYPE);
                derivedArtifacts.add(sourceArtifact);
                this.sources.add(sourceArtifact);

                { // name
                    final String name = source.getAttribute(VdbSchemaSource.ManifestId.NAME);
                    sourceArtifact.setName(name);
                }

                { // JNDI name
                    final String jndiName = source.getAttribute(VdbSchemaSource.ManifestId.JNDI_NAME);
                    SrampModelUtils.setCustomProperty(sourceArtifact, VdbSchemaSource.PropertyId.JNDI_NAME, jndiName);
                }

                { // translator
                    final String translatorName = source.getAttribute(VdbSchemaSource.ManifestId.TRANSLATOR_NAME);
                    SrampModelUtils.setCustomProperty(sourceArtifact, VdbSchemaSource.PropertyId.TRANSLATOR_NAME, translatorName);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:schema source name '{}'", sourceArtifact.getName()); //$NON-NLS-1$

                    // properties
                    for (final Property prop : sourceArtifact.getProperty()) {
                        LOGGER.debug("VdbManifestDeriver:Source property '{}' with value '{}'", //$NON-NLS-1$
                                     prop.getPropertyName(),
                                     prop.getPropertyValue());
                    }
                }

                // add the relationships
                Utils.addTwoWayRelationship(schemaArtifact,
                                            sourceArtifact,
                                            VdbSchema.SOURCES_RELATIONSHIP,
                                            VdbSchemaSource.SCHEMA_RELATIONSHIP);
            }
        }
    }

    private void processSchemaValidationErrors( final Collection<BaseArtifactType> derivedArtifacts,
                                                final BaseArtifactType schemaArtifact,
                                                final Element modelElement,
                                                final XPath xpath ) throws Exception {
        final NodeList errors = (NodeList)query(xpath,
                                                modelElement,
                                                Utils.getElementQueryString(VdbManifest.ManifestId.VALIDATION_ERROR),
                                                XPathConstants.NODESET);

        if (errors.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' validation errors for schema '{}'", errors.getLength(), schemaArtifact.getName()); //$NON-NLS-1$

            for (int i = 0, numErrors = errors.getLength(); i < numErrors; ++i) {
                final Element errorElement = (Element)errors.item(i);
                final BaseArtifactType errorArtifact = VdbManifestDeriver.create(VdbValidationError.ARTIFACT_TYPE);
                derivedArtifacts.add(errorArtifact);

                { // use path as the name (can have duplicates)
                    String path = errorElement.getAttribute(VdbValidationError.ManifestId.PATH);

                    if (Utils.isEmpty(path)) {
                        path = VdbValidationError.ROOT_PATH;
                    }

                    errorArtifact.setName(path);
                }

                { // severity
                    final String severity = errorElement.getAttribute(VdbValidationError.ManifestId.SEVERITY);
                    SrampModelUtils.setCustomProperty(errorArtifact, VdbValidationError.PropertyId.SEVERITY, severity);
                }

                { // message
                    final String message = errorElement.getTextContent();
                    SrampModelUtils.setCustomProperty(errorArtifact, VdbValidationError.PropertyId.MESSAGE, message);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:model artifact '{}' has validation error with severity '{}', path '{}', and message '{}'", //$NON-NLS-1$
                                 new Object[] {schemaArtifact.getName(),
                                     SrampModelUtils.getCustomProperty(errorArtifact, VdbValidationError.PropertyId.SEVERITY),
                                     errorArtifact.getName(),
                                     SrampModelUtils.getCustomProperty(errorArtifact, VdbValidationError.PropertyId.MESSAGE)});
                }

                // add the relationship from schema to validation error
                Utils.addTwoWayRelationship(schemaArtifact,
                                            errorArtifact,
                                            VdbSchema.VALIDATION_ERRORS_RELATIONSHIP,
                                            VdbValidationError.SOURCE_RELATIONSHIP);
            }
        }
    }

    private void processTranslators( final Collection<BaseArtifactType> derivedArtifacts,
                                     final ExtendedDocument vdbArtifact,
                                     final Element vdb,
                                     final XPath xpath ) throws Exception {
        final NodeList translators = (NodeList)query(xpath,
                                                     vdb,
                                                     Utils.getElementQueryString(VdbManifest.ManifestId.TRANSLATOR),
                                                     XPathConstants.NODESET);

        if (translators.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' translators", translators.getLength()); //$NON-NLS-1$

            for (int translatorIndex = 0, numTranslators = translators.getLength(); translatorIndex < numTranslators; ++translatorIndex) {
                final Element translator = (Element)translators.item(translatorIndex);
                final BaseArtifactType translatorArtifact = VdbManifestDeriver.create(VdbTranslator.ARTIFACT_TYPE);
                derivedArtifacts.add(translatorArtifact);
                this.translators.add(translatorArtifact);

                { // name
                    final String name = translator.getAttribute(VdbTranslator.ManifestId.NAME);
                    translatorArtifact.setName(name);
                }

                { // description
                    final String description = translator.getAttribute(XmlId.DESCRIPTION);
                    translatorArtifact.setDescription(description);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:translator name '{}'", translatorArtifact.getName()); //$NON-NLS-1$
                    LOGGER.debug("VdbManifestDeriver:translator description '{}'", translatorArtifact.getDescription()); //$NON-NLS-1$
                }

                { // type
                    final String type = translator.getAttribute(VdbTranslator.ManifestId.TYPE);
                    SrampModelUtils.setCustomProperty(translatorArtifact, VdbTranslator.PropertyId.TYPE, type);
                }

                // properties
                processProperties(translatorArtifact, translator, xpath);

                // add the relationship from VDB to translator (inverse is created by deriver framework)
                Utils.addRelationship(vdbArtifact, translatorArtifact, VdbManifest.CONTAINS_RELATIONSHIP);
            }
        }
    }

    private void processVdb( final Collection<BaseArtifactType> derivedArtifacts,
                             final ExtendedDocument vdbArtifact,
                             final Element vdb,
                             final XPath xpath ) throws Exception {
        { // name
            final String name = vdb.getAttribute(VdbManifest.ManifestId.NAME);
            vdbArtifact.setName(name);
        }

        // description
        setDescriptionFromElementValue(vdb, XmlId.DESCRIPTION, vdbArtifact, xpath);

        // version
        setVdbVersion(vdb, VdbManifest.ManifestId.VERSION, vdbArtifact, VdbManifest.PropertyId.VERSION);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("VdbManifestDeriver:VDB name '{}'", vdbArtifact.getName()); //$NON-NLS-1$
            LOGGER.debug("VdbManifestDeriver:VDB description '{}'", vdbArtifact.getDescription()); //$NON-NLS-1$
            LOGGER.debug("VdbManifestDeriver:VDB version '{}'", vdbArtifact.getVersion()); //$NON-NLS-1$
        }

        processProperties(vdbArtifact, vdb, xpath);

        // derive artifacts
        processVdbImports(derivedArtifacts, vdbArtifact, vdb, xpath);
        processTranslators(derivedArtifacts, vdbArtifact, vdb, xpath);
        processDataRoles(derivedArtifacts, vdbArtifact, vdb, xpath);
        processEntries(derivedArtifacts, vdbArtifact, vdb, xpath);
        processSchemas(derivedArtifacts, vdbArtifact, vdb, xpath);
    }

    private void processVdbImports( final Collection<BaseArtifactType> derivedArtifacts,
                                    final ExtendedDocument vdbArtifact,
                                    final Element vdb,
                                    final XPath xpath ) throws Exception {
        final NodeList vdbImports = (NodeList)query(xpath,
                                                    vdb,
                                                    Utils.getElementQueryString(VdbManifest.ManifestId.IMPORT_VDB),
                                                    XPathConstants.NODESET);

        if (vdbImports.getLength() != 0) {
            LOGGER.debug("VdbManifestDeriver:processing '{}' VDB imports", vdbImports.getLength()); //$NON-NLS-1$

            for (int vdbImportIndex = 0, numVdbImports = vdbImports.getLength(); vdbImportIndex < numVdbImports; ++vdbImportIndex) {
                final Element vdbImport = (Element)vdbImports.item(vdbImportIndex);
                final BaseArtifactType vdbImportArtifact = VdbManifestDeriver.create(VdbImport.ARTIFACT_TYPE);
                derivedArtifacts.add(vdbImportArtifact);

                { // name
                    final String name = vdbImport.getAttribute(VdbImport.ManifestId.NAME);
                    vdbImportArtifact.setName(name);
                }

                // version
                setVdbVersion(vdbImport, VdbImport.ManifestId.VERSION, vdbImportArtifact, VdbImport.PropertyId.VERSION);

                { // import data policies
                    String importDataPolicies = vdbImport.getAttribute(VdbImport.ManifestId.IMPORT_DATA_POLICIES);

                    if (Utils.isEmpty(importDataPolicies)) {
                        importDataPolicies = Boolean.toString(VdbImport.DEFAULT_IMPORT_DATA_POLICIES);
                    }

                    SrampModelUtils.setCustomProperty(vdbImportArtifact,
                                                      VdbImport.PropertyId.IMPORT_DATA_POLICIES,
                                                      importDataPolicies);
                }

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("VdbManifestDeriver:Import VDB name '{}'", vdbImportArtifact.getName()); //$NON-NLS-1$
                    LOGGER.debug("VdbManifestDeriver:Import VDB version '{}'", vdbImportArtifact.getVersion()); //$NON-NLS-1$

                    // properties
                    for (final Property prop : vdbImportArtifact.getProperty()) {
                        LOGGER.debug("VdbManifestDeriver:Import VDB property '{}' with value '{}'", //$NON-NLS-1$
                                     prop.getPropertyName(),
                                     prop.getPropertyValue());
                    }
                }

                // add the relationship from VDB to import VDB (inverse is created by deriver framework)
                Utils.addRelationship(vdbArtifact, vdbImportArtifact, VdbManifest.CONTAINS_RELATIONSHIP);
            }
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#query(javax.xml.xpath.XPath, org.w3c.dom.Element,
     *      java.lang.String, javax.xml.namespace.QName)
     */
    @Override
    protected Object query( final XPath xpath,
                            final Element context,
                            final String query,
                            final QName returnType ) throws XPathExpressionException {
        LOGGER.debug("VdbManifestDeriver:executing query '{}'", query); //$NON-NLS-1$
        return super.query(xpath, context, query, returnType);
    }

    private void setDescriptionFromElementValue( final Element parent,
                                                 final String elementName,
                                                 final BaseArtifactType artifact,
                                                 final XPath xpath ) throws Exception {
        final Element element = (Element)query(xpath, parent, Utils.getElementQueryString(elementName), XPathConstants.NODE);

        if (element != null) {
            final String description = element.getTextContent();
            artifact.setDescription(description);
        }
    }

    private Element setPropertyFromElementValue( final Element parent,
                                                 final String elementName,
                                                 final BaseArtifactType artifact,
                                                 final String propertyName,
                                                 final XPath xpath ) throws Exception {
        final Element element = (Element)query(xpath, parent, Utils.getElementQueryString(elementName), XPathConstants.NODE);

        if (element != null) {
            final String value = element.getTextContent();
            SrampModelUtils.setCustomProperty(artifact, propertyName, value);
        }

        return element;
    }

    private void setVdbVersion( final Element element,
                                final String attributeName,
                                final BaseArtifactType artifact,
                                final String propertyName ) throws Exception {
        String version = element.getAttribute(attributeName);

        if (Utils.isEmpty(version)) {
            version = Vdb.DEFAULT_VERSION;
        }

        SrampModelUtils.setCustomProperty(artifact, propertyName, version);

        // set artifact version if not already set
        if (Utils.isEmpty(artifact.getVersion())) {
            artifact.setVersion(version);
        }
    }

}
