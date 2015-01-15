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
package org.overlord.sramp.demos.artifactbuilder;

import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.artifactbuilder.ArtifactBuilder;
import org.overlord.sramp.integration.artifactbuilder.XmlArtifactBuilder;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * An {@link ArtifactBuilder} that will create derived content from a standard
 * JEE web.xml file.
 *
 * @author eric.wittmann@redhat.com
 */
public class WebXmlArtifactBuilder extends XmlArtifactBuilder {
    
    private final WebXmlArtifactCollection derivedArtifacts = new WebXmlArtifactCollection();

    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);

        namespaceContext.addMapping("jee", "http://java.sun.com/xml/ns/javaee");
    }

    @Override
    public Collection<BaseArtifactType> getDerivedArtifacts() {
        return derivedArtifacts;
    }

    @Override
    protected void derive() throws IOException {
        try {
            String displayName = (String) query(rootElement, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName != null && displayName.trim().length() > 0) {
                getPrimaryArtifact().setName(displayName);
            }

            processListeners();
            processFilters();
            processFilterMappings();
            processServlets();
            processServletMappings();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void processListeners() throws XPathExpressionException {
        NodeList nodes = (NodeList) query(rootElement, "./jee:listener", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType listener = new ExtendedArtifactType();
            listener.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            listener.setUuid(UUID.randomUUID().toString());
            listener.setExtendedType("ListenerDeclaration");

            String listenerClass = (String) query(node, "string(./jee:listener-class)", XPathConstants.STRING);
            String displayName = (String) query(node, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName == null || displayName.trim().length() == 0) {
                displayName = listenerClass;
            }
            listener.setName(displayName);

            String description = (String) query(node, "string(./jee:description)", XPathConstants.STRING);
            if (description != null && description.trim().length() > 0) {
                listener.setDescription(description);
            }
            SrampModelUtils.setCustomProperty(listener, "listener-class", listenerClass);
            derivedArtifacts.add(listener);
        }
    }

    private void processFilters() throws XPathExpressionException {
        NodeList nodes = (NodeList) query(rootElement, "./jee:filter", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType filter = new ExtendedArtifactType();
            filter.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            filter.setUuid(UUID.randomUUID().toString());
            filter.setExtendedType("FilterDeclaration");

            String filterClass = (String) query(node, "string(./jee:listener-class)", XPathConstants.STRING);
            String filterName = (String) query(node, "string(./jee:filter-name)", XPathConstants.STRING);
            String displayName = (String) query(node, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName == null || displayName.trim().length() == 0) {
                displayName = filterClass;
            }
            String description = (String) query(node, "string(./jee:description)", XPathConstants.STRING);
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

    private void processFilterMappings() throws XPathExpressionException {
        NodeList nodes = (NodeList) query(rootElement, "./jee:filter-mapping", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType filterMapping = new ExtendedArtifactType();
            filterMapping.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            filterMapping.setUuid(UUID.randomUUID().toString());
            filterMapping.setExtendedType("FilterMapping");

            String filterName = (String) query(node, "string(./jee:filter-name)", XPathConstants.STRING);
            String urlPattern = (String) query(node, "string(./jee:url-pattern)", XPathConstants.STRING);

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

    private void processServlets() throws XPathExpressionException {
        NodeList nodes = (NodeList) query(rootElement, "./jee:servlet", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType servlet = new ExtendedArtifactType();
            servlet.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            servlet.setUuid(UUID.randomUUID().toString());
            servlet.setExtendedType("ServletDeclaration");

            String servletClass = (String) query(node, "string(./jee:listener-class)", XPathConstants.STRING);
            String servletName = (String) query(node, "string(./jee:servlet-name)", XPathConstants.STRING);
            String displayName = (String) query(node, "string(./jee:display-name)", XPathConstants.STRING);
            if (displayName == null || displayName.trim().length() == 0) {
                displayName = servletClass;
            }
            String description = (String) query(node, "string(./jee:description)", XPathConstants.STRING);
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

    private void processServletMappings() throws XPathExpressionException {
        NodeList nodes = (NodeList) query(rootElement, "./jee:servlet-mapping", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            ExtendedArtifactType servletMapping = new ExtendedArtifactType();
            servletMapping.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
            servletMapping.setUuid(UUID.randomUUID().toString());
            servletMapping.setExtendedType("ServletMapping");

            String servletName = (String) query(node, "string(./jee:servlet-name)", XPathConstants.STRING);
            String urlPattern = (String) query(node, "string(./jee:url-pattern)", XPathConstants.STRING);

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
