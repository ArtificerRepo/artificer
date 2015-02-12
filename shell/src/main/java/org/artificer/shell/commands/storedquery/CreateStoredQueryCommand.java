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
package org.artificer.shell.commands.storedquery;

import org.apache.commons.lang.StringUtils;
import org.artificer.shell.BuiltInShellCommand;
import org.artificer.shell.i18n.Messages;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.artificer.client.ArtificerAtomApiClient;

/**
 * CLI command to create a new stored query in the S-RAMP repository.
 * 
 * @author Brett Meyer
 */
public class CreateStoredQueryCommand extends BuiltInShellCommand {

    /**
     * @see org.artificer.shell.api.ShellCommand#execute()
     */
    @Override
    public boolean execute() throws Exception {
        String name = this.requiredArgument(0, Messages.i18n.format("StoredQuery.Name.Mandatory")); //$NON-NLS-1$
        String query = this.requiredArgument(1, Messages.i18n.format("StoredQuery.Query.Mandatory")); //$NON-NLS-1$
        String propertyNames = this.optionalArgument(2);

        ArtificerAtomApiClient client = StoredQueryCommandUtil.client(this, getContext());
        if (client == null) {
            return false;
        }

        StoredQuery storedQuery = new StoredQuery();
        storedQuery.setQueryName(name);
        storedQuery.setQueryExpression(query);
        if (StringUtils.isNotBlank(propertyNames)) {
            String[] split = propertyNames.split(","); //$NON-NLS-1$
            for (String s : split) {
                storedQuery.getPropertyName().add(s);
            }
        }

        try {
            client.createStoredQuery(storedQuery);
            print(Messages.i18n.format("CreateStoredQueryCommand.Success", name)); //$NON-NLS-1$
            return true;
        } catch (Exception e) {
            print(Messages.i18n.format("CreateStoredQueryCommand.Fail", name)); //$NON-NLS-1$
            return false;
        }
    }

}
