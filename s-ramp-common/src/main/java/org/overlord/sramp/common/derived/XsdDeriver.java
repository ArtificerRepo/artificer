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
package org.overlord.sramp.common.derived;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.AttributeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ComplexTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ElementDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.SimpleTypeDeclaration;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.XsdDocument;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates derived content from an XSD document.  This will create the derived content as
 * defined in the XML Schema model found in the s-ramp specification.  The following derived
 * artifact types will (potentially) be created:
 *
 * <ul>
 *   <li>AttributeDeclaration</li>
 *   <li>ElementDeclaration</li>
 *   <li>ComplexTypeDeclaration</li>
 *   <li>SimpleTypeDeclaration</li>
 * </ul>
 *
 * @author eric.wittmann@redhat.com
 */
public class XsdDeriver extends AbstractXmlDeriver {

	/**
	 * Constructor.
	 */
	public XsdDeriver() {
	}

	/**
	 * @see org.overlord.sramp.common.repository.derived.AbstractXmlDeriver#configureNamespaceMappings(org.overlord.sramp.common.query.xpath.StaticNamespaceContext)
	 */
	@Override
	protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
		super.configureNamespaceMappings(namespaceContext);

		namespaceContext.addMapping("xs", "http://www.w3.org/2001/XMLSchema");
		namespaceContext.addMapping("xsd", "http://www.w3.org/2001/XMLSchema");
	}

	/**
	 * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#derive(java.util.Collection, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)
	 */
	@Override
	protected void derive(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
			Element rootElement, XPath xpath) throws IOException {
		try {
			processSchema(derivedArtifacts, artifact, rootElement, xpath);
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

	/**
	 * Process the entire schema for all derived content.
	 * @param derivedArtifacts
	 * @param artifact
	 * @param schema
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	public void processSchema(Collection<BaseArtifactType> derivedArtifacts,
			BaseArtifactType artifact, Element schema, XPath xpath) throws XPathExpressionException {
        String targetNS = schema.getAttribute("targetNamespace");
	    if (artifact instanceof XsdDocument)
	        ((XsdDocument) artifact).setTargetNamespace(targetNS);

        processElementDeclarations(derivedArtifacts, artifact, schema, xpath);
		processAttributeDeclarations(derivedArtifacts, artifact, schema, xpath);
		processSimpleTypeDeclarations(derivedArtifacts, artifact, schema, xpath);
		processComplexTypeDeclarations(derivedArtifacts, artifact, schema, xpath);

		// Pre-set the UUIDs for all the derived artifacts.  This is useful
		// if something downstream needs to reference them.
		for (BaseArtifactType derivedArtifact : derivedArtifacts) {
			derivedArtifact.setUuid(UUID.randomUUID().toString());
		}
	}

	/**
	 * Processes the global element declarations found in the schema.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param schema
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processElementDeclarations(Collection<BaseArtifactType> derivedArtifacts,
			BaseArtifactType sourceArtifact, Element schema, XPath xpath) throws XPathExpressionException {
		String targetNS = schema.getAttribute("targetNamespace");

		// xpath expression to find all global element decls
		NodeList nodes = (NodeList) this.query(xpath, schema, "./xsd:element", XPathConstants.NODESET);
		for (int idx = 0; idx < nodes.getLength(); idx++) {
			Element node = (Element) nodes.item(idx);
			if (node.hasAttribute("name")) {
				String nsName = node.getAttribute("name");
				ElementDeclaration elementDecl = new ElementDeclaration();
				elementDecl.setArtifactType(BaseArtifactEnum.ELEMENT_DECLARATION);
				elementDecl.setName(nsName);
				elementDecl.setNamespace(targetNS);
				elementDecl.setNCName(nsName);
				derivedArtifacts.add(elementDecl);
			}
		}
	}

	/**
	 * Processes the global attribute declarations found in the schema.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param schema
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processAttributeDeclarations(Collection<BaseArtifactType> derivedArtifacts,
			BaseArtifactType sourceArtifact, Element schema, XPath xpath) throws XPathExpressionException {
		String targetNS = schema.getAttribute("targetNamespace");

		// xpath expression to find all global attribute decls
		NodeList nodes = (NodeList) this.query(xpath, schema, "./xsd:attribute", XPathConstants.NODESET);
		for (int idx = 0; idx < nodes.getLength(); idx++) {
			Element node = (Element) nodes.item(idx);
			if (node.hasAttribute("name")) {
				String nsName = node.getAttribute("name");
				AttributeDeclaration attributeDecl = new AttributeDeclaration();
				attributeDecl.setArtifactType(BaseArtifactEnum.ATTRIBUTE_DECLARATION);
				attributeDecl.setName(nsName);
				attributeDecl.setNamespace(targetNS);
				attributeDecl.setNCName(nsName);
				derivedArtifacts.add(attributeDecl);
			}
		}
	}

	/**
	 * Processes the global simple type declarations found in the schema.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param schema
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processSimpleTypeDeclarations(Collection<BaseArtifactType> derivedArtifacts,
			BaseArtifactType sourceArtifact, Element schema, XPath xpath) throws XPathExpressionException {
		String targetNS = schema.getAttribute("targetNamespace");

		// xpath expression to find all global simple type decls
		NodeList nodes = (NodeList) this.query(xpath, schema, "./xsd:simpleType", XPathConstants.NODESET);
		for (int idx = 0; idx < nodes.getLength(); idx++) {
			Element node = (Element) nodes.item(idx);
			if (node.hasAttribute("name")) {
				String nsName = node.getAttribute("name");
				SimpleTypeDeclaration simpleTypeDecl = new SimpleTypeDeclaration();
				simpleTypeDecl.setArtifactType(BaseArtifactEnum.SIMPLE_TYPE_DECLARATION);
				simpleTypeDecl.setName(nsName);
				simpleTypeDecl.setNamespace(targetNS);
				simpleTypeDecl.setNCName(nsName);
				derivedArtifacts.add(simpleTypeDecl);
			}
		}
	}

	/**
	 * Processes the global complex type declarations found in the schema.
	 * @param derivedArtifacts
	 * @param sourceArtifact
	 * @param schema
	 * @param xpath
	 * @throws XPathExpressionException
	 */
	private void processComplexTypeDeclarations(Collection<BaseArtifactType> derivedArtifacts,
			BaseArtifactType sourceArtifact, Element schema, XPath xpath) throws XPathExpressionException {
		String targetNS = schema.getAttribute("targetNamespace");

		// xpath expression to find all global complex type decls
		NodeList nodes = (NodeList) this.query(xpath, schema, "./xsd:complexType", XPathConstants.NODESET);
		for (int idx = 0; idx < nodes.getLength(); idx++) {
			Element node = (Element) nodes.item(idx);
			if (node.hasAttribute("name")) {
				String nsName = node.getAttribute("name");
				ComplexTypeDeclaration complexTypeDecl = new ComplexTypeDeclaration();
				complexTypeDecl.setArtifactType(BaseArtifactEnum.COMPLEX_TYPE_DECLARATION);
				complexTypeDecl.setName(nsName);
				complexTypeDecl.setNamespace(targetNS);
				complexTypeDecl.setNCName(nsName);
				derivedArtifacts.add(complexTypeDecl);
			}
		}
	}

	/**
	 * @see org.overlord.sramp.common.derived.ArtifactDeriver#link(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
	 */
	@Override
	public void link(LinkerContext context, BaseArtifactType sourceArtifact,
	        Collection<BaseArtifactType> derivedArtifacts) {
	}

}
