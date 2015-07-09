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
package org.artificer.shell.ontology;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.jboss.aesh.cl.Arguments;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

/**
 * Uploads an ontology (S-RAMP OWL format) to the s-ramp repository.
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "update",
        description = "The \"update\" command updates the structure of an existing ontology in the Artificer repository.\n")
public class UpdateOntologyCommand extends AbstractOntologyCommand {

    @Arguments(description = "<file path>", completer = Completer.class)
    private List<String> arguments;

    @Option(name = "uuid", hasValue = true, required = false,
            description = "Ontology UUID")
    private String ontologyUuid;

    @Option(name = "feed", hasValue = true, required = false,
            description = "Feed index")
    private String feedIndex;

    @Override
    protected String getName() {
        return "ontology update";
    }

    @Override
    protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
        if (CollectionUtils.isEmpty(arguments)) {
            return doHelp(commandInvocation);
        }

        String filePath = requiredArgument(commandInvocation, arguments, 0);

        ArtificerAtomApiClient client = client(commandInvocation);

        if (StringUtils.isNotBlank(feedIndex)) {
            OntologySummary ontologySummary = ontologySummaryFromFeed(commandInvocation, feedIndex);
            ontologyUuid = ontologySummary.getUuid();
        } else if (StringUtils.isBlank(ontologyUuid)) {
            commandInvocation.getShell().out().println(Messages.i18n.format("Ontology.Arguments"));
            return CommandResult.FAILURE;
        }

        InputStream content = null;
		try {
		    File file = new File(filePath);
		    if (file.exists()) {
		        content = FileUtils.openInputStream(file);
		    } else {
		        URL url = this.getClass().getClassLoader().getResource(filePath);
		        if (url != null) {
                    commandInvocation.getShell().out().println(Messages.i18n.format("UpdateOntology.ReadingOntology", url.toExternalForm()));
		            content = url.openStream();
		        } else {
                    commandInvocation.getShell().out().println(Messages.i18n.format("UpdateOntology.CannotFind", filePath));
                    return CommandResult.FAILURE;
		        }
		    }
	        client.updateOntology(ontologyUuid, content);
            commandInvocation.getShell().out().println(Messages.i18n.format("UpdateOntology.SuccessfulUpdate"));
		} catch (Exception e) {
            commandInvocation.getShell().out().println(Messages.i18n.format("UpdateOntology.UpdateFailed"));
            commandInvocation.getShell().out().println("\t" + e.getMessage());
			IOUtils.closeQuietly(content);
            return CommandResult.FAILURE;
		} finally {
			IOUtils.closeQuietly(content);
		}
        return CommandResult.SUCCESS;
	}

    private static class Completer implements OptionCompleter<CompleterInvocation> {
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            UpdateOntologyCommand command = (UpdateOntologyCommand) completerInvocation.getCommand();
            if (CollectionUtils.isEmpty(command.arguments)) {
                FileNameCompleterDelegate.complete(completerInvocation);
            }
        }
    }
}
