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
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Closes the currently open S-RAMP archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "close",
        description = "This command will close the currently open Artificer archive.  If no archive is currently open, this command does nothing.\n")
public class CloseArchiveCommand extends AbstractArchiveCommand {

    @Override
    protected String getName() {
        return "close";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        ArtificerArchive.closeQuietly(context(commandInvocation).getCurrentArchive());
        context(commandInvocation).setCurrentArchive(null);
        commandInvocation.getShell().out().println(Messages.i18n.format("CloseArchive.Closed"));
        return CommandResult.SUCCESS;
	}

}
