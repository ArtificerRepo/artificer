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
package org.overlord.sramp.shell.commands.storedquery;

import java.util.List;

import javax.xml.namespace.QName;

import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.shell.api.AbstractShellCommand;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellContext;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Name tab completion for stored query commands.
 * 
 * @author Brett Meyer
 */
public class StoredQueryCommandUtil {

    public static int tabCompletion(AbstractShellCommand command, Arguments args, ShellContext context,
            List<CharSequence> candidates) {
        if (args.isEmpty()) {
            SrampAtomApiClient client = client(command, context);
            if (client != null) {
                try {
                    List<StoredQuery> storedQueries = client.getStoredQueries();
                    for (StoredQuery storedQuery : storedQueries) {
                        candidates.add(storedQuery.getQueryName());
                    }
                    return storedQueries.size() > 0 ? 0 : -1;
                } catch (Exception e) {
                    // do nothing
                }
            }
        }
        return -1;
    }

    public static SrampAtomApiClient client(AbstractShellCommand command, ShellContext context) {
        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        SrampAtomApiClient client = (SrampAtomApiClient) context.getVariable(clientVarName);
        if (client == null) {
            command.print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
        }
        return client;
    }
}
