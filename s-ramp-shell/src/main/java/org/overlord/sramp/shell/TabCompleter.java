/*
 * Copyright 2012 JBoss Inc
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

import org.jboss.aesh.complete.CompleteOperation;
import org.jboss.aesh.complete.Completion;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.api.ShellContext;

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
	 * @param factory
	 * @param context
	 */
	public TabCompleter(ShellCommandFactory factory, ShellContext context) {
		this.factory = factory;
		this.context = context;
	}


    @Override
    public void complete(CompleteOperation completeOperation) {
        String buffer = completeOperation.getBuffer();
        // Case 1 - nothing has been typed yet - show all command namespaces
        if (buffer.trim().length() == 0) {
            for (String ns : factory.getNamespaces()) {
                completeOperation.addCompletionCandidate(ns + ":"); //$NON-NLS-1$
            }
        }

        // Case 2 - a partial namespace has been typed - show all namespaces
        // that match
        if (!buffer.contains(":") && !buffer.contains(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
            for (String ns : factory.getNamespaces()) {
                if (ns.startsWith(buffer)) {
                    completeOperation.addCompletionCandidate(ns + ":"); //$NON-NLS-1$
                }
            }
            // If no namespaces matched, then try to match the default commands
            if (completeOperation.getCompletionCandidates().isEmpty()) {
                for (QName cmdName : factory.getCommandNames("s-ramp")) { //$NON-NLS-1$
                    if (cmdName.getLocalPart().startsWith(buffer)) {
                        completeOperation.addCompletionCandidate("s-ramp:" + cmdName.getLocalPart() + " "); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                }
            }

        }

        // Case 3 - a namespace has been typed and we're waiting at the colon
        if (buffer.endsWith(":") && !buffer.contains(" ")) { //$NON-NLS-1$ //$NON-NLS-2$
            String ns = buffer.substring(0, buffer.length() - 1);
            for (QName cmdName : factory.getCommandNames(ns)) {
                completeOperation.addCompletionCandidate(cmdName.getLocalPart() + " "); //$NON-NLS-1$
            }

        }

        // Case 4 - a partial command has been typed - show all command names
        // that match
        if (buffer.contains(":") && !buffer.endsWith(":") && !buffer.contains(" ")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            int colonIdx = buffer.indexOf(':');
            String ns = buffer.substring(0, colonIdx);
            String name = buffer.substring(colonIdx + 1);
            for (QName cmdName : factory.getCommandNames(ns)) {
                if (cmdName.getLocalPart().startsWith(name)) {
                    completeOperation.addCompletionCandidate(cmdName.getLocalPart() + " "); //$NON-NLS-1$
                }
            }
        }

        // Case 5 - a full command name has been entered, delegate further
        // procesing
        // to the specific command

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
        try {
            ShellCommand command = factory.createCommand(commandName);
            if (command != null) {
                command.setContext(this.context);
                command.setArguments(arguments);

                List<CharSequence> list = new ArrayList<CharSequence>();
                command.tabCompletion(lastArgument, list);
                if (!list.isEmpty()) {
                    for (CharSequence sequence : list) {
                        completeOperation.addCompletionCandidate(sequence.toString());
                    }
                }
            }
        } catch (Exception e) {
        }

    }

}
