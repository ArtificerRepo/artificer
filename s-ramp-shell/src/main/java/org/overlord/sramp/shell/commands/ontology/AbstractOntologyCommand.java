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
import org.jboss.aesh.cl.validator.CommandValidatorException;
import org.jboss.aesh.cl.validator.OptionValidatorException;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.i18n.Messages;

/**
 * Abstract class with common methods and attributes for all the ontology shell
 * commands.
 *
 * @author David Virgil Naranjo
 */
public abstract class AbstractOntologyCommand extends BuiltInShellCommand {

    protected final static QName feedVarName = new QName("ontology", "feed"); //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Instantiates a new abstract ontology command.
     */
    public AbstractOntologyCommand() {

    }

    /**
     * Gets the feed index.
     *
     * @return the feed index
     */
    public Integer getFeedIndex() {
        return null;
    }

    /**
     * Gets the uuid.
     *
     * @return the uuid
     */
    public String getUuid() {
        return null;
    }

    /**
     * Gets the ontology.
     *
     * @param feedIndex
     *            the feed index
     * @return the ontology
     * @throws OptionValidatorException
     *             the option validator exception
     */
    protected OntologySummary getOntology(Integer feedIndex) throws OptionValidatorException {
        List<OntologySummary> ontologies = (List<OntologySummary>) getContext().getVariable(feedVarName);
        if (ontologies == null) {
            throw new OptionValidatorException(Messages.i18n.format("DeleteOntology.NoOntologyFeed")); //$NON-NLS-1$
        }
        int feedIdx = feedIndex - 1;
        if (feedIdx < 0 || feedIdx >= ontologies.size()) {
            throw new OptionValidatorException(Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
        }
        OntologySummary summary = ontologies.get(feedIdx);
        return summary;
    }


    /**
     * Complete ontology.
     *
     * @param completerInvocation
     *            the completer invocation
     */
    protected void completeOntology(CompleterInvocation completerInvocation) {
        @SuppressWarnings("unchecked")
        List<OntologySummary> ontologies = (List<OntologySummary>) getContext().getVariable(feedVarName);
        if (ontologies != null) {
            for (int idx = 0; idx < ontologies.size(); idx++) {
                String candidate = "" + (idx + 1); //$NON-NLS-1$
                if (StringUtils.isBlank(completerInvocation.getGivenCompleteValue())) {
                    completerInvocation.addCompleterValue(candidate);
                } else if (candidate.startsWith(completerInvocation.getGivenCompleteValue())) {
                    completerInvocation.addCompleterValue(candidate);
                }
            }
        }
    }

    /**
     * Validate feed ontology.
     *
     * @throws CommandValidatorException
     *             the command validator exception
     */
    protected void validateFeedOntology() throws CommandValidatorException {
        if (getFeedIndex() == null && StringUtils.isBlank(getUuid())) {
            throw new CommandValidatorException(Messages.i18n.format("Ontology.NoOptionSelected"));
        } else if (getFeedIndex() != null && StringUtils.isNotBlank(getUuid())) {
            throw new CommandValidatorException(Messages.i18n.format("Ontology.BothOptionSelected"));

        }
    }
}
