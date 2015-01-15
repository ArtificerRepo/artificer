/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.integration.artifactbuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DerivedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.DocumentArtifactTarget;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XmlDocument;
import org.overlord.sramp.common.ArtifactContent;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Provides the basis for an {@link ArtifactBuilder} responsible for an XML artifact.  Sets up commonly-used namespaces,
 * initializes XPath, and generates "relatedDocument" relationships.
 * 
 * @author Brett Meyer
 */
public class XmlArtifactBuilder extends AbstractArtifactBuilder {
    
    protected final List<RelationshipSource> relationshipSources = new ArrayList<RelationshipSource>();
    
    protected InputStream contentStream;
    
    protected Element rootElement;
    
    protected XPath xpath;

    @Override
    public ArtifactBuilder buildArtifacts(BaseArtifactType primaryArtifact, ArtifactContent artifactContent) throws IOException {
        super.buildArtifacts(primaryArtifact, artifactContent);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false); //$NON-NLS-1$
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(getContentStream());
            // This *must* be setup prior to calling #configureNamespaceMappings.  Most subclasses will need it.
            rootElement = document.getDocumentElement();
            
            if (primaryArtifact instanceof XmlDocument) {
                String encoding = document.getXmlEncoding();
                if (StringUtils.isBlank(encoding)) {
                    encoding = "UTF-8";
                }
                ((XmlDocument) primaryArtifact).setContentEncoding(encoding);
            }
            
            XPathFactory xPathfactory = XPathFactory.newInstance();
            xpath = xPathfactory.newXPath();
            StaticNamespaceContext nsCtx = new StaticNamespaceContext();
            configureNamespaceMappings(nsCtx);
            xpath.setNamespaceContext(nsCtx);
            
            // Create all derived artifacts
            derive();

            // Set the relatedDocument relationship for all derived artifacts
            for (BaseArtifactType derivedArtifact : getDerivedArtifacts()) {
                if (derivedArtifact instanceof DerivedArtifactType) {
                    DerivedArtifactType dat = (DerivedArtifactType) derivedArtifact;
                    if (dat.getRelatedDocument() == null) {
                        DocumentArtifactTarget related = new DocumentArtifactTarget();
                        related.setValue(primaryArtifact.getUuid());
                        related.setArtifactType(DocumentArtifactEnum.fromValue(primaryArtifact.getArtifactType()));
                        dat.setRelatedDocument(related);
                    }
                } else {
                    Relationship genericRelationship = SrampModelUtils.getGenericRelationship(derivedArtifact, "relatedDocument"); //$NON-NLS-1$
                    if (genericRelationship == null) {
                        SrampModelUtils.addGenericRelationship(derivedArtifact, "relatedDocument", primaryArtifact.getUuid()); //$NON-NLS-1$
                    }
                }
            }
            
            return this;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    @Override
    public ArtifactBuilder buildRelationships(RelationshipContext context) throws IOException {
        for (RelationshipSource relationshipSource : relationshipSources) {
            relationshipSource.build(context);
        }
        return this;
    }
    
    protected void derive() throws IOException {
        
    }
    
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
    }

    protected Object query(Element element, String query, QName returnType) throws XPathExpressionException {
        XPathExpression expr = xpath.compile(query);
        return expr.evaluate(element, returnType);
    }
}
