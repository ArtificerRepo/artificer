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

import java.util.List;

import javax.xml.namespace.QName;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.api.AbstractShellCommand;

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
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:connect <endpointUrl> [username password] [--disableValidation]");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'connect' command creates a connection to a remote");
		print("S-RAMP repository at its Atom endpoint.  The connection");
		print("to the repository will be validated unless the ");
		print("'--disableValidation' option is set.");
		print("");
		print("Example usage:");
		print(">  s-ramp:connect http://localhost:8080/s-ramp-server");
		print(">  s-ramp:connect http://example.org/s-ramp --disableValidation");
        print(">  s-ramp:connect http://localhost:8080/s-ramp-server admin password");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String endpointUrlArg = this.requiredArgument(0, "Please specify a valid s-ramp URL.");
		String opt1 = this.optionalArgument(1);
        String opt2 = this.optionalArgument(2);
        String opt3 = this.optionalArgument(3);
        String username = null;
        String password = null;
		String disableValidationOptionArg = null;
		boolean hasCreds = false;

		if (opt3 != null) {
		    username = opt1;
		    password = opt2;
		    disableValidationOptionArg = opt3;
		    hasCreds = true;
		} else if (opt2 != null) {
            username = opt1;
            password = opt2;
            hasCreds = true;
		} else {
		    disableValidationOptionArg = opt1;
		}

		boolean validating = !"--disableValidation".equals(disableValidationOptionArg);
		if (!endpointUrlArg.startsWith("http")) {
			endpointUrlArg = "http://" + endpointUrlArg;
		}
		QName varName = new QName("s-ramp", "client");
		try {
			SrampAtomApiClient client = null;
			if (hasCreds) {
			    client = new SrampAtomApiClient(endpointUrlArg, username, password, validating);
			} else {
			    client = new SrampAtomApiClient(endpointUrlArg, validating);
			}
			getContext().setVariable(varName, client);
			print("Successfully connected to S-RAMP endpoint: " + endpointUrlArg);
		} catch (Exception e) {
			print("FAILED to connect to S-RAMP endpoint: " + endpointUrlArg);
			print("\t" + e.getMessage());
		}
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			candidates.add("http://localhost:8080/s-ramp-server");
			return 0;
		} else {
			return -1;
		}
	}

}
