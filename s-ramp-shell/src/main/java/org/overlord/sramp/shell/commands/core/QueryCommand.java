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

import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang.StringUtils;
import org.jboss.aesh.cl.CommandDefinition;
import org.jboss.aesh.cl.Option;
import org.jboss.aesh.cl.completer.OptionCompleter;
import org.jboss.aesh.console.command.completer.CompleterInvocation;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.common.ArtifactType;
import org.overlord.sramp.common.ArtifactTypeEnum;
import org.overlord.sramp.shell.ShellCommandConstants;
import org.overlord.sramp.shell.i18n.Messages;


/**
 * Performs a query against the s-ramp server and displays the result.
 *
 * @author eric.wittmann@redhat.com
 */
@CommandDefinition(name = ShellCommandConstants.Sramp.S_RAMP_COMMAND_QUERY, description = "Performs a query against the s-ramp server and displays the result.")
public class QueryCommand extends AbstractCoreShellCommand {



    @Option(required = true, hasValue = true, name = "query", shortName = 'q', completer = QueryCommand.QueryCompleter.class)
    private String _query;

    @Option(overrideRequired = true, hasValue = false, name = "help", shortName = 'h')
    private boolean _help;

	/**
	 * Constructor.
	 */
	public QueryCommand() {
	}

	/**
     * Execute.
     *
     * @return true, if successful
     * @throws Exception
     *             the exception
     * @see org.overlord.sramp.shell.api.shell.ShellCommand#execute()
     */
	@Override
	public boolean execute() throws Exception {
        super.execute();

        if (client == null) {
			print(Messages.i18n.format("MissingSRAMPConnection")); //$NON-NLS-1$
			return false;
		}
        if (_query.endsWith("/")) { //$NON-NLS-1$
            _query = _query.substring(0, _query.length() - 1);
		}

		print(Messages.i18n.format("Query.Querying")); //$NON-NLS-1$
        print("\t" + _query); //$NON-NLS-1$
		try {
            QueryResultSet rset = client.query(_query, 0, 100, "uuid", true); //$NON-NLS-1$
    		int entryIndex = 1;
    		print(Messages.i18n.format("Query.AtomFeedSummary", rset.size())); //$NON-NLS-1$
    		print("  Idx                    Type Name"); //$NON-NLS-1$
    		print("  ---                    ---- ----"); //$NON-NLS-1$
    		for (ArtifactSummary summary : rset) {
    			ArtifactType type = summary.getType();
    			String displayType = type.getArtifactType().getType().toString();
    			if (type.isExtendedType() && type.getExtendedType() != null) {
    			    displayType = type.getExtendedType();
    			}
                print("  %1$3d %2$23s %3$-40s", entryIndex++, displayType, //$NON-NLS-1$
    					summary.getName());
    		}
    		getContext().setVariable(new QName("s-ramp", "feed"), rset); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception e) {
            print(Messages.i18n.format("Query.Failure")); //$NON-NLS-1$
            print("\t" + e.getMessage()); //$NON-NLS-1$
            return false;
		}
        return true;
	}

    /**
     * Completes the query input string with the different possibilites allowed
     * in the shell.
     *
     * @author David Virgil Naranjo
     */
    private class QueryCompleter implements OptionCompleter<CompleterInvocation> {

        /* (non-Javadoc)
         * @see org.jboss.aesh.cl.completer.OptionCompleter#complete(org.jboss.aesh.console.command.completer.CompleterInvocation)
         */
        @Override
        public void complete(CompleterInvocation completerInvocation) {
            String query = completerInvocation.getGivenCompleteValue();

            if (StringUtils.isBlank(query)) {
                completerInvocation.addCompleterValue("\"/s-ramp/"); //$NON-NLS-1$
            } else {
                String[] split = query.split("/"); //$NON-NLS-1$
                if (split.length == 0 || split.length == 1 || (split.length == 2 && !query.endsWith("/"))) { //$NON-NLS-1$
                    completerInvocation.addCompleterValue("\"/s-ramp/"); //$NON-NLS-1$
                }
                // All artifact models
                if (query.equals("/s-ramp/")) { //$NON-NLS-1$
                    List<String> modelCandidates = new ArrayList<String>();
                    for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                        modelCandidates.add(t.getModel());
                    }
                    completerInvocation.addAllCompleterValues(modelCandidates);
                }
                // Artifact models matching the partial value
                if (split.length == 3 && !query.endsWith("/") && query.startsWith("/s-ramp/")) { //$NON-NLS-1$ //$NON-NLS-2$
                    String partialModel = split[2];
                    List<String> modelCandidates = new ArrayList<String>();
                    for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                        if (t.getModel().startsWith(partialModel))
                            modelCandidates.add(t.getModel());
                    }
                    if (modelCandidates.size() == 1) {
                        completerInvocation.addCompleterValue(modelCandidates.iterator().next() + "/"); //$NON-NLS-1$
                    } else {
                        completerInvocation.addAllCompleterValues(modelCandidates);
                    }
                }
                // All artifact types
                if (split.length == 3 && query.endsWith("/") && query.startsWith("/s-ramp/")) { //$NON-NLS-1$ //$NON-NLS-2$
                    String model = split[2];
                    List<String> typeCandidates = new ArrayList<String>();
                    for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                        if (t.getModel().equals(model)) {
                            typeCandidates.add(t.getType());
                        }
                    }
                    completerInvocation.addAllCompleterValues(typeCandidates);
                }
                // Artifact types matching the partial value
                if (split.length == 4 && !query.endsWith("/") && query.startsWith("/s-ramp/")) { //$NON-NLS-1$ //$NON-NLS-2$
                    String model = split[2];
                    String partialType = split[3];
                    List<String> typeCandidates = new ArrayList<String>();
                    for (ArtifactTypeEnum t : ArtifactTypeEnum.values()) {
                        if (t.getModel().equals(model) && t.getType().startsWith(partialType)) {
                            typeCandidates.add(t.getType());
                        }
                    }
                    completerInvocation.addAllCompleterValues(typeCandidates);

                }
            }
        }

    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#getName()
     */
    @Override
    public String getName() {
        return ShellCommandConstants.Sramp.S_RAMP_COMMAND_QUERY;
    }

    /* (non-Javadoc)
     * @see org.overlord.sramp.shell.BuiltInShellCommand#isHelp()
     */
    @Override
    public boolean isHelp() {
        return _help;
    }

    /**
     * Sets the help.
     *
     * @param help
     *            the new help
     */
    public void setHelp(boolean help) {
        this._help = help;
    }

    /**
     * Gets the query.
     *
     * @return the query
     */
    public String getQuery() {
        return _query;
    }

    /**
     * Sets the query.
     *
     * @param query
     *            the new query
     */
    public void setQuery(String query) {
        this._query = query;
    }

}
