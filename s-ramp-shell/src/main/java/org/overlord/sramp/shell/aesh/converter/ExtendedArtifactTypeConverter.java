/*
 * Copyright 2014 JBoss Inc
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
package org.overlord.sramp.shell.aesh.converter;

import org.jboss.aesh.cl.converter.Converter;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.converter.ConverterInvocation;
import org.overlord.sramp.common.ArtifactType;

/**
 * Converts the input string to an Extended Artifact type object
 * 
 * @author David Virgil Naranjo
 */
public class ExtendedArtifactTypeConverter implements Converter<ArtifactType, ConverterInvocation> {

    /* (non-Javadoc)
     * @see org.jboss.aesh.cl.converter.Converter#convert(org.jboss.aesh.console.command.converter.ConverterInvocation)
     */
    @Override
    public ArtifactType convert(ConverterInvocation converterInvocation) throws OptionValidatorException {
        ArtifactType artifactType = ArtifactType.valueOf(converterInvocation.getInput());
        if (artifactType.isExtendedType()) {
            artifactType = ArtifactType.ExtendedArtifactType(artifactType.getExtendedType(), false);
        }
        return artifactType;
    }
}