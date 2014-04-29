package org.overlord.sramp.shell.commands.ontology;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.CompletionConstants;
import org.overlord.sramp.shell.api.Arguments;
import org.overlord.sramp.shell.api.ShellContext;

public class FeedTabCompleter {
    public static int tabCompletion(Arguments args, ShellContext context, String lastArgument,
            List<CharSequence> candidates) {
        if (args.isEmpty() && StringUtils.isBlank(lastArgument)) { //$NON-NLS-1$
            candidates.add("feed:");
            candidates.add("uuid:");
        } else if (args.isEmpty() && StringUtils.isNotBlank(lastArgument)) {
            if (lastArgument.startsWith("feed:")) {
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
