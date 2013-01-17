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
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.common.SrampModelUtils;
import org.overlord.sramp.shell.AbstractShellCommand;
import org.overlord.sramp.shell.ShellContext;
import org.overlord.sramp.shell.commands.InvalidCommandArgumentException;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateEntryArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("archive:updateEntry <archivePath> <subCommand> <subCommandArgs>");
		print("\tSub-Commands");
		print("\t------------");
		print("\tsetContent <pathToFileContent>");
		print("\tsetProperty <propertyName> <propertyValue>");
		print("\tsetRelationship <relationshipType> <targetUUID>");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'updateEntry' command is used to modify entries in the");
		print("currently open S-RAMP batch archive.  The path to the entry");
		print("must be specified, along with details about how the entry is");
		print("to be modified.");
		print("");
		print(" setContent: a sub-command that sets the file content on a ");
		print("      particular entry");
		print(" setProperty: a sub-command that sets a single custom S-RAMP");
		print("      property on the entry");
		print(" setRelationship: a sub-command that sets a single generic");
		print("      S-RAMP relationship on the entry");
	}

	/**
	 * @see org.overlord.sramp.common.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String archivePathArg = requiredArgument(0, "Please include an entry path (relative archive path).");
		String subCommandArg = requiredArgument(1, "Please specify a sub-command.");

		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print("No S-RAMP archive is currently open.");
		} else {
			if (!archive.containsEntry(archivePathArg)) {
				throw new InvalidCommandArgumentException(0, "Archive Entry not found: " + archivePathArg);
			}

			if ("setContent".equals(subCommandArg)) {
				executeSetContent(archive, archivePathArg, getContext());
			}
			if ("setProperty".equals(subCommandArg)) {
				executeSetProperty(archive, archivePathArg, getContext());
			}
			if ("setRelationship".equals(subCommandArg)) {
				executeSetRelationship(archive, archivePathArg, getContext());
			}
		}
	}

	/**
	 * Can set the content for an entry.
	 * @param archive
	 * @param entryPath
	 * @param context
	 * @throws Exception
	 */
	private void executeSetContent(SrampArchive archive, String entryPath, ShellContext context) throws Exception {
		String pathToContentArg = requiredArgument(2, "Please specify a path to the content file.");
		File file = new File(pathToContentArg);
		if (!file.isFile()) {
			throw new InvalidCommandArgumentException(2, "File not found: " + pathToContentArg);
		}

		InputStream contentStream = null;
		try {
			contentStream = FileUtils.openInputStream(file);
			SrampArchiveEntry entry = archive.getEntry(entryPath);
			archive.updateEntry(entry, contentStream);
			print("Entry (content) successfully set.");
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
		String propNameArg = requiredArgument(2, "Please specify the name of the property.");
		String propValArg = requiredArgument(3, "Please specify the property value.");

		SrampArchiveEntry entry = archive.getEntry(entryPath);
		BaseArtifactType metaData = entry.getMetaData();

		if ("name".equals(propNameArg)) {
			metaData.setName(propValArg);
		} else if ("description".equals(propNameArg)) {
			metaData.setDescription(propValArg);
		} else if ("version".equals(propNameArg)) {
			metaData.setVersion(propValArg);
		} else if ("createdBy".equals(propNameArg)) {
			metaData.setCreatedBy(propValArg);
		} else if ("lastModifiedBy".equals(propNameArg)) {
			metaData.setLastModifiedBy(propValArg);
		} else if ("uuid".equals(propNameArg)) {
			metaData.setUuid(propValArg);
		} else if ("createdTimestamp".equals(propNameArg)) {
		} else if ("lastModifiedTimestamp".equals(propNameArg)) {
		}

		SrampModelUtils.setCustomProperty(metaData, propNameArg, propValArg);
		archive.updateEntry(entry, null);
		print("Entry (meta-data) successfully set.");
	}

	/**
	 * Can set a relationship on an entry.
	 * @param archive
	 * @param entryPath
	 * @param context
	 * @throws Exception
	 */
	private void executeSetRelationship(SrampArchive archive, String entryPath, ShellContext context) throws Exception {
		throw new InvalidCommandArgumentException(0, "setRelationship sub-command not yet implemented.");
	}

}
