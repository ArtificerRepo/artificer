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
package org.artificer.shell.commands.core;

import java.util.List;

import javax.xml.namespace.QName;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.i18n.Messages;

/**
 * Connects to an s-ramp server.
 *
 * @author eric.wittmann@redhat.com
 */
public class ConnectCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public ConnectCommand() {
	}

	@Override
	public boolean execute() throws Exception {
		String endpointUrlArg = this.requiredArgument(0, Messages.i18n.format("Connect.InvalidArgMsg.NoUrl")); //$NON-NLS-1$
		String opt1 = this.optionalArgument(1);
        String opt2 = this.optionalArgument(2);
        String opt3 = this.optionalArgument(3);
        String username = null;
        String password = null;
		String disableValidationOptionArg = null;

		if (opt3 != null) {
		    username = opt1;
		    password = opt2;
		    disableValidationOptionArg = opt3;
		} else if (opt2 != null) {
            username = opt1;
            password = opt2;
		} else {
		    disableValidationOptionArg = opt1;
		}

		if (username == null) {
		    username = promptForUsername();
		}
		if (password == null) {
		    password = promptForPassword();
		}

		boolean validating = !"--disableValidation".equals(disableValidationOptionArg); //$NON-NLS-1$
		if (!endpointUrlArg.startsWith("http")) { //$NON-NLS-1$
			endpointUrlArg = "http://" + endpointUrlArg; //$NON-NLS-1$
		}
		QName varName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			ArtificerAtomApiClient client = null;
		    client = new ArtificerAtomApiClient(endpointUrlArg, username, password, validating);
			getContext().setVariable(varName, client);
			print(Messages.i18n.format("Connect.Success", endpointUrlArg)); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("Connect.Failure", endpointUrlArg)); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
	        return false;
		}
        return true;
	}

    /**
     * Prompts the user to enter a username for authentication credentials.
     */
    private String promptForUsername() {
        String username = System.getProperty("artificer.shell.username"); //$NON-NLS-1$
        if (username != null) {
            return username;
        }
        return getContext().promptForInput(Messages.i18n.format("Connect.UserPrompt")); //$NON-NLS-1$
    }

    /**
     * Prompts the user to enter a password for authentication credentials.
     */
    private String promptForPassword() {
        String password = System.getProperty("artificer.shell.password"); //$NON-NLS-1$
        if (password != null) {
            return password;
        }
        return getContext().promptForPassword(Messages.i18n.format("Connect.PasswordPrompt")); //$NON-NLS-1$
    }

	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
		if (getArguments().isEmpty()) {
			candidates.add("http://localhost:8080/artificer-server"); //$NON-NLS-1$
			return 0;
		} else {
			return -1;
		}
	}

}
