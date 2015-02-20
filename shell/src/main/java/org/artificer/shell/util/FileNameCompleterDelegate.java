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
import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.console.AeshContext;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.util.FileLister;


/**
 * File name completer delegate
 *
 * @author Brett Meyer
 */
public class FileNameCompleterDelegate {

    /**
     * Complete.
     *
     * @param completerInvocation
     */
    public static void complete(CompleterInvocation completerInvocation) {
        AeshContext context = completerInvocation.getAeshContext();
        String currentValue = completerInvocation.getGivenCompleteValue();

        CompleteOperation completeOperation = new CompleteOperation(context, currentValue, 0);
        if (StringUtils.isBlank(currentValue))
            new FileLister("", context.getCurrentWorkingDirectory()).
                    findMatchingDirectories(completeOperation);
        else
            new FileLister(currentValue,
                    context.getCurrentWorkingDirectory()).findMatchingDirectories(completeOperation);

        if (completeOperation.getCompletionCandidates().size() > 1) {
            completeOperation.removeEscapedSpacesFromCompletionCandidates();
        }

        completerInvocation.setCompleterValuesTerminalString(completeOperation.getCompletionCandidates());
        if (currentValue != null && completerInvocation.getCompleterValues().size() == 1) {
            completerInvocation.setAppendSpace(completeOperation.hasAppendSeparator());
        }
    }

}