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
package org.overlord.sramp.shell.commands.ontology;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.CompletionConstants;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellContext;

/**
 * Generates the feed completion for Ontology commands.
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
        if (args.isEmpty() && StringUtils.isBlank(lastArgument)) {
            candidates.add("feed:"); //$NON-NLS-1$
            candidates.add("uuid:"); //$NON-NLS-1$
        } else if (args.isEmpty() && StringUtils.isNotBlank(lastArgument)) {
            if (lastArgument.startsWith("feed:")) { //$NON-NLS-1$
                QName feedVarName = new QName("ontology", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
                @SuppressWarnings("unchecked")
                List<OntologySummary> ontologies = (List<OntologySummary>) context.getVariable(feedVarName);
                if (ontologies != null) {
                    for (int idx = 0; idx < ontologies.size(); idx++) {
                        String candidate = "feed:" + (idx + 1); //$NON-NLS-1$
                        if (lastArgument == null) {
                            candidates.add(candidate);
                        }
                        if (lastArgument != null && candidate.startsWith(lastArgument)) {
                            candidates.add(candidate);
                        }
                    }
                }
            } else if ("feed:".startsWith(lastArgument)) { //$NON-NLS-1$
                candidates.add("feed:"); //$NON-NLS-1$
                return CompletionConstants.NO_APPEND_SEPARATOR;
            } else if ("uuid:".startsWith(lastArgument)) { //$NON-NLS-1$
                candidates.add("uuid:"); //$NON-NLS-1$
                return CompletionConstants.NO_APPEND_SEPARATOR;
            }
        }
        return 0;
    }
}
