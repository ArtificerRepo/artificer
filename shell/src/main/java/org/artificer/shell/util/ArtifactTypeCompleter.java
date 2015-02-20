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
package org.artificer.shell.util;

import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtifactTypeEnum;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.completer.CompleterInvocation;


/**
 * Artifact type completer
 *
 * @author Brett Meyer
 */
public class ArtifactTypeCompleter implements OptionCompleter<CompleterInvocation> {

    @Override
    public void complete(CompleterInvocation completerInvocation) {
        String currentValue = completerInvocation.getGivenCompleteValue();

        for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
            String candidate = t.getType();
            if (StringUtils.isBlank(currentValue) || candidate.startsWith(currentValue)) {
                completerInvocation.addCompleterValue(candidate);
            }
        }
    }
}