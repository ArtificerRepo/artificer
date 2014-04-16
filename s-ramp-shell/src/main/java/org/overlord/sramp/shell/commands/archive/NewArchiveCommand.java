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
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.shell.AbstractShellContextVariableLifecycleHandler;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Creates a new, empty s-ramp batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Archive.ARCHIVE_COMMAND_NEW, description = "Adds an entry to the current S-RAMP batch archive.")
public class NewArchiveCommand extends AbstractArchiveShellCommand {

    @Option(overrideRequired = true, name = "help", hasValue = false, shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public NewArchiveCommand() {
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
		if (archive != null) {
			print(Messages.i18n.format("NewArchive.AlreadyOpen")); //$NON-NLS-1$
			return false;
		}

		archive = new SrampArchive();
		getContext().setVariable(varName, archive, new AbstractShellContextVariableLifecycleHandler() {
			@Override
			public void onRemove(Object object) {
				SrampArchive.closeQuietly((SrampArchive) object);
			}
			@Override
			public void onContextDestroyed(Object object) {
				SrampArchive.closeQuietly((SrampArchive) object);
			}
		});
		print(Messages.i18n.format("NewArchive.Opened")); //$NON-NLS-1$
        return true;
	}

    /*
     * (non-Javadoc)
     *
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Archive.ARCHIVE_COMMAND_NEW;
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
