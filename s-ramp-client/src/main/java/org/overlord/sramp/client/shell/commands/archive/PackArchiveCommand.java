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

import java.io.File;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.client.shell.AbstractShellCommand;
import org.overlord.sramp.client.shell.ShellContext;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class PackArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public PackArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("archive:pack <outputLocation>");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		System.out.println("The 'pack' command packages up the currently open S-RAMP batch");
		System.out.println("archive file.  The S-RAMP batch archive is zip'd up and then");
		System.out.println("copied to the output file location provided.");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		String outputLocationArg = requiredArgument(0, "Please include the output location (file path).");

		QName varName = new QName("archive", "active-archive");
		SrampArchive archive = (SrampArchive) context.getVariable(varName);

		if (archive == null) {
			System.out.println("No S-RAMP archive is currently open.");
		} else {
			File outputFile = new File(outputLocationArg);
			if (outputFile.exists()) {
				System.out.println("Output location already exists!");
			}
			if (!outputFile.getParentFile().exists()) {
				outputFile.mkdirs();
			}
			File packedFile = archive.pack();
			FileUtils.copyFile(packedFile, outputFile);
			System.out.println("S-RAMP archive packaged and copied to: " + outputFile.getCanonicalPath());
		}
	}

}
