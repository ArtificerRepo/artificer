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
package org.overlord.sramp.common.artifactbuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocumentEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocumentTarget;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * @author Brett Meyer
 */
public class XsdDocumentArtifactBuilder extends AbstractXmlArtifactBuilder {
    
    protected IndexedArtifactCollection derivedArtifacts = new IndexedArtifactCollection();

    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);

        namespaceContext.addMapping("xs", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
        namespaceContext.addMapping("xsd", "http://www.w3.org/2001/XMLSchema"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected void derive() throws IOException {
        try {
            String targetNS = rootElement.getAttribute("targetNamespace"); //$NON-NLS-1$
            
            if (getPrimaryArtifact() instanceof XsdDocument) {
                ((XsdDocument) getPrimaryArtifact()).setTargetNamespace(targetNS);
            }

            deriveXsd(rootElement);
            processXsdImports(rootElement, targetNS);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
    
    protected void deriveXsd(Element schema) throws XPathExpressionException {
        String targetNS = schema.getAttribute("targetNamespace"); //$NON-NLS-1$

        processElementDeclarations(schema, targetNS);
        processAttributeDeclarations(schema, targetNS);
        processSimpleTypeDeclarations(schema, targetNS);
        processComplexTypeDeclarations(schema, targetNS);
    }

    private void processElementDeclarations(Element schema, String targetNS) throws XPathExpressionException {
        // xpath expression to find all global element decls
        NodeList nodes = (NodeList) query(schema, "./xsd:element", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (node.hasAttribute("name")) { //$NON-NLS-1$
                String nsName = node.getAttribute("name"); //$NON-NLS-1$
                ElementDeclaration elementDecl = new ElementDeclaration();
                elementDecl.setArtifactType(BaseArtifactEnum.ELEMENT_DECLARATION);
                elementDecl.setName(nsName);
                elementDecl.setNamespace(targetNS);
                elementDecl.setNCName(nsName);
                derivedArtifacts.add(elementDecl);
            }
        }
    }

    private void processAttributeDeclarations(Element schema, String targetNS) throws XPathExpressionException {
        // xpath expression to find all global attribute decls
        NodeList nodes = (NodeList) query(schema, "./xsd:attribute", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (node.hasAttribute("name")) { //$NON-NLS-1$
                String nsName = node.getAttribute("name"); //$NON-NLS-1$
                AttributeDeclaration attributeDecl = new AttributeDeclaration();
                attributeDecl.setArtifactType(BaseArtifactEnum.ATTRIBUTE_DECLARATION);
                attributeDecl.setName(nsName);
                attributeDecl.setNamespace(targetNS);
                attributeDecl.setNCName(nsName);
                derivedArtifacts.add(attributeDecl);
            }
        }
    }

    private void processSimpleTypeDeclarations(Element schema, String targetNS) throws XPathExpressionException {
        // xpath expression to find all global simple type decls
        NodeList nodes = (NodeList) query(schema, "./xsd:simpleType", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (node.hasAttribute("name")) { //$NON-NLS-1$
                String nsName = node.getAttribute("name"); //$NON-NLS-1$
                SimpleTypeDeclaration simpleTypeDecl = new SimpleTypeDeclaration();
                simpleTypeDecl.setArtifactType(BaseArtifactEnum.SIMPLE_TYPE_DECLARATION);
                simpleTypeDecl.setName(nsName);
                simpleTypeDecl.setNamespace(targetNS);
                simpleTypeDecl.setNCName(nsName);
                derivedArtifacts.add(simpleTypeDecl);
            }
        }
    }

    private void processComplexTypeDeclarations(Element schema, String targetNS) throws XPathExpressionException {
        // xpath expression to find all global complex type decls
        NodeList nodes = (NodeList) query(schema, "./xsd:complexType", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (node.hasAttribute("name")) { //$NON-NLS-1$
                String nsName = node.getAttribute("name"); //$NON-NLS-1$
                ComplexTypeDeclaration complexTypeDecl = new ComplexTypeDeclaration();
                complexTypeDecl.setArtifactType(BaseArtifactEnum.COMPLEX_TYPE_DECLARATION);
                complexTypeDecl.setName(nsName);
                complexTypeDecl.setNamespace(targetNS);
                complexTypeDecl.setNCName(nsName);
                derivedArtifacts.add(complexTypeDecl);
            }
        }
    }
    
    private void processXsdImports(Element schema, String targetNS) throws XPathExpressionException {
        if (getPrimaryArtifact() instanceof XsdDocument) {
            processXsdImports(((XsdDocument) getPrimaryArtifact()).getImportedXsds(), schema, targetNS);
        }
    }
    
    protected void processXsdImports(List<XsdDocumentTarget> targetCollection, Element schema, String targetNS) throws XPathExpressionException {
        NodeList nodes = (NodeList) query(schema, "./xsd:import", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (node.hasAttribute("namespace")) { //$NON-NLS-1$
                String namespace = node.getAttribute("namespace");
                XsdDocument xsdDocumentRef = derivedArtifacts.lookupXsdDocument(namespace);
                XsdDocumentTarget xsdDocumentTarget = new XsdDocumentTarget();
                xsdDocumentTarget.setArtifactType(XsdDocumentEnum.XSD_DOCUMENT);
                if (xsdDocumentRef != null) {
                    xsdDocumentTarget.setValue(xsdDocumentRef.getUuid());
                } else {
                    relationshipSources.add(new NamespaceRelationshipSource(namespace, xsdDocumentTarget, targetCollection,
                            ArtifactTypeEnum.XsdDocument.getModel(), ArtifactTypeEnum.XsdDocument.getType()));
                }
                targetCollection.add(xsdDocumentTarget);
            }
        }
    }
    
    @Override
    public Collection<BaseArtifactType> getDerivedArtifacts() {
        return derivedArtifacts;
    }
    
}
