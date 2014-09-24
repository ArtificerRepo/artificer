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

import org.apache.commons.lang.StringUtils;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * CLI command to list all stored queries in the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
public class ListStoredQueriesCommand extends BuiltInShellCommand {

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        SrampAtomApiClient client = StoredQueryCommandUtil.client(this, getContext());
        if (client == null) {
            return false;
        }

        try {
            List<StoredQuery> storedQueries = client.getStoredQueries();
            for (StoredQuery storedQuery : storedQueries) {
                print(storedQuery.getQueryName() + ": " + storedQuery.getQueryExpression()); //$NON-NLS-1$
                if (storedQuery.getPropertyName().size() > 0) {
                    print("     property names: " + StringUtils.join(storedQuery.getPropertyName(), ", ")); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        } catch (Exception e) {
            print(Messages.i18n.format("ListStoredQueriesCommand.Fail")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
        }
        return true;
    }
}
