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
package org.overlord.sramp.shell.commands.ontology;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.ontology.OntologySummary;
import org.overlord.sramp.shell.BuiltInShellCommand;
import org.overlord.sramp.shell.api.InvalidCommandArgumentException;
import org.overlord.sramp.shell.i18n.Messages;
import org.overlord.sramp.shell.util.FileNameCompleter;

/**
 * Uploads an ontology (S-RAMP OWL format) to the s-ramp repository.
 *
 * @author eric.wittmann@redhat.com
 */
public class UpdateOntologyCommand extends BuiltInShellCommand {

	/**
	 * Constructor.
	 */
	public UpdateOntologyCommand() {
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
	 */
	@Override
	public boolean execute() throws Exception {
        String ontologyIdArg = this.requiredArgument(0, Messages.i18n.format("UpdateOntology.InvalidArgMsg.OntologyId")); //$NON-NLS-1$
		String filePathArg = this.requiredArgument(1, Messages.i18n.format("UpdateOntology.InvalidArgMsg.MissingPath")); //$NON-NLS-1$

        QName feedVarName = new QName("ontology", "feed"); //$NON-NLS-1$ //$NON-NLS-2$
        QName clientVarName = new QName("s-ramp", "client"); //$NON-NLS-1$ //$NON-NLS-2$
        SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(clientVarName);
        if (client == null) {
            print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
            return false;
        }

        if (!ontologyIdArg.contains(":") || ontologyIdArg.endsWith(":")) { //$NON-NLS-1$ //$NON-NLS-2$
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidOntologyIdFormat")); //$NON-NLS-1$
        }

        String ontologyUuid = null;
        int colonIdx = ontologyIdArg.indexOf(':');
        String idType = ontologyIdArg.substring(0, colonIdx);
        String idValue = ontologyIdArg.substring(colonIdx + 1);
        if ("feed".equals(idType)) { //$NON-NLS-1$
            @SuppressWarnings("unchecked")
            List<OntologySummary> ontologies = (List<OntologySummary>) getContext().getVariable(feedVarName);
            if (ontologies == null) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("DeleteOntology.NoOntologyFeed")); //$NON-NLS-1$
            }
            int feedIdx = Integer.parseInt(idValue) - 1;
            if (feedIdx < 0 || feedIdx >= ontologies.size()) {
                throw new InvalidCommandArgumentException(0, Messages.i18n.format("FeedIndexOutOfRange")); //$NON-NLS-1$
            }
            OntologySummary summary = ontologies.get(feedIdx);
            ontologyUuid = summary.getUuid();
        } else if ("uuid".equals(idType)) { //$NON-NLS-1$
            ontologyUuid = idValue;
        } else {
            throw new InvalidCommandArgumentException(0, Messages.i18n.format("InvalidIdFormat")); //$NON-NLS-1$
        }

        InputStream content = null;
		try {
		    File file = new File(filePathArg);
		    if (file.exists()) {
		        content = FileUtils.openInputStream(file);
		    } else {
		        URL url = this.getClass().getResource(filePathArg);
		        if (url != null) {
		            print(Messages.i18n.format("UpdateOntology.ReadingOntology", url.toExternalForm())); //$NON-NLS-1$
		            content = url.openStream();
		        } else {
		            print(Messages.i18n.format("UpdateOntology.CannotFind", filePathArg)); //$NON-NLS-1$
		        }
		    }
	        client.updateOntology(ontologyUuid, content);
			print(Messages.i18n.format("UpdateOntology.SuccessfulUpdate")); //$NON-NLS-1$
		} catch (Exception e) {
			print(Messages.i18n.format("UpdateOntology.UpdateFailed")); //$NON-NLS-1$
			print("\t" + e.getMessage()); //$NON-NLS-1$
			IOUtils.closeQuietly(content);
            return false;
		} finally {
			IOUtils.closeQuietly(content);
		}
        return true;
	}

	/**
	 * @see org.overlord.sramp.shell.api.shell.AbstractShellCommand#tabCompletion(java.lang.String, java.util.List)
	 */
	@Override
	public int tabCompletion(String lastArgument, List<CharSequence> candidates) {
        int toReturn = FeedTabCompleter.tabCompletion(getArguments(), getContext(), lastArgument, candidates);
        if (getArguments().size() == 1) {
            if (lastArgument == null)
                lastArgument = ""; //$NON-NLS-1$
            FileNameCompleter delegate = new FileNameCompleter();
            return delegate.complete(lastArgument, lastArgument.length(), candidates);
        }
        return toReturn;
	}
}
