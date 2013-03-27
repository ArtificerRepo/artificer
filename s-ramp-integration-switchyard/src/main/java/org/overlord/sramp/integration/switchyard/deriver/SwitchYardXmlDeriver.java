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
package org.overlord.sramp.integration.switchyard.deriver;

import java.io.IOException;
import java.util.Collection;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This deriver operates on the switchyard.xml file found in a typical SwitchYard
 * application JAR/WAR.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardXmlDeriver extends AbstractXmlDeriver {

    /**
     * Constructor.
     */
    public SwitchYardXmlDeriver() {
    }

    /**
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#configureNamespaceMappings(org.overlord.sramp.common.query.xpath.StaticNamespaceContext)
     */
    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);
        namespaceContext.addMapping("sca", SwitchYardModel.SCA_NS);
        namespaceContext.addMapping("swyd", SwitchYardModel.SWITCHYARD_NS);
        namespaceContext.addMapping("bean", SwitchYardModel.BEAN_NS);
        namespaceContext.addMapping("tf", SwitchYardModel.TRANSFORM_NS);
        namespaceContext.addMapping("val", SwitchYardModel.VALIDATE_NS);
        namespaceContext.addMapping("soap", SwitchYardModel.SOAP_NS);
    }

    /**
     * @see org.overlord.sramp.common.derived.AbstractXmlDeriver#derive(java.util.Collection, org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType, org.w3c.dom.Element, javax.xml.xpath.XPath)
     */
    @Override
    protected void derive(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element rootElement, XPath xpath) throws IOException {
        try {
            // Pull out the target namespace and save it as a custom property
            String targetNS = rootElement.getAttribute("targetNamespace");
            SrampModelUtils.setCustomProperty(artifact, "targetNamespace", targetNS);
            // Pull out the name and set it (unless the name has already been set)
            String name = rootElement.getAttribute("name");
            if ("switchyard.xml".equals(artifact.getName())) {
                artifact.setName(name);
            }

            processServices(derivedArtifacts, artifact, rootElement, xpath);
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    /**
     * Create derived services found in the switchyard.xml.
     * @param derivedArtifacts
     * @param sourceArtifact
     * @param rootElement
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processServices(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType sourceArtifact, Element rootElement, XPath xpath) throws XPathExpressionException {

        // xpath expression to find all services
        NodeList nodes = (NodeList) this.query(xpath, rootElement, "./sca:composite/sca:service", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            String name = node.getAttribute("name");
            ExtendedArtifactType serviceArtifact = SwitchYardModel.newServiceArtifact(name);
            derivedArtifacts.add(serviceArtifact);
        }
    }

}
