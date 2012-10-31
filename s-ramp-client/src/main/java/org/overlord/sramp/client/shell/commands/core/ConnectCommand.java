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

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;

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
		System.out.println("s-ramp:connect <endpointUrl> [--disableValidation]");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("The 'connect' command creates a connection to a remote");
		System.out.println("S-RAMP repository at its Atom endpoint.  The connection");
		System.out.println("to the repository will be validated unless the ");
		System.out.println("'--disableValidation' option is set.");
		System.out.println("");
		System.out.println("Example usage:");
		System.out.println(">  s-ramp:connect http://localhost:8080/s-ramp-atom/s-ramp");
		System.out.println(">  s-ramp:connect http://example.org/s-ramp --disableValidation");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		String endpointUrlArg = this.requiredArgument(0, "Please specify a valid s-ramp URL.");
		String disableValidationOptionArg = this.optionalArgument(1);
		boolean validating = disableValidationOptionArg == null ? true : !Boolean.parseBoolean(disableValidationOptionArg);
		QName varName = new QName("s-ramp", "client");
		try {
			SrampAtomApiClient client = new SrampAtomApiClient(endpointUrlArg, validating);
			context.setVariable(varName, client);
			System.out.println("Successfully connected to S-RAMP endpoint: " + endpointUrlArg);
		} catch (Exception e) {
			System.out.println("FAILED to connect to S-RAMP endpoint: " + endpointUrlArg);
			System.out.println("\t" + e.getMessage());
		}
	}

}
