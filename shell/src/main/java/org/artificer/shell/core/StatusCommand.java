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
package org.artificer.shell.core;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.QueryResultSet;
import org.artificer.common.ArtifactType;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Displays a summary of the current status, including what repository the
 * user is currently connected to (if any).
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "status",
		description = "The \"status\" command displays the current Artificer status.\n")
public class StatusCommand extends AbstractCommand {

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		ArtificerAtomApiClient client = client(commandInvocation);
		BaseArtifactType artifact = context(commandInvocation).getCurrentArtifact();
		QueryResultSet feed = context(commandInvocation).getCurrentArtifactFeed();

		if (client == null) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status1")); //$NON-NLS-1$
		} else {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status2", client.getEndpoint())); //$NON-NLS-1$
		}

		if (artifact == null) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status3")); //$NON-NLS-1$
		} else {
			ArtifactType type = ArtifactType.valueOf(artifact);
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status4", artifact.getName(), type.getType())); //$NON-NLS-1$
		}

		if (feed == null) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status5")); //$NON-NLS-1$
		} else {
			commandInvocation.getShell().out().println(Messages.i18n.format("Status.Status6", feed.size())); //$NON-NLS-1$
		}

        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "status";
	}

}
