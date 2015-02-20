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

import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import java.util.List;

/**
 * CLI command to list all stored queries in the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
@CommandDefinition(name = "list",
        description = "The \"listStoredQueries\" command retrieves all stored queries from the repository.\n")
public class ListStoredQueriesCommand extends AbstractCommand {

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "list";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }

        ArtificerAtomApiClient client = client(commandInvocation);

        try {
            List<StoredQuery> storedQueries = client.getStoredQueries();
            for (StoredQuery storedQuery : storedQueries) {
                commandInvocation.getShell().out().println(storedQuery.getQueryName() + ": " + storedQuery.getQueryExpression());
                if (storedQuery.getPropertyName().size() > 0) {
                    commandInvocation.getShell().out().println("     property names: " + StringUtils.join(storedQuery.getPropertyName(), ", "));
                }
            }
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("ListStoredQueriesCommand.Fail"));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
        }
        return CommandResult.SUCCESS;
    }
}
