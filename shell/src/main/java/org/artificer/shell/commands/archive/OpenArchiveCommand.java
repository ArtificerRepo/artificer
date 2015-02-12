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

import java.io.File;
import java.util.List;

import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.shell.AbstractShellContextVariableLifecycleHandler;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleter;

/**
 * Opens an existing S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class OpenArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public OpenArchiveCommand() {
	}

	@Override
	public boolean execute() throws Exception {
        super.initialize();
        String pathToArchive = requiredArgument(0,
                Messages.i18n.format("OpenArchive.InvalidArgMsg.PathToArchive")); //$NON-NLS-1$

        if (!validate()) {
			return false;
		}

		File archiveFile = new File(pathToArchive);

		archive = new ArtificerArchive(archiveFile);
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
		print(Messages.i18n.format("OpenArchive.Opened", archiveFile.getCanonicalPath())); //$NON-NLS-1$
        return true;
	}

    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (getArguments().isEmpty()) {
            if (lastArgument == null)
                lastArgument = ""; //$NON-NLS-1$
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        }
        return -1;
    }

    @Override
    protected boolean validate(String... args) {
        if (!validateArchiveSession()) {
            return false;
        }
        return true;
    }

}
