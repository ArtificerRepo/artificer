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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;
import org.overlord.sramp.common.ArtifactType;

/**
 * Information about the SwitchYard model implemented in S-RAMP by the
 * Switchyard Deriver(s).
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardModel {

    public static final String SCA_NS = "http://docs.oasis-open.org/ns/opencsa/sca/200912";
    public static final String SWITCHYARD_NS = "urn:switchyard-config:switchyard:1.0";
    public static final String BEAN_NS = "urn:switchyard-component-bean:config:1.0";
    public static final String TRANSFORM_NS = "urn:switchyard-config:transform:1.0";
    public static final String VALIDATE_NS = "urn:switchyard-config:validate:1.0";
    public static final String SOAP_NS = "urn:switchyard-component-soap:config:1.0";

    public static final String SwitchYardService = "SwitchYardService";
    public static final ArtifactType SwitchYardServiceType = ArtifactType.valueOf(SwitchYardService);


    /**
     * Creates a new extended artifact type for a SwitchYard Service.
     * @param name
     */
    public static final ExtendedArtifactType newServiceArtifact(String name) {
        ExtendedArtifactType artifact = new ExtendedArtifactType();
        artifact.setArtifactType(BaseArtifactEnum.EXTENDED_ARTIFACT_TYPE);
        artifact.setExtendedType(SwitchYardService);
        artifact.setName(name);
        return artifact;
    }

}
