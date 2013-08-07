/*
 * Copyright 2013 JBoss Inc
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
package org.overlord.sramp.shell;

import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Abstract base class for all built-in shell commands.
 *
 * @author eric.wittmann@redhat.com
 */
public abstract class BuiltInShellCommand extends AbstractShellCommand {

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printUsage()
     */
    @Override
    public void printUsage() {
        print(Messages.i18n.format(getClass().getSimpleName() + ".usage")); //$NON-NLS-1$
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#printHelp()
     */
    @Override
    public void printHelp() {
        print(Messages.i18n.format(getClass().getSimpleName() + ".help")); //$NON-NLS-1$
    }

}
