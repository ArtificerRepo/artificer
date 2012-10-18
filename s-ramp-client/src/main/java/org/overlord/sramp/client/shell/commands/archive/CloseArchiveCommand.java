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
package org.overlord.sramp.client.shell.commands.archive;

import javax.xml.namespace.QName;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;

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
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("archive:close");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) context.removeVariable(varName);

		if (archive == null) {
			System.out.println("No S-RAMP archive is currently open.");
		} else {
			System.out.println("S-RAMP archive closed (and deleted if appropriate).");
		}
	}

}
