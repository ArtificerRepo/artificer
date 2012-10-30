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

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.shell.commands.NoOpCommand;

/**
 * An implementation of the {@link ShellCommandReader} that uses allows more
 * dynamic interactions with the user.  This implementation supports features
 * such as tab completion and command history navigation.
 * 
 * TODO this doesn't work interactively yet because I can't figure out how to get the characters typed by the user as she types them - can only get them after she hits enter
 * 
 * @author eric.wittmann@redhat.com
 */
public class InteractiveShellCommandReader implements ShellCommandReader {

	private ShellCommandFactory factory;
	private List<String> lineHistory = new LinkedList<String>();
	private InputStream userStream = new FileInputStream(FileDescriptor.in);

	/**
	 * Constructor.
	 * @param factory
	 */
	public InteractiveShellCommandReader(ShellCommandFactory factory) {
		this.factory = factory;
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#close()
	 */
	@Override
	public void close() throws IOException {
		userStream.close();
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommandReader#read()
	 */
	@Override
	public ShellCommand read() throws Exception {
		String line = readLine();
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

	/**
	 * Prompts the user for a single line of input.
	 * @param userStream
	 * @throws IOException
	 */
	private String readLine() throws IOException {
		if (userStream.available() == 0) {
			System.out.print("sramp> ");
		}
		boolean done = false;
		StringBuilder builder = new StringBuilder();
		while (!done) {
			char c = (char) userStream.read();
			if (c == '\n') {
				done = true;
			} else {
				builder.append(c);
			}
		}
		String line = builder.toString().trim();
		if (line.length() > 0) {
			lineHistory.add(line);
		}
		return line;
	}

}
