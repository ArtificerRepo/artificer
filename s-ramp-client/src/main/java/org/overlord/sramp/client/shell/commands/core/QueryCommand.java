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
package org.overlord.sramp.client.shell.commands.core;

import javax.xml.namespace.QName;

import org.overlord.sramp.ArtifactType;
import org.overlord.sramp.client.SrampAtomApiClient;
import org.overlord.sramp.client.query.ArtifactSummary;
import org.overlord.sramp.client.query.QueryResultSet;
import org.overlord.sramp.client.shell.AbstractShellCommand;

/**
 * Performs a query against the s-ramp server and displays the result.
 *
 * @author eric.wittmann@redhat.com
 */
public class QueryCommand extends AbstractShellCommand {

	/**
	 * Constructor.
	 */
	public QueryCommand() {
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printUsage()
	 */
	@Override
	public void printUsage() {
		print("s-ramp:query <srampQuery>");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#printHelp()
	 */
	@Override
	public void printHelp() {
		print("The 'query' command issues a standard S-RAMP formatted");
		print("query against the S-RAMP server.  The query will result");
		print("in a Feed of entries.");
		print("");
		print("Example usage:");
		print(">  s-ramp:query /s-ramp/wsdl/WsdlDocument");
	}

	/**
	 * @see org.overlord.sramp.client.shell.ShellCommand#execute()
	 */
	@Override
	public void execute() throws Exception {
		String queryArg = this.requiredArgument(0, "Please specify a valid S-RAMP query.");
		QName varName = new QName("s-ramp", "client");
		SrampAtomApiClient client = (SrampAtomApiClient) getContext().getVariable(varName);
		if (client == null) {
			print("No S-RAMP repository connection is currently open.");
			return;
		}
		QueryResultSet rset = client.query(queryArg, 0, 100, "uuid", true);
		int entryIndex = 1;
		print("Atom Feed (%1$d entries)", rset.size());
		print("  Idx                    Type Name");
		print("  ---                    ---- ----");
		for (ArtifactSummary summary : rset) {
			ArtifactType type = summary.getType();
			print("  %1$3d %2$23s %3$-40s", entryIndex++, type.getArtifactType().getType().toString(),
					summary.getName());
		}
		getContext().setVariable(new QName("s-ramp", "feed"), rset);
	}

}
