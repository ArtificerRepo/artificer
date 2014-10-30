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
package org.overlord.sramp.integration.switchyard.model;

import java.util.UUID;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.query.xpath.StaticNamespaceContext;

/**
 * Information about the SwitchYard model implemented in S-RAMP by the
 * Switchyard artifact builder(s).
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardModel {

    public static final String SCA_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912"; //$NON-NLS-1$
    public static final String SWITCHYARD_NS = "urn:switchyard-config:switchyard:1.0"; //$NON-NLS-1$
    public static final String BEAN_NS = "urn:switchyard-component-bean:config:1.0"; //$NON-NLS-1$
    public static final String TRANSFORM_NS = "urn:switchyard-config:transform:1.0"; //$NON-NLS-1$
    public static final String VALIDATE_NS = "urn:switchyard-config:validate:1.0"; //$NON-NLS-1$
    public static final String SOAP_NS = "urn:switchyard-component-soap:config:1.0"; //$NON-NLS-1$
    public static final String CAMEL_NS = "urn:switchyard-component-camel:config:1.0"; //$NON-NLS-1$

    public static final String REL_IMPLEMENTS = "implements"; //$NON-NLS-1$
    public static final String REL_PROMOTES = "promotes"; //$NON-NLS-1$
    public static final String REL_USES = "uses"; //$NON-NLS-1$
    public static final String REL_TRANSFORMS_FROM = "transformsFrom"; //$NON-NLS-1$
    public static final String REL_TRANSFORMS_TO = "transformsTo"; //$NON-NLS-1$
    public static final String REL_IMPLEMENTED_BY = "implementedBy"; //$NON-NLS-1$
    public static final String REL_REFERENCES = "references"; //$NON-NLS-1$
    public static final String REL_VALIDATES = "validates"; //$NON-NLS-1$
    public static final String REL_OFFERS = "offers"; //$NON-NLS-1$

    public static final String PROP_TRANSFORMER_TYPE = "transformer-type"; //$NON-NLS-1$
    public static final String PROP_VALIDATE_TYPE = "validator-type"; //$NON-NLS-1$
    public static final String PROP_GATEWAY_TYPE = "gateway-type"; //$NON-NLS-1$

    public static final String SwitchYardApplication = "SwitchYardApplication"; //$NON-NLS-1$
    public static final String SwitchYardXmlDocument = "SwitchYardXmlDocument"; //$NON-NLS-1$
    public static final ArtifactType SwitchYardXmlDocumentType = ArtifactType.valueOf(SwitchYardXmlDocument);
    public static final String SwitchYardService = "SwitchYardService"; //$NON-NLS-1$
    public static final String SwitchYardComponent = "SwitchYardComponent"; //$NON-NLS-1$
    public static final String SwitchYardComponentService = "SwitchYardComponentService"; //$NON-NLS-1$
    public static final String SwitchYardTransformer = "SwitchYardTransformer"; //$NON-NLS-1$
    public static final String SwitchYardValidator = "SwitchYardValidator"; //$NON-NLS-1$
    public static final ArtifactType SwitchYardServiceType = ArtifactType.valueOf(SwitchYardService);
    public static final ArtifactType SwitchYardComponentType = ArtifactType.valueOf(SwitchYardComponent);
    public static final ArtifactType SwitchYardComponentServiceType = ArtifactType.valueOf(SwitchYardComponentService);
    public static final ArtifactType SwitchYardTransformerType = ArtifactType.valueOf(SwitchYardTransformer);
    public static final ArtifactType SwitchYardValidatorType = ArtifactType.valueOf(SwitchYardValidator);

    /**
     * Creates a new extended artifact type for a SwitchYard artifact.
     * @param name
     */
    private static final ExtendedArtifactType newArtifact(String switchYardType, String name) {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType(switchYardType);
        artifact.setUuid(UUID.randomUUID().toString());
        artifact.setName(name);
        return artifact;
    }

    /**
     * Creates a new extended artifact type for a SwitchYard Service.
     * @param name
     */
    public static final ExtendedArtifactType newServiceArtifact(String name) {
        return newArtifact(SwitchYardService, name);
    }

    /**
     * Creates a new extended artifact type for a SwitchYard Transform.
     * @param name
     */
    public static final ExtendedArtifactType newTransformerArtifact(String name) {
        return newArtifact(SwitchYardTransformer, name);
    }

    /**
     * Creates a new extended artifact type for a SwitchYard Validator.
     * @param name
     */
    public static final ExtendedArtifactType newValidateArtifact(String name) {
        return newArtifact(SwitchYardValidator, name);
    }

    /**
     * Creates a new extended artifact type for a SwitchYard Component.
     * @param name
     */
    public static final ExtendedArtifactType newComponentArtifact(String name) {
        return newArtifact(SwitchYardComponent, name);
    }

    /**
     * Creates a new extended artifact type for a SwitchYard Service Component.
     * @param name
     */
    public static final ExtendedArtifactType newComponentServiceArtifact(String name) {
        return newArtifact(SwitchYardComponentService, name);
    }

    /**
     * Adds some namespace mappings to the given namespace context.
     * @param namespaceContext
     */
    public static void addNamespaceMappings(StaticNamespaceContext namespaceContext) {
        namespaceContext.addMapping("sca", SwitchYardModel.SCA_NS); //$NON-NLS-1$
        namespaceContext.addMapping("swyd", SwitchYardModel.SWITCHYARD_NS); //$NON-NLS-1$
        namespaceContext.addMapping("bean", SwitchYardModel.BEAN_NS); //$NON-NLS-1$
        namespaceContext.addMapping("tf", SwitchYardModel.TRANSFORM_NS); //$NON-NLS-1$
        namespaceContext.addMapping("val", SwitchYardModel.VALIDATE_NS); //$NON-NLS-1$
        namespaceContext.addMapping("soap", SwitchYardModel.SOAP_NS); //$NON-NLS-1$
        namespaceContext.addMapping("camel", SwitchYardModel.CAMEL_NS); //$NON-NLS-1$
    }

}
