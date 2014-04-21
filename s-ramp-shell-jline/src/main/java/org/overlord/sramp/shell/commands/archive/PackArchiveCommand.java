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

import org.apache.commons.io.FileUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class PackArchiveCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public PackArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		String outputLocationArg = requiredArgument(0, Messages.i18n.format("PackArchive.InvalidArgMsg.OutputLocation")); //$NON-NLS-1$

		QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
            return false;
		} else {
			File outputFile = new File(outputLocationArg);
			if (outputFile.exists()) {
				print(Messages.i18n.format("PackArchive.OutputLocAlreadyExists")); //$NON-NLS-1$
			}
			if (!outputFile.getParentFile().exists()) {
				outputFile.mkdirs();
			}
			File packedFile = archive.pack();
			FileUtils.copyFile(packedFile, outputFile);
			print(Messages.i18n.format("PackArchive.Packaged", outputFile.getCanonicalPath())); //$NON-NLS-1$
		}
        return true;
	}

}
