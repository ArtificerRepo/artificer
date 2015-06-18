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
import org.artificer.common.query.ArtifactSummary;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.shell.AbstractCommand;
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
 * CLI command to retrieve a stored query from the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
@CommandDefinition(name = "execute",
        description = "The \"executeStoredQuery\" command executes the given stored query.  The results are displayed identically to query.\n")
public class ExecuteStoredQueryCommand extends AbstractCommand {

    @Arguments(description = "<name>", completer = Completer.class)
    private List<String> arguments;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "storedQuery execute";
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
            commandInvocation.getShell().out().println(Messages.i18n.format("Query.Querying"));
            commandInvocation.getShell().out().println("\t" + name);

            QueryResultSet rset = client.queryWithStoredQuery(name);
            
            int entryIndex = 1;
            commandInvocation.getShell().out().println(Messages.i18n.format("Query.AtomFeedSummary", rset.size()));
            commandInvocation.getShell().out().println("  Idx, UUID, Type, Name");
            commandInvocation.getShell().out().println("  ---------------------");
            for (ArtifactSummary summary : rset) {
                ArtifactType type = summary.getArtifactType();
                String displayType = type.getArtifactType().getType().toString();
                if (type.isExtendedType() && type.getExtendedType() != null) {
                    displayType = type.getExtendedType();
                }
                commandInvocation.getShell().out().printf("  %d, %s, %s, %s\n", entryIndex++, summary.getUuid(),
                        displayType, summary.getName());
            }

            context(commandInvocation).setCurrentArtifactFeed(rset);
            
            return CommandResult.SUCCESS;
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("ExecuteStoredQueryCommand.Fail"));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
        }
    }

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            ExecuteStoredQueryCommand command = (ExecuteStoredQueryCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                StoredQueryCompleter.complete(completerInvocation);
            }
        }
    }
}
