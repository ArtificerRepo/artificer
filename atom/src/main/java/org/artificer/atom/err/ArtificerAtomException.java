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
package org.artificer.atom.err;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The exception thrown by the Atom layer whenever something goes horribly, horribly wrong.
 *
 * @author eric.wittmann@redhat.com
 */
public class ArtificerAtomException extends Exception {

	private static final long serialVersionUID = -4954468657023096910L;

	private static final Pattern ST_PATTERN = Pattern.compile("([a-zA-Z0-9_\\.]*)\\.([a-zA-Z0-9_\\.]*)\\(([a-zA-Z0-9_\\.]*):([\\d]*)\\)"); //$NON-NLS-1$

	/**
	 * Constructor.
	 */
	public ArtificerAtomException() {
	}

	/**
	 * Constructor.
	 * @param message
	 */
	public ArtificerAtomException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 * @param message
	 * @param cause
	 */
	public ArtificerAtomException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 * @param cause
	 */
	public ArtificerAtomException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructor.
	 * @param msg
	 * @param stackTrace
	 */
	public ArtificerAtomException(String msg, String stackTrace) {
		super(msg);
		setStackTrace(parseStackTrace(stackTrace));
	}

	/**
	 * Parses a stack trace string into an array of stack trace elements.  Basically
	 * reverses the "printStackTrace" process.
	 * @param stackTrace string formatted java stack trace
	 * @return stack trace element array
	 */
	private static StackTraceElement[] parseStackTrace(String stackTrace) {
		List<StackTraceElement> stElements = new ArrayList<StackTraceElement>();

		Matcher matcher = ST_PATTERN.matcher(stackTrace);
		while (matcher.find()){
		    String className = matcher.group(1);
		    String methodName = matcher.group(2);
		    String fileName = matcher.group(3);
		    int lineNumber = Integer.parseInt(matcher.group(4) == null ? "0" : matcher.group(4)); //$NON-NLS-1$
		    stElements.add(new StackTraceElement(className, methodName, fileName, lineNumber));
		}

		return stElements.toArray(new StackTraceElement[stElements.size()]);
	}

}
