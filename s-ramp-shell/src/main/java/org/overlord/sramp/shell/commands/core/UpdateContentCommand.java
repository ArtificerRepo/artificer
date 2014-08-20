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
package org.overlord.sramp.shell.commands.core;

import org.overlord.sramp.shell.BuiltInShellCommand;

/**
 * Updates an artifact's content in the s-ramp repository. This requires an active artifact to exist in the
 * context.
 *
 * @author eric.wittmann@redhat.com
 * 
 * @deprecated No longer supported.  See SRAMP-541
 */
@Deprecated
public class UpdateContentCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateContentCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
		print("Deleting artifact content is no longer supported!  See SRAMP-541.  This command now does nothing.");
        return true;
	}

}
