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
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.aesh.validator.EntryFileNameValidator;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Archive.ARCHIVE_COMMAND_LIST_ENTRY, description = "Adds an entry to the current S-RAMP batch archive.")
public class ListEntryArchiveCommand extends AbstractArchiveShellCommand {

    @Option(required = true, name = "path", hasValue = true, shortName = 'p')
    private String _path;

    @Option(required = true, name = "fileName", hasValue = true, shortName = 'f', validator = EntryFileNameValidator.class)
    private String _fileName;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public ListEntryArchiveCommand() {
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
            String entryRelativePath = getCompletePath(_path, _fileName);
            SrampArchiveEntry entry = archive.getEntry(entryRelativePath);
			BaseArtifactType metaData = entry.getMetaData();
            print(Messages.i18n.format("ENTRY", entryRelativePath)); //$NON-NLS-1$
			print("-----"); //$NON-NLS-1$
			PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
			ArtifactVisitorHelper.visitArtifact(visitor, metaData);
		}
        return true;
	}

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Archive.ARCHIVE_COMMAND_LIST_ENTRY;
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

    /*
     * (non-Javadoc)
     *
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
