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

import org.artificer.shell.i18n.Messages;

/**
 * Uploads an s-ramp archive to the repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UploadArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public UploadArchiveCommand() {

	}

	@Override
	public boolean execute() throws Exception {
        super.initialize();

        if (!validate()) {
            return false;
        }

		try {
	        client.uploadBatch(archive);
	        // Success!  Close the archive.
	        getContext().removeVariable(varName);
	        print(Messages.i18n.format("UploadArchive.Success")); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("UploadArchive.Failure")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

    @Override
    protected boolean validate(String... args) {
        if (!validateArchiveSession()) {
            return false;
        }
        if (client == null) {
            print(Messages.i18n.format("MissingArtificerConnection")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

}
