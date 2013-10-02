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
import java.util.Collection;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.overlord.sramp.integration.teiid.Messages;
import org.overlord.sramp.integration.teiid.Utils;
import org.overlord.sramp.integration.teiid.model.Describable.XmlId;
import org.overlord.sramp.integration.teiid.model.TeiidArtifactType;
import org.overlord.sramp.integration.teiid.model.TeiidModel;
import org.overlord.sramp.integration.teiid.model.TeiidModelObject.PropertyId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * A deriver for Teiid <coded>*.xmi</code> model files.
 */
public final class ModelDeriver extends AbstractXmlDeriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelDeriver.class);

    private StaticNamespaceContext namespaceContext;

    /**
     * {@inheritDoc}
     * 
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#configureNamespaceMappings(org.overlord.sramp.common.query.xpath.StaticNamespaceContext)
     */
    @Override
    protected void configureNamespaceMappings( final StaticNamespaceContext namespaceContext ) {
        super.configureNamespaceMappings(namespaceContext);
        this.namespaceContext = namespaceContext;
    }

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
        LOGGER.debug("ModelDeriver:root element='{}' of artifact '{}'", rootElement.getLocalName(), artifact.getName()); //$NON-NLS-1$

        // make sure Teiid model
        if (!(artifact instanceof ExtendedDocument)
            || !TeiidArtifactType.MODEL.extendedType().equals(((ExtendedDocument)artifact).getExtendedType())) {
            throw new IllegalArgumentException(Messages.I18N.format("notModelArtifact", artifact.getName())); //$NON-NLS-1$
        }

        final ExtendedDocument modelArtifact = (ExtendedDocument)artifact;

        try {
            // root element should be the XMI element
            if (!TeiidModel.XmiId.ROOT_ELEMENT.equals(rootElement.getLocalName())) {
                throw new IllegalArgumentException(Messages.I18N.format("missingModelRootElement", artifact.getName())); //$NON-NLS-1$
            }

            processNamespaces(rootElement);
            processModelAnnotation(derivedArtifacts, modelArtifact, rootElement, xpath);
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
        // nothing to do
    }

    private void processModelAnnotation( final Collection<BaseArtifactType> derivedArtifacts,
                                         final ExtendedDocument modelArtifact,
                                         final Element xmiElement,
                                         final XPath xpath ) throws Exception {
        final Element annotationElement = (Element)query(xpath,
                                                         xmiElement,
                                                         Utils.getElementQueryString(TeiidModel.XmiId.MODEL_ANNOTATION),
                                                         XPathConstants.NODE);

        if (annotationElement == null) {
            throw new IllegalArgumentException(Messages.I18N.format("missingModelAnnotationElement", modelArtifact.getName())); //$NON-NLS-1$
        }

        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.UUID, PropertyId.MMUID);
        setProperty(modelArtifact,
                    annotationElement,
                    TeiidModel.XmiId.PRIMARY_METAMODEL_URI,
                    TeiidModel.PropertyId.PRIMARY_METAMODEL_URI);
        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.MODEL_TYPE, TeiidModel.PropertyId.MODEL_TYPE);
        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.PRODUCER_NAME, TeiidModel.PropertyId.PRODUCER_NAME);
        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.PRODUCER_VERSION, TeiidModel.PropertyId.PRODUCER_VERSION);
        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.MAX_SET_SIZE, TeiidModel.PropertyId.MAX_SET_SIZE);
        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.NAME_IN_SOURCE, PropertyId.NAME_IN_SOURCE);
        setProperty(modelArtifact,
                    annotationElement,
                    XmlId.DESCRIPTION,
                    org.overlord.sramp.integration.teiid.model.Describable.PropertyId.DESCRIPTION);
        setProperty(modelArtifact, annotationElement, TeiidModel.XmiId.VISIBLE, TeiidModel.PropertyId.VISIBLE);
    }

    private void processNamespaces( final Element xmiElement ) {
        final NamedNodeMap attributes = xmiElement.getAttributes();

        for (int i = 0, numAttrs = attributes.getLength(); i < numAttrs; ++i) {
            final Node attr = attributes.item(i);

            if (TeiidModel.XmiId.XML_NAMESPACE.equals(attr.getPrefix())) {
                this.namespaceContext.addMapping(attr.getLocalName(), attr.getNodeValue());
                LOGGER.debug("ModelDeriver:adding namespace with prefix '{}' and URI '{}'", attr.getLocalName(), attr.getNodeValue()); //$NON-NLS-1$
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
        LOGGER.debug("ModelDeriver:executing query '{}'", query); //$NON-NLS-1$
        return super.query(xpath, context, query, returnType);
    }

    private void setProperty( final ExtendedDocument modelArtifact,
                              final Element element,
                              final String attributeName,
                              final String propertyName ) {
        final String propValue = element.getAttribute(attributeName);
        SrampModelUtils.setCustomProperty(modelArtifact, propertyName, propValue);
    }

}
