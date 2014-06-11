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
package org.overlord.sramp.shell.api;

import java.io.Writer;
import java.util.List;

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
	public void setArguments(Arguments arguments);

	/**
	 * Configure the command's output.
	 * @param output
	 */
	public void setOutput(Writer output);

	/**
	 * The shell context.
	 * @param context
	 */
	public void setContext(ShellContext context);

	/**
	 * Called to execute the command.  Returns true if the command
	 * was successful or false if not.  If false is returned, and
	 * the command is part of a batch, then the batch will be
	 * halted.
	 */
	public boolean execute() throws Exception;

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

	/**
	 * Handle tab completion for the command.  This is optional, but provides a mechanism by
	 * which individual commands can enable command-specific tab completion functionality.
	 *
	 * The return value of this method represents the cursor position within lastArgument
	 * that represents the origin point for all of the candidates.  Return 0 to indicate that
	 * the candidates are all full-replacements of lastArgument.  Return -1 to indicate that
	 * no candidates were supplied.  Return a positive, non-negative value if the returned
	 * candidates are partial completions.
	 *
	 * For example, if the user has typed "aar", then a command could return any of:
	 *
	 * candidates=[]  rval=-1
	 * candidates=["aardvark", "aardwolf"]  rval=0
	 * candidates=["dvark", "dwolf"]  rval=3
	 *
	 * In the latter two examples, the tab-completion will be the same, but the user will
	 * be shown the different candidate values (when more than 1 candidate is returned).
	 *
	 * @param lastArgument
	 * @param candidates
	 * @return the cursor position
	 */
	public int tabCompletion(String lastArgument, List<CharSequence> candidates);

}
