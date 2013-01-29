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
package org.overlord.sramp.shell.commands.core;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.api.AbstractShellCommand;

/**
 * Disconnects from the current S-RAMP repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class DisconnectCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public DisconnectCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:disconnect");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'disconnect' command disconnects from the currently");
		print("active S-RAMP repository.");
		print("");
		print("Example usage:");
		print(">  s-ramp:disconnect");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName varName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(varName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}
		getContext().removeVariable(varName);
		print("Successfully disconnected from the S-RAMP repository.");
	}

}
