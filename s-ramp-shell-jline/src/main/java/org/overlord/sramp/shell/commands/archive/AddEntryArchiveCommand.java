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
import java.io.InputStream;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Adds an entry to the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class AddEntryArchiveCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public AddEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		String archivePathArg = requiredArgument(0, Messages.i18n.format("InvalidArgMsg.EntryPath")); //$NON-NLS-1$
		String artifactTypeArg = requiredArgument(1, Messages.i18n.format("AddEntry.InvalidArgMsg.ArtifactType")); //$NON-NLS-1$
		String pathToContent = optionalArgument(2);

		QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
			return false;
		}
		InputStream contentStream = null;
		try {
			ArtifactType type = ArtifactType.valueOf(artifactTypeArg);
			String name = new File(archivePathArg).getName();
			if (pathToContent != null) {
				File contentFile = new File(pathToContent);
				contentStream = FileUtils.openInputStream(contentFile);
			}
			BaseArtifactType artifact = type.newArtifactInstance();
			artifact.setName(name);
			archive.addEntry(archivePathArg, artifact, contentStream);
			print(Messages.i18n.format("AddEntry.Added", archivePathArg)); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(contentStream);
		}

		return true;
	}

}
