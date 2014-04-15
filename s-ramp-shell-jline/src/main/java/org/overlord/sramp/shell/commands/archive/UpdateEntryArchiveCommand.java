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
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateEntryArchiveCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		String archivePathArg = requiredArgument(0, Messages.i18n.format("InvalidArgMsg.EntryPath")); //$NON-NLS-1$
		String subCommandArg = requiredArgument(1, Messages.i18n.format("UpdateEntry.InvalidArgMsg.SubCommand")); //$NON-NLS-1$

		QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
            return false;
		} else {
			if (!archive.containsEntry(archivePathArg)) {
				throw new InvalidCommandArgumentException(0, Messages.i18n.format("UpdateEntry.EntryNotFound", archivePathArg)); //$NON-NLS-1$
			}

			if ("setContent".equals(subCommandArg)) { //$NON-NLS-1$
				executeSetContent(archive, archivePathArg, getContext());
			}
			if ("setProperty".equals(subCommandArg)) { //$NON-NLS-1$
				executeSetProperty(archive, archivePathArg, getContext());
			}
			if ("setRelationship".equals(subCommandArg)) { //$NON-NLS-1$
				executeSetRelationship(archive, archivePathArg, getContext());
			}
		}
        return true;
	}

	/**
	 * Can set the content for an entry.
	 * @param archive
	 * @param entryPath
	 * @param context
	 * @throws Exception
	 */
	private void executeSetContent(SrampArchive archive, String entryPath, ShellContext context) throws Exception {
		String pathToContentArg = requiredArgument(2, Messages.i18n.format("UpdateEntry.InvalidArgMsg.MissingPath")); //$NON-NLS-1$
		File file = new File(pathToContentArg);
		if (!file.isFile()) {
			throw new InvalidCommandArgumentException(2, Messages.i18n.format("UpdateEntry.FileNotFound", pathToContentArg)); //$NON-NLS-1$
		}

		InputStream contentStream = null;
		try {
			contentStream = FileUtils.openInputStream(file);
			SrampArchiveEntry entry = archive.getEntry(entryPath);
			archive.updateEntry(entry, contentStream);
			print(Messages.i18n.format("UpdateEntry.SuccessMsg")); //$NON-NLS-1$
		} finally {
			IOUtils.closeQuietly(contentStream);
		}
	}

	/**
	 * Can set a property (built-in or custom) on the entry.
	 * @param archive
	 * @param entryPath
	 * @param context
	 * @throws Exception
	 */
	private void executeSetProperty(SrampArchive archive, String entryPath, ShellContext context) throws Exception {
		String propNameArg = requiredArgument(2, Messages.i18n.format("UpdateEntry.InvalidArgMsg.PropertyName")); //$NON-NLS-1$
		String propValArg = requiredArgument(3, Messages.i18n.format("UpdateEntry.InvalidArgMsg.PropertyValue")); //$NON-NLS-1$

		SrampArchiveEntry entry = archive.getEntry(entryPath);
		BaseArtifactType metaData = entry.getMetaData();

		if ("name".equals(propNameArg)) { //$NON-NLS-1$
			metaData.setName(propValArg);
		} else if ("description".equals(propNameArg)) { //$NON-NLS-1$
			metaData.setDescription(propValArg);
		} else if ("version".equals(propNameArg)) { //$NON-NLS-1$
			metaData.setVersion(propValArg);
		} else if ("createdBy".equals(propNameArg)) { //$NON-NLS-1$
			metaData.setCreatedBy(propValArg);
		} else if ("lastModifiedBy".equals(propNameArg)) { //$NON-NLS-1$
			metaData.setLastModifiedBy(propValArg);
		} else if ("uuid".equals(propNameArg)) { //$NON-NLS-1$
			metaData.setUuid(propValArg);
		} else if ("createdTimestamp".equals(propNameArg)) { //$NON-NLS-1$
		} else if ("lastModifiedTimestamp".equals(propNameArg)) { //$NON-NLS-1$
		}

		SrampModelUtils.setCustomProperty(metaData, propNameArg, propValArg);
		archive.updateEntry(entry, null);
		print(Messages.i18n.format("UpdateEntry.MetaDataSuccessMsg")); //$NON-NLS-1$
	}

	/**
	 * Can set a relationship on an entry.
	 * @param archive
	 * @param entryPath
	 * @param context
	 * @throws Exception
	 */
	private void executeSetRelationship(SrampArchive archive, String entryPath, ShellContext context) throws Exception {
		throw new InvalidCommandArgumentException(0, Messages.i18n.format("UpdateEntry.NotYetImplemented.Relationships")); //$NON-NLS-1$
	}

}
