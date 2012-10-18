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
import org.overlord.sramp.client.shell.AbstractShellContextVariableLifecycleHandler;
import org.overlord.sramp.client.shell.ShellContext;

/**
 * Creates a new, empty s-ramp batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class NewArchiveCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public NewArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		System.out.println("archive:new");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute(org.overlord.sramp.client.shell.ShellContext)
	 */
	@Override
	public void execute(ShellContext context) throws Exception {
		SrampArchive archive = null;
		QName varName = new QName("archive", "active-archive");
		archive = (SrampArchive) context.getVariable(varName);

		if (archive != null) {
			System.out.println("An S-RAMP archive is already open.  Please archive:close it before creating a new one.");
			return;
		}

		archive = new SrampArchive();
		context.setVariable(varName, archive, new AbstractShellContextVariableLifecycleHandler() {
			@Override
			public void onRemove(Object object) {
				SrampArchive.closeQuietly((SrampArchive) object);
			}
			@Override
			public void onContextDestroyed(Object object) {
				SrampArchive.closeQuietly((SrampArchive) object);
			}
		});
		System.out.println("New S-RAMP batch archive opened.");
	}

}
