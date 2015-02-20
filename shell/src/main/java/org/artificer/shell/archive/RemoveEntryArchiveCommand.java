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
package org.artificer.shell.archive;

import org.apache.commons.collections.CollectionUtils;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "removeEntry",
        description = "The \"removeEntry\" command will remove a single entry from the currently open Artificer batch archive.  The path to the entry must be specified.\n")
public class RemoveEntryArchiveCommand extends AbstractArchiveCommand {

    @Arguments(description = "<entry path>")
    private List<String> arguments;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "removeEntry";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String archivePathArg = requiredArgument(commandInvocation, arguments, 0);

        ArtificerArchive archive = currentArchive(commandInvocation);
        boolean success = archive.removeEntry(archivePathArg);
        if (success) {
            commandInvocation.getShell().out().println(Messages.i18n.format("RemoveEntry.EntryDeleted"));
        } else {
            commandInvocation.getShell().out().println(Messages.i18n.format("RemoveEntry.NoEntryFound", archivePathArg));
        }

        return CommandResult.SUCCESS;
	}
}
