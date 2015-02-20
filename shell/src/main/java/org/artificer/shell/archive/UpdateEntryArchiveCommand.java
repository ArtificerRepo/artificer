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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.archive.ArtificerArchiveEntry;
import org.artificer.common.ArtificerModelUtils;
import org.artificer.shell.ArtificerShellException;
import org.artificer.shell.i18n.Messages;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "updateEntry",
		description = "The \"updateEntry\" command is used to modify entries in the currently open Artificer batch archive.  The path to the entry must be specified, along with details about how the entry is to be modified.\\nsetContent: a sub-command that sets the file content on a particular entry\nsetProperty: a sub-command that sets a single custom Artificer property on the entry\nsetRelationship: a sub-command that sets a single generic Artificer relationship on the entry\n")
public class UpdateEntryArchiveCommand extends AbstractArchiveCommand {

    private static final Set<String> subcommands = new HashSet<String>();
    {
        subcommands.add("setContent");
        subcommands.add("setProperty");
        subcommands.add("setRelationship");
    }

	@Arguments(description = "setContent|setProperty|setRelationship <value>", completer = Completer.class)
	private List<String> arguments;

	@Option(name = "entryPath", hasValue = true, required = true,
			description = "Entry path")
	private String entryPath;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected String getName() {
		return "updateEntry";
	}

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}
		if (CollectionUtils.isEmpty(arguments)) {
			return doHelp(commandInvocation);
		}

		String subCommandArg = requiredArgument(commandInvocation, arguments, 0);

		if (!currentArchive(commandInvocation).containsEntry(entryPath)) {
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateEntry.EntryNotFound", entryPath));
			return CommandResult.FAILURE;
		}

		if ("setContent".equals(subCommandArg)) {
			executeSetContent(commandInvocation, entryPath);
		}
		if ("setProperty".equals(subCommandArg)) {
			executeSetProperty(commandInvocation, entryPath);
		}
		if ("setRelationship".equals(subCommandArg)) {
			executeSetRelationship(commandInvocation, entryPath);
		}

        return CommandResult.SUCCESS;
	}

	/**
	 * Can set the content for an entry.
	 * @param commandInvocation
	 * @param archivePathArg
	 * @throws Exception
	 */
	private void executeSetContent(CommandInvocation commandInvocation, String archivePathArg) throws Exception {
		String pathToContentArg = requiredArgument(commandInvocation, arguments, 1);
		File file = new File(pathToContentArg);
		if (!file.isFile()) {
			throw new ArtificerShellException(Messages.i18n.format("UpdateEntry.FileNotFound", pathToContentArg));
		}

		ArtificerArchive archive = currentArchive(commandInvocation);
		InputStream contentStream = null;
		try {
			contentStream = FileUtils.openInputStream(file);
			ArtificerArchiveEntry entry = archive.getEntry(archivePathArg);
			archive.updateEntry(entry, contentStream);
			commandInvocation.getShell().out().println(Messages.i18n.format("UpdateEntry.SuccessMsg"));
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	/**
	 * Can set a property (built-in or custom) on the entry.
	 * @param commandInvocation
	 * @param archivePathArg
	 * @throws Exception
	 */
	private void executeSetProperty(CommandInvocation commandInvocation, String archivePathArg) throws Exception {
		String propNameArg = requiredArgument(commandInvocation, arguments, 1);
		String propValArg = optionalArgument(arguments, 2);

		ArtificerArchive archive = currentArchive(commandInvocation);

		ArtificerArchiveEntry entry = archive.getEntry(archivePathArg);
		BaseArtifactType metaData = entry.getMetaData();

		if ("name".equals(propNameArg)) {
			metaData.setName(propValArg);
		} else if ("description".equals(propNameArg)) {
			metaData.setDescription(propValArg);
		} else if ("version".equals(propNameArg)) {
			metaData.setVersion(propValArg);
		} else if ("createdBy".equals(propNameArg)) {
			metaData.setCreatedBy(propValArg);
		} else if ("lastModifiedBy".equals(propNameArg)) {
			metaData.setLastModifiedBy(propValArg);
		} else if ("uuid".equals(propNameArg)) {
			metaData.setUuid(propValArg);
		} else if ("createdTimestamp".equals(propNameArg)) {
		} else if ("lastModifiedTimestamp".equals(propNameArg)) {
		}

		ArtificerModelUtils.setCustomProperty(metaData, propNameArg, propValArg);
		archive.updateEntry(entry, null);
		commandInvocation.getShell().out().println(Messages.i18n.format("UpdateEntry.MetaDataSuccessMsg"));
	}

	/**
	 * Can set a relationship on an entry.
	 * @param commandInvocation
	 * @param archivePathArg
	 * @throws Exception
	 */
	private void executeSetRelationship(CommandInvocation commandInvocation, String archivePathArg) throws Exception {
		throw new ArtificerShellException(Messages.i18n.format("UpdateEntry.NotYetImplemented.Relationships"));
	}

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			UpdateEntryArchiveCommand command = (UpdateEntryArchiveCommand) completerInvocation.getCommand();
			if (CollectionUtils.isEmpty(command.arguments)) {
				String currentValue = completerInvocation.getGivenCompleteValue();
				for (String subcmd : subcommands) {
					if (StringUtils.isBlank(currentValue) || subcmd.startsWith(currentValue)) {
						completerInvocation.addCompleterValue(subcmd);
					}
				}
			}
		}
	}

}
