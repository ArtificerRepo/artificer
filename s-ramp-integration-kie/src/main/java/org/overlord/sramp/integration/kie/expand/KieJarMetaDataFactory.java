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
package org.overlord.sramp.integration.kie.expand;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactEnum;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedDocument;
import org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory;
import org.overlord.sramp.atom.archive.expand.DiscoveredArtifact;
import org.overlord.sramp.integration.kie.model.KieJarModel;

/**
 * The meta-data factory used when expanding a Kie JAR into an S-RAMP archive.
 *
 */
public class KieJarMetaDataFactory extends DefaultMetaDataFactory {

    /**
     * Constructor.
     */
    public KieJarMetaDataFactory() {
    }

    /**
     * @see org.overlord.sramp.atom.archive.expand.DefaultMetaDataFactory#createArtifact(org.overlord.sramp.atom.archive.expand.DiscoveredArtifact)
     */
    @Override
    protected BaseArtifactType createArtifact(DiscoveredArtifact discoveredArtifact) {
    	String name = discoveredArtifact.getName().toLowerCase();
        if (name.equals("kmodule.xml")) { //$NON-NLS-1$
            ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(KieJarModel.KieXmlDocument);
            return artifact;
        }
        if (name.endsWith(".bpmn") || name.endsWith(".bpmn2")) { //$NON-NLS-1$ //$NON-NLS-2$
            ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(KieJarModel.BpmnDocument);
            return artifact;
        }
        if (name.endsWith(".drl")) { //$NON-NLS-1$
            ExtendedDocument artifact = new ExtendedDocument();
            artifact.setArtifactType(BaseArtifactEnum.EXTENDED_DOCUMENT);
            artifact.setExtendedType(KieJarModel.DroolsDocument);
            return artifact;
        }
        return super.createArtifact(discoveredArtifact);
    }
}
