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

import java.io.Console;
import java.io.IOException;

import org.overlord.sramp.shell.api.ShellContext;

/**
 * An implementation of the {@link ShellCommandReader} that uses standard input
 * to read commands typed in by the user.  This implementation uses the Java
 * System.console() facility to read user input.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConsoleShellCommandReader extends AbstractShellCommandReader {

	private Console console;

	    /**
     * Constructor.
     *
     * @param factory
     *            the factory
     * @param context
     *            the context
     * @param args
     *            the args
     */
	public ConsoleShellCommandReader(ShellCommandFactory factory, ShellContext context, ShellArguments args) {
		super(factory, context, args);
	}

	/**
     * Open.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#open()
     */
	@Override
	public void open() throws IOException {
		this.console = System.console();
	}

	    /**
     * Read line.
     *
     * @return the string
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.AbstractShellCommandReader#readLine()
     */
	@Override
	protected String readLine() throws IOException {
		return console.readLine("s-ramp> "); //$NON-NLS-1$
	}

	    /**
     * Close.
     *
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @see org.overlord.sramp.common.shell.ShellCommandReader#close()
     */
	@Override
	public void close() throws IOException {
		console.flush();
		console = null;
	}

	    /**
     * Prompt for input.
     *
     * @param prompt
     *            the prompt
     * @return the string
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForInput(java.lang.String)
     */
	@Override
	public String promptForInput(String prompt) {
	    return console.readLine(prompt);
	}

	    /**
     * Prompt for password.
     *
     * @param prompt
     *            the prompt
     * @return the string
     * @see org.overlord.sramp.shell.ShellCommandReader#promptForPassword(java.lang.String)
     */
	@Override
	public String promptForPassword(String prompt) {
	    return new String(console.readPassword(prompt));
	}

}
