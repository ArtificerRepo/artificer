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

import org.apache.commons.lang.StringUtils;
import org.artificer.client.ArtificerAtomApiClient;
import org.artificer.client.ontology.OntologySummary;
import org.artificer.shell.i18n.Messages;
import org.artificer.shell.util.FileNameCompleterDelegate;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.CommandResult;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.jboss.aesh.console.command.invocation.CommandInvocation;
import org.w3._1999._02._22_rdf_syntax_ns_.RDF;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.File;

/**
 * Gets an ontology (and saves it to a local file).
 *
 * @author Brett Meyer
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = "get",
		description = "The \"get\" command retrieves a single ontology from the Artificer repository, optionally saving it to a local file.\n")
public class GetOntologyCommand extends AbstractOntologyCommand {

	@Option(name = "uuid", hasValue = true, required = false,
			description = "Ontology UUID")
	private String ontologyUuid;

	@Option(name = "feed", hasValue = true, required = false,
			description = "Feed index")
	private String feedIndex;

	@Option(name = "outputFile", hasValue = true, required = false, completer = Completer.class,
			description = "Output file path")
	private String outputFilePath;

	@Option(shortName = 'h', name = "help", hasValue = false, required = false, overrideRequired = true,
			description = "Display help")
	private boolean help;

	@Override
	protected String getName() {
		return "get";
	}

	@Override
	protected CommandResult doExecute(CommandInvocation commandInvocation) throws Exception {
		if (help) {
			return doHelp(commandInvocation);
		}

		ArtificerAtomApiClient client = client(commandInvocation);

		if (StringUtils.isNotBlank(feedIndex)) {
			OntologySummary ontologySummary = ontologySummaryFromFeed(commandInvocation, feedIndex);
			ontologyUuid = ontologySummary.getUuid();
		} else if (StringUtils.isBlank(ontologyUuid)) {
			commandInvocation.getShell().out().println(Messages.i18n.format("Ontology.Arguments"));
			return CommandResult.FAILURE;
		}

		File filePath = null;
		if (StringUtils.isNotBlank(outputFilePath)) {
		    filePath = new File(outputFilePath);
		    if (filePath.exists()) {
				commandInvocation.getShell().out().println(Messages.i18n.format("GetOntology.PathExists", filePath));
		        return CommandResult.FAILURE;
		    }
		    if (filePath.getParentFile() != null && filePath.getParentFile().isFile()) {
				commandInvocation.getShell().out().println(Messages.i18n.format("GetOntology.InvalidOutputPath", filePath));
                return CommandResult.FAILURE;
		    }
		}

		try {
			RDF ontology = client.getOntology(ontologyUuid);

			if (StringUtils.isNotBlank(outputFilePath)) {
			    saveOntologyToFile(commandInvocation, ontology, filePath);
			} else {
			    printOntology(commandInvocation, ontology);
			}
		} catch (Exception e) {
			commandInvocation.getShell().out().println(Messages.i18n.format("GetOntology.GetFailed"));
			commandInvocation.getShell().out().println("\t" + e.getMessage());
            return CommandResult.FAILURE;
		}
		return CommandResult.SUCCESS;
	}

	/**
	 * Outputs an RDF to stdout.
     * @param ontology
     */
    private void printOntology(CommandInvocation commandInvocation, RDF ontology) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(RDF.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(ontology, commandInvocation.getShell().out());
    }

    /**
     * Outputs an RDF to a file.
     * @param ontology
     * @param filePath
     */
    private void saveOntologyToFile(CommandInvocation commandInvocation, RDF ontology, File filePath) throws Exception {
        JAXBContext jaxbContext = JAXBContext.newInstance(RDF.class);
        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(ontology, filePath);
		commandInvocation.getShell().out().println(Messages.i18n.format("GetOntology.OntologySaved", filePath.getCanonicalPath()));
    }

	private static class Completer implements OptionCompleter<CompleterInvocation> {
		@Override
		public void complete(CompleterInvocation completerInvocation) {
			GetOntologyCommand command = (GetOntologyCommand) completerInvocation.getCommand();
			if (StringUtils.isBlank(command.outputFilePath)) {
				FileNameCompleterDelegate.complete(completerInvocation);
			}
		}
	}
}
