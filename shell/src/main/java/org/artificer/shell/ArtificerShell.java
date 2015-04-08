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
package org.artificer.shell;

import org.artificer.shell.archive.AddEntryArchiveCommand;
import org.artificer.shell.archive.CloseArchiveCommand;
import org.artificer.shell.archive.ListArchiveCommand;
import org.artificer.shell.archive.ListEntryArchiveCommand;
import org.artificer.shell.archive.NewArchiveCommand;
import org.artificer.shell.archive.OpenArchiveCommand;
import org.artificer.shell.archive.PackArchiveCommand;
import org.artificer.shell.archive.RemoveEntryArchiveCommand;
import org.artificer.shell.archive.UpdateEntryArchiveCommand;
import org.artificer.shell.archive.UploadArchiveCommand;
import org.artificer.shell.audit.ShowAuditTrailCommand;
import org.artificer.shell.core.AddCommentCommand;
import org.artificer.shell.core.ClassificationCommand;
import org.artificer.shell.core.ConnectCommand;
import org.artificer.shell.core.CreateArtifactCommand;
import org.artificer.shell.core.DeleteCommand;
import org.artificer.shell.core.DeleteContentCommand;
import org.artificer.shell.core.DisconnectCommand;
import org.artificer.shell.core.GetContentCommand;
import org.artificer.shell.core.GetMetaDataCommand;
import org.artificer.shell.core.PropertyCommand;
import org.artificer.shell.core.QueryCommand;
import org.artificer.shell.core.RefreshMetaDataCommand;
import org.artificer.shell.core.ShowMetaDataCommand;
import org.artificer.shell.core.StatusCommand;
import org.artificer.shell.core.UpdateContentCommand;
import org.artificer.shell.core.UpdateMetaDataCommand;
import org.artificer.shell.core.UploadArtifactCommand;
import org.artificer.shell.maven.DeployCommand;
import org.artificer.shell.ontology.DeleteOntologyCommand;
import org.artificer.shell.ontology.GetOntologyCommand;
import org.artificer.shell.ontology.ListOntologiesCommand;
import org.artificer.shell.ontology.OntologyStatusCommand;
import org.artificer.shell.ontology.UpdateOntologyCommand;
import org.artificer.shell.ontology.UploadOntologyCommand;
import org.artificer.shell.storedquery.CreateStoredQueryCommand;
import org.artificer.shell.storedquery.DeleteStoredQueryCommand;
import org.artificer.shell.storedquery.ExecuteStoredQueryCommand;
import org.artificer.shell.storedquery.GetStoredQueryCommand;
import org.artificer.shell.storedquery.ListStoredQueriesCommand;
import org.artificer.shell.storedquery.UpdateStoredQueryCommand;
import org.jboss.aesh.cl.GroupCommandDefinition;
import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Console;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.Command;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.AeshCommandRegistryBuilder;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.helper.InterruptHook;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.jboss.aesh.edit.actions.Action;
import org.jboss.aesh.extensions.clear.Clear;
import org.jboss.aesh.extensions.echo.Echo;
import org.jboss.aesh.extensions.exit.Exit;

import java.io.IOException;
import java.util.Locale;

/**
 * @author Brett Meyer.
 */
public class ArtificerShell {

    @GroupCommandDefinition(name = "archive", description = "",
            groupCommands = {AddEntryArchiveCommand.class, CloseArchiveCommand.class, ListArchiveCommand.class,
                    ListEntryArchiveCommand.class, NewArchiveCommand.class, OpenArchiveCommand.class,
                    PackArchiveCommand.class, RemoveEntryArchiveCommand.class, UpdateEntryArchiveCommand.class,
                    UploadArchiveCommand.class})
    public static class ArchiveCommands implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
            return CommandResult.SUCCESS;
        }
    }

    @GroupCommandDefinition(name = "audit", description = "", groupCommands = {ShowAuditTrailCommand.class})
    public static class AuditCommands implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
            return CommandResult.SUCCESS;
        }
    }

    @GroupCommandDefinition(name = "maven", description = "", groupCommands = {DeployCommand.class})
    public static class MavenCommands implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
            return CommandResult.SUCCESS;
        }
    }

    @GroupCommandDefinition(name = "ontology", description = "",
            groupCommands = {DeleteOntologyCommand.class, GetOntologyCommand.class, ListOntologiesCommand.class,
                    OntologyStatusCommand.class, UpdateOntologyCommand.class, UploadOntologyCommand.class})
    public static class OntologyCommands implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
            return CommandResult.SUCCESS;
        }
    }

    @GroupCommandDefinition(name = "storedQuery", description = "",
            groupCommands = {CreateStoredQueryCommand.class, DeleteStoredQueryCommand.class, ExecuteStoredQueryCommand.class,
                    GetStoredQueryCommand.class, ListStoredQueriesCommand.class, UpdateStoredQueryCommand.class})
    public static class StoredQueryCommands implements Command {
        @Override
        public CommandResult execute(CommandInvocation commandInvocation) throws IOException, InterruptedException {
            return CommandResult.SUCCESS;
        }
    }

    public static void main(String[] args) {
        CommandRegistry registry = new AeshCommandRegistryBuilder()
                // aesh-extensions
                .command(Clear.class)
                .command(Echo.class)
                .command(Exit.class)
                // artificer core
                .command(AddCommentCommand.class)
                .command(ClassificationCommand.class)
                .command(ConnectCommand.class)
                .command(CreateArtifactCommand.class)
                .command(DeleteCommand.class)
                .command(DeleteContentCommand.class)
                .command(DisconnectCommand.class)
                .command(GetContentCommand.class)
                .command(GetMetaDataCommand.class)
                .command(PropertyCommand.class)
                .command(QueryCommand.class)
                .command(RefreshMetaDataCommand.class)
                .command(ShowMetaDataCommand.class)
                .command(StatusCommand.class)
                .command(UpdateContentCommand.class)
                .command(UpdateMetaDataCommand.class)
                .command(UploadArtifactCommand.class)
                // artificer groups
                .command(ArchiveCommands.class)
                .command(AuditCommands.class)
                .command(MavenCommands.class)
                .command(OntologyCommands.class)
                .command(StoredQueryCommands.class)

                .create();

        SettingsBuilder settingsBuilder = new SettingsBuilder();
        settingsBuilder.readInputrc(false);
        settingsBuilder.logging(true);
        settingsBuilder.aeshContext(new ArtificerContext());

        // map Ctrl+C to a 'clear' action, rather than 'exit'
        settingsBuilder.interruptHook(new InterruptHook() {
            @Override
            public void handleInterrupt(Console console, Action action) {
                if (action == Action.INTERRUPT) {
                    console.getShell().out().println("^C");
                    console.clearBufferAndDisplayPrompt();
                } else {
                    console.stop();
                    console.currentProcessFinished(null);
                }
            }
        });

        AeshConsole console = new AeshConsoleBuilder()
                .commandRegistry(registry)
                .settings(settingsBuilder.create())
                .create();

        console.getShell().out().println("**********************************************************************");
        console.getShell().out().println("*  Artificer CLI");
        console.getShell().out().println("*  Licensed under Apache License V2.0, Copyright 2015");
        console.getShell().out().println("*  Locale: " + Locale.getDefault().toString().trim());
        console.getShell().out().println("**********************************************************************");

        console.setPrompt(new Prompt("artificer> "));
        console.start();

        // TODO
        /* String locale_str = System.getProperty(LOCALE_PROPERTY);
        if (locale_str != null) {
            String lang = null;
            String region = null;
            String[] lsplit = locale_str.split("_"); //$NON-NLS-1$
            if (lsplit.length > 0) {
                lang = lsplit[0];
            }
            if (lsplit.length > 1) {
                region = lsplit[1];
            }
            if (lang != null && region != null) {
                Locale.setDefault(new Locale(lang, region));
            } else if (lang != null) {
                Locale.setDefault(new Locale(lang));
            }
        }*/

        // TODO
//        /**
//         * Discover any contributed commands, both on the classpath and registered
//         * in the .sramp/commands.ini file in the user's home directory.
//         */
//        private void discoverContributedCommands() {
//            List<ClassLoader> commandClassloaders = new ArrayList<ClassLoader>();
//            commandClassloaders.add(Thread.currentThread().getContextClassLoader());
//
//            // Register commands listed in the user's commands.ini config file
//            String userHome = System.getProperty("user.home", "/"); //$NON-NLS-1$ //$NON-NLS-2$
//            String commandsDirName = System.getProperty("artificer.shell.commandsDir", //$NON-NLS-1$
//                    userHome + "/.artificer/commands"); //$NON-NLS-1$
//            File commandsDir = new File(commandsDirName);
//            if (!commandsDir.exists()) {
//                commandsDir.mkdirs();
//            }
//            if (commandsDir.isDirectory()) {
//                try {
//                    Collection<File> jarFiles = FileUtils.listFiles(commandsDir, new String[]{"jar"}, false); //$NON-NLS-1$
//                    List<URL> jarURLs = new ArrayList<URL>(jarFiles.size());
//                    for (File jarFile : jarFiles) {
//                        jarURLs.add(jarFile.toURI().toURL());
//                    }
//                    URL[] urls = jarURLs.toArray(new URL[jarURLs.size()]);
//                    ClassLoader extraCommandsCL = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
//                    commandClassloaders.add(extraCommandsCL);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            // Now that we have identified all ClassLoaders to check for commands, iterate
//            // through them all and use the Java ServiceLoader mechanism to actually
//            // load the commands.
//            for (ClassLoader classLoader : commandClassloaders) {
//                for (ShellCommandProvider provider : ServiceLoader.load(ShellCommandProvider.class, classLoader)) {
//                    Map<String, Class<? extends ShellCommand>> commands = provider.provideCommands();
//                    for (Map.Entry<String, Class<? extends ShellCommand>> entry : commands.entrySet()) {
//                        QName qualifiedCmdName = new QName(provider.getNamespace(), entry.getKey());
//                        registry.put(qualifiedCmdName, entry.getValue());
//                    }
//                }
//            }
//        }
    }
}
