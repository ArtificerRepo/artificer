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

import java.io.Writer;

/**
 * Interface implemented by all shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public interface ShellCommand {

	/**
	 * Sets the arguments that can be used by this command.
	 * @param arguments
	 */
	public void setArguments(String [] arguments);

	/**
	 * Configure the command's output.
	 * @param output
	 */
	public void setOutput(Writer output);

	/**
	 * Called to execute the command.
	 */
	public void execute(ShellContext context) throws Exception;

	/**
	 * Prints the usage help for this command.
	 */
	public void printUsage();

	/**
	 * Prints the help text for this command.
	 */
	public void printHelp();

	/**
	 * Prints the given message to the output stream.
	 * @param formattedMessage
	 * @param params
	 */
	public void print(String formattedMessage, Object ... params);

}
