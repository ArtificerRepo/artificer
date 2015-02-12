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
package org.artificer.shell.api;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

/**
 * Base class for shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class AbstractShellCommand implements ShellCommand {

	private ShellContext context;
	private Arguments arguments;
	private Writer writer;

	/**
	 * Constructor.
	 */
	public AbstractShellCommand() {
	}

	/**
	 * @see ShellCommand#setContext(ShellContext)
	 */
	@Override
	public void setContext(ShellContext context) {
		this.context = context;
	}

	/**
	 * @return the shell context
	 */
	protected ShellContext getContext() {
		return this.context;
	}

	/**
	 * @see ShellCommand#setArguments(String[])
	 */
	@Override
	public void setArguments(Arguments arguments) {
		this.arguments = arguments;
	}

	/**
	 * Gets the command's arguments.
	 */
	protected Arguments getArguments() {
		return this.arguments;
	}

	/**
	 * Returns the argument at the given index.  Throws an exception if the argument
	 * does not exist.
	 * @param argIndex
	 * @param message
	 * @throws InvalidCommandArgumentException
	 */
	protected String requiredArgument(int argIndex, String message) throws InvalidCommandArgumentException {
		if (getArguments().size() <= argIndex) {
			throw new InvalidCommandArgumentException(argIndex, message);
		}
		return getArguments().get(argIndex);
	}

	/**
	 * Returns the optional argument at the given index.  Returns null if the argument
	 * does not exist.
	 * @param argIndex
	 */
	protected String optionalArgument(int argIndex) {
		return optionalArgument(argIndex, null);
	}

	/**
	 * Returns the optional argument at the given index.  Returns the given default value if
	 * the argument does not exist.
	 * @param argIndex
	 * @param defaultValue
	 */
	protected String optionalArgument(int argIndex, String defaultValue) {
		if (getArguments().size() <= argIndex) {
			return defaultValue;
		}
		return getArguments().get(argIndex);
	}

	/**
	 * @see ShellCommand#print(String, Object[])
	 */
	@Override
	public void print(String formattedMessage, Object... params) {
		String msg = String.format(formattedMessage, params);
		if (writer != null) {
			try {
				writer.write(msg);
				writer.write('\n');
				writer.flush();
			} catch (IOException e) {
			}
		} else {
			System.out.println(msg);
		}
	}

	/**
	 * @param output the output to set
	 */
	@Override
	public void setOutput(Writer output) {
		this.writer = output;
	}
	
	protected Writer getOutput() {
	    return writer;
	}

	/**
	 * @see ShellCommand#tabCompletion(String, List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		return -1;
	}

}
