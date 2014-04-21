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
package org.overlord.sramp.shell.commands;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.ShellCommandFactory;
import org.overlord.sramp.shell.aesh.SrampCommandInvocation;
import org.overlord.sramp.shell.api.ShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Implements the 'help' command.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.COMMAND_HELP, description = "Exit from the shell")
public class HelpCommand extends BuiltInShellCommand {

    private Map<QName, Class<? extends ShellCommand>> _commands;

    @Option(required = false, name = "namespace", hasValue = true, shortName = 'n', completer = NamespaceCompleter.class)
    private String _namespace;

    @Option(required = false, name = "commandName", hasValue = true, shortName = 'c', completer = CommandNameCompleter.class)
    private String _commandName;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
    public HelpCommand() {
        ShellCommandFactory factory = new ShellCommandFactory();
        _commands = factory.getCommands();
    }

    @Override
    public CommandResult execute(SrampCommandInvocation commandInvocation) throws IOException {
        _commands = commandInvocation.getFactory().getCommands();
        return super.execute(commandInvocation);
    }
    /**
     * Constructor.
     *
     * @param commands
     */
    public HelpCommand(Map<QName, Class<? extends ShellCommand>> commands) {
        this._commands = commands;
    }


    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
     */
    @Override
    public void printUsage() {
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
     */
    @Override
    public void printHelp() {
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        super.execute();
        if (StringUtils.isBlank(_namespace) && StringUtils.isBlank(_commandName)) {
            printHelpAll();
        } else if (StringUtils.isBlank(_commandName)) {
            printHelpForNamespace();
        } else {
            printHelpForCommand();
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
        for (Entry<QName, Class<? extends ShellCommand>> entry : this._commands.entrySet()) {
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
     * @param _namespace
     */
    private void printHelpForNamespace() {
        print(Messages.i18n.format("Help.COMMAND_LIST_MSG_2", _namespace)); //$NON-NLS-1$
        for (Entry<QName, Class<? extends ShellCommand>> entry : this._commands.entrySet()) {
            QName cmdName = entry.getKey();
            String ns = cmdName.getNamespaceURI();
            String name = cmdName.getLocalPart();
            if (ns.equals(_namespace)) {
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
     * @throws Exception
     */
    private void printHelpForCommand() throws Exception {
        if (getCommandInvocation() == null) {
            QName cmdName = new QName(_namespace, _commandName);
            Class<? extends ShellCommand> commandClass = this._commands.get(cmdName);
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
        } else {
            if (getCommandInvocation().getCommandRegistry().getCommand(
                    _namespace + ShellCommandConstants.SEPARATOR + _commandName, null) != null) {

                super.getShell()
                        .out()
                        .println(
                                getCommandInvocation().getHelpInfo(
                                        _namespace + ShellCommandConstants.SEPARATOR + _commandName));
            }

        }
    }



    /**
     * @return a collection of all possible namespace names
     */
    private Set<String> generateNamespaceCandidates(String givenValue) {
        TreeSet<String> candidates = new TreeSet<String>();
        for (QName key : this._commands.keySet()) {
            if (key.getNamespaceURI().startsWith(givenValue) && !candidates.contains(key.getNamespaceURI())) {
                String candidate = key.getNamespaceURI();
                candidates.add(candidate);
            }
        }
        return candidates;
    }

    /**
     * @return a collection of all possible command names
     */
    private Set<String> generateCommandNamesCandidates(String namespace, String givenValue) {
        TreeSet<String> candidates = new TreeSet<String>();
        for (QName key : this._commands.keySet()) {
            if (StringUtils.isBlank(namespace)) {
                if (key.getLocalPart().startsWith(givenValue)) {
                    String candidate = key.getLocalPart(); //$NON-NLS-1$
                    candidates.add(candidate);
                }
            } else {
                if (key.getLocalPart().startsWith(givenValue) && namespace.equals(key.getNamespaceURI())) {
                    candidates.add(key.getLocalPart());
                }
            }

        }
        return candidates;
    }

    protected class NamespaceCompleter implements OptionCompleter<CompleterInvocation> {

        @Override
        public void complete(CompleterInvocation completerInvocation) {
            completerInvocation.clearCompleterValues();
            Set<String> candidates = generateNamespaceCandidates(completerInvocation.getGivenCompleteValue());

            for (String candidate : candidates) {
                completerInvocation.addCompleterValue(candidate);
            }
        }

    }

    protected class CommandNameCompleter implements OptionCompleter<CompleterInvocation> {

        @Override
        public void complete(CompleterInvocation completerInvocation) {
            completerInvocation.clearCompleterValues();
            HelpCommand current = ((HelpCommand) completerInvocation.getCommand());
            Set<String> candidates = generateCommandNamesCandidates(current.getNamespace(),
                    completerInvocation.getGivenCompleteValue());

            for (String candidate : candidates) {
                completerInvocation.addCompleterValue(candidate);
            }
        }

    }

    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.COMMAND_HELP;
    }

    public Map<QName, Class<? extends ShellCommand>> getCommands() {
        return _commands;
    }

    public void setCommands(Map<QName, Class<? extends ShellCommand>> commands) {
        this._commands = commands;
    }

    public String getNamespace() {
        return _namespace;
    }

    public void setNamespace(String namespace) {
        this._namespace = namespace;
    }

    public String getCommandName() {
        return _commandName;
    }

    public void setCommandName(String commandName) {
        this._commandName = commandName;
    }

    @Override
    public boolean isHelp() {
        return _help;
    }

    public void setHelp(boolean help) {
        this._help = help;
    }

}
