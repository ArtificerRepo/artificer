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
import java.io.IOException;

import javax.xml.namespace.QName;

import org.jboss.aesh.console.command.CommandResult;
import org.overlord.sramp.atom.archive.SrampArchive;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.aesh.SrampCommandInvocation;

/**
 * Abstract class with common methods and attributes for all the archive shell
 * commands.
 * 
 * @author David Virgil Naranjo
 */
public abstract class AbstractArchiveShellCommand extends BuiltInShellCommand {
    protected QName varName = new QName("archive", "active-archive"); //$NON-NLS-1$ //$NON-NLS-2$
    protected SrampArchive archive;

    /**
     * Instantiates a new abstract archive shell command.
     */
    public AbstractArchiveShellCommand() {

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.overlord.sramp.shell.BuiltInShellCommand#execute(org.overlord.sramp
     * .shell.aesh.SrampCommandInvocation)
     */
    @Override
    public CommandResult execute(SrampCommandInvocation commandInvocation) throws IOException {
        if (this.getContext() == null) {
            this.setContext(commandInvocation.getContext());
        }

        if (getContext().getVariable(varName) != null) {
            archive = (SrampArchive) getContext().getVariable(varName);
        }

        return super.execute(commandInvocation);
    }

    /**
     * Gets the var name.
     *
     * @return the var name
     */
    public QName getVarName() {
        return varName;
    }

    /**
     * Gets the archive.
     *
     * @return the archive
     */
    public SrampArchive getArchive() {
        return archive;
    }

    /**
     * Gets the complete path.
     *
     * @param entryPath
     *            the entry path
     * @param entryFileName
     *            the entry file name
     * @return the complete path
     */
    protected String getCompletePath(String entryPath, String entryFileName) {
        if (entryPath.endsWith(File.separatorChar + "")) {
            return entryPath + entryFileName;
        } else {
            return entryPath + File.separatorChar + entryFileName;
        }
    }
}
