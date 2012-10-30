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

import java.util.Arrays;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.commands.NoOpCommand;

/**
 * An implementation of the {@link ShellCommandReader} that uses standard input
 * to read commands typed in by the user.  This implementation uses the Java
 * System.console() facility to read user input.
 * 
 * @author eric.wittmann@redhat.com
 */
public class ConsoleShellCommandReader implements ShellCommandReader {

	private ShellCommandFactory factory;

	/**
	 * Constructor.
	 * @param factory
	 */
	public ConsoleShellCommandReader(ShellCommandFactory factory) {
		this.factory = factory;
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#read()
	 */
	@Override
	public ShellCommand read() throws Exception {
		if (System.console() == null) {
			System.out.println("No interactive console available, exiting.");
			System.exit(1);
		}
		String line = System.console().readLine("s-ramp> ");
		if (line.trim().length() == 0) {
			return new NoOpCommand();
		}

		String [] split = line.split("\\s");
		String encodedCommandName = split[0];
		QName commandName = null;
		if (encodedCommandName != null) {
			if (encodedCommandName.contains(":")) {
				String [] nameSplit = encodedCommandName.split(":");
				commandName = new QName(nameSplit[0], nameSplit[1]);
			} else {
				commandName = new QName("s-ramp", encodedCommandName);
			}
		}
		String [] args = Arrays.copyOfRange(split, 1, split.length);
		return factory.createCommand(commandName, args);
	}

}
