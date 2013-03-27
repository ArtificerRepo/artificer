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
package org.overlord.sramp.demos.deriver;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.common.derived.ArtifactDeriver;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An {@link ArtifactDeriver} that will create derived content from a standard
 * JEE web.xml file.
 *
 * @author eric.wittmann@redhat.com
 */
public class WebXmlDeriver extends AbstractXmlDeriver {

    /**
     * Constructor.
     */
    public WebXmlDeriver() {
    }

    /**
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#configureNamespaceMappings(org.overlord.sramp.common.query.xpath.StaticNamespaceContext)
     */
    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);

        namespaceContext.addMapping("jee", "http://java.sun.com/xml/ns/javaee");
    }

    /**
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#createDerivedArtifactCollection()
     */
    @Override
    protected Collection<BaseArtifactType> createDerivedArtifactCollection() {
        return new WebXmlArtifactCollection();
    }

    /**
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#derive(java.util.Collection, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)
     */
    @Override
    protected void derive(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element rootElement, XPath xpath) throws IOException {
        try {
            processDescriptor(derivedArtifacts, artifact, rootElement, xpath);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Process the web.xml descriptor.
     * @param derivedArtifacts
     * @param artifact
     * @param webXml
     * @param xpath
     * @throws XPathExpressionException
     */
    public void processDescriptor(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType artifact, Element webXml, XPath xpath) throws XPathExpressionException {
        String displayName = (String) this.query(xpath, webXml, "string(./jee:display-name)", XPathConstants.STRING);
        if (displayName != null && displayName.trim().length() > 0) {
            artifact.setName(displayName);
        }

        processListeners(derivedArtifacts, artifact, webXml, xpath);
        processFilters(derivedArtifacts, artifact, webXml, xpath);
        processFilterMappings(derivedArtifacts, artifact, webXml, xpath);
        processServlets(derivedArtifacts, artifact, webXml, xpath);
        processServletMappings(derivedArtifacts, artifact, webXml, xpath);
    }

    /**
     * @param derivedArtifacts
     * @param artifact
     * @param webXml
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processListeners(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType artifact, Element webXml, XPath xpath) throws XPathExpressionException {
        NodeList nodes = (NodeList) this.query(xpath, webXml, "./jee:listener", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType listener = new ExtendedArtifactType();
            listener.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            listener.setUuid(UUID.randomUUID().toString());
            listener.setExtendedType("ListenerDeclaration");

            String listenerClass = (String) this.query(xpath, node, "string(./jee:listener-class)", XPathConstants.STRING);
            String displayName = (String) this.query(xpath, node, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName == null || displayName.trim().length() == 0) {
                displayName = listenerClass;
            }
            listener.setName(displayName);

            String description = (String) this.query(xpath, node, "string(./jee:description)", XPathConstants.STRING);
            if (description != null && description.trim().length() > 0) {
                listener.setDescription(description);
            }
            SrampModelUtils.setCustomProperty(listener, "listener-class", listenerClass);
            derivedArtifacts.add(listener);
        }
    }

    /**
     * @param derivedArtifacts
     * @param artifact
     * @param webXml
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processFilters(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element webXml, XPath xpath) throws XPathExpressionException {
        NodeList nodes = (NodeList) this.query(xpath, webXml, "./jee:filter", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType filter = new ExtendedArtifactType();
            filter.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            filter.setUuid(UUID.randomUUID().toString());
            filter.setExtendedType("FilterDeclaration");

            String filterClass = (String) this.query(xpath, node, "string(./jee:listener-class)", XPathConstants.STRING);
            String filterName = (String) this.query(xpath, node, "string(./jee:filter-name)", XPathConstants.STRING);
            String displayName = (String) this.query(xpath, node, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName == null || displayName.trim().length() == 0) {
                displayName = filterClass;
            }
            String description = (String) this.query(xpath, node, "string(./jee:description)", XPathConstants.STRING);
            if (description != null && description.trim().length() > 0) {
                description = null;
            }

            filter.setName(filterName);
            filter.setDescription(description);
            SrampModelUtils.setCustomProperty(filter, "display-name", displayName);
            SrampModelUtils.setCustomProperty(filter, "filter-class", filterClass);
            derivedArtifacts.add(filter);
        }
    }

    /**
     * @param derivedArtifacts
     * @param artifact
     * @param webXml
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processFilterMappings(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType artifact, Element webXml, XPath xpath) throws XPathExpressionException {
        NodeList nodes = (NodeList) this.query(xpath, webXml, "./jee:filter-mapping", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType filterMapping = new ExtendedArtifactType();
            filterMapping.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            filterMapping.setUuid(UUID.randomUUID().toString());
            filterMapping.setExtendedType("FilterMapping");

            String filterName = (String) this.query(xpath, node, "string(./jee:filter-name)", XPathConstants.STRING);
            String urlPattern = (String) this.query(xpath, node, "string(./jee:url-pattern)", XPathConstants.STRING);

            filterMapping.setName(filterName + " Mapping");
            filterMapping.setDescription("Maps URLs of the form '"+urlPattern+"' to filter "+filterName+".");
            SrampModelUtils.setCustomProperty(filterMapping, "filter-name", filterName);
            SrampModelUtils.setCustomProperty(filterMapping, "url-pattern", urlPattern);

            WebXmlArtifactCollection index = (WebXmlArtifactCollection) derivedArtifacts;
            ExtendedArtifactType filter = index.lookupFilter(filterName);
            if (filter != null) {
                SrampModelUtils.addGenericRelationship(filterMapping, "mapsFilter", filter.getUuid());
            }

            derivedArtifacts.add(filterMapping);
        }
    }

    /**
     * @param derivedArtifacts
     * @param artifact
     * @param webXml
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processServlets(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element webXml, XPath xpath) throws XPathExpressionException {
        NodeList nodes = (NodeList) this.query(xpath, webXml, "./jee:servlet", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType servlet = new ExtendedArtifactType();
            servlet.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            servlet.setUuid(UUID.randomUUID().toString());
            servlet.setExtendedType("ServletDeclaration");

            String servletClass = (String) this.query(xpath, node, "string(./jee:listener-class)", XPathConstants.STRING);
            String servletName = (String) this.query(xpath, node, "string(./jee:servlet-name)", XPathConstants.STRING);
            String displayName = (String) this.query(xpath, node, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName == null || displayName.trim().length() == 0) {
                displayName = servletClass;
            }
            String description = (String) this.query(xpath, node, "string(./jee:description)", XPathConstants.STRING);
            if (description != null && description.trim().length() > 0) {
                description = null;
            }

            servlet.setName(servletName);
            servlet.setDescription(description);
            SrampModelUtils.setCustomProperty(servlet, "display-name", displayName);
            SrampModelUtils.setCustomProperty(servlet, "servlet-class", servletClass);
            derivedArtifacts.add(servlet);
        }
    }

    /**
     * @param derivedArtifacts
     * @param artifact
     * @param webXml
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processServletMappings(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType artifact, Element webXml, XPath xpath) throws XPathExpressionException {
        NodeList nodes = (NodeList) this.query(xpath, webXml, "./jee:servlet-mapping", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType servletMapping = new ExtendedArtifactType();
            servletMapping.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            servletMapping.setUuid(UUID.randomUUID().toString());
            servletMapping.setExtendedType("ServletMapping");

            String servletName = (String) this.query(xpath, node, "string(./jee:servlet-name)", XPathConstants.STRING);
            String urlPattern = (String) this.query(xpath, node, "string(./jee:url-pattern)", XPathConstants.STRING);

            servletMapping.setName(servletName + " Mapping");
            servletMapping.setDescription("Maps URLs of the form '"+urlPattern+"' to servlet "+servletName+".");
            SrampModelUtils.setCustomProperty(servletMapping, "servlet-name", servletName);
            SrampModelUtils.setCustomProperty(servletMapping, "url-pattern", urlPattern);

            WebXmlArtifactCollection index = (WebXmlArtifactCollection) derivedArtifacts;
            ExtendedArtifactType servlet = index.lookupServlet(servletName);
            if (servlet != null) {
                SrampModelUtils.addGenericRelationship(servletMapping, "mapsServlet", servlet.getUuid());
            }

            derivedArtifacts.add(servletMapping);
        }
    }

}
