/*
 * Copyright 2014 JBoss Inc
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

import java.io.IOException;

import javax.xml.namespace.QName;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Connects to an s-ramp server.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_CONNECT, description = "Connect to a specific s-ramp instance")
public class ConnectCommand extends AbstractCoreShellCommand {

    @Option(hasValue = true, required = true, name = "url", shortName = 'U', defaultValue = { "http://localhost:8080/s-ramp-server" })
    private String _endpointUrl;

    @Option(hasValue = true, name = "username", shortName = 'u')
    private String _username;

    @Option(hasValue = true, name = "password", shortName = 'p')
    private String _password;

    @Option(hasValue = false, name = "disableValidation", shortName = 'd')
    private boolean _disableValidation;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    private boolean _validateConnection;
	/**
	 * Constructor.
	 */
	public ConnectCommand() {
        _validateConnection = true;
	}


    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        super.execute();

		if (_username == null) {
		    _username = promptForUsername();
		}
		if (_password == null) {
		    _password = promptForPassword();
		}

        if (!_endpointUrl.startsWith("http")) { //$NON-NLS-1$
            _endpointUrl = "http://" + _endpointUrl; //$NON-NLS-1$
		}
		QName varName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			SrampAtomApiClient client = null;
            if (_disableValidation) {
                _validateConnection = false;
            }
            client = new SrampAtomApiClient(_endpointUrl, _username, _password, _validateConnection);

			getContext().setVariable(varName, client);
            print(Messages.i18n.format("Connect.Success", _endpointUrl)); //$NON-NLS-1$
		} catch (Exception e) {
            print(Messages.i18n.format("Connect.Failure", _endpointUrl)); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
	        return false;
		}
        return true;
	}


    /**
     * Prompts the user to enter a username for authentication credentials.
     *
     * @return the string
     */
    private String promptForUsername() {
        String username = System.getProperty("s-ramp.shell.username"); //$NON-NLS-1$
        if (username != null) {
            return username;
        }
        try {
            username = promptForInput(Messages.i18n.format("Connect.UserPrompt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return username;
    }

    /**
     * Prompts the user to enter a password for authentication credentials.
     *
     * @return the string
     */
    private String promptForPassword() {
        String password = System.getProperty("s-ramp.shell.password"); //$NON-NLS-1$
        if (password != null) {
            return password;
        }
        try {
            password = promptForInput(Messages.i18n.format("Connect.PasswordPrompt"), '*');
        } catch (IOException e) {
            e.printStackTrace();
        } //$NON-NLS-1$
        return password;
    }


    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_CONNECT;
    }

    /**
     * Gets the endpoint url.
     *
     * @return the endpoint url
     */
    public String getEndpointUrl() {
        return _endpointUrl;
    }

    /**
     * Sets the endpoint url.
     *
     * @param endpointUrl
     *            the new endpoint url
     */
    public void setEndpointUrl(String endpointUrl) {
        this._endpointUrl = endpointUrl;
    }

    /**
     * Gets the username.
     *
     * @return the username
     */
    public String getUsername() {
        return _username;
    }

    /**
     * Sets the username.
     *
     * @param username
     *            the new username
     */
    public void setUsername(String username) {
        this._username = username;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return _password;
    }

    /**
     * Sets the password.
     *
     * @param password
     *            the new password
     */
    public void setPassword(String password) {
        this._password = password;
    }

    /**
     * Checks if is disable validation.
     *
     * @return true, if is disable validation
     */
    public boolean isDisableValidation() {
        return _disableValidation;
    }

    /**
     * Sets the disable validation.
     *
     * @param disableValidation
     *            the new disable validation
     */
    public void setDisableValidation(boolean disableValidation) {
        this._disableValidation = disableValidation;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

    /**
     * Checks if is validate connection.
     *
     * @return true, if is validate connection
     */
    public boolean isValidateConnection() {
        return _validateConnection;
    }

    /**
     * Sets the validate connection.
     *
     * @param validateConnection
     *            the new validate connection
     */
    public void setValidateConnection(boolean validateConnection) {
        this._validateConnection = validateConnection;
    }

}
