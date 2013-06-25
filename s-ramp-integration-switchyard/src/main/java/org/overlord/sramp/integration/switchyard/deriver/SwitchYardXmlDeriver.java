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

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.AbstractXmlDeriver;
import org.overlord.sramp.common.derived.LinkerContext;
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

    public static final QName UNRESOLVED_REF = new QName("urn:s-ramp:switchyard-deriver", "unresolvedRef");

    private final SwitchYardLinker linker = new SwitchYardLinker();

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
        SwitchYardModel.addNamespaceMappings(namespaceContext);
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
            if ("switchyard.xml".equals(artifact.getName()) && rootElement.hasAttribute("name")) {
                String name = rootElement.getAttribute("name");
                artifact.setName(name);
            }

            processComponents(derivedArtifacts, artifact, rootElement, xpath);
            // Note - process services after components so that components can be promoted easily.
            processServices(derivedArtifacts, artifact, rootElement, xpath);
            processTransformers(derivedArtifacts, artifact, rootElement, xpath);
            processValidators(derivedArtifacts, artifact, rootElement, xpath);
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

            if (node.hasAttribute("promote")) {
                String promote = node.getAttribute("promote");
                BaseArtifactType component = findComponentByName(derivedArtifacts, promote);
                if (component != null) {
                    SrampModelUtils.addGenericRelationship(serviceArtifact, SwitchYardModel.REL_PROMOTES, component.getUuid());
                }
            }
        }
    }

    /**
     * Create derived components found in the switchyard.xml.
     * @param derivedArtifacts
     * @param artifact
     * @param rootElement
     * @param xpath
     */
    private void processComponents(Collection<BaseArtifactType> derivedArtifacts, BaseArtifactType artifact,
            Element rootElement, XPath xpath) throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) this.query(xpath, rootElement, "./sca:composite/sca:component", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            String name = node.getAttribute("name");
            ExtendedArtifactType componentArtifact = SwitchYardModel.newComponentArtifact(name);
            derivedArtifacts.add(componentArtifact);

            Element implBean = (Element) this.query(xpath, node, "bean:implementation.bean", XPathConstants.NODE);
            if (implBean != null) {
                if (implBean.hasAttribute("class")) {
                    String implClassName = implBean.getAttribute("class");
                    Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                    relationship.getOtherAttributes().put(UNRESOLVED_REF, "class:" + implClassName);
                }
                if (implBean.hasAttribute("requires")) {
                    String requires = implBean.getAttribute("requires");
                    SrampModelUtils.setCustomProperty(componentArtifact, "requires", requires);
                }
            }
            Element implCamel = (Element) this.query(xpath, node, "camel:implementation.camel", XPathConstants.NODE);
            if (implCamel != null) {
                Element xml = (Element) this.query(xpath, implCamel, "camel:xml", XPathConstants.NODE);
                if (xml != null) {
                    String path = xml.getAttribute("path");
                    Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                    relationship.getOtherAttributes().put(UNRESOLVED_REF, "camel:" + path);
                }
                if (implCamel.hasAttribute("requires")) {
                    String requires = implCamel.getAttribute("requires");
                    SrampModelUtils.setCustomProperty(componentArtifact, "requires", requires);
                }
            }


            NodeList refs = (NodeList) this.query(xpath, node, "sca:reference", XPathConstants.NODESET);
            for (int jdx = 0; jdx < refs.getLength(); jdx++) {
                Element ref = (Element) refs.item(jdx);
                Element iface = (Element) this.query(xpath, ref, "sca:interface.java", XPathConstants.NODE);
                if (iface != null) {
                    if (iface.hasAttribute("interface")) {
                        String ifaceName = iface.getAttribute("interface");
                        Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_REFERENCES, null);
                        relationship.getOtherAttributes().put(UNRESOLVED_REF, "java:" + ifaceName);
                    }
                }
                iface = (Element) this.query(xpath, ref, "sca:interface.wsdl", XPathConstants.NODE);
                if (iface != null) {
                    if (iface.hasAttribute("interface")) {
                        String wsdlInfo = iface.getAttribute("interface");
                        Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_REFERENCES, null);
                        relationship.getOtherAttributes().put(UNRESOLVED_REF, "wsdl:" + wsdlInfo);
                    }
                }
            }
        }
    }

    /**
     * Create derived transformers found in the switchyard.xml.
     * @param derivedArtifacts
     * @param artifact
     * @param rootElement
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processTransformers(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType artifact, Element rootElement, XPath xpath) throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) this.query(xpath, rootElement, "./swyd:transforms/tf:transform.java | ./swyd:transforms/tf:transform.xslt | ./swyd:transforms/tf:transform.json | ./swyd:transforms/tf:transform.smooks | ./swyd:transforms/tf:transform.jaxb", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            String name = node.getAttribute("name"); // in case SY supports named transformers at some point
            if (name == null || name.trim().length() == 0) {
                if (node.hasAttribute("from") && node.hasAttribute("to")) {
                    String from = node.getAttribute("from");
                    String to = node.getAttribute("to");
                    if (from.startsWith("{")) {
                        name = from.substring(from.lastIndexOf("}")+1);
                    } else if (from.startsWith("java:")) {
                        name = from.substring(from.lastIndexOf('.')+1);
                    }
                    if (to.startsWith("{")) {
                        name = name + "->" + to.substring(to.lastIndexOf("}")+1);
                    } else if (to.startsWith("java:")) {
                        name = name + "->" + to.substring(from.lastIndexOf('.')+1);
                    }
                } else {
                    name = node.getLocalName();
                }
            }

            ExtendedArtifactType transformerArtifact = SwitchYardModel.newTransformerArtifact(name);
            String type = node.getLocalName();
            type = type.substring(type.lastIndexOf('.') + 1);
            SrampModelUtils.setCustomProperty(transformerArtifact, SwitchYardModel.PROP_TRANSFORMER_TYPE, type);
            if (node.hasAttribute("class")) {
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, "class:" + node.getAttribute("class"));
            }
            if (node.hasAttribute("bean")) {
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, "bean:" + node.getAttribute("bean"));
            }
            if (node.hasAttribute("xsltFile")) {
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, "xslt:" + node.getAttribute("xsltFile"));
            }
            if (node.hasAttribute("config")) {
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, "smooks:" + node.getAttribute("config"));
            }
            if (node.hasAttribute("from")) {
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_TRANSFORMS_FROM, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, node.getAttribute("from"));
            }
            if (node.hasAttribute("to")) {
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_TRANSFORMS_TO, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, node.getAttribute("to"));
            }

            derivedArtifacts.add(transformerArtifact);
        }
    }

    /**
     * Create derived validators found in the switchyard.xml.
     * @param derivedArtifacts
     * @param artifact
     * @param rootElement
     * @param xpath
     * @throws XPathExpressionException
     */
    private void processValidators(Collection<BaseArtifactType> derivedArtifacts,
            BaseArtifactType artifact, Element rootElement, XPath xpath) throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) this.query(xpath, rootElement, "./swyd:validates/val:validate.java | ./swyd:validates/val:validate.xml", XPathConstants.NODESET);
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (!node.hasAttribute("name"))
                continue;

            String name = node.getAttribute("name");

            ExtendedArtifactType validatorArtifact = SwitchYardModel.newValidateArtifact(name);
            String type = node.getLocalName();
            type = type.substring(type.lastIndexOf('.') + 1);
            SrampModelUtils.setCustomProperty(validatorArtifact, SwitchYardModel.PROP_VALIDATE_TYPE, type);

            // Unresolved 'validates' reference
            Relationship relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_VALIDATES, null);
            relationship.getOtherAttributes().put(UNRESOLVED_REF, name);

            // Unresolved 'implementedBy' reference
            if (node.hasAttribute("class")) {
                relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, "class:" + node.getAttribute("class"));
            }
            if (node.hasAttribute("bean")) {
                relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationship.getOtherAttributes().put(UNRESOLVED_REF, "bean:" + node.getAttribute("bean"));
            }
            if (node.hasAttribute("schemaType")) {
                // TODO handle relationships to the schemas
//                relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
//                relationship.getOtherAttributes().put(UNRESOLVED_REF, "xml:" + node.getAttribute("schemaType"));
            }

            derivedArtifacts.add(validatorArtifact);
        }
    }

    /**
     * @see org.overlord.sramp.common.derived.ArtifactDeriver#link(org.overlord.sramp.common.derived.LinkerContext, org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType, java.util.Collection)
     */
    @Override
    public void link(LinkerContext context, BaseArtifactType sourceArtifact, Collection<BaseArtifactType> derivedArtifacts) {
        for (BaseArtifactType derivedArtifact : derivedArtifacts) {
            linker.link(context, (ExtendedArtifactType) derivedArtifact);
        }
    }

    /**
     * Finds a component artifact (previously created) with the given name.
     * @param derivedArtifacts
     * @param componentName
     */
    private BaseArtifactType findComponentByName(Collection<BaseArtifactType> derivedArtifacts, String componentName) {
        for (BaseArtifactType artifact : derivedArtifacts) {
            ArtifactType at = ArtifactType.valueOf(artifact);
            if (at.getType().equals(SwitchYardModel.SwitchYardComponent) && artifact.getName().equals(componentName)) {
                return artifact;
            }
        }
        return null;
    }

}
