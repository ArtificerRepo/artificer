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
package org.overlord.sramp.client.shell.commands.core;

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.shell.AbstractShellCommand;

/**
 * Connects to an s-ramp server.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConnectCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public ConnectCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:connect <endpointUrl> [--disableValidation]");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'connect' command creates a connection to a remote");
		print("S-RAMP repository at its Atom endpoint.  The connection");
		print("to the repository will be validated unless the ");
		print("'--disableValidation' option is set.");
		print("");
		print("Example usage:");
		print(">  s-ramp:connect http://localhost:8080/s-ramp-atom");
		print(">  s-ramp:connect http://example.org/s-ramp --disableValidation");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String endpointUrlArg = this.requiredArgument(0, "Please specify a valid s-ramp URL.");
		String disableValidationOptionArg = this.optionalArgument(1);
		boolean validating = !"--disableValidation".equals(disableValidationOptionArg);
		if (!endpointUrlArg.startsWith("http")) {
			endpointUrlArg = "http://" + endpointUrlArg;
		}
		QName varName = new QName("s-ramp", "client");
		try {
			SrampAtomApiClient client = new SrampAtomApiClient(endpointUrlArg, validating);
			getContext().setVariable(varName, client);
			print("Successfully connected to S-RAMP endpoint: " + endpointUrlArg);
		} catch (Exception e) {
			print("FAILED to connect to S-RAMP endpoint: " + endpointUrlArg);
			print("\t" + e.getMessage());
		}
	}

	/**
	 * @see org.overlord.sramp.client.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			candidates.add("http://localhost:8080/s-ramp-atom");
			return 0;
		} else {
			return -1;
		}
	}

}
