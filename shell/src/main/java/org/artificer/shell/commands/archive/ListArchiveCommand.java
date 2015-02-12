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
package org.artificer.shell.commands.archive;

import java.util.Collection;

import org.artificer.atom.archive.ArtificerArchiveEntry;
import org.artificer.shell.i18n.Messages;


/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class ListArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public ListArchiveCommand() {
	}

	    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
	@Override
	public boolean execute() throws Exception {
        super.initialize();
        if (!validate()) {
	        return false;
		}
		Collection<ArtificerArchiveEntry> entries = archive.getEntries();
		print(Messages.i18n.format("ENTRY_PATH")); //$NON-NLS-1$
		print("  ----------"); //$NON-NLS-1$
		for (ArtificerArchiveEntry entry : entries) {
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
        return true;
	}

    /*
     * (non-Javadoc)
     * 
     * @see
     * AbstractArchiveCommand#validate
     * (java.lang.String[])
     */
    @Override
    protected boolean validate(String... args) {
        if (!validateArchiveSession()) {
            return false;
        }
        return true;
    }

}
