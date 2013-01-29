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
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.s_ramp.xmlns._2010.s_ramp.BaseArtifactType;

/**
 * Adds an entry to the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class AddEntryArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public AddEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("archive:addEntry <archivePath> <srampArtifactType> [<pathToFileContent>]");
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'addEntry' command provides a way to add a single entry to the ");
		print("currently open S-RAMP batch archive.  The command requires a path");
		print("within the archive to be specified.  In addition, the type of ");
		print("artifact must be included along with an optional path to a file");
		print("representing the content.");

	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String archivePathArg = requiredArgument(0, "Please include an entry path (relative archive path).");
		String artifactTypeArg = requiredArgument(1, "Please include an entry path (relative archive path).");
		String pathToContent = optionalArgument(2);

		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) getContext().getVariable(varName);

		if (archive == null) {
			print("No S-RAMP archive is currently open.");
		} else {
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
				print("Entry added to S-RAMP archive:  " + archivePathArg);
			} finally {
				IOUtils.closeQuietly(contentStream);
			}
		}
	}

}
