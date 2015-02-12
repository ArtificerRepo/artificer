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
package org.artificer.shell.commands;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.CompletionConstants;
import org.artificer.shell.api.ShellCommand;

/**
 * Implements the 'help' command.
 *
 * @author eric.wittmann@redhat.com
 */
public class HelpCommand extends BuiltInShellCommand {

	private final Map<QName, Class<? extends ShellCommand>> commands;

    private final Set<String> namespaces;

	    /**
     * Constructor.
     *
     * @param commands
     *            the commands
     * @param namespaces
     *            the namespaces
     */
    public HelpCommand(Map<QName, Class<? extends ShellCommand>> commands, Set<String> namespaces) {
		this.commands = commands;
        this.namespaces = namespaces;
	}

	    /**
     * Prints the usage.
     */
	@Override
	public void printUsage() {
	}

	    /**
     * Prints the help.
     */
	@Override
	public void printHelp() {
	}

	    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
	@Override
	public boolean execute() throws Exception {
		String namespaceOrCmdName = optionalArgument(0);
		if (namespaceOrCmdName == null) {
			printHelpAll();
		} else {
			int colonIdx = namespaceOrCmdName.indexOf(':');
			if (colonIdx == -1) {
				printHelpForNamespace(namespaceOrCmdName);
			} else {
				String ns = namespaceOrCmdName.substring(0, colonIdx);
				String localPart = namespaceOrCmdName.substring(colonIdx + 1);
				QName cmdName = new QName(ns, localPart);
				printHelpForCommand(cmdName);
			}
		}
        return true;
	}

	/**
	 * Prints the generic help (lists all commands, etc).
	 */
	private void printHelpAll() {
		print(Messages.i18n.format("Help.COMMAND_LIST_MSG")); //$NON-NLS-1$
		String currentNamespace = null;
		int colCount = 0;
		StringBuilder builder = new StringBuilder();
		for (Entry<QName,Class<? extends ShellCommand>> entry : this.commands.entrySet()) {
			QName cmdName = entry.getKey();
			String namespace = cmdName.getNamespaceURI();
			String name = cmdName.getLocalPart();
			if (!namespace.equals(currentNamespace)) {
				builder.append(String.format("\n\nNamespace: %1$s\n-----------------------\n", namespace)); //$NON-NLS-1$
				currentNamespace = namespace;
				builder.append(String.format("  %1$-18s", name)); //$NON-NLS-1$
				colCount = 0;
			} else {
				builder.append(String.format("%1$-18s", name)); //$NON-NLS-1$
				colCount++;
			}
			if (colCount == 3) {
				builder.append("\n  "); //$NON-NLS-1$
				colCount = 0;
			}
		}
		print(builder.toString());
		print("\n"); //$NON-NLS-1$
		print(Messages.i18n.format("Help.GET_HELP_1")); //$NON-NLS-1$
		print(""); //$NON-NLS-1$
		print(Messages.i18n.format("Help.GET_HELP_2")); //$NON-NLS-1$
		print(""); //$NON-NLS-1$
	}

	    /**
     * Prints the help for a single namespace.
     *
     * @param namespace
     *            the namespace
     */
	private void printHelpForNamespace(String namespace) {
		print(Messages.i18n.format("Help.COMMAND_LIST_MSG_2", namespace)); //$NON-NLS-1$
		for (Entry<QName,Class<? extends ShellCommand>> entry : this.commands.entrySet()) {
			QName cmdName = entry.getKey();
			String ns = cmdName.getNamespaceURI();
			String name = cmdName.getLocalPart();
			if (ns.equals(namespace)) {
				print("   " + name); //$NON-NLS-1$
			}
		}
		print(""); //$NON-NLS-1$
		print(Messages.i18n.format("Help.HELP_PER_CMD_MSG")); //$NON-NLS-1$
	}

	    /**
     * Prints the help for a single command.
     *
     * @param cmdName
     *            the cmd name
     * @throws Exception
     *             the exception
     */
	private void printHelpForCommand(QName cmdName) throws Exception {
		Class<? extends ShellCommand> commandClass = this.commands.get(cmdName);
		if (commandClass == null) {
			print(Messages.i18n.format("Help.INVALID_COMMAND")); //$NON-NLS-1$
		} else {
			ShellCommand command = commandClass.newInstance();
			print(Messages.i18n.format("Help.USAGE")); //$NON-NLS-1$
			command.printUsage();
			print(""); //$NON-NLS-1$
			command.printHelp();
			print(""); //$NON-NLS-1$
		}
	}

	    /**
     * Tab completion.
     *
     * @param lastArgument
     *            the last argument
     * @param candidates
     *            the candidates
     * @return the int
     */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
            String namespace = getNamespaceIfExist(lastArgument);
            if (StringUtils.isNotBlank(namespace)) {
                candidates.add(namespace);
                return CompletionConstants.NO_APPEND_SEPARATOR;
            } else {
                for (String candidate : generateHelpCandidates()) {
                    if (lastArgument == null || candidate.startsWith(lastArgument)) {
                        candidates.add(candidate);
                    }
                }
            }

			return 0;
		} else {
			return -1;
		}
	}

    /**
     * Gets the namespace if exist.
     *
     * @param candidate
     *            the candidate
     * @return the namespace if exist
     */
    private String getNamespaceIfExist(String candidate) {
        if (StringUtils.isNotBlank(candidate)) {
            for (String namespace : namespaces) {
                if (namespace.startsWith(candidate)) {
                    return namespace + ":"; //$NON-NLS-1$
                }
            }
        }

        return null;
    }

	    /**
     * Generate help candidates.
     *
     * @return a collection of all possible command names
     */
	private Collection<String> generateHelpCandidates() {
		TreeSet<String> candidates = new TreeSet<String>();
		for (QName key : this.commands.keySet()) {
			String candidate = key.getNamespaceURI() + ":" + key.getLocalPart(); //$NON-NLS-1$
			candidates.add(candidate);
		}
		return candidates;
	}

}
