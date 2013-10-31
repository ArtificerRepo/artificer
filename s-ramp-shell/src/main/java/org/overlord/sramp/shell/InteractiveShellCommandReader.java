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

import java.io.IOException;
import java.io.Writer;

import javax.xml.namespace.QName;

import jline.console.ConsoleReader;

import org.overlord.sramp.shell.api.ShellContextEventHandler;

/**
 * An implementation of the {@link ShellCommandReader} that uses JLine to provide
 * a rich console experience to the user, complete with history, tab completion,
 * and ansi output.
 *
 * @author eric.wittmann@redhat.com
 */
public class InteractiveShellCommandReader extends AbstractShellCommandReader implements ShellContextEventHandler {

	private static final QName CLIENT_NAME = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$

	private ConsoleReader consoleReader;

	/**
	 * Constructor.
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
		consoleReader = new ConsoleReader();
		String prompt = defaultAnsiPrompt();
		consoleReader.setPrompt(prompt);
		consoleReader.addCompleter(new TabCompleter(getFactory(), getContext()));
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
		return consoleReader.readLine();
	}

	/**
	 * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#getCommandOutput()
	 */
	@Override
	protected Writer getCommandOutput() {
		return consoleReader.getOutput();
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
		consoleReader.shutdown();
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContextEventHandler#onVariableAdded(javax.xml.namespace.QName, java.lang.Object)
	 */
	@Override
	public void onVariableAdded(QName variableName, Object value) {
		if (CLIENT_NAME.equals(variableName)) {
			consoleReader.setPrompt(connectedAnsiPrompt());
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellContextEventHandler#onVariableChanged(javax.xml.namespace.QName, java.lang.Object)
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
			consoleReader.setPrompt(defaultAnsiPrompt());
		}
	}

	/**
	 * @see org.overlord.sramp.shell.ShellCommandReader#promptForInput(java.lang.String)
	 */
	@Override
	public String promptForInput(String prompt) {
        String oldprompt = consoleReader.getPrompt();
	    try {
            return this.consoleReader.readLine(prompt);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.consoleReader.setPrompt(oldprompt);
        }
	}

	/**
	 * @see org.overlord.sramp.shell.ShellCommandReader#promptForPassword(java.lang.String)
	 */
	@Override
	public String promptForPassword(String prompt) {
        String oldprompt = consoleReader.getPrompt();
        try {
            return this.consoleReader.readLine(prompt, '*');
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            this.consoleReader.setPrompt(oldprompt);
        }
	}

}
