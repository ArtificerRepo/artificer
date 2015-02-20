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
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Updates an artifact's meta-data in the s-ramp repository. This requires an active artifact to exist in the
 * context, which was presumably modified in some way (updated core meta-data, properties, relationships,
 * etc).
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "updateMetaData",
		description = "The \"updateMetaData\" command updates the meta-data of the currently active artifact in the context.  Whatever changes were made to the active artifact will be sent back to the Artificer repository.\n")
public class UpdateMetaDataCommand extends AbstractCommand {

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		ArtificerAtomApiClient client = client(commandInvocation);
		BaseArtifactType artifact = currentArtifact(commandInvocation);

		try {
			client.updateArtifactMetaData(artifact);
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateMetaData.Success", artifact.getName())); //$NON-NLS-1$
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateMetaData.Failure")); //$NON-NLS-1$
			commandInvocation.getShell().out().println("\t" + e.getMessage()); //$NON-NLS-1$
			return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "updateMetaData";
	}

}
