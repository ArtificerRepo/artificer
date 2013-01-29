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
package org.overlord.sramp.shell.api;

import java.util.ArrayList;

import javax.xml.namespace.QName;

/**
 * A class that can parse the arguments that should be passed to a command.
 *
 * @author eric.wittmann@redhat.com
 */
public class Arguments extends ArrayList<String> {

	private static final long serialVersionUID = 4475521615147664784L;

	/**
	 * Constructor.
	 * @param arguments
	 */
	public Arguments(String arguments) {
		parseArguments(arguments);
	}

	/**
	 * Parses the arguments from the given string.
	 * @param arguments
	 */
	private void parseArguments(String arguments) {
		ScannerState state = ScannerState.scanningForStart;
		char quotChar = '\'';
		int startPos = -1;
		int endPos = -1;
		for (int position = 0; position < arguments.length(); position++) {
			char c = arguments.charAt(position);
			if (state == ScannerState.scanningForStart) {
				if (c == '\"' || c == '\'') {
					startPos = position;
					state = ScannerState.scanningForEndQuote;
					quotChar = c;
				} else if (!Character.isWhitespace(c)) {
					startPos = position;
					state = ScannerState.scanningForEnd;
				}
			} else if (state == ScannerState.scanningForEnd) {
				if (Character.isWhitespace(c)) {
					endPos = position;
					add(arguments.substring(startPos, endPos));
					state = ScannerState.scanningForStart;
				}
			} else if (state == ScannerState.scanningForEndQuote) {
				if (c == quotChar) {
					endPos = position;
					add(arguments.substring(startPos+1, endPos));
					state = ScannerState.scanningForStart;
				}
			}
		}
		if (state == ScannerState.scanningForEnd) {
			add(arguments.substring(startPos));
		}
	}

	/**
	 * Remove, decode, and return the command name.
	 */
	public QName removeCommandName() {
		if (isEmpty())
			return null;

		String encodedCommandName = remove(0);
		QName commandName = null;
		if (encodedCommandName != null) {
			if (encodedCommandName.contains(":") && !encodedCommandName.endsWith(":")) {
				String [] nameSplit = encodedCommandName.split(":");
				commandName = new QName(nameSplit[0], nameSplit[1]);
			} else {
				commandName = new QName("s-ramp", encodedCommandName);
			}
		}

		return commandName;
	}

	private static enum ScannerState {
		scanningForStart,
		scanningForEnd,
		scanningForEndQuote
	}

}
