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
 * Base class for switchyard visitors.
 * @author eric.wittmann@redhat.com
 */
public class AbstractSwitchYardArtifactVisitor implements SwitchYardArtifactVisitor {

    /**
     * Constructor.
     */
    public AbstractSwitchYardArtifactVisitor() {
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
    }

    /**
     * @see org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor#visitTransformer(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    public void visitTransformer(ExtendedArtifactType artifact) {
    }

    /**
     * @see org.overlord.sramp.integration.switchyard.model.SwitchYardArtifactVisitor#visitValidator(org.oasis_open.docs.s_ramp.ns.s_ramp_v1.ExtendedArtifactType)
     */
    @Override
    public void visitValidator(ExtendedArtifactType artifact) {
    }

}
