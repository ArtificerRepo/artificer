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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
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
import org.overlord.sramp.client.shell.commands.core.ClassificationCommand;
import org.overlord.sramp.client.shell.commands.core.ConnectCommand;
import org.overlord.sramp.client.shell.commands.core.DisconnectCommand;
import org.overlord.sramp.client.shell.commands.core.GetContentCommand;
import org.overlord.sramp.client.shell.commands.core.GetMetaDataCommand;
import org.overlord.sramp.client.shell.commands.core.PropertyCommand;
import org.overlord.sramp.client.shell.commands.core.QueryCommand;
import org.overlord.sramp.client.shell.commands.core.RefreshMetaDataCommand;
import org.overlord.sramp.client.shell.commands.core.ShowMetaDataCommand;
import org.overlord.sramp.client.shell.commands.core.StatusCommand;
import org.overlord.sramp.client.shell.commands.core.UpdateContentCommand;
import org.overlord.sramp.client.shell.commands.core.UpdateMetaDataCommand;
import org.overlord.sramp.client.shell.commands.core.UploadArtifactCommand;
import org.overlord.sramp.client.shell.commands.ontology.DeleteOntologyCommand;
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

		// S-RAMP core commands
		registry.put(new QName("s-ramp", "connect"), ConnectCommand.class);
		registry.put(new QName("s-ramp", "disconnect"), DisconnectCommand.class);
		registry.put(new QName("s-ramp", "status"), StatusCommand.class);
		registry.put(new QName("s-ramp", "query"), QueryCommand.class);
		registry.put(new QName("s-ramp", "getMetaData"), GetMetaDataCommand.class);
		registry.put(new QName("s-ramp", "getContent"), GetContentCommand.class);
		registry.put(new QName("s-ramp", "upload"), UploadArtifactCommand.class);
		registry.put(new QName("s-ramp", "updateMetaData"), UpdateMetaDataCommand.class);
		registry.put(new QName("s-ramp", "updateContent"), UpdateContentCommand.class);
		registry.put(new QName("s-ramp", "property"), PropertyCommand.class);
		registry.put(new QName("s-ramp", "classification"), ClassificationCommand.class);
		registry.put(new QName("s-ramp", "showMetaData"), ShowMetaDataCommand.class);
		registry.put(new QName("s-ramp", "refreshMetaData"), RefreshMetaDataCommand.class);

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
		registry.put(new QName("ontology", "delete"), DeleteOntologyCommand.class);

		discoverContributedCommands();
	}

    /**
     * Discover any contributed commands, both on the classpath and registered
     * in the .sramp/commands.ini file in the user's home directory.
     */
    private void discoverContributedCommands() {
        List<ClassLoader> commandClassloaders = new ArrayList<ClassLoader>();
        commandClassloaders.add(Thread.currentThread().getContextClassLoader());

        // Register commands listed in the user's commands.ini config file
        String userHome = System.getProperty("user.home", "/");
        String commandsFileName = System.getProperty("s-ramp.shell.commandsIni",
                userHome + "/.s-ramp/commands.ini");
        File commandsFile = new File(commandsFileName);
        if (commandsFile.isFile()) {
            try {
                List<String> lines = FileUtils.readLines(commandsFile);
                for (String line : lines) {
                    if (!line.startsWith("#") && line.trim().length() > 0) {
                        try {
                            File f = new File(line);
                            if (f.exists()) {
                                commandClassloaders.add(new URLClassLoader(new URL[] { f.toURI().toURL() }));
                            }
                            continue;
                        } catch (Throwable t) {
                            // skip error
                        }
                        try {
                            URL url = new URL(line);
                            URLConnection urlConnection = url.openConnection();
                            InputStream inputStream = urlConnection.getInputStream();
                            inputStream.close();
                            // We have a valid URL!
                            commandClassloaders.add(new URLClassLoader(new URL[] { url }));
                        } catch (Throwable t) {
                            // skip error
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Now that we have identified all ClassLoaders to check for commands, iterate
        // through them all and use the Java ServiceLoader mechanism to actually
        // load the commands.
        for (ClassLoader classLoader : commandClassloaders) {
            for (ShellCommandProvider provider : ServiceLoader.load(ShellCommandProvider.class, classLoader)) {
                Map<String, Class<? extends ShellCommand>> commands = provider.provideCommands();
                for (Map.Entry<String, Class<? extends ShellCommand>> entry : commands.entrySet()) {
                    QName qualifiedCmdName = new QName(provider.getNamespace(), entry.getKey());
                    registry.put(qualifiedCmdName, entry.getValue());
                }
            }
        }
    }

	/**
	 * Called to create a shell command.
	 * @param commandName
	 * @throws Exception
	 */
	public ShellCommand createCommand(QName commandName) throws Exception {
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

	/**
	 * Gets the set of namespaces for all commands in the factory.
	 */
	public Set<String> getNamespaces() {
		Set<String> namespaces = new TreeSet<String>();
		for (QName cmdName : this.registry.keySet()) {
			namespaces.add(cmdName.getNamespaceURI());
		}
		return namespaces;
	}

	/**
	 * Gets the set of commands available within the given namespace.
	 * @param namespace
	 */
	public Set<QName> getCommandNames(String namespace) {
		Set<QName> commandNames = new TreeSet<QName>(new Comparator<QName>() {
			@Override
			public int compare(QName o1, QName o2) {
				return o1.getLocalPart().compareTo(o2.getLocalPart());
			}
		});
		for (QName cmdName : this.registry.keySet()) {
			if (namespace.equals(cmdName.getNamespaceURI())) {
				commandNames.add(cmdName);
			}
		}
		return commandNames;
	}

}
