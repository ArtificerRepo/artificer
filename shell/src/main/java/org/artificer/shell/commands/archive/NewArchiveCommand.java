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
package org.artificer.shell.commands.archive;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.shell.AbstractShellContextVariableLifecycleHandler;
import org.artificer.shell.i18n.Messages;

/**
 * Creates a new, empty s-ramp batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class NewArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public NewArchiveCommand() {
	}

	@Override
	public boolean execute() throws Exception {
        super.initialize();
        if (!validate()) {
            return false;
        }

		archive = new ArtificerArchive();
		getContext().setVariable(varName, archive, new AbstractShellContextVariableLifecycleHandler() {
			@Override
			public void onRemove(Object object) {
				ArtificerArchive.closeQuietly((ArtificerArchive) object);
			}
			@Override
			public void onContextDestroyed(Object object) {
				ArtificerArchive.closeQuietly((ArtificerArchive) object);
			}
		});
		print(Messages.i18n.format("NewArchive.Opened")); //$NON-NLS-1$
        return true;
	}

    @Override
    protected boolean validate(String... args) {
        if (archive != null) {
            print(Messages.i18n.format("NewArchive.AlreadyOpen")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

}
