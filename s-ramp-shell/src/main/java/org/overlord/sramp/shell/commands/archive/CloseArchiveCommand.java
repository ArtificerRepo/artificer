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
import org.overlord.sramp.shell.AbstractShellCommand;

/**
 * Closes the currently open S-RAMP archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class CloseArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public CloseArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("archive:close");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("This command will close the currently open S-RAMP archive.  If");
		print("no archive is currently open, this command does nothing.");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) getContext().removeVariable(varName);

		if (archive == null) {
			print("No S-RAMP archive is currently open.");
		} else {
			print("S-RAMP archive closed (and deleted if appropriate).");
		}
	}

}
