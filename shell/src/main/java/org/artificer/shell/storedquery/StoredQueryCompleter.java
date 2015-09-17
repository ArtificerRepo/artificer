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
package org.artificer.shell.storedquery;

import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.shell.ArtificerContext;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.oasis_open.docs.s_ramp.ns.s_ramp_v1.StoredQuery;

import java.util.List;

/**
 * Name tab completion for stored query commands.
 * 
 * @author Brett Meyer
 */
public class StoredQueryCompleter {

    public static void complete(CompleterInvocation completerInvocation) {
        ArtificerContext context = (ArtificerContext) completerInvocation.getAeshContext();
        ArtificerAtomApiClient client = context.getClient();
        try {
            List<StoredQuery> storedQueries = client.getStoredQueries();
            for (StoredQuery storedQuery : storedQueries) {
                completerInvocation.addCompleterValue(storedQuery.getQueryName());
            }
        } catch (Exception e) {
            // do nothing
        }
    }
}
