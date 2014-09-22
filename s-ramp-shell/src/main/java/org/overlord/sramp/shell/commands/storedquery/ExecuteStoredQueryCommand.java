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
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * CLI command to retrieve a stored query from the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
public class ExecuteStoredQueryCommand extends BuiltInShellCommand {

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
            print(Messages.i18n.format("Query.Querying")); //$NON-NLS-1$
            print("\t" + name); //$NON-NLS-1$

            QueryResultSet rset = client.queryWithStoredQuery(name);
            
            int entryIndex = 1;
            print(Messages.i18n.format("Query.AtomFeedSummary", rset.size())); //$NON-NLS-1$
            print("  Idx                    Type Name"); //$NON-NLS-1$
            print("  ---                    ---- ----"); //$NON-NLS-1$
            for (ArtifactSummary summary : rset) {
                ArtifactType type = summary.getType();
                String displayType = type.getArtifactType().getType().toString();
                if (type.isExtendedType() && type.getExtendedType() != null) {
                    displayType = type.getExtendedType();
                }
                print("TEST  %1$3d %2$23s %3$-40s", entryIndex++, displayType, //$NON-NLS-1$
                        summary.getName());
            }
            
            return true;
        } catch (Exception e) {
            print(Messages.i18n.format("ExecuteStoredQueryCommand.Fail")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
        }
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
