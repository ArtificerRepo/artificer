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

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType;

/**
 * Helper used to visit switchyard artifacts.
 * @author eric.wittmann@redhat.com
 */
public class SwitchYardArtifactVisitorHelper {

    /**
     * Visits a switchyard artifact.
     * @param artifact
     * @param visitor
     */
    public static final void visitArtifact(ExtendedArtifactType artifact, SwitchYardArtifactVisitor visitor) {
        if (SwitchYardModel.SwitchYardService.equals(artifact.getExtendedType())) {
            visitor.visitService(artifact);
        } else if (SwitchYardModel.SwitchYardComponent.equals(artifact.getExtendedType())) {
            visitor.visitComponent(artifact);
        } else if (SwitchYardModel.SwitchYardTransformer.equals(artifact.getExtendedType())) {
            visitor.visitTransformer(artifact);
        } else if (SwitchYardModel.SwitchYardValidator.equals(artifact.getExtendedType())) {
            visitor.visitValidator(artifact);
        } else {
            throw new RuntimeException("Visitor pattern not implemented for SwitchYard artifact of type: " + artifact.getExtendedType());
        }
    }

}
