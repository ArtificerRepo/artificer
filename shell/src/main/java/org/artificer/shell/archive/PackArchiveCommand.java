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
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "pack",
        description = "The \"pack\" command packages up the currently open Artificer batch archive file.  The Artificer batch archive is zip'd up and then copied to the output file location provided.\n")
public class PackArchiveCommand extends AbstractArchiveCommand {

    @Arguments(description = "<output path>", completer = Completer.class)
    private List<String> arguments;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "pack";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        ArtificerArchive archive = currentArchive(commandInvocation);
        String outputLocationArg = requiredArgument(commandInvocation, arguments, 0);
        File outputFile = new File(outputLocationArg);
        if (outputFile.exists()) {
            commandInvocation.getShell().out().println(Messages.i18n.format("PackArchive.OutputLocAlreadyExists"));
        }
        if (!outputFile.getParentFile().exists()) {
            outputFile.mkdirs();
        }
        File packedFile = archive.pack();
        FileUtils.copyFile(packedFile, outputFile);
        commandInvocation.getShell().out().println(Messages.i18n.format("PackArchive.Packaged", outputFile.getCanonicalPath()));
        
        return CommandResult.SUCCESS;
	}

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            PackArchiveCommand command = (PackArchiveCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            }
        }
    }
}
