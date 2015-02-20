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
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.io.File;
import java.util.List;

/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "open",
        description = "The \"open\" operation opens an existing Artificer batch archive file.  Once open, the contents of the archive can be modified or just listed.\n")
public class OpenArchiveCommand extends AbstractArchiveCommand {

    @Arguments(description = "<path to archive>", completer = Completer.class)
    private List<String> arguments;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "open";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String pathToArchive = requiredArgument(commandInvocation, arguments, 0);

		File archiveFile = new File(pathToArchive);

        ArtificerArchive archive = new ArtificerArchive(archiveFile);
        context(commandInvocation).setCurrentArchive(archive);
        commandInvocation.getShell().out().println(Messages.i18n.format("OpenArchive.Opened", archiveFile.getCanonicalPath())); //$NON-NLS-1$
        return CommandResult.SUCCESS;
	}

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            OpenArchiveCommand command = (OpenArchiveCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            }
        }
    }

}
