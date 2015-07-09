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
import org.artificer.client.query.QueryResultSet;
import org.artificer.shell.common.AbstractExecuteQueryCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.OptionGroup;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;
import java.util.Map;

/**
 * CLI command to retrieve a stored query from the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
@CommandDefinition(name = "execute",
        description = "The \"executeStoredQuery\" command executes the given stored query.  Optionally, if the stored query " +
				"was created using parameters (ex: /s-ramp/core/Document[@uuid = '${uuid}']), provide the parameter " +
				"values as arguments (ex: -Duuid=12345).  The results are displayed identically to query.\n")
public class ExecuteStoredQueryCommand extends AbstractExecuteQueryCommand {

    @Arguments(description = "<name>", completer = Completer.class)
    private List<String> arguments;

	@OptionGroup(shortName = 'D', description = "Parameter substitution values", required = false)
	private Map<String, String> params;

    @Override
    protected String getName() {
        return "storedQuery execute";
    }

    @Override
    protected QueryResultSet doExecute(String argument, ArtificerAtomApiClient client,
			CommandInvocation commandInvocation) throws Exception {
        commandInvocation.getShell().out().println(Messages.i18n.format("Query.Querying"));
		commandInvocation.getShell().out().println("\t" + argument);

		return client.queryWithStoredQuery(argument, getStartIndex(), getCount(), getOrderBy(), isAscending(), params);
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

	@Override
	protected List<String> getArguments() {
		return arguments;
	}
}
