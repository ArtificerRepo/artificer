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
import java.io.OutputStreamWriter;
import java.io.Writer;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.commands.Arguments;
import org.overlord.sramp.client.shell.commands.NoOpCommand;

/**
 * A base class for all shell command readers.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractShellCommandReader implements ShellCommandReader {

	private ShellContext context;
	private ShellCommandFactory factory;

	/**
	 * Constructor.
	 * @param factory
	 * @param context
	 */
	public AbstractShellCommandReader(ShellCommandFactory factory, ShellContext context) {
		this.factory = factory;
		this.context = context;
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#open()
	 */
	@Override
	public abstract void open() throws IOException;

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#read()
	 */
	@Override
	public final ShellCommand read() throws Exception {
		String line = readLine();
		if (line == null) {
			return null;
		}

		Arguments arguments = new Arguments(line);
		if (arguments.isEmpty()) {
			return new NoOpCommand();
		}

		// The first argument is the qualified command name.
		QName commandName = arguments.removeCommandName();

		// Create the command.
		ShellCommand command = factory.createCommand(commandName);
		command.setContext(this.context);
		command.setArguments(arguments);
		command.setOutput(getCommandOutput());
		return command;
	}

	/**
	 * Gets the output stream that should be used by commands when they need
	 * to print a message to the console.
	 */
	protected Writer getCommandOutput() {
		return new OutputStreamWriter(System.out);
	}

	/**
	 * Reads a single line from the input source (e.g. user input) and returns it.
	 * @throws IOException
	 */
	protected abstract String readLine() throws IOException;

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
	}

	/**
	 * @return the factory
	 */
	public ShellCommandFactory getFactory() {
		return factory;
	}

	/**
	 * @return the context
	 */
	public ShellContext getContext() {
		return context;
	}

}
