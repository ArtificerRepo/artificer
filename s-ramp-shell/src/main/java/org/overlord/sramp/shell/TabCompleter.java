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
package org.overlord.sramp.shell;

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.commands.CommandNotFoundCommand;

/**
 * Implements tab completion for the interactive
 *
 * @author eric.wittmann@redhat.com
 */
public class TabCompleter implements Completion {

    private final ShellCommandFactory factory;
    private final ShellContext context;

    /**
     * Constructor.
     *
     * @param factory
     *            the factory
     * @param context
     *            the context
     */
    public TabCompleter(ShellCommandFactory factory, ShellContext context) {
        this.factory = factory;
        this.context = context;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.aesh.complete.Completion#complete(org.jboss.aesh.complete.
     * CompleteOperation)
     */
    @Override
    public void complete(CompleteOperation completeOperation) {
        String buffer = completeOperation.getBuffer();
        // Case 1 - nothing has been typed yet - show all command namespaces
        if (buffer.trim().length() == 0) {
            for (String ns : factory.getNamespaces()) {
                completeOperation.addCompletionCandidate(ns + ":"); //$NON-NLS-1$
            }
        } else {
            if (!buffer.contains(":") && !buffer.contains(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
                //This is the case of archive:list and archive:listEntry.
                //If the user do not type " " at the end then when click on tab button should display the two options archive:list and archive:listEntry.
                // Case 2 - a partial namespace has been typed - show all
                // namespaces that match
                for (String ns : factory.getNamespaces()) {
                    if (ns.startsWith(buffer)) {
                        completeOperation.addCompletionCandidate(ns + ":"); //$NON-NLS-1$
                    }
                }
                // If no namespaces matched, then try to match the default
                // commands

                if (completeOperation.getCompletionCandidates().isEmpty()) {
                    for (QName cmdName : factory.getCommandNames("s-ramp")) { //$NON-NLS-1$
                        if (cmdName.getLocalPart().startsWith(buffer)) {
                            completeOperation.addCompletionCandidate(cmdName.getLocalPart());

                        }
                    }
                } else if (completeOperation.getCompletionCandidates().size() == 1) {
                    completeOperation.doAppendSeparator(false);
                }
            }
            // Case 3 - a namespace has been typed and we're waiting at the colon
            else if (buffer.endsWith(":") && !buffer.contains(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
                String ns = buffer.substring(0, buffer.length() - 1);
                for (QName cmdName : factory.getCommandNames(ns)) {
                    completeOperation.addCompletionCandidate(cmdName.getLocalPart() + " "); //$NON-NLS-1$
                }
                // TODO: This can be removed later on, once we upgrade to a more recent aesh.  It should do this
                // automatically, but for now we need to set it.  Otherwise, commands get chopped off (SRAMP-534)
                if (completeOperation.getCompletionCandidates().size() == 1) {
                    completeOperation.setOffset(completeOperation.getCursor());
                }
            }
            // Case 4 - a partial command has been typed - show all command
            // names that match
            else if (buffer.contains(":") && !buffer.endsWith(":") && !buffer.contains(" ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                int colonIdx = buffer.indexOf(':');
                String ns = buffer.substring(0, colonIdx);
                String name = buffer.substring(colonIdx + 1);
                for (QName cmdName : factory.getCommandNames(ns)) {
                    if (cmdName.getLocalPart().startsWith(name)) {
                        completeOperation.addCompletionCandidate(cmdName.getNamespaceURI()+":"+cmdName.getLocalPart()); //$NON-NLS-1$
                    }
                }
            } else {
                // We check the first thing if what is introduced is a command or not.
                // Check if what was introduced is a command itself
                Arguments arguments = null;
                try {
                    arguments = new Arguments(buffer, true);
                } catch (InvalidCommandArgumentException e1) {
                    // should never happen...but if it does, just bail
                }
                QName commandName = arguments.removeCommandName();
                String lastArgument = null;
                if (arguments.size() > 0 && !buffer.endsWith(" ")) { //$NON-NLS-1$
                    lastArgument = arguments.remove(arguments.size() - 1);
                }
                ShellCommand command = null;
                try {
                    command = factory.createCommand(commandName);
                } catch (Exception e) {
                }
                // In case it is a command then we print the tabCompletion
                // specific of the command
                if (command != null && !(command instanceof CommandNotFoundCommand)) {
                    command.setContext(this.context);
                    command.setArguments(arguments);

                    List<CharSequence> list = new ArrayList<CharSequence>();
                    int tabCompletionResult = command.tabCompletion(lastArgument, list);
                    if (!list.isEmpty()) {
                        // In case the tab completion return just one result it
                        // is printed the previous buffer plus the argument
                        if (list.size() == 1) {
                            if (buffer.endsWith(" ")) { //$NON-NLS-1$
                                completeOperation.addCompletionCandidate(buffer
                                        + list.get(0).toString().trim());
                            } else if (buffer.indexOf(" ") != -1) { //$NON-NLS-1$
                                completeOperation.addCompletionCandidate(buffer.substring(0,
                                        buffer.lastIndexOf(" ")) //$NON-NLS-1$
                                        + " " + list.get(0).toString().trim()); //$NON-NLS-1$
                            } else {
                                completeOperation.addCompletionCandidate(buffer + " " //$NON-NLS-1$
                                        + list.get(0).toString().trim());
                            }

                        } else {
                            // In case the result of the command tab completion
                            // contains more than one result (like the FileNameCompleter)
                            for (CharSequence sequence : list) {
                                completeOperation.addCompletionCandidate(sequence.toString());
                            }
                        }
                        if (tabCompletionResult == CompletionConstants.NO_APPEND_SEPARATOR) {
                            completeOperation.doAppendSeparator(false);
                        }

                    }
                }

            }
        }

        String commonPartCandidates = mergeCandidates(completeOperation.getCompletionCandidates(), buffer);
        if (StringUtils.isNotBlank(commonPartCandidates)) {
            String tokenToCompare = ""; //$NON-NLS-1$
            if (buffer.contains(" ")) { //$NON-NLS-1$
                tokenToCompare = buffer.substring(buffer.lastIndexOf(" ") + 1); //$NON-NLS-1$
            } else {
                tokenToCompare = buffer;
            }
            completeOperation.getCompletionCandidates().clear();
            if (StringUtils.isBlank(tokenToCompare) || commonPartCandidates.startsWith(tokenToCompare)) {
                if (buffer.contains(" ")) { //$NON-NLS-1$
                    completeOperation.addCompletionCandidate(buffer.substring(0, buffer.lastIndexOf(" ")) //$NON-NLS-1$
                            .trim() + " " + commonPartCandidates); //$NON-NLS-1$
                } else {
                    completeOperation.addCompletionCandidate(commonPartCandidates);
                }

            } else {
                completeOperation.addCompletionCandidate(commonPartCandidates);
            }

            completeOperation.doAppendSeparator(false);
        }

    }

    /**
     * Merge candidates.
     *
     * @param completionCandidates
     *            the completion candidates
     * @param buffer
     *            the buffer
     * @return the string
     */
    private String mergeCandidates(List<String> completionCandidates, String buffer) {
        if (completionCandidates.size() > 1) {
            int indexOfDifference = StringUtils.indexOfDifference(completionCandidates
                    .toArray(new String[completionCandidates.size()]));
            if (indexOfDifference == -1) {
                return completionCandidates.get(0);
            } else {
                String partToCompare = ""; //$NON-NLS-1$
                String commonPart = completionCandidates.get(0).substring(0, indexOfDifference);
                if (commonPart.startsWith(buffer)) {
                    partToCompare = buffer;
                } else {
                    partToCompare = buffer.substring(buffer.lastIndexOf(" ") + 1); //$NON-NLS-1$
                }
                if (partToCompare.length() != indexOfDifference) {
                    return commonPart;
                }

            }
        }
        return ""; //$NON-NLS-1$

    }
}
