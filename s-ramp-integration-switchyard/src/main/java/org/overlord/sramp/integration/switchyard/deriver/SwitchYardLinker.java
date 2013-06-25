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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Relationship;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.Target;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.common.derived.LinkerContext;
import org.overlord.sramp.integration.java.model.JavaModel;
import org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor;
import org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitorHelper;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to link unresolved references found during the derive phase.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardLinker implements SwitchYardArtifactVisitor {

    private static Logger log = LoggerFactory.getLogger(SwitchYardLinker.class);

    private static ThreadLocal<LinkerContext> linkerContext = new ThreadLocal<LinkerContext>();

    /**
     * Constructor.
     */
    public SwitchYardLinker() {
    }

    /**
     * @see org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor#visitService(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    public void visitService(ExtendedArtifactType artifact) {
    }

    /**
     * @see org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor#visitComponent(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    public void visitComponent(ExtendedArtifactType artifact) {
        // Handle unresolved "implementedBy" relationships
        Relationship relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_IMPLEMENTED_BY);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("class:")) {
                String refClassName = ref.substring(6);
                BaseArtifactType artifactRef = findJavaClassArtifact(refClassName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }

        // Handle unresolved "references" relationships
        relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_REFERENCES);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("java:")) {
                String refInterfaceName = ref.substring(5);
                BaseArtifactType artifactRef = findJavaInterfaceArtifact(refInterfaceName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("wsdl:")) {
                String refWsdl = ref.substring(5);
                BaseArtifactType artifactRef = findWsdlArtifact(refWsdl);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }
    }

    /**
     * @see org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor#visitTransformer(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    public void visitTransformer(ExtendedArtifactType artifact) {
        // Handle unresolved "implementedBy" relationships
        Relationship relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_IMPLEMENTED_BY);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("class:")) {
                String refClassName = ref.substring(6);
                BaseArtifactType artifactRef = findJavaClassArtifact(refClassName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("bean:")) {
                String refBeanName = ref.substring(5);
                BaseArtifactType artifactRef = findCDIBeanArtifact(refBeanName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("xslt:")) {
                String refXsltFile = ref.substring(5);
                BaseArtifactType artifactRef = findXsltArtifact(refXsltFile);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("smooks:")) {
                String smooksConfig = ref.substring(7);
                BaseArtifactType artifactRef = findSmooksArtifact(smooksConfig);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("camel:")) {
                String camelPath = ref.substring(6);
                BaseArtifactType artifactRef = findCamelArtifact(camelPath);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }
        // Handle unresolved "transformsFrom" relationships
        relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_TRANSFORMS_FROM);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("java:")) { // java class
                String refClassName = ref.substring(5);
                BaseArtifactType artifactRef = findJavaClassArtifact(refClassName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("{")) { // xml type (qname)
                QName refQName = QName.valueOf(ref);
                BaseArtifactType artifactRef = findElementDeclarationArtifact(refQName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }
        // Handle unresolved "transformsTo" relationships
        relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_TRANSFORMS_TO);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("java:")) { // java class
                String refClassName = ref.substring(5);
                BaseArtifactType artifactRef = findJavaClassArtifact(refClassName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("{")) { // xml type (qname)
                QName refQName = QName.valueOf(ref);
                BaseArtifactType artifactRef = findElementDeclarationArtifact(refQName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }
    }

    /**
     * @see org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor#visitValidator(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    public void visitValidator(ExtendedArtifactType artifact) {
        // Handle unresolved "implementedBy" relationships
        Relationship relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_IMPLEMENTED_BY);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("class:")) {
                String refClassName = ref.substring(6);
                BaseArtifactType artifactRef = findJavaClassArtifact(refClassName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("bean:")) {
                String refBeanName = ref.substring(5);
                BaseArtifactType artifactRef = findCDIBeanArtifact(refBeanName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }
        // Handle unresolved "validates" relationships
        relationship = SrampModelUtils.getGenericRelationship(artifact, SwitchYardModel.REL_VALIDATES);
        if (relationship != null && relationship.getOtherAttributes().containsKey(SwitchYardXmlDeriver.UNRESOLVED_REF)) {
            String ref = relationship.getOtherAttributes().remove(SwitchYardXmlDeriver.UNRESOLVED_REF);
            if (ref.startsWith("java:")) { // java class
                String refClassName = ref.substring(5);
                BaseArtifactType artifactRef = findJavaClassArtifact(refClassName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            } else if (ref.startsWith("{")) { // xml type (qname)
                QName refQName = QName.valueOf(ref);
                BaseArtifactType artifactRef = findElementDeclarationArtifact(refQName);
                if (artifactRef != null) {
                    Target target = new Target();
                    target.setValue(artifactRef.getUuid());
                    relationship.getRelationshipTarget().add(target);
                }
            }
        }
    }

    /**
     * Finds an artifact by java classname.
     * @param refClassName
     */
    private BaseArtifactType findJavaClassArtifact(String className) {
        int idx = className.lastIndexOf('.');
        String packageName = className.substring(0, idx);
        String shortName = className.substring(idx+1);
        LinkerContext context = linkerContext.get();
        Map<String, String> criteria = new HashMap<String, String>();;
        criteria.put(JavaModel.PROP_PACKAGE_NAME, packageName);
        criteria.put(JavaModel.PROP_CLASS_NAME, shortName);
        Collection<BaseArtifactType> artifacts = context.findArtifacts("ext", JavaModel.TYPE_JAVA_CLASS, criteria);
        if (artifacts != null && !artifacts.isEmpty()) {
            return artifacts.iterator().next();
        } else {
            log.debug("No java class found for: " + className);
            return null;
        }
    }

    /**
     * Finds an artifact by java interface name.
     * @param refInterfaceName
     */
    private BaseArtifactType findJavaInterfaceArtifact(String interfaceName) {
        int idx = interfaceName.lastIndexOf('.');
        String packageName = interfaceName.substring(0, idx);
        String shortName = interfaceName.substring(idx+1);
        LinkerContext context = linkerContext.get();
        Map<String, String> criteria = new HashMap<String, String>();;
        criteria.put(JavaModel.PROP_PACKAGE_NAME, packageName);
        criteria.put(JavaModel.PROP_CLASS_NAME, shortName);
        Collection<BaseArtifactType> artifacts = context.findArtifacts("ext", JavaModel.TYPE_JAVA_INTERFACE, criteria);
        if (artifacts != null && !artifacts.isEmpty()) {
            return artifacts.iterator().next();
        } else {
            log.debug("No java interface found for: " + interfaceName);
            return null;
        }
    }

    /**
     * Finds an element declaration artifact by its QName.
     * @param refQName
     */
    private BaseArtifactType findElementDeclarationArtifact(QName refQName) {
        LinkerContext context = linkerContext.get();
        Map<String, String> criteria = new HashMap<String, String>();;
        criteria.put("namespace", refQName.getNamespaceURI());
        criteria.put("ncName", refQName.getLocalPart());
        Collection<BaseArtifactType> artifacts = context.findArtifacts(
                ArtifactTypeEnum.ElementDeclaration.getModel(),
                ArtifactTypeEnum.ElementDeclaration.getType(),
                criteria);
        if (artifacts != null && !artifacts.isEmpty()) {
            return artifacts.iterator().next();
        } else {
            log.debug("No element declaration found for: " + refQName);
            return null;
        }
    }

    /**
     * Finds a smooks config artifact.
     * @param smooksConfig
     */
    private BaseArtifactType findSmooksArtifact(String smooksConfig) {
        // TODO Implement finding a smooks config artifact in the repository
        return null;
    }

    /**
     * Finds a camel route.xml artifact by path.
     * @param camelXmlPath
     */
    private BaseArtifactType findCamelArtifact(String camelXmlPath) {
        // TODO Implement finding a smooks config artifact in the repository
        return null;
    }

    /**
     * Finds an xslt artifact.
     * @param xsltFile
     */
    private BaseArtifactType findXsltArtifact(String xsltFile) {
        // TODO Implement finding an xslt artifact in the repository
        return null;
    }

    /**
     * Finds a java class artifact given its CDI bean name.
     * @param beanName
     */
    private BaseArtifactType findCDIBeanArtifact(String beanName) {
        // TODO Implement finding a java class artifact by its cdi bean name
        return null;
    }

    /**
     * Finds an artifact by WSDL information in the switchyard.xml.
     * @param refWsdl
     */
    private BaseArtifactType findWsdlArtifact(String refWsdl) {
        // TODO Implement finding an artifact in the s-ramp repo given the wsdl information in switchyard.xml (why couldn't a QName be used in there???)
        return null;
    }

    /**
     * Visits the artifact to do the linking.
     * @param context
     * @param derivedArtifact
     */
    public void link(LinkerContext context, ExtendedArtifactType derivedArtifact) {
        linkerContext.set(context);
        SwitchYardArtifactVisitorHelper.visitArtifact(derivedArtifact, this);
    }

}
