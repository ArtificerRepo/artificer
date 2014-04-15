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

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.FileOptionCompleter;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Archive.ARCHIVE_COMMAND_PACK, description = "Adds an entry to the current S-RAMP batch archive.")
public class PackArchiveCommand extends AbstractArchiveShellCommand {

    @Option(required = true, hasValue = true, name = "outputFile", shortName = 'f', completer = FileOptionCompleter.class)
    private File _outputFile;

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

    /**
     * Constructor.
     */
	public PackArchiveCommand() {
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
			if (_outputFile.exists()) {
				print(Messages.i18n.format("PackArchive.OutputLocAlreadyExists")); //$NON-NLS-1$
			}
            if (_outputFile.getParentFile() != null && !_outputFile.getParentFile().exists()) {
                _outputFile.getParentFile().mkdirs();
			}
			File packedFile = archive.pack();
			FileUtils.copyFile(packedFile, _outputFile);
			print(Messages.i18n.format("PackArchive.Packaged", _outputFile.getCanonicalPath())); //$NON-NLS-1$
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
        return ShellCommandConstants.Archive.ARCHIVE_COMMAND_PACK;
    }

    /**
     * Gets the output file.
     *
     * @return the output file
     */
    public File getOutputFile() {
        return _outputFile;
    }

    /**
     * Sets the output file.
     *
     * @param outputFile
     *            the new output file
     */
    public void setOutputFile(File outputFile) {
        this._outputFile = outputFile;
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
