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
package org.overlord.sramp.integration.switchyard.artifactbuilder;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.integration.artifactbuilder.QNameRelationshipSource;
import org.overlord.sramp.integration.artifactbuilder.XmlArtifactBuilder;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.Collection;
import java.util.UUID;

/**
 * This artifact builder operates on the switchyard.xml file found in a typical SwitchYard
 * application JAR/WAR.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardXmlArtifactBuilder extends XmlArtifactBuilder {

    @Override
    protected void configureNamespaceMappings(StaticNamespaceContext namespaceContext) {
        super.configureNamespaceMappings(namespaceContext);
        SwitchYardModel.addNamespaceMappings(namespaceContext);
    }

    @Override
    protected void derive() throws IOException {
        try {
            // Pull out the target namespace and save it as a custom property
            String targetNS = rootElement.getAttribute("targetNamespace"); //$NON-NLS-1$
            SrampModelUtils.setCustomProperty(getPrimaryArtifact(), "targetNamespace", targetNS); //$NON-NLS-1$
            // Pull out the name and set it (unless the name has already been set)
            if ("switchyard.xml".equals(getPrimaryArtifact().getName()) && rootElement.hasAttribute("name")) { //$NON-NLS-1$ //$NON-NLS-2$
                String name = rootElement.getAttribute("name"); //$NON-NLS-1$
                getPrimaryArtifact().setName(name);
            }

            processComponents();
            // Note - process services after components so that components can be promoted easily.
            processServices();
            processTransformers();
            processValidators();
        } catch (XPathExpressionException e) {
            throw new IOException(e);
        }
    }

    private void processServices() throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) query(rootElement, "./sca:composite/sca:service", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            String name = node.getAttribute("name"); //$NON-NLS-1$
            ExtendedArtifactType serviceArtifact = SwitchYardModel.newServiceArtifact(name);
            getDerivedArtifacts().add(serviceArtifact);

            if (node.hasAttribute("promote")) { //$NON-NLS-1$
                String promote = node.getAttribute("promote"); //$NON-NLS-1$
                BaseArtifactType component = findComponentByName(getDerivedArtifacts(), promote);
                if (component != null) {
                    SrampModelUtils.addGenericRelationship(serviceArtifact, SwitchYardModel.REL_PROMOTES, component.getUuid());
                }
            }

            Element iface = (Element) query(node, "sca:interface.java", XPathConstants.NODE); //$NON-NLS-1$
            if (iface != null) {
                if (iface.hasAttribute("interface")) { //$NON-NLS-1$
                    String ifaceName = iface.getAttribute("interface"); //$NON-NLS-1$
                    Relationship relationship = SrampModelUtils.addGenericRelationship(serviceArtifact, SwitchYardModel.REL_IMPLEMENTS, null);
                    relationshipSources.add(new JavaRelationshipSource(ifaceName, null,
                            relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_INTERFACE));
                }
            }
            iface = (Element) query(node, "sca:interface.wsdl", XPathConstants.NODE); //$NON-NLS-1$
            if (iface != null) {
                if (iface.hasAttribute("interface")) { //$NON-NLS-1$
                    String wsdlInfo = iface.getAttribute("interface"); //$NON-NLS-1$
                    Relationship relationship = SrampModelUtils.addGenericRelationship(serviceArtifact, SwitchYardModel.REL_IMPLEMENTS, null);
                    // TODO Implement finding an artifact in the s-ramp repo given the wsdl information in switchyard.xml (why couldn't a QName be used in there???)
                }
            }

        }
    }

    private void processComponents() throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) query(rootElement, "./sca:composite/sca:component", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            String name = node.getAttribute("name"); //$NON-NLS-1$
            ExtendedArtifactType componentArtifact = SwitchYardModel.newComponentArtifact(name);
            getDerivedArtifacts().add(componentArtifact);

            Element implBean = (Element) query(node, "bean:implementation.bean", XPathConstants.NODE); //$NON-NLS-1$
            if (implBean != null) {
                if (implBean.hasAttribute("class")) { //$NON-NLS-1$
                    String implClassName = implBean.getAttribute("class"); //$NON-NLS-1$
                    Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                    relationshipSources.add(new JavaRelationshipSource(implClassName, null,
                            relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_CLASS));
                }
                if (implBean.hasAttribute("requires")) { //$NON-NLS-1$
                    String requires = implBean.getAttribute("requires"); //$NON-NLS-1$
                    SrampModelUtils.setCustomProperty(componentArtifact, "requires", requires); //$NON-NLS-1$
                }
            }
            Element implCamel = (Element) query(node, "camel:implementation.camel", XPathConstants.NODE); //$NON-NLS-1$
            if (implCamel != null) {
                Element xml = (Element) query(implCamel, "camel:xml", XPathConstants.NODE); //$NON-NLS-1$
                if (xml != null) {
                    String path = xml.getAttribute("path"); //$NON-NLS-1$
                    Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                    // TODO Implement finding a camel config artifact in the repository
                }
                if (implCamel.hasAttribute("requires")) { //$NON-NLS-1$
                    String requires = implCamel.getAttribute("requires"); //$NON-NLS-1$
                    SrampModelUtils.setCustomProperty(componentArtifact, "requires", requires); //$NON-NLS-1$
                }
            }

            // Process references ('service' children of the component)
            NodeList refs = (NodeList) query(node, "sca:reference", XPathConstants.NODESET); //$NON-NLS-1$
            for (int jdx = 0; jdx < refs.getLength(); jdx++) {
                Element ref = (Element) refs.item(jdx);
                Element iface = (Element) query(ref, "sca:interface.java", XPathConstants.NODE); //$NON-NLS-1$
                if (iface != null) {
                    if (iface.hasAttribute("interface")) { //$NON-NLS-1$
                        String ifaceName = iface.getAttribute("interface"); //$NON-NLS-1$
                        Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_REFERENCES, null);
                        relationshipSources.add(new JavaRelationshipSource(ifaceName, null,
                                relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_INTERFACE));
                    }
                }
                iface = (Element) query(ref, "sca:interface.wsdl", XPathConstants.NODE); //$NON-NLS-1$
                if (iface != null) {
                    if (iface.hasAttribute("interface")) { //$NON-NLS-1$
                        String wsdlInfo = iface.getAttribute("interface"); //$NON-NLS-1$
                        Relationship relationship = SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_REFERENCES, null);
                        // TODO Implement finding an artifact in the s-ramp repo given the wsdl information in switchyard.xml (why couldn't a QName be used in there???)
                    }
                }
            }

            // Process component services ('service' children of the component)
            NodeList services = (NodeList) query(node, "sca:service", XPathConstants.NODESET); //$NON-NLS-1$
            for (int jdx = 0; jdx < services.getLength(); jdx++) {
                Element componentSvc = (Element) services.item(jdx);
                name = componentSvc.getAttribute("name"); //$NON-NLS-1$
                ExtendedArtifactType componentServiceArtifact = SwitchYardModel.newComponentServiceArtifact(name);
                componentServiceArtifact.setUuid(UUID.randomUUID().toString());
                getDerivedArtifacts().add(componentServiceArtifact);
                SrampModelUtils.addGenericRelationship(componentArtifact, SwitchYardModel.REL_OFFERS, componentServiceArtifact.getUuid());

                Element iface = (Element) query(componentSvc, "sca:interface.java", XPathConstants.NODE); //$NON-NLS-1$
                if (iface != null) {
                    if (iface.hasAttribute("interface")) { //$NON-NLS-1$
                        String ifaceName = iface.getAttribute("interface"); //$NON-NLS-1$
                        Relationship relationship = SrampModelUtils.addGenericRelationship(componentServiceArtifact, SwitchYardModel.REL_IMPLEMENTS, null);
                        relationshipSources.add(new JavaRelationshipSource(ifaceName, null,
                                relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_INTERFACE));
                    }
                }
                iface = (Element) query(componentSvc, "sca:interface.wsdl", XPathConstants.NODE); //$NON-NLS-1$
                if (iface != null) {
                    if (iface.hasAttribute("interface")) { //$NON-NLS-1$
                        String wsdlInfo = iface.getAttribute("interface"); //$NON-NLS-1$
                        Relationship relationship = SrampModelUtils.addGenericRelationship(componentServiceArtifact, SwitchYardModel.REL_IMPLEMENTS, null);
                        // TODO Implement finding an artifact in the s-ramp repo given the wsdl information in switchyard.xml (why couldn't a QName be used in there???)
                    }
                }

                if (componentSvc.hasAttribute("requires")) { //$NON-NLS-1$
                    String requires = componentSvc.getAttribute("requires"); //$NON-NLS-1$
                    SrampModelUtils.setCustomProperty(componentServiceArtifact, "requires", requires); //$NON-NLS-1$
                }

            }
        }
    }

    private void processTransformers() throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) query(rootElement, "./swyd:transforms/tf:transform.java | ./swyd:transforms/tf:transform.xslt | ./swyd:transforms/tf:transform.json | ./swyd:transforms/tf:transform.smooks | ./swyd:transforms/tf:transform.jaxb", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            String name = node.getAttribute("name"); // in case SY supports named transformers at some point //$NON-NLS-1$
            if (name == null || name.trim().length() == 0) {
                if (node.hasAttribute("from") && node.hasAttribute("to")) { //$NON-NLS-1$ //$NON-NLS-2$
                    String from = node.getAttribute("from"); //$NON-NLS-1$
                    String to = node.getAttribute("to"); //$NON-NLS-1$
                    if (from.startsWith("{")) { //$NON-NLS-1$
                        name = from.substring(from.lastIndexOf("}")+1); //$NON-NLS-1$
                    } else if (from.startsWith("java:")) { //$NON-NLS-1$
                        name = from.substring(from.lastIndexOf('.')+1);
                    }
                    if (to.startsWith("{")) { //$NON-NLS-1$
                        name = name + "->" + to.substring(to.lastIndexOf("}")+1); //$NON-NLS-1$ //$NON-NLS-2$
                    } else if (to.startsWith("java:")) { //$NON-NLS-1$
                        name = name + "->" + to.substring(to.lastIndexOf('.')+1); //$NON-NLS-1$
                    }
                } else {
                    name = node.getLocalName();
                }
            }

            ExtendedArtifactType transformerArtifact = SwitchYardModel.newTransformerArtifact(name);
            String type = node.getLocalName();
            type = type.substring(type.lastIndexOf('.') + 1);
            SrampModelUtils.setCustomProperty(transformerArtifact, SwitchYardModel.PROP_TRANSFORMER_TYPE, type);
            if (node.hasAttribute("class")) { //$NON-NLS-1$
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationshipSources.add(new JavaRelationshipSource(node.getAttribute("class"), null,
                        relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_CLASS));
            }
            if (node.hasAttribute("bean")) { //$NON-NLS-1$
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                // TODO Implement finding a java class artifact by its cdi bean name
            }
            if (node.hasAttribute("xsltFile")) { //$NON-NLS-1$
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                // TODO Implement finding an xslt artifact in the repository
            }
            if (node.hasAttribute("config")) { //$NON-NLS-1$
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                // TODO Implement finding a smooks config artifact in the repository
            }
            if (node.hasAttribute("from")) { //$NON-NLS-1$
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_TRANSFORMS_FROM, null);
                String from = node.getAttribute("from");
                if (from.startsWith("{")) {
                    relationshipSources.add(new QNameRelationshipSource(QName.valueOf(from), null,
                            relationship.getRelationshipTarget(), ArtifactTypeEnum.ElementDeclaration.getModel(), ArtifactTypeEnum.ElementDeclaration.getType()));
                } else {
                    relationshipSources.add(new JavaRelationshipSource(from, null,
                            relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_CLASS));
                }
            }
            if (node.hasAttribute("to")) { //$NON-NLS-1$
                Relationship relationship = SrampModelUtils.addGenericRelationship(transformerArtifact, SwitchYardModel.REL_TRANSFORMS_TO, null);
                String to = node.getAttribute("to");
                if (to.startsWith("{")) {
                    relationshipSources.add(new QNameRelationshipSource(QName.valueOf(to), null,
                            relationship.getRelationshipTarget(), ArtifactTypeEnum.ElementDeclaration.getModel(), ArtifactTypeEnum.ElementDeclaration.getType()));
                } else {
                    relationshipSources.add(new JavaRelationshipSource(to, null,
                            relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_CLASS));
                }
            }

            getDerivedArtifacts().add(transformerArtifact);
        }
    }

    private void processValidators() throws XPathExpressionException {
        // xpath expression to find all services
        NodeList nodes = (NodeList) query(rootElement, "./swyd:validates/val:validate.java | ./swyd:validates/val:validate.xml", XPathConstants.NODESET); //$NON-NLS-1$
        for (int idx = 0; idx < nodes.getLength(); idx++) {
            Element node = (Element) nodes.item(idx);
            if (!node.hasAttribute("name")) //$NON-NLS-1$
                continue;

            String name = node.getAttribute("name"); //$NON-NLS-1$

            ExtendedArtifactType validatorArtifact = SwitchYardModel.newValidateArtifact(name);
            String type = node.getLocalName();
            type = type.substring(type.lastIndexOf('.') + 1);
            SrampModelUtils.setCustomProperty(validatorArtifact, SwitchYardModel.PROP_VALIDATE_TYPE, type);

            // Unresolved 'validates' reference
            Relationship relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_VALIDATES, null);
            if (name.startsWith("{")) {
                relationshipSources.add(new QNameRelationshipSource(QName.valueOf(name), null,
                        relationship.getRelationshipTarget(), ArtifactTypeEnum.ElementDeclaration.getModel(), ArtifactTypeEnum.ElementDeclaration.getType()));
            } else {
                relationshipSources.add(new JavaRelationshipSource(name, null,
                        relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_INTERFACE));
            }

            // Unresolved 'implementedBy' reference
            if (node.hasAttribute("class")) { //$NON-NLS-1$
                relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                relationshipSources.add(new JavaRelationshipSource(node.getAttribute("class"), null,
                        relationship.getRelationshipTarget(), "ext", JavaModel.TYPE_JAVA_CLASS));
            }
            if (node.hasAttribute("bean")) { //$NON-NLS-1$
                relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
                // TODO Implement finding a java class artifact by its cdi bean name
            }
            if (node.hasAttribute("schemaType")) { //$NON-NLS-1$
                // TODO handle relationships to the schemas
//                relationship = SrampModelUtils.addGenericRelationship(validatorArtifact, SwitchYardModel.REL_IMPLEMENTED_BY, null);
//                relationship.getOtherAttributes().put(UNRESOLVED_REF, "xml:" + node.getAttribute("schemaType"));
            }

            getDerivedArtifacts().add(validatorArtifact);
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
