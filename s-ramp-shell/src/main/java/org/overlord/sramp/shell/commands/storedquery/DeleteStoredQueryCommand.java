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
package org.overlord.sramp.shell.commands.storedquery;

import java.util.List;

import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * CLI command to delete a stored query from the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
public class DeleteStoredQueryCommand extends BuiltInShellCommand {

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String name = this.requiredArgument(0, Messages.i18n.format("StoredQuery.Name.Mandatory")); //$NON-NLS-1$

        SrampAtomApiClient client = StoredQueryCommandUtil.client(this, getContext());
        if (client == null) {
            return false;
        }

        try {
            client.deleteStoredQuery(name);
            print(Messages.i18n.format("DeleteStoredQueryCommand.Success", name)); //$NON-NLS-1$
        } catch (Exception e) {
            print(Messages.i18n.format("DeleteStoredQueryCommand.Fail", name)); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    /**
     * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String,
     *      java.util.List)
     */
    @Override
    public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        return StoredQueryCommandUtil.tabCompletion(this, getArguments(), getContext(), candidates);
    }
}
