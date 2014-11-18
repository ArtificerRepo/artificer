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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
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
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.api.ShellCommandProvider;
import org.overlord.sramp.shell.commands.CommandNotFoundCommand;
import org.overlord.sramp.shell.commands.EchoCommand;
import org.overlord.sramp.shell.commands.ExitCommand;
import org.overlord.sramp.shell.commands.HelpCommand;
import org.overlord.sramp.shell.commands.archive.AddEntryArchiveCommand;
import org.overlord.sramp.shell.commands.archive.CloseArchiveCommand;
import org.overlord.sramp.shell.commands.archive.ListArchiveCommand;
import org.overlord.sramp.shell.commands.archive.ListEntryArchiveCommand;
import org.overlord.sramp.shell.commands.archive.NewArchiveCommand;
import org.overlord.sramp.shell.commands.archive.OpenArchiveCommand;
import org.overlord.sramp.shell.commands.archive.PackArchiveCommand;
import org.overlord.sramp.shell.commands.archive.RemoveEntryArchiveCommand;
import org.overlord.sramp.shell.commands.archive.UpdateEntryArchiveCommand;
import org.overlord.sramp.shell.commands.archive.UploadArchiveCommand;
import org.overlord.sramp.shell.commands.audit.ShowAuditTrailCommand;
import org.overlord.sramp.shell.commands.core.*;
import org.overlord.sramp.shell.commands.maven.DeployCommand;
import org.overlord.sramp.shell.commands.ontology.DeleteOntologyCommand;
import org.overlord.sramp.shell.commands.ontology.GetOntologyCommand;
import org.overlord.sramp.shell.commands.ontology.ListOntologiesCommand;
import org.overlord.sramp.shell.commands.ontology.UpdateOntologyCommand;
import org.overlord.sramp.shell.commands.ontology.UploadOntologyCommand;
import org.overlord.sramp.shell.commands.storedquery.CreateStoredQueryCommand;
import org.overlord.sramp.shell.commands.storedquery.DeleteStoredQueryCommand;
import org.overlord.sramp.shell.commands.storedquery.ExecuteStoredQueryCommand;
import org.overlord.sramp.shell.commands.storedquery.GetStoredQueryCommand;
import org.overlord.sramp.shell.commands.storedquery.ListStoredQueriesCommand;
import org.overlord.sramp.shell.commands.storedquery.UpdateStoredQueryCommand;

/**
 * Factory used to create shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public class ShellCommandFactory {

	private static QName HELP_CMD_NAME = new QName("s-ramp", "help"); //$NON-NLS-1$ //$NON-NLS-2$
	private static QName EXIT_CMD_NAME = new QName("s-ramp", "exit"); //$NON-NLS-1$ //$NON-NLS-2$
	private static QName QUIT_CMD_NAME = new QName("s-ramp", "quit"); //$NON-NLS-1$ //$NON-NLS-2$
    private static QName ECHO_CMD_NAME = new QName("s-ramp", "echo"); //$NON-NLS-1$ //$NON-NLS-2$

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
		registry.put(new QName("s-ramp", "connect"), ConnectCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "disconnect"), DisconnectCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "status"), StatusCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "query"), QueryCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "getMetaData"), GetMetaDataCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "getContent"), GetContentCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "upload"), UploadArtifactCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "updateMetaData"), UpdateMetaDataCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "updateContent"), UpdateContentCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "property"), PropertyCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "classification"), ClassificationCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "showMetaData"), ShowMetaDataCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "refreshMetaData"), RefreshMetaDataCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "delete"), DeleteCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("s-ramp", "deleteContent"), DeleteContentCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "create"), CreateArtifactCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "exit"), ExitCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "createStoredQuery"), CreateStoredQueryCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "deleteStoredQuery"), DeleteStoredQueryCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "getStoredQuery"), GetStoredQueryCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "listStoredQueries"), ListStoredQueriesCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "updateStoredQuery"), UpdateStoredQueryCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("s-ramp", "executeStoredQuery"), ExecuteStoredQueryCommand.class); //$NON-NLS-1$ //$NON-NLS-2$

		// Archive commands
		registry.put(new QName("archive", "new"), NewArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "open"), OpenArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "close"), CloseArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "list"), ListArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "addEntry"), AddEntryArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "updateEntry"), UpdateEntryArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "removeEntry"), RemoveEntryArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "listEntry"), ListEntryArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("archive", "pack"), PackArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("archive", "upload"), UploadArchiveCommand.class); //$NON-NLS-1$ //$NON-NLS-2$

		// Ontology commands
		registry.put(new QName("ontology", "upload"), UploadOntologyCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("ontology", "list"), ListOntologiesCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
		registry.put(new QName("ontology", "delete"), DeleteOntologyCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("ontology", "get"), GetOntologyCommand.class); //$NON-NLS-1$ //$NON-NLS-2$
        registry.put(new QName("ontology", "update"), UpdateOntologyCommand.class); //$NON-NLS-1$ //$NON-NLS-2$

        // Audit commands
        registry.put(new QName("audit", "showAuditTrail"), ShowAuditTrailCommand.class); //$NON-NLS-1$ //$NON-NLS-2$

        // Maven commands
        registry.put(new QName("maven", "deploy"), DeployCommand.class); //$NON-NLS-1$ //$NON-NLS-2$

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
        String userHome = System.getProperty("user.home", "/"); //$NON-NLS-1$ //$NON-NLS-2$
        String commandsDirName = System.getProperty("s-ramp.shell.commandsDir", //$NON-NLS-1$
                userHome + "/.s-ramp/commands"); //$NON-NLS-1$
        File commandsDir = new File(commandsDirName);
        if (!commandsDir.exists()) {
            commandsDir.mkdirs();
        }
        if (commandsDir.isDirectory()) {
            try {
                Collection<File> jarFiles = FileUtils.listFiles(commandsDir, new String[] { "jar" }, false); //$NON-NLS-1$
                List<URL> jarURLs = new ArrayList<URL>(jarFiles.size());
                for (File jarFile : jarFiles) {
                    jarURLs.add(jarFile.toURI().toURL());
                }
                URL[] urls = jarURLs.toArray(new URL[jarURLs.size()]);
                ClassLoader extraCommandsCL = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                commandClassloaders.add(extraCommandsCL);
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
            command = new HelpCommand(getCommands(), this.getNamespaces());
		} else if (commandName.equals(QUIT_CMD_NAME)) {
			command = new ExitCommand();
        } else if (commandName.equals(EXIT_CMD_NAME)) {
            command = new ExitCommand();
        } else if (commandName.equals(ECHO_CMD_NAME)) {
            command = new EchoCommand();
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
