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

import org.apache.commons.collections.CollectionUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.common.ArtifactType;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.util.List;

/**
 * Adds a comment to an artifact. Requires an active artifact to exist in the
 * context.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "addComment",
		description = "The \"addComment\" adds a comment to the currently active artifact in the context.\n")
public class AddCommentCommand extends AbstractCommand {

	@Arguments(description = "<comment text>")
	private List<String> arguments;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}
		if (CollectionUtils.isEmpty(arguments) || arguments.size() < 1) {
			return doHelp(commandInvocation);
		}

		String text = arguments.get(0);

		ArtificerAtomApiClient client = client(commandInvocation);
		BaseArtifactType artifact = currentArtifact(commandInvocation);

		try {
			client.addComment(artifact.getUuid(), ArtifactType.valueOf(artifact), text);
			commandInvocation.getShell().out().println(Messages.i18n.format("AddComment.Success", artifact.getName()));
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("AddComment.Failure"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
			return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}

	@Override
	protected String getName() {
		return "addComment";
	}

}
