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
package org.artificer.shell.storedquery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.common.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import java.util.List;

/**
 * CLI command to create a new stored query in the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
@CommandDefinition(name = "create",
        description = "The \"createStoredQuery\" command creates a stored query in the repository.  Provide a name the query-expression.  Optionally provide a list of property names to indicate artifact property values that MUST be included in the feed resulting from the query execution.\n")
public class CreateStoredQueryCommand extends AbstractCommand {

    @Arguments(description = "<query>")
    private List<String> arguments;

    @Option(name = "name", hasValue = true, required = true,
            description = "Query name")
    private String name;

    @Option(name = "propertyNames", hasValue = true, required = false,
            description = "Required property names")
    private String propertyNames;

    @Override
    protected String getName() {
        return "storedQuery create";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String query = requiredArgument(commandInvocation, arguments, 0);

        ArtificerAtomApiClient client = client(commandInvocation);

        StoredQuery storedQuery = new StoredQuery();
        storedQuery.setQueryName(name);
        storedQuery.setQueryExpression(query);
        if (StringUtils.isNotBlank(propertyNames)) {
            String[] split = propertyNames.split(",");
            for (String s : split) {
                storedQuery.getPropertyName().add(s);
            }
        }

        try {
            client.createStoredQuery(storedQuery);
            commandInvocation.getShell().out().println(Messages.i18n.format("CreateStoredQueryCommand.Success", name));
            return CommandResult.SUCCESS;
        } catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("CreateStoredQueryCommand.Fail", name));
            return CommandResult.FAILURE;
        }
    }

}
