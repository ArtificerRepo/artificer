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
import java.util.Arrays;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.commands.NoOpCommand;

/**
 * A base class for all shell command readers.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractShellCommandReader implements ShellCommandReader {

	private ShellCommandFactory factory;

	/**
	 * Constructor.
	 * @param factory
	 */
	public AbstractShellCommandReader(ShellCommandFactory factory) {
		this.factory = factory;
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
		if (line.trim().length() == 0) {
			return new NoOpCommand();
		}

		//  TODO support quoted strings
		String [] split = line.split("\\s");
		String encodedCommandName = split[0];
		QName commandName = null;
		if (encodedCommandName != null) {
			if (encodedCommandName.contains(":") && !encodedCommandName.endsWith(":")) {
				String [] nameSplit = encodedCommandName.split(":");
				commandName = new QName(nameSplit[0], nameSplit[1]);
			} else {
				commandName = new QName("s-ramp", encodedCommandName);
			}
		}
		String [] args = Arrays.copyOfRange(split, 1, split.length);
		return factory.createCommand(commandName, args);
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

}
