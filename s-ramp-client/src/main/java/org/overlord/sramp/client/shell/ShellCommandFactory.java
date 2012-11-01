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
package org.overlord.sramp.client.shell;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.commands.CommandNotFoundCommand;
import org.overlord.sramp.client.shell.commands.ExitCommand;
import org.overlord.sramp.client.shell.commands.HelpCommand;
import org.overlord.sramp.client.shell.commands.archive.AddEntryArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.CloseArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.ListArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.ListEntryArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.NewArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.OpenArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.PackArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.RemoveEntryArchiveCommand;
import org.overlord.sramp.client.shell.commands.archive.UpdateEntryArchiveCommand;
import org.overlord.sramp.client.shell.commands.core.ConnectCommand;
import org.overlord.sramp.client.shell.commands.core.DisconnectCommand;
import org.overlord.sramp.client.shell.commands.core.GetContentCommand;
import org.overlord.sramp.client.shell.commands.core.GetMetaDataCommand;
import org.overlord.sramp.client.shell.commands.core.QueryCommand;
import org.overlord.sramp.client.shell.commands.core.UploadArtifactCommand;
import org.overlord.sramp.client.shell.commands.ontology.ListOntologiesCommand;
import org.overlord.sramp.client.shell.commands.ontology.UploadOntologyCommand;

/**
 * Factory used to create shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShellCommandFactory {

	private static QName HELP_CMD_NAME = new QName("s-ramp", "help");
	private static QName EXIT_CMD_NAME = new QName("s-ramp", "exit");
	private static QName QUIT_CMD_NAME = new QName("s-ramp", "quit");

	private Map<QName, Class<? extends ShellCommand>> registry;

	/**
	 * Constructor.
	 */
	public ShellCommandFactory() {
		registerCommands();
	}

	/**
	 * Registers all known commands.
	 */
	private void registerCommands() {
		registry = new HashMap<QName, Class<? extends ShellCommand>>();

		// S-RAMP client commands
		registry.put(new QName("s-ramp", "connect"), ConnectCommand.class);
		registry.put(new QName("s-ramp", "disconnect"), DisconnectCommand.class);
		registry.put(new QName("s-ramp", "query"), QueryCommand.class);
		registry.put(new QName("s-ramp", "getMetaData"), GetMetaDataCommand.class);
		registry.put(new QName("s-ramp", "getContent"), GetContentCommand.class);
		registry.put(new QName("s-ramp", "upload"), UploadArtifactCommand.class);

		// Archive commands
		registry.put(new QName("archive", "new"), NewArchiveCommand.class);
		registry.put(new QName("archive", "open"), OpenArchiveCommand.class);
		registry.put(new QName("archive", "close"), CloseArchiveCommand.class);
		registry.put(new QName("archive", "list"), ListArchiveCommand.class);
		registry.put(new QName("archive", "addEntry"), AddEntryArchiveCommand.class);
		registry.put(new QName("archive", "updateEntry"), UpdateEntryArchiveCommand.class);
		registry.put(new QName("archive", "removeEntry"), RemoveEntryArchiveCommand.class);
		registry.put(new QName("archive", "listEntry"), ListEntryArchiveCommand.class);
		registry.put(new QName("archive", "pack"), PackArchiveCommand.class);

		// Ontology commands
		registry.put(new QName("ontology", "upload"), UploadOntologyCommand.class);
		registry.put(new QName("ontology", "list"), ListOntologiesCommand.class);
	}

	/**
	 * Called to create a shell command.
	 * @param commandName
	 * @param args
	 * @throws Exception
	 */
	public ShellCommand createCommand(QName commandName, String [] args) throws Exception {
		ShellCommand command = null;
		if (commandName.equals(HELP_CMD_NAME)) {
			command = new HelpCommand(getCommands());
		} else if (commandName.equals(QUIT_CMD_NAME)) {
			command = new ExitCommand();
		} else if (commandName.equals(EXIT_CMD_NAME)) {
			command = new ExitCommand();
		} else {
			Class<? extends ShellCommand> commandClass = registry.get(commandName);
			if (commandClass == null)
				return new CommandNotFoundCommand();
			command = commandClass.newInstance();
		}
		command.setArguments(args);
		return command;
	}

	/**
	 * Gets the available commands, ordered by command {@link QName}.
	 */
	private Map<QName, Class<? extends ShellCommand>> getCommands() {
		TreeMap<QName, Class<? extends ShellCommand>> treeMap = new TreeMap<QName, Class<? extends ShellCommand>>(new Comparator<QName>() {
			@Override
			public int compare(QName name1, QName name2) {
				return name1.toString().compareTo(name2.toString());
			}
		});
		treeMap.putAll(this.registry);
		return treeMap;
	}

}
