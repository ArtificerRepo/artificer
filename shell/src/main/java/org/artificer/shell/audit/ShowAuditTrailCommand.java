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
package org.artificer.shell.audit;

import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.audit.AuditEntrySummary;
import org.artificer.client.audit.AuditResultSet;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

/**
 * Displays the audit trail for an artifact.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "showAuditTrail", description = "\n")
public class ShowAuditTrailCommand extends AbstractCommand {

    @Option(name = "uuid", hasValue = true, required = false,
            description = "Artifact UUID")
    private String artifactUuid;

    @Option(name = "feed", hasValue = true, required = false,
            description = "Feed index")
    private String feedIndex;

    @Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
            description = "Display help")
    private boolean help;

    @Override
    protected String getName() {
        return "showAuditTrail";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (help) {
            return doHelp(commandInvocation);
        }

        if (StringUtils.isNotBlank(feedIndex)) {
            artifactUuid = artifactSummaryFromFeed(commandInvocation, feedIndex).getUuid();
        } else if (StringUtils.isBlank(artifactUuid)) {
            commandInvocation.getShell().out().println(Messages.i18n.format("Artifact.Arguments"));
            return CommandResult.FAILURE;
        }
            
        ArtificerAtomApiClient client = client(commandInvocation);

        AuditResultSet auditTrail = client.getAuditTrailForArtifact(artifactUuid);
        commandInvocation.getShell().out().println(Messages.i18n.format("ShowAuditTrail.EntriesSummary", auditTrail.size()));
        commandInvocation.getShell().out().println("  Idx  " + Messages.i18n.format("AuditEntryLabel"));
        commandInvocation.getShell().out().println("  ---  -----------");
        int idx = 1;
        for (AuditEntrySummary auditEntrySummary : auditTrail) {
            commandInvocation.getShell().out().printf("  %1$3d  %2$s\n", idx++, auditEntrySummary.toString());
        }
        return CommandResult.SUCCESS;
	}

}
