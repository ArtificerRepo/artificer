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

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.ArtificerShellException;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Common class for all the archive commands.
 * 
 * @author Brett Meyer
 */
public abstract class AbstractArchiveCommand extends AbstractCommand {

    protected ArtificerArchive currentArchive(CommandInvocation commandInvocation) throws ArtificerShellException {
        ArtificerArchive archive = context(commandInvocation).getCurrentArchive();
        if (archive == null) {
            throw new ArtificerShellException(Messages.i18n.format("NO_ARCHIVE_OPEN"));
        }
        return archive;
    }
}
