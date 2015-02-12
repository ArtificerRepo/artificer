/*
 * Copyright 2014 JBoss Inc
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
import java.io.InputStream;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileEntryPathCompleter;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.BaseArtifactType;
import org.artificer.atom.archive.ArtificerArchive;
import org.artificer.atom.archive.ArtificerArchiveException;
import org.artificer.common.ArtifactType;
import org.artificer.shell.CompletionConstants;
import org.artificer.shell.util.FileNameCompleter;


/**
 * Adds an entry to the current S-RAMP batch archive.
 *
 * @author eric.wittmann@redhat.com
 */
public class AddEntryArchiveCommand extends AbstractArchiveCommand {

	/**
	 * Constructor.
	 */
	public AddEntryArchiveCommand() {
	}

	    /**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
	@Override
	public boolean execute() throws Exception {
        super.initialize();
		String archivePathArg = requiredArgument(0, Messages.i18n.format("InvalidArgMsg.EntryPath")); //$NON-NLS-1$
		String artifactTypeArg = requiredArgument(1, Messages.i18n.format("AddEntry.InvalidArgMsg.ArtifactType")); //$NON-NLS-1$
		String pathToContent = optionalArgument(2);

        if (!this.validate(archivePathArg)) {
            return false;
        }

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
			print(Messages.i18n.format("AddEntry.Added", archivePathArg)); //$NON-NLS-1$
        } catch (ArtificerArchiveException e) {
            print(Messages.i18n.format("AddEntry.ArtificerArchiveException", e.getLocalizedMessage())); //$NON-NLS-1$
        } finally {
			IOUtils.closeQuietly(contentStream);
		}

		return true;
	}

    /**
     * Tab completion.
     *
     * @param lastArgument
     *            the last argument
     * @param candidates
     *            the candidates
     * @return the int
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        if (lastArgument == null)
            lastArgument = ""; //$NON-NLS-1$

        if (getArguments().isEmpty()) {
            QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
            ArtificerArchive archive = (ArtificerArchive) getContext().getVariable(varName);
            FileEntryPathCompleter delegate = new FileEntryPathCompleter(archive);
            delegate.complete(lastArgument, lastArgument.length(), candidates);
            return CompletionConstants.NO_APPEND_SEPARATOR;
        } else if (getArguments().size() == 2) {
            FileNameCompleter delegate = new FileNameCompleter();
            delegate.complete(lastArgument, lastArgument.length(), candidates);
            return CompletionConstants.NO_APPEND_SEPARATOR;
        }
        return -1;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * AbstractArchiveCommand#validate
     * (java.lang.String[])
     */
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
