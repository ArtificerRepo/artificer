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
package org.overlord.sramp.integration.switchyard.jar;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.atom.archive.jar.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.jar.DiscoveredArtifact;
import org.overlord.sramp.atom.archive.jar.JarToSrampArchiveContext;
import org.overlord.sramp.integration.switchyard.model.SwitchYardModel;

/**
 * The meta-data factory used when expanding a SwitchYard JAR/WAR into an S-RAMP
 * archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardMetaDataFactory extends DefaultMetaDataFactory {

    /**
     * Constructor.
     */
    public SwitchYardMetaDataFactory() {
    }

    /**
     * @see org.overlord.sramp.atom.archive.jar.MetaDataFactory#setContext(org.overlord.sramp.atom.archive.jar.JarToSrampArchiveContext)
     */
    @Override
    public void setContext(JarToSrampArchiveContext context) {
    }

    /**
     * @see org.overlord.sramp.atom.archive.jar.DefaultMetaDataFactory#createArtifact(org.overlord.sramp.atom.archive.jar.DiscoveredArtifact)
     */
    @Override
    protected BaseArtifactType createArtifact(DiscoveredArtifact discoveredArtifact) {
        if (discoveredArtifact.getName().equals("switchyard.xml")) {
            ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(SwitchYardModel.SwitchYardXmlDocument);
            return artifact;
        }
        if ("class".equals(discoveredArtifact.getExtension())) {
            ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType("JavaClass");
            return artifact;
        }
        return super.createArtifact(discoveredArtifact);
    }

}
