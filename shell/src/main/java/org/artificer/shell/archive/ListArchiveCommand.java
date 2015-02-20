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
package org.artificer.shell.archive;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.archive.ArtificerArchiveEntry;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.Collection;


/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "list",
		description = "This command display a list of the entries in the currently open Artificer archive.\n")
public class ListArchiveCommand extends AbstractArchiveCommand {

	@Override
	protected String getName() {
		return "list";
	}

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		ArtificerArchive archive = context(commandInvocation).getCurrentArchive();
		Collection<ArtificerArchiveEntry> entries = archive.getEntries();
		commandInvocation.getShell().out().println(Messages.i18n.format("ENTRY_PATH"));
		commandInvocation.getShell().out().println("  ----------");
		for (ArtificerArchiveEntry entry : entries) {
			String modifier = null;
			if (entry.hasContent()) {
				modifier = "  C ";
			} else {
				modifier = "  E ";
			}
			commandInvocation.getShell().out().println(modifier + entry.getPath());
		}
		commandInvocation.getShell().out().println("  ----------");
		commandInvocation.getShell().out().println(Messages.i18n.format("ENTRY_LIST_SUMMARY", entries.size()));
        return CommandResult.SUCCESS;
	}

}
