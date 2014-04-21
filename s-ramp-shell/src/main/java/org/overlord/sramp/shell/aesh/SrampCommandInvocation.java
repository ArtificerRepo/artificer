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
package org.overlord.sramp.shell.aesh;

import org.jboss.aesh.console.AeshContext;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.CommandOperation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.operator.ControlOperator;
import org.jboss.aesh.terminal.Shell;
import org.overlord.sramp.shell.ShellCommandFactory;
import org.overlord.sramp.shell.api.ShellContext;

/**
 * Implementation class of the Aesh CommandInvocation. This is done to allow put
 * in the aesh context some objects necessary to our custom commands. The
 * CommandInvocation object is the only object with access from the commands
 * during execution.
 * 
 * @author David Virgil Naranjo
 */
public class SrampCommandInvocation implements CommandInvocation {

    private final CommandInvocation commandInvocation;

    private final ShellContext context;

    private final ShellCommandFactory factory;

    /**
     * Instantiates a new sramp command invocation.
     *
     * @param commandInvocation
     *            the command invocation
     * @param context
     *            the context
     * @param factory
     *            the factory
     */
    public SrampCommandInvocation(CommandInvocation commandInvocation, ShellContext context,
            ShellCommandFactory factory) {
        this.commandInvocation = commandInvocation;
        this.context = context;
        this.factory = factory;
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getControlOperator()
     */
    @Override
    public ControlOperator getControlOperator() {
        return commandInvocation.getControlOperator();
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getCommandRegistry()
     */
    @Override
    public CommandRegistry getCommandRegistry() {
        return commandInvocation.getCommandRegistry();
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getShell()
     */
    @Override
    public Shell getShell() {
        return commandInvocation.getShell();
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#setPrompt(org.jboss.aesh.console.Prompt)
     */
    @Override
    public void setPrompt(Prompt prompt) {
        commandInvocation.setPrompt(prompt);
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getPrompt()
     */
    @Override
    public Prompt getPrompt() {
        return commandInvocation.getPrompt();
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getHelpInfo(java.lang.String)
     */
    @Override
    public String getHelpInfo(String commandName) {
        return commandInvocation.getHelpInfo(commandName);
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#stop()
     */
    @Override
    public void stop() {
        commandInvocation.stop();
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getAeshContext()
     */
    @Override
    public AeshContext getAeshContext() {
        return commandInvocation.getAeshContext();
    }

    /* (non-Javadoc)
     * @see org.jboss.aesh.console.command.invocation.CommandInvocation#getInput()
     */
    @Override
    public CommandOperation getInput() throws InterruptedException {
        return commandInvocation.getInput();
    }

    /**
     * Gets the context.
     *
     * @return the context
     */
    public ShellContext getContext() {
        return context;
    }

    /**
     * Gets the factory.
     *
     * @return the factory
     */
    public ShellCommandFactory getFactory() {
        return factory;
    }

}
