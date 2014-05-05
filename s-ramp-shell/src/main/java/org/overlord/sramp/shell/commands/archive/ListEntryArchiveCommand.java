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

import java.util.List;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.atom.archive.SrampArchiveEntry;
import org.overlord.sramp.common.visitors.ArtifactVisitorHelper;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileEntryPathCompleter;
import org.overlord.sramp.shell.util.PrintArtifactMetaDataVisitor;

/**
 * Removes an entry from the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class ListEntryArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public ListEntryArchiveCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
        super.initialize();
        String archivePathArg = requiredArgument(0, Messages.i18n.format("InvalidArgMsg")); //$NON-NLS-1$

        if (!validate(archivePathArg)) {
            return false;
        } else {
			SrampArchiveEntry entry = archive.getEntry(archivePathArg);
            if (entry != null) {
                BaseArtifactType metaData = entry.getMetaData();
                print(Messages.i18n.format("ENTRY", archivePathArg)); //$NON-NLS-1$
                print("-----"); //$NON-NLS-1$
                PrintArtifactMetaDataVisitor visitor = new PrintArtifactMetaDataVisitor();
                ArtifactVisitorHelper.visitArtifact(visitor, metaData);
            } else {
                print(Messages.i18n.format("ListEntryArchive.Entry.Not.Exist", archivePathArg)); //$NON-NLS-1$
            }

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
            QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
            SrampArchive archive = (SrampArchive) getContext().getVariable(varName);
            FileEntryPathCompleter delegate = new FileEntryPathCompleter(archive);
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        }
        return -1;
    }

    @Override
    protected boolean validate(String... args) {
        if (!validateArchiveSession()) {
            return false;
        }
        if (!validateArchivePath(args[0])) {
            return false;
        }
        return true;
    }

}
