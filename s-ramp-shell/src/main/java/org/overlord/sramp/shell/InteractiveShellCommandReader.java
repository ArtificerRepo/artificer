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
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;

import org.jboss.aesh.console.AeshConsole;
import org.jboss.aesh.console.AeshConsoleBuilder;
import org.jboss.aesh.console.Prompt;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocationProvider;
import org.jboss.aesh.console.command.invocation.CommandInvocationServices;
import org.jboss.aesh.console.command.registry.CommandRegistry;
import org.jboss.aesh.console.settings.Settings;
import org.jboss.aesh.console.settings.SettingsBuilder;
import org.overlord.sramp.shell.aesh.SrampAeshCommandRegistryBuilder;
import org.overlord.sramp.shell.aesh.SrampCommandInvocation;
import org.overlord.sramp.shell.aesh.TabCompleter;
import org.overlord.sramp.shell.api.ShellContextEventHandler;

/**
 * An implementation of the {@link ShellCommandReader} that uses JLine to
 * provide a rich console experience to the user, complete with history, tab
 * completion, and ansi output.
 *
 * @author eric.wittmann@redhat.com
 */
public class InteractiveShellCommandReader extends AbstractShellCommandReader implements
        ShellContextEventHandler {

    private static final QName CLIENT_NAME = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$

    private AeshConsole console;

    private String line;

    /**
     * Constructor.
     *
     * @param factory
     * @param context
     */
    public InteractiveShellCommandReader(ShellCommandFactory factory, ShellContextImpl context) {
        super(factory, context);
        context.addHandler(this);
    }

    /**
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#open()
     */
    @Override
    public void open() throws IOException {
        File aliasFile = null;

        try {
            aliasFile = new File(InteractiveShellCommandReader.class.getResource("/alias.txt").toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        // Created the settings builder enabling logging, history and setting
        // the alias file
        SettingsBuilder settingsBuilder = new SettingsBuilder().logging(true).disableHistory(false)
                .aliasFile(aliasFile).enableAlias(true);

        Settings settings = settingsBuilder.create();
        String prompt = defaultAnsiPrompt();

        // The command registry defines the shell commands
        CommandRegistry registry = SrampAeshCommandRegistryBuilder.getCommmandRegistyBuilder(
                super.getFactory()).create();
        AeshConsoleBuilder consoleBuilder = new AeshConsoleBuilder().commandRegistry(registry);
        consoleBuilder.completerInvocationProvider(new TabCompleter(getFactory(), getContext())).settings(
                settings);

        // This 2 lines allow store in the aesh context the s-ramp context and
        // the factory,
        // than later in the commands is gonna be useful
        CommandInvocationServices services = new CommandInvocationServices();
        CommandInvocationProvider<SrampCommandInvocation> provider = new CommandInvocationProvider<SrampCommandInvocation>() {

            @Override
            public SrampCommandInvocation enhanceCommandInvocation(CommandInvocation commandInvocation) {
                return new SrampCommandInvocation(commandInvocation, getContext(), getFactory());
            }
        };
        services.registerDefaultProvider(provider);
        consoleBuilder.commandInvocationProvider(services);
        console = consoleBuilder.create();
        console.setPrompt(new Prompt(prompt));

        // starts the shell.
        console.start();
        // console.setExpandEvents(false);
    }

    /**
     * Creates the ANSI compatible prompt.
     */
    private String defaultAnsiPrompt() {
        return "\033[1m\033[31ms-ramp>\033[0m "; //$NON-NLS-1$
    }

    /**
     * Creates the ANSI compatible prompt.
     */
    private String connectedAnsiPrompt() {
        return "\033[1m\033[32ms-ramp>\033[0m "; //$NON-NLS-1$
    }

    /**
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#readLine()
     */
    @Override
    protected String readLine() throws IOException {

        return line;
    }

    /**
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#getCommandOutput()
     */
    @Override
    protected Writer getCommandOutput() {
        return new PrintWriter(console.getShell().out());
    }

    /**
     * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
     */
    @Override
    public void close() throws IOException {
        console.stop();
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellContextEventHandler#onVariableAdded(javax.xml.namespace.QName,
     *      java.lang.Object)
     */
    @Override
    public void onVariableAdded(QName variableName, Object value) {
        if (CLIENT_NAME.equals(variableName)) {
            console.setPrompt(new Prompt(connectedAnsiPrompt()));
        }
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellContextEventHandler#onVariableChanged(javax.xml.namespace.QName,
     *      java.lang.Object)
     */
    @Override
    public void onVariableChanged(QName variableName, Object value) {
        // Nothing to do here
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellContextEventHandler#onVariableRemoved(javax.xml.namespace.QName)
     */
    @Override
    public void onVariableRemoved(QName variableName) {
        if (CLIENT_NAME.equals(variableName)) {
            console.setPrompt(new Prompt(defaultAnsiPrompt()));
        }
    }

    /**
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForInput(java.lang.String)
     */
    @Override
    public String promptForInput(String prompt) {
        return "";
    }

    /**
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForPassword(java.lang.String)
     */
    @Override
    public String promptForPassword(String prompt) {
        return "";
    }

}
