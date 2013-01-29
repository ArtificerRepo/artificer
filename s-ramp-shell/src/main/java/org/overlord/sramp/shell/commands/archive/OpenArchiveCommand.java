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

import java.io.File;

import javax.xml.namespace.QName;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.shell.AbstractShellContextVariableLifecycleHandler;
import org.overlord.sramp.shell.api.AbstractShellCommand;

/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class OpenArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public OpenArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("archive:open <pathToArchive>");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'open' operation opens an existing S-RAMP batch archive");
		print("file.  Once open, the contents of the archive can be modified");
		print("or just listed.");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String pathToArchive = requiredArgument(0, "Please supply the path to an S-RAMP batch archive file.");

		SrampArchive archive = null;
		QName varName = new QName("archive", "active-archive");
		archive = (SrampArchive) getContext().getVariable(varName);
		if (archive != null) {
			print("An S-RAMP archive is already open.  Please archive:close it before creating a new one.");
			return;
		}

		File archiveFile = new File(pathToArchive);

		archive = new SrampArchive(archiveFile);
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
		print("S-RAMP batch archive opened from " + archiveFile.getCanonicalPath());
	}

}
