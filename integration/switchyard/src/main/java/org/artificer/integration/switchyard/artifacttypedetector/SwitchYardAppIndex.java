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
package org.artificer.integration.switchyard.artifacttypedetector;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.artificer.common.query.xpath.StaticNamespaceContext;
import org.artificer.integration.switchyard.model.SwitchYardModel;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An index of interesting facts found in the SwitchYard application's required
 * switchyard.xml file.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardAppIndex {

    private File switchyardXml;
    private Set<String> javaClasses = new HashSet<String>();

    /**
     * Constructor.
     */
    public SwitchYardAppIndex(File switchyardXml) {
        this.switchyardXml = switchyardXml;
        index();
    }

    /**
     * Index the switchyard.xml file for interesting/relevant content.
     */
    private void index() {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false); //$NON-NLS-1$
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false); //$NON-NLS-1$
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(switchyardXml);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            StaticNamespaceContext nsCtx = new StaticNamespaceContext();
            SwitchYardModel.addNamespaceMappings(nsCtx);
            xpath.setNamespaceContext(nsCtx);

            Element rootElement = document.getDocumentElement();
            index(xpath, rootElement);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Index the switchyard XML.
     * @param xpath
     * @param rootElement
     * @throws XPathExpressionException
     */
    private void index(XPath xpath, Element rootElement) throws XPathExpressionException {
        AttributeValueParser attrParser = new AttributeValueParser() {
            @Override
            public String parse(String attributeValue) {
                if (attributeValue.startsWith("java:")) { //$NON-NLS-1$
                    return attributeValue.substring(5);
                }
                return null;
            }
        };

        indexJavaClassesFromAttributes(xpath, rootElement, "//bean:implementation.bean/@class"); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//sca:interface.java/@interface"); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:transform.java/@from", attrParser); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:transform.java/@to", attrParser); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:transform.java/@class"); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:transform.java/@bean"); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:validate.java/@from", attrParser); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:validate.java/@to", attrParser); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:validate.java/@class"); //$NON-NLS-1$
        indexJavaClassesFromAttributes(xpath, rootElement, "//tf:validate.java/@bean"); //$NON-NLS-1$
    }

    /**
     * Indexes java classes found in attributes.
     * @param xpath
     * @param rootElement
     * @param expression
     * @throws XPathExpressionException
     */
    private void indexJavaClassesFromAttributes(XPath xpath, Element rootElement, String expression) throws XPathExpressionException {
        indexJavaClassesFromAttributes(xpath, rootElement, expression, null);
    }

    /**
     * Indexes java classes found in attributes.
     * @param xpath
     * @param rootElement
     * @param expression
     * @param attributeValueParser
     * @throws XPathExpressionException
     */
    private void indexJavaClassesFromAttributes(XPath xpath, Element rootElement, String expression,
            AttributeValueParser attributeValueParser) throws XPathExpressionException {
        NodeList nodes = (NodeList) xpath.evaluate(expression, rootElement, XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Attr node = (Attr) nodes.item(idx);
            String beanName = null;
            if (attributeValueParser != null) {
                beanName = attributeValueParser.parse(node.getValue());
            } else {
                beanName = node.getValue();
            }
            if (beanName != null)
                this.javaClasses.add(beanName);
        }
    }

    /**
     * @param fullyQualifiedJavaClassname
     * @return true if the java class was referenced by the switchyard.xml
     */
    public boolean contains(String fullyQualifiedJavaClassname) {
        return javaClasses.contains(fullyQualifiedJavaClassname);
    }

    /**
     * Implement this to parse a Java classname from an attribute value.
     *
     * @author eric.wittmann@redhat.com
     */
    private static interface AttributeValueParser {

        /**
         * Called to parse an attribute value into a Java classname.
         * @param attributeValue
         */
        public String parse(String attributeValue);

    }

}