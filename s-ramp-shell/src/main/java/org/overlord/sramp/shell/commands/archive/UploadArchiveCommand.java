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
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Uploads an s-ramp archive to the repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UploadArchiveCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public UploadArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
		if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}
        QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
        SrampArchive archive = (SrampArchive) getContext().getVariable(varName);
        if (archive == null) {
            print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
            return false;
        }

		try {
	        client.uploadBatch(archive);
	        // Success!  Close the archive.
	        getContext().removeVariable(varName);
	        print(Messages.i18n.format("UploadArchive.Success")); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("UploadArchive.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

}
