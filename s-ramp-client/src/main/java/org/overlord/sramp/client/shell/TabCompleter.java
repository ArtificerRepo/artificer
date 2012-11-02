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
package org.overlord.sramp.client.shell;

import java.util.List;

import javax.xml.namespace.QName;

import jline.console.completer.Completer;

/**
 * Implements tab completion for the interactive
 *
 * @author eric.wittmann@redhat.com
 */
public class TabCompleter implements Completer {

	private ShellCommandFactory factory;

	/**
	 * Constructor.
	 * @param factory
	 */
	public TabCompleter(ShellCommandFactory factory) {
		this.factory = factory;
	}

	/**
	 * @see jline.console.completer.Completer#complete(java.lang.String, int, java.util.List)
	 */
	@Override
	public int complete(String buffer, int cursor, List<CharSequence> candidates) {
		// Case 1 - nothing has been typed yet - show all command namespaces
		if (buffer.trim().length() == 0) {
			for (String ns: factory.getNamespaces()) {
				candidates.add(ns + ":");
			}
			return cursor;
		}

		// Case 2 - a partial namespace has been typed - show all namespaces that match
		if (!buffer.contains(":") && !buffer.contains(" ")) {
			for (String ns: factory.getNamespaces()) {
				if (ns.startsWith(buffer)) {
					candidates.add(ns + ":");
				}
			}
			// If no namespaces matched, then try to match the default commands
			if (candidates.isEmpty()) {
				for (QName cmdName : factory.getCommandNames("s-ramp")) {
					if (cmdName.getLocalPart().startsWith(buffer)) {
						candidates.add("s-ramp:" + cmdName.getLocalPart() + " ");
					}
				}
			}
			return 0;
		}

		// Case 3 - a namespace has been typed and we're waiting at the colon
		if (buffer.endsWith(":") && !buffer.contains(" ")) {
			String ns = buffer.substring(0, buffer.length() - 1);
			for (QName cmdName : factory.getCommandNames(ns)) {
				candidates.add(cmdName.getLocalPart() + " ");
			}
			return cursor;
		}

		// Case 4 - a partial command has been typed - show all command names that match
		if (buffer.contains(":") && !buffer.endsWith(":") && !buffer.contains(" ")) {
			int colonIdx = buffer.indexOf(':');
			String ns = buffer.substring(0, colonIdx);
			String name = buffer.substring(colonIdx + 1);
			for (QName cmdName : factory.getCommandNames(ns)) {
				if (cmdName.getLocalPart().startsWith(name)) {
					candidates.add(cmdName.getLocalPart() + " ");
				}
			}
			return colonIdx + 1;
		}

		return cursor;
	}

}
