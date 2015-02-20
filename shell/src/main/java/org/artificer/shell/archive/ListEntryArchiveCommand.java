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
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.archive.ArtificerArchiveEntry;
import org.artificer.common.visitors.ArtifactVisitorHelper;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.PrintArtifactMetaDataVisitor;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;

import java.util.List;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "listEntry",
        description = "The \"listEntry\" command is used to display information about a single entry in the currently open Artificer archive.  The path within the archive must be specified.\n")
public class ListEntryArchiveCommand extends AbstractArchiveCommand {

    @Arguments(description = "<entry path>")
    private List<String> arguments;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "listEntry";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String archivePathArg = requiredArgument(commandInvocation, arguments, 0);

        ArtificerArchive archive = currentArchive(commandInvocation);
        ArtificerArchiveEntry entry = archive.getEntry(archivePathArg);
        if (entry != null) {
            BaseArtifactType metaData = entry.getMetaData();
            commandInvocation.getShell().out().println(Messages.i18n.format("ENTRY", archivePathArg));
            commandInvocation.getShell().out().println("-----");
            PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor(commandInvocation);
            ArtifactVisitorHelper.visitArtifact(visitor, metaData);
        } else {
            commandInvocation.getShell().out().println(Messages.i18n.format("ListEntryArchive.Entry.Not.Exist", archivePathArg));
        }
        
        return CommandResult.SUCCESS;
	}
}
