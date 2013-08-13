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
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class OpenArchiveCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public OpenArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		String pathToArchive = requiredArgument(0, Messages.i18n.format("OpenArchive.InvalidArgMsg.PathToArchive")); //$NON-NLS-1$

		SrampArchive archive = null;
		QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
		archive = (SrampArchive) getContext().getVariable(varName);
		if (archive != null) {
			print(Messages.i18n.format("OpenArchive.AlreadyOpen")); //$NON-NLS-1$
			return false;
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
		print(Messages.i18n.format("OpenArchive.Opened", archiveFile.getCanonicalPath())); //$NON-NLS-1$
        return true;
	}

}
