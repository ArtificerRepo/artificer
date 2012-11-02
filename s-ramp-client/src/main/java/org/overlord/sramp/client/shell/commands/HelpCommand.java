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
package org.overlord.sramp.client.shell.commands;

import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellCommand;
import org.overlord.sramp.client.shell.ShellContext;

/**
 * Implements the 'help' command.
 *
 * @author eric.wittmann@redhat.com
 */
public class HelpCommand extends AbstractShellCommand {

	private Map<QName, Class<? extends ShellCommand>> commands;

	/**
	 * Constructor.
	 * @param commands
	 */
	public HelpCommand(Map<QName, Class<? extends ShellCommand>> commands) {
		this.commands = commands;
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
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
	}

	/**
	 * Prints the generic help (lists all commands, etc).
	 */
	private void printHelpAll() {
		print("The S-RAMP Shell supports the following commands:");
		String currentNamespace = null;
		int colCount = 0;
		StringBuilder builder = new StringBuilder();
		for (Entry<QName,Class<? extends ShellCommand>> entry : this.commands.entrySet()) {
			QName cmdName = entry.getKey();
			String namespace = cmdName.getNamespaceURI();
			String name = cmdName.getLocalPart();
			if (!namespace.equals(currentNamespace)) {
				builder.append(String.format("\n\nNamespace: %1$s\n-----------------------\n", namespace));
				currentNamespace = namespace;
				builder.append(String.format("  %1$-18s", name));
				colCount = 0;
			} else {
				builder.append(String.format("%1$-18s", name));
				colCount++;
			}
			if (colCount == 3) {
				builder.append("\n  ");
				colCount = 0;
			}
		}
		print(builder.toString());
		print("\n");
		print("To get help for a specific command, try 'help <cmdNamespace>:<cmdName>'.");
		print("");
		print("To execute a specific command, try '<namespace>:<commandName> <args>'.");
		print("Some examples:");
		print("   s-ramp:connect http://localhost:8080/s-ramp-atom/s-ramp");
		print("   archive:open /home/uname/files/my-package.sramp");
		print("");
	}

	/**
	 * Prints the help for a single namespace.
	 * @param namespace
	 */
	private void printHelpForNamespace(String namespace) {
		System.out.printf("The S-RAMP Shell supports the following commands for the '%1$s' namespace:\n", namespace);
		for (Entry<QName,Class<? extends ShellCommand>> entry : this.commands.entrySet()) {
			QName cmdName = entry.getKey();
			String ns = cmdName.getNamespaceURI();
			String name = cmdName.getLocalPart();
			if (ns.equals(namespace)) {
				print("   " + name);
			}
		}
		print("");
		print("To get help for a specific command, try 'help <cmdNamespace>:<cmdName>'.");
	}

	/**
	 * Prints the help for a single command.
	 * @param cmdName
	 * @throws Exception
	 */
	private void printHelpForCommand(QName cmdName) throws Exception {
		Class<? extends ShellCommand> commandClass = this.commands.get(cmdName);
		if (commandClass == null) {
			System.out.printf("No help available:  not a valid command");
		} else {
			ShellCommand command = commandClass.newInstance();
			System.out.print("Usage: ");
			command.printUsage();
			print("");
			command.printHelp();
			print("");
		}
	}

}
