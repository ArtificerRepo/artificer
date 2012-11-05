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

import java.io.IOException;
import java.io.Writer;

import jline.console.ConsoleReader;

/**
 * An implementation of the {@link ShellCommandReader} that uses JLine to provide
 * a rich console experience to the user, complete with history, tab completion,
 * and ansi output.
 *
 * @author eric.wittmann@redhat.com
 */
public class InteractiveShellCommandReader extends AbstractShellCommandReader {

	private ConsoleReader consoleReader;

	/**
	 * Constructor.
	 * @param factory
	 * @param context
	 */
	public InteractiveShellCommandReader(ShellCommandFactory factory, ShellContextImpl context) {
		super(factory, context);
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommandReader#open()
	 */
	@Override
	public void open() throws IOException {
		consoleReader = new ConsoleReader();
		String prompt = createAnsiPrompt();
		consoleReader.setPrompt(prompt);
		consoleReader.addCompleter(new TabCompleter(getFactory(), getContext()));
	}

	/**
	 * Creates the ANSI compatible prompt.
	 */
	private String createAnsiPrompt() {
		return "\033[1m\033[31ms-ramp>\033[0m ";
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommandReader#readLine()
	 */
	@Override
	protected String readLine() throws IOException {
		return consoleReader.readLine();
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommandReader#getCommandOutput()
	 */
	@Override
	protected Writer getCommandOutput() {
		return consoleReader.getOutput();
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
		consoleReader.shutdown();
	}

}
