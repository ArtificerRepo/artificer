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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.File;
import java.io.InputStream;
import java.util.List;

/**
 * Updates an artifact's content in the s-ramp repository. This requires an active artifact to exist in the
 * context.
 *
 * @author Brett Meyer
 */
@CommandDefinition(name = "updateContent",
		description = "The \"updateContent\" command updates the content of the currently active artifact in the context.  The new content is uploaded to the Artificer server.\n")
public class UpdateContentCommand extends AbstractCommand {

	@Arguments(description = "<file path>", completer = Completer.class)
	private List<String> arguments;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		ArtificerAtomApiClient client = client(commandInvocation);
		BaseArtifactType artifact = currentArtifact(commandInvocation);

		String filePath = requiredArgument(commandInvocation, arguments, 0);
		File file = new File(filePath);
		if (!file.isFile()) {
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateContent.InvalidArgMsg.PathToFile"));
			return CommandResult.FAILURE;
		}

		InputStream content = null;
		try {
			content = FileUtils.openInputStream(file);
			client.updateArtifactContent(artifact, content);
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateContent.Success", artifact.getName()));
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateContent.Failure"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
			IOUtils.closeQuietly(content);
			return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			UpdateContentCommand command = (UpdateContentCommand) completerInvocation.getCommand();
			if (CollectionUtils.isEmpty(command.arguments)) {
				FileNameCompleterDelegate.complete(completerInvocation);
			}
		}
	}

	@Override
	protected String getName() {
		return "updateContent";
	}

}
