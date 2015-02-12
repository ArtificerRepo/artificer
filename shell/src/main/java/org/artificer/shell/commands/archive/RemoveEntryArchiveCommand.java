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
package org.artificer.shell.commands.archive;

import java.util.List;

import javax.xml.namespace.QName;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileEntryPathCompleter;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class RemoveEntryArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public RemoveEntryArchiveCommand() {
	}

	@Override
	public boolean execute() throws Exception {
        super.initialize();
        String archivePathArg = requiredArgument(0, Messages.i18n.format("InvalidArgMsg.EntryPath")); //$NON-NLS-1$


        if (!validate(archivePathArg)) {
            return false;
        } else {
			boolean success = archive.removeEntry(archivePathArg);
			if (success) {
				print(Messages.i18n.format("RemoveEntry.EntryDeleted")); //$NON-NLS-1$
			} else {
				print(Messages.i18n.format("RemoveEntry.NoEntryFound", archivePathArg)); //$NON-NLS-1$
			}
		}
        return true;
	}

    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (lastArgument == null)
            lastArgument = ""; //$NON-NLS-1$

        if (getArguments().isEmpty()) {
            QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
            ArtificerArchive archive = (ArtificerArchive) getContext().getVariable(varName);
            FileEntryPathCompleter delegate = new FileEntryPathCompleter(archive);
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        }
        return -1;
    }

    @Override
    protected boolean validate(String... args) {
        if (!validateArchiveSession()) {
            return false;
        }
        if (!validateArchivePath(args[0])) {
            return false;
        }
        return true;
    }
}
