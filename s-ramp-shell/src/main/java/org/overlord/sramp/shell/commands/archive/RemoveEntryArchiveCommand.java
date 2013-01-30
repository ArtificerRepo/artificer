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

import javax.xml.namespace.QName;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.shell.api.AbstractShellCommand;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class RemoveEntryArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public RemoveEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("archive:removeEntry <archivePath>");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'removeEntry' command will remove a single entry from the");
		print("currently open S-RAMP batch archive.  The path to the entry");
		print("must be specified.");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String archivePathArg = requiredArgument(0, "Please include an entry path (relative archive path).");

		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print("No S-RAMP archive is currently open.");
		} else {
			boolean success = archive.removeEntry(archivePathArg);
			if (success) {
				print("The S-RAMP archive entry was successfully delete.");
			} else {
				print("No S-RAMP archive entry could be found at path: " + archivePathArg);
			}
		}
	}

}
