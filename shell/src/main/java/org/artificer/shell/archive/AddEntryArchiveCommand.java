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

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artificer.atom.archive.ArtificerArchiveException;
import org.artificer.common.ArtifactType;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.ArtifactTypeCompleter;
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
 * Adds an entry to the current S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "addEntry",
        description = "The \"addEntry\" command provides a way to add a single entry to the currently open Artificer batch archive.  The command requires a path within the archive to be specified.  In addition, the type of artifact must be included along with an optional path to a file representing the content.\nExample Usage:\narchive:addEntry /artifacts/myfile.xml XmlDocument /home/uname/artifacts/myfile.xml\narchive:addEntry /artifacts/no-content.artifact MyLogicalArtifact\n")
public class AddEntryArchiveCommand extends AbstractArchiveCommand {

    @Arguments(description = "[<content path>]", completer = Completer.class)
    private List<String> arguments;

    @Option(name = "entryPath", hasValue = true, required = true,
            description = "Entry path")
    private String entryPath;

    @Option(name = "type", hasValue = true, required = true, completer = ArtifactTypeCompleter.class,
            description = "Artifact type")
    private String type;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "addEntry";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String pathToContent = optionalArgument(arguments, 0);

		InputStream contentStream = null;
		try {
			ArtifactType artifactType = ArtifactType.valueOf(type);
			String name = new File(type).getName();
			if (pathToContent != null) {
				File contentFile = new File(pathToContent);
				contentStream = FileUtils.openInputStream(contentFile);
			}
			BaseArtifactType artifact = artifactType.newArtifactInstance();
			artifact.setName(name);
			currentArchive(commandInvocation).addEntry(entryPath, artifact, contentStream);
            commandInvocation.getShell().out().println(Messages.i18n.format("AddEntry.Added", entryPath)); //$NON-NLS-1$
        } catch (ArtificerArchiveException e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("AddEntry.ArtificerArchiveException", e.getLocalizedMessage())); //$NON-NLS-1$
        } finally {
			IOUtils.closeQuietly(contentStream);
		}

		return CommandResult.SUCCESS;
	}

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            AddEntryArchiveCommand command = (AddEntryArchiveCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            }
        }
    }
}
