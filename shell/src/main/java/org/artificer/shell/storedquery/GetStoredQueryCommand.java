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
package org.artificer.shell.storedquery;

import org.apache.commons.collections.CollectionUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.util.List;

/**
 * CLI command to retrieve a stored query from the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
@CommandDefinition(name = "get",
        description = "The \"getStoredQuery\" command retrives the given stored query from the repository.\n")
public class GetStoredQueryCommand extends AbstractCommand {

    @Arguments(description = "<name>", completer = Completer.class)
    private List<String> arguments;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "get";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String name = this.requiredArgument(commandInvocation, arguments, 0);

        ArtificerAtomApiClient client = client(commandInvocation);

        try {
            StoredQuery storedQuery = client.getStoredQuery(name);
            print(commandInvocation, storedQuery);
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("GetStoredQueryCommand.Fail"));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }

    private void print(CommandInvocation commandInvocation, StoredQuery storedQuery) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(StoredQuery.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(storedQuery, commandInvocation.getShell().out());
    }

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            GetStoredQueryCommand command = (GetStoredQueryCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                StoredQueryCompleter.complete(completerInvocation);
            }
        }
    }
}
