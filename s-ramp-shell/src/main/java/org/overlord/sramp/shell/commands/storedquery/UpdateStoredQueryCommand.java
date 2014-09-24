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
 * CLI command to update a stored query in the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
public class UpdateStoredQueryCommand extends BuiltInShellCommand {

    /**
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String name = this.requiredArgument(0, Messages.i18n.format("StoredQuery.Name.Mandatory")); //$NON-NLS-1$
        String query = this.requiredArgument(1, Messages.i18n.format("StoredQuery.Query.Mandatory")); //$NON-NLS-1$
        String propertyNames = this.optionalArgument(2);

        SrampAtomApiClient client = StoredQueryCommandUtil.client(this, getContext());
        if (client == null) {
            return false;
        }

        StoredQuery storedQuery = new StoredQuery();
        storedQuery.setQueryName(name);
        storedQuery.setQueryExpression(query);
        if (StringUtils.isNotBlank(propertyNames)) {
            String[] split = propertyNames.split(",");
            for (String s : split) {
                storedQuery.getPropertyName().add(s);
            }
        }

        try {
            client.updateStoredQuery(name, storedQuery);
            print(Messages.i18n.format("UpdateStoredQueryCommand.Success", name)); //$NON-NLS-1$
            return true;
        } catch (Exception e) {
            print(Messages.i18n.format("UpdateStoredQueryCommand.Fail", name)); //$NON-NLS-1$
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
