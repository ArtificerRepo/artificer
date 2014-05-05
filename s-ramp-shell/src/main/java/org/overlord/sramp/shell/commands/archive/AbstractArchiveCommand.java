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
package org.overlord.sramp.shell.commands.archive;

import java.io.File;

import javax.xml.namespace.QName;

import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Common class for all the archive commands.
 * 
 * @author David Virgil Naranjo
 */
public abstract class AbstractArchiveCommand extends BuiltInShellCommand {

    protected SrampArchive archive;

    protected QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$

    protected SrampAtomApiClient client;

    protected QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$;

    /**
     * Instantiates a new abstract archive command.
     */
    public AbstractArchiveCommand(){

    }

    /**
     * Validate archive path.
     *
     * @param archivePath
     *            the archive path
     * @return true, if successful
     */
    public boolean validateArchivePath(String archivePath) {
        if (archivePath.endsWith(File.separator)) {
            print(Messages.i18n.format("Archive.Path.Arg.should.not.contain.slash.end")); //$NON-NLS-1$
            return false;
        }

        return true;
    }

    /**
     * Validate archive session.
     *
     * @return true, if successful
     */
    public boolean validateArchiveSession(){
        if (archive == null) {
            print(Messages.i18n.format("NO_ARCHIVE_OPEN")); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    /**
     * Validate.
     *
     * @param args
     *            the args
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    protected boolean validate(String... args) throws Exception {
        return true;
    }

    /**
     * Initialize.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     */
    public boolean initialize() throws Exception {
        if (getContext() != null) {
            if (getContext().getVariable(varName) != null) {
                archive = (SrampArchive) getContext().getVariable(varName);
            }
            if (getContext().getVariable(clientVarName) != null) {
                client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
            }

        }
        return true;

    }
}
