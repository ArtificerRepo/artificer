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
package org.overlord.sramp.shell.commands.archive;

import java.util.Collection;

import javax.xml.namespace.QName;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class ListArchiveCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public ListArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
	        return false;
		} else {
			Collection<SrampArchiveEntry> entries = archive.getEntries();
			print(Messages.i18n.format("ENTRY_PATH")); //$NON-NLS-1$
			print("  ----------"); //$NON-NLS-1$
			for (SrampArchiveEntry entry : entries) {
				String modifier = null;
				if (entry.hasContent()) {
					modifier = "  C "; //$NON-NLS-1$
				} else {
					modifier = "  E "; //$NON-NLS-1$
				}
				print(modifier + entry.getPath());
			}
			print("  ----------"); //$NON-NLS-1$
            print(Messages.i18n.format("ENTRY_LIST_SUMMARY", entries.size())); //$NON-NLS-1$
		}
        return true;
	}

}
