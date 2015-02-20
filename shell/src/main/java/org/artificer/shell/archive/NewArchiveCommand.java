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

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Creates a new, empty s-ramp batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "new",
		description = "The \"new\" operation creates and opens an empty Artificer archive.")
public class NewArchiveCommand extends AbstractArchiveCommand {

	@Override
	protected String getName() {
		return "new";
	}

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (context(commandInvocation).getCurrentArchive() != null) {
			commandInvocation.getShell().out().println(Messages.i18n.format("NewArchive.AlreadyOpen"));
			return CommandResult.FAILURE;
		}

		ArtificerArchive archive = new ArtificerArchive();
		context(commandInvocation).setCurrentArchive(archive);
		commandInvocation.getShell().out().println(Messages.i18n.format("NewArchive.Opened"));
        return CommandResult.SUCCESS;
	}

}
