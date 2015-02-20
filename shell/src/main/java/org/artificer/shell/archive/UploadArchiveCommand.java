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
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Uploads an s-ramp archive to the repository.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "upload",
        description = "The \"upload\" command uploads the currently active Artificer archive to the Artificer repository (closing the archive if it is open).\n")
public class UploadArchiveCommand extends AbstractArchiveCommand {

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "upload";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }

		try {
	        client(commandInvocation).uploadBatch(currentArchive(commandInvocation));
	        // Success!  Close the archive.
            ArtificerArchive.closeQuietly(context(commandInvocation).getCurrentArchive());
            context(commandInvocation).setCurrentArchive(null);
            commandInvocation.getShell().out().println(Messages.i18n.format("UploadArchive.Success"));
		} catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("UploadArchive.Failure"));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
		}
        return CommandResult.SUCCESS;
	}

}
