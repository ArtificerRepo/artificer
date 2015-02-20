package org.artificer.shell.ontology;

import org.artificer.client.ontology.OntologySummary;
import org.artificer.shell.AbstractCommand;
import org.artificer.shell.ArtificerShellException;
import org.artificer.shell.i18n.Messages;
import org.jboss.aesh.console.command.invocation.CommandInvocation;

import java.util.List;

/**
 * @author Brett Meyer
 */
public abstract class AbstractOntologyCommand extends AbstractCommand {

    protected List<OntologySummary> currentOntologyFeed(CommandInvocation commandInvocation) throws ArtificerShellException {
        List<OntologySummary> ontologies = context(commandInvocation).getCurrentOntologyFeed();
        if (ontologies == null) {
            throw new ArtificerShellException(Messages.i18n.format("NoOntologyFeed"));
        }
        return ontologies;
    }

    protected OntologySummary ontologySummaryFromFeed(CommandInvocation commandInvocation, String feedIndex) throws Exception {
        List<OntologySummary> ontologies = currentOntologyFeed(commandInvocation);
        int feedIdx = Integer.parseInt(feedIndex) - 1;
        if (feedIdx < 0 || feedIdx >= ontologies.size()) {
            throw new ArtificerShellException(Messages.i18n.format("FeedIndexOutOfRange"));
        }
        return ontologies.get(feedIdx);
    }
}
