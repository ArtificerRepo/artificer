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

import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.query.ArtifactSummary;
import org.artificer.common.ArtifactType;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

/**
 * Deletes an artifact from the S-RAMP repository.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "delete",
		description = "The \"delete\" command removes an artifact from the Artificer repository.  The artifact can be identified either by its unique Artificer uuid or else by an index into the most recent Feed.  Additionally, the currently active artifact can be deleted by omitting the uuid and feed options.\n")
public class DeleteCommand extends AbstractCommand {

	@Option(name = "uuid", hasValue = true, required = false,
			description = "Artifact UUID")
	private String artifactUuid;

	@Option(name = "feed", hasValue = true, required = false,
			description = "Feed index")
	private String feedIndex;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		ArtificerAtomApiClient client = client(commandInvocation);

		ArtifactType artifactType;
		String artifactName;
		if (StringUtils.isNotBlank(artifactUuid)) {
			BaseArtifactType artifact = artifact(commandInvocation, artifactUuid);
			artifactType = ArtifactType.valueOf(artifact);
			artifactName = artifact.getName();
		} else if (StringUtils.isNotBlank(feedIndex)) {
			ArtifactSummary artifactSummary = artifactSummaryFromFeed(commandInvocation, feedIndex);
			artifactType = artifactSummary.getType();
			artifactUuid = artifactSummary.getUuid();
			artifactName = artifactSummary.getName();
		} else if (hasCurrentArtifact(commandInvocation)) {
			BaseArtifactType artifact = currentArtifact(commandInvocation);
			artifactType = ArtifactType.valueOf(artifact);
			artifactName = artifact.getName();
		} else {
			commandInvocation.getShell().out().println(Messages.i18n.format("Artifact.Arguments"));
			return CommandResult.FAILURE;
		}

        try {
			client.deleteArtifact(artifactUuid, artifactType);
			commandInvocation.getShell().out().println(Messages.i18n.format("Delete.Success", artifactName)); //$NON-NLS-1$
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Delete.Failure")); //$NON-NLS-1$
			commandInvocation.getShell().out().println("\t" + e.getMessage()); //$NON-NLS-1$
	        return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "delete";
	}

}
