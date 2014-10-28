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
package org.overlord.sramp.integration.teiid.artifactbuilder;

import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.artifactbuilder.AbstractXmlArtifactBuilder;
import org.overlord.sramp.common.artifactbuilder.ArtifactBuilder;
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
@Component(name = "Teiid Model Artifact Builder", immediate = true)
@Service(value = ArtifactBuilder.class)
public final class ModelArtifactBuilder extends AbstractXmlArtifactBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModelArtifactBuilder.class);

    @Override
    protected void derive() throws IOException {
        LOGGER.debug("ModelDeriver:root element='{}' of artifact '{}'", rootElement.getLocalName(), getPrimaryArtifact().getName()); //$NON-NLS-1$

        // make sure Teiid model
        if (!(getPrimaryArtifact() instanceof ExtendedDocument)
            || !TeiidArtifactType.MODEL.extendedType().equals(((ExtendedDocument)getPrimaryArtifact()).getExtendedType())) {
            throw new IllegalArgumentException(Messages.I18N.format("notModelArtifact", getPrimaryArtifact().getName())); //$NON-NLS-1$
        }

        try {
            // root element should be the XMI element
            if (!TeiidModel.XmiId.ROOT_ELEMENT.equals(rootElement.getLocalName())) {
                throw new IllegalArgumentException(Messages.I18N.format("missingModelRootElement", getPrimaryArtifact().getName())); //$NON-NLS-1$
            }

            processModelAnnotation();
        } catch (final Exception e) {
            throw new IOException(e);
        }
    }

    private void processModelAnnotation() throws Exception {
        final Element annotationElement = (Element)query(rootElement,
                                                         Utils.getElementQueryString(TeiidModel.XmiId.MODEL_ANNOTATION),
                                                         XPathConstants.NODE);

        if (annotationElement == null) {
            throw new IllegalArgumentException(Messages.I18N.format("missingModelAnnotationElement", getPrimaryArtifact().getName())); //$NON-NLS-1$
        }

        setProperty(annotationElement, TeiidModel.XmiId.UUID, PropertyId.MMUID);
        setProperty(annotationElement,
                    TeiidModel.XmiId.PRIMARY_METAMODEL_URI,
                    TeiidModel.PropertyId.PRIMARY_METAMODEL_URI);
        setProperty(annotationElement, TeiidModel.XmiId.MODEL_TYPE, TeiidModel.PropertyId.MODEL_TYPE);
        setProperty(annotationElement, TeiidModel.XmiId.PRODUCER_NAME, TeiidModel.PropertyId.PRODUCER_NAME);
        setProperty(annotationElement, TeiidModel.XmiId.PRODUCER_VERSION, TeiidModel.PropertyId.PRODUCER_VERSION);
        setProperty(annotationElement, TeiidModel.XmiId.MAX_SET_SIZE, TeiidModel.PropertyId.MAX_SET_SIZE);
        setProperty(annotationElement, TeiidModel.XmiId.NAME_IN_SOURCE, PropertyId.NAME_IN_SOURCE);
        setProperty(annotationElement,
                    XmlId.DESCRIPTION,
                    org.overlord.sramp.integration.teiid.model.Describable.PropertyId.DESCRIPTION);
        setProperty(annotationElement, TeiidModel.XmiId.VISIBLE, TeiidModel.PropertyId.VISIBLE);
    }

    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);

        final NamedNodeMap attributes = rootElement.getAttributes();

        for (int i = 0, numAttrs = attributes.getLength(); i < numAttrs; ++i) {
            final Node attr = attributes.item(i);

            if (TeiidModel.XmiId.XML_NAMESPACE.equals(attr.getPrefix())) {
                namespaceContext.addMapping(attr.getLocalName(), attr.getNodeValue());
                LOGGER.debug("ModelDeriver:adding namespace with prefix '{}' and URI '{}'", attr.getLocalName(), attr.getNodeValue()); //$NON-NLS-1$
            }
        }
    }

    @Override
    protected Object query( final Element context,
                            final String query,
                            final QName returnType ) throws XPathExpressionException {
        LOGGER.debug("ModelDeriver:executing query '{}'", query); //$NON-NLS-1$
        return super.query(context, query, returnType);
    }

    private void setProperty( final Element element,
                              final String attributeName,
                              final String propertyName ) {
        final String propValue = element.getAttribute(attributeName);
        SrampModelUtils.setCustomProperty(getPrimaryArtifact(), propertyName, propValue);
    }

}
