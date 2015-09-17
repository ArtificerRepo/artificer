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
package org.artificer.shell.core;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.common.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;

/**
 * Connects to an Artificer server.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "connect", description = "<options> <endpointUrl>\n" +
        "\nThe \"connect\" command creates a connection to a remote " +
        "Artificer repository at its Atom endpoint.  The connection " +
        "to the repository will be validated unless the " +
        "\"--disableValidation\" option is set.\n")
public class ConnectCommand extends AbstractCommand {

    @Arguments(description = "<endpoint url>", completer = Completer.class)
    private List<String> arguments;

    @Option(name = "username", shortName = 'u', hasValue = true, required = false,
            description = "Username")
    private String username;

    @Option(name = "password", shortName = 'p', hasValue = true, required = false,
            description = "Password")
    private String password;

    @Option(name = "disableValidation", hasValue = false, required = false,
            description = "Disable repository validation")
    private boolean disableValidation;

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (CollectionUtils.isEmpty(arguments) || arguments.size() < 1) {
            return doHelp(commandInvocation);
        }

        String endpointUrl = arguments.get(0);
        if (StringUtils.isBlank(username)) {
            username = promptForInput("username: ", null, commandInvocation);
        }
        if (StringUtils.isBlank(password)) {
            password = promptForInput("password: ", '*', commandInvocation);
        }

        if (!endpointUrl.startsWith("http")) {
            endpointUrl = "http://" + endpointUrl;
        }

        try {
            context(commandInvocation).setClient(new ArtificerAtomApiClient(
                    endpointUrl, username, password, !disableValidation));
            commandInvocation.getShell().out().println(Messages.i18n.format("Connect.Success", endpointUrl));
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("Connect.Failure", endpointUrl));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
        }
        
        return CommandResult.SUCCESS;
    }

    @Override
    protected String getName() {
        return "connect";
    }

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            ConnectCommand command = (ConnectCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments) && StringUtils.isEmpty(completerInvocation.getGivenCompleteValue())) {
                completerInvocation.addCompleterValue("http://localhost:8080/artificer-server");
            }
        }
    }
}
