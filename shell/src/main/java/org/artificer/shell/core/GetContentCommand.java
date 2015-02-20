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

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.common.ArtifactType;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Gets the content for a single artifact in the s-ramp repo.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "getContent",
		description = "The \"getContent\" command downloads the file content for a single artifact from the Artificer repository.  The artifact can be identified either by its unique Artificer uuid or else by an index into the most recent Feed.\n")
public class GetContentCommand extends AbstractCommand {

	@Option(name = "uuid", hasValue = true, required = false,
			description = "Artifact UUID")
	private String artifactUuid;

	@Option(name = "feed", hasValue = true, required = false,
			description = "Feed index")
	private String feedIndex;

	@Option(name = "outputFile", hasValue = true, required = true, completer = Completer.class,
			description = "Output file path")
	private String outputFilePath;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		BaseArtifactType artifact = null;
		if (StringUtils.isNotBlank(artifactUuid)) {
			artifact = artifact(commandInvocation, artifactUuid);
		} else if (StringUtils.isNotBlank(feedIndex)) {
			artifact = artifactFromFeed(commandInvocation, feedIndex);
		} else {
			commandInvocation.getShell().out().println(Messages.i18n.format("Artifact.Arguments"));
			return CommandResult.FAILURE;
		}

		File outFile = new File(outputFilePath);
		if (outFile.isFile()) {
			commandInvocation.getShell().out().println(Messages.i18n.format("GetContent.OutputFileExists", outFile.getCanonicalPath()));
			return CommandResult.FAILURE;
		} else if (outFile.isDirectory()) {
			String fileName = artifact.getName();
			outFile = new File(outFile, fileName);
		}
		if (outFile.getParentFile() != null)
		    outFile.getParentFile().mkdirs();

		InputStream artifactContent = null;
		OutputStream outputStream = null;

		try {
			artifactContent = client(commandInvocation).getArtifactContent(ArtifactType.valueOf(artifact), artifact.getUuid());
			outputStream = new FileOutputStream(outFile);
			IOUtils.copy(artifactContent, outputStream);
			commandInvocation.getShell().out().println(Messages.i18n.format("GetContent.ContentSaved", outFile.getCanonicalPath()));
		} finally {
			IOUtils.closeQuietly(artifactContent);
			IOUtils.closeQuietly(outputStream);
		}
        return CommandResult.SUCCESS;
	}

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			GetContentCommand command = (GetContentCommand) completerInvocation.getCommand();
			if (StringUtils.isBlank(command.outputFilePath)) {
				FileNameCompleterDelegate.complete(completerInvocation);
			}
		}
	}

	@Override
	protected String getName() {
		return "getContent";
	}

}
