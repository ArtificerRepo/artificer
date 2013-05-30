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
 * Visitor interface for visiting SwitchYard artifacts.
 * @author eric.wittmann@redhat.com
 */
public interface SwitchYardArtifactVisitor {

    public void visitService(ExtendedArtifactType artifact);
    public void visitComponent(ExtendedArtifactType artifact);
    public void visitTransformer(ExtendedArtifactType artifact);
    public void visitValidator(ExtendedArtifactType artifact);

}
