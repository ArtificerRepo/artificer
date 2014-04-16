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
package org.overlord.sramp.shell.commands.archive;

import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.EntryFileNameValidator;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Archive.ARCHIVE_COMMAND_REMOVE_ENTRY, description = "Adds an entry to the current S-RAMP batch archive.")
public class RemoveEntryArchiveCommand extends AbstractArchiveShellCommand {

    @Option(required = true, hasValue = true, name = "path", shortName = 'p')
    private String _path;

    @Option(required = true, hasValue = true, name = "fileName", shortName = 'f', validator = EntryFileNameValidator.class)
    private String _fileName;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
	public RemoveEntryArchiveCommand() {
	}

	/**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
	@Override
	public boolean execute() throws Exception {
        super.execute();

		if (archive == null) {
			print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
            return false;
		} else {
            String relativeEntryPath = getCompletePath(_path, _fileName);
            boolean success = archive.removeEntry(relativeEntryPath);
			if (success) {
				print(Messages.i18n.format("RemoveEntry.EntryDeleted")); //$NON-NLS-1$
			} else {
                print(Messages.i18n.format("RemoveEntry.NoEntryFound", relativeEntryPath)); //$NON-NLS-1$
			}
		}
        return true;
	}

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Archive.ARCHIVE_COMMAND_REMOVE_ENTRY;
    }

    /**
     * Gets the path.
     *
     * @return the path
     */
    public String getPath() {
        return _path;
    }

    /**
     * Sets the path.
     *
     * @param path
     *            the new path
     */
    public void setPath(String path) {
        this._path = path;
    }

    /**
     * Gets the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * Sets the file name.
     *
     * @param fileName
     *            the new file name
     */
    public void setFileName(String fileName) {
        this._fileName = fileName;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

}
