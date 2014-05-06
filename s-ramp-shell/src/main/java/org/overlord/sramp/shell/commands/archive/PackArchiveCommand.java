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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileNameCompleter;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class PackArchiveCommand extends AbstractArchiveCommand {

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
        super.initialize();
        String outputLocationArg = requiredArgument(0,
                Messages.i18n.format("PackArchive.InvalidArgMsg.OutputLocation")); //$NON-NLS-1$
        if (!validate()) {
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

    /**
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String,
     *      java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (lastArgument == null)
            lastArgument = ""; //$NON-NLS-1$

        if (getArguments().isEmpty()) {
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
