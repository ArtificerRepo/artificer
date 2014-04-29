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
package org.overlord.sramp.shell.commands.core;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.shell.CompletionConstants;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellContext;

/**
 * Generates the feed completion for Core S-ramp commands.
 *
 * @author David Virgil Naranjo
 */
public class FeedTabCompleter {

    /**
     * Tab completion.
     *
     * @param args
     *            the args
     * @param context
     *            the context
     * @param lastArgument
     *            the last argument
     * @param candidates
     *            the candidates
     * @return the int
     */
    public static int tabCompletion(Arguments args, ShellContext context, String lastArgument,
            List<CharSequence> candidates) {
        if (args.isEmpty() && StringUtils.isBlank(lastArgument)) { //$NON-NLS-1$
            candidates.add("feed:");
            candidates.add("uuid:");
        } else if (args.isEmpty() && StringUtils.isNotBlank(lastArgument)) {
            if (lastArgument.startsWith("feed:")) {
                QName feedVarName = new QName("s-ramp", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
                QueryResultSet rset = (QueryResultSet) context.getVariable(feedVarName);
                if (rset != null) {
                    for (int idx = 0; idx < rset.size(); idx++) {
                        String candidate = "feed:" + (idx + 1); //$NON-NLS-1$
                        if (candidate.startsWith(lastArgument)) {
                            candidates.add(candidate);
                        }
                    }
                }
            } else if ("feed:".startsWith(lastArgument)) {
                candidates.add("feed:");
                return CompletionConstants.NO_APPEND_SEPARATOR;
            } else if ("uuid:".startsWith(lastArgument)) {
                candidates.add("uuid:");
                return CompletionConstants.NO_APPEND_SEPARATOR;
            }
        }
        return 0;
    }
}
