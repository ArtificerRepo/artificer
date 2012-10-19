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

import org.overlord.sramp.client.shell.commands.InvalidCommandArgumentException;

/**
 * Base class for shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractShellCommand implements ShellCommand {

	private String [] arguments;

	/**
	 * Constructor.
	 */
	public AbstractShellCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#setArguments(java.lang.String[])
	 */
	@Override
	public void setArguments(String[] arguments) {
		this.arguments = arguments;
	}

	/**
	 * Gets the command's arguments.
	 */
	protected String [] getArguments() {
		return this.arguments;
	}

	/**
	 * @param argIndex
	 * @throws InvalidCommandArgumentException
	 */
	protected String requiredArgument(int argIndex, String message) throws InvalidCommandArgumentException {
		if (getArguments() == null || getArguments().length <= argIndex) {
			throw new InvalidCommandArgumentException(argIndex, message);
		}
		return getArguments()[argIndex];
	}

	/**
	 * @param argIndex
	 * @throws InvalidCommandArgumentException
	 */
	protected String optionalArgument(int argIndex) throws InvalidCommandArgumentException {
		if (getArguments() == null || getArguments().length <= argIndex) {
			return null;
		}
		return getArguments()[argIndex];
	}

}
