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

import java.io.IOException;

import javax.xml.namespace.QName;

import org.jboss.aesh.console.AeshConsoleBufferBuilder;
import org.jboss.aesh.console.AeshInputProcessorBuilder;
import org.jboss.aesh.console.ConsoleBuffer;
import org.jboss.aesh.console.InputProcessor;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.terminal.Shell;
import org.jboss.aesh.util.ANSI;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.aesh.SrampCommandInvocation;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Abstract base class for all built-in shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class BuiltInShellCommand extends AbstractShellCommand<SrampCommandInvocation> {

    protected QName s_ramp_client_name = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$

    protected SrampAtomApiClient client;

    private Shell shell;
    private CommandInvocation commandInvocation;

    /**
     * Instantiates a new built in shell command.
     */
    public BuiltInShellCommand() {



    }

    /**
     * Prints the usage.
     *
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
     */
    @Override
    public void printUsage() {
        print(Messages.i18n.format(getClass().getSimpleName() + ".usage")); //$NON-NLS-1$
    }

    /**
     * Prints the help.
     *
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
     */
    @Override
    public void printHelp() {
        print(Messages.i18n.format(getClass().getSimpleName() + ".help")); //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.sramp.shell.api.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        if (getContext() != null && getContext().getVariable(s_ramp_client_name) != null) {
            client = (SrampAtomApiClient) getContext().getVariable(s_ramp_client_name);
        }
        return true;
    }


    /*
     * (non-Javadoc)
     *
     * @see
     * org.overlord.sramp.shell.api.AbstractShellCommand#execute(org.jboss.aesh
     * .console.command.invocation.CommandInvocation)
     */
    @Override
    public CommandResult execute(SrampCommandInvocation commandInvocation) throws IOException {
        this.commandInvocation = commandInvocation;
        boolean result = false;
        this.shell = commandInvocation.getShell();
        if (this.getContext() == null) {
            this.setContext(commandInvocation.getContext());
        }

        if (client == null && getContext().getVariable(s_ramp_client_name) != null) {
            client = (SrampAtomApiClient) getContext().getVariable(s_ramp_client_name);
        }

        if (isHelp()) {
            shell.out().println(commandInvocation.getHelpInfo(getName()));
        } else {
            try {
                result = this.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        if (result) {
            return CommandResult.SUCCESS;
        } else {
            return CommandResult.FAILURE;
        }
    }


    /**
     * Stop.
     */
    private void stop() {
        shell.out().print(ANSI.getMainBufferScreen());
    }

    /**
     * Prompt for input.
     *
     * @param prompt
     *            the prompt
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String promptForInput(String prompt) throws IOException {
        return promptForInput(prompt, null);
    }

    /**
     * Prompt for input.
     *
     * @param prompt
     *            the prompt
     * @param mask
     *            the mask
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public String promptForInput(String prompt, Character mask) throws IOException {

        ConsoleBuffer consoleBuffer = new AeshConsoleBufferBuilder().shell(commandInvocation.getShell())
                .prompt(new Prompt(prompt, mask)).create();
        InputProcessor inputProcessor = new AeshInputProcessorBuilder().consoleBuffer(consoleBuffer)
                .create();

        consoleBuffer.displayPrompt();
        try {
            String result;
            do {
                result = inputProcessor.parseOperation(commandInvocation.getInput());
            } while (result == null);
            return result;
        } catch (InterruptedException e) {
            e.printStackTrace();
            stop();
            return null;
        }
    }

    /**
     * Gets the command name.
     *
     * @return the name
     */
    public abstract String getName();

    /**
     * Checks if the help option has been introduced.
     * 
     * @return true, if is help
     */
    protected boolean isHelp() {
        return false;
    }

    /**
     * Gets the command invocation.
     *
     * @return the command invocation
     */
    public CommandInvocation getCommandInvocation() {
        return commandInvocation;
    }

    /**
     * Sets the command invocation.
     *
     * @param commandInvocation
     *            the new command invocation
     */
    public void setCommandInvocation(CommandInvocation commandInvocation) {
        this.commandInvocation = commandInvocation;
    }

    /**
     * Gets the shell.
     *
     * @return the shell
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * Sets the shell.
     *
     * @param shell
     *            the new shell
     */
    public void setShell(Shell shell) {
        this.shell = shell;
    }


}
