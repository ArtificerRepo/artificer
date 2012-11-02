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

import java.io.Console;
import java.io.IOException;

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
	 * @param factory
	 */
	public ConsoleShellCommandReader(ShellCommandFactory factory) {
		super(factory);
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommandReader#open()
	 */
	@Override
	public void open() throws IOException {
		this.console = System.console();
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommandReader#readLine()
	 */
	@Override
	protected String readLine() throws IOException {
		return console.readLine("s-ramp> ");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
		console.flush();
		console = null;
	}

}
